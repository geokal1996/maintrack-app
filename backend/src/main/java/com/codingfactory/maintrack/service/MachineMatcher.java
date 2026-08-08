package com.codingfactory.maintrack.service;

import com.codingfactory.maintrack.model.Machine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

// Prospathei na antistoixisei ena ELEFTHERO ONOMA mihanis (opos to grafei o
// proistamenos sto Excel tou, p.x. "Πρέσα 1") me mia mihani pou YPARXEI idi sti
// vasi (p.x. kodikos "7100-EXT-PRS1", onoma "ΠΡΕΣΑ ΔΙΕΛΑΣΗΣ 1").
//
// VASIKOS KANONAS ASFALEIAS: TA PSIFIA DEN SYGXOROUNTAI POTE.
// To "Πρέσα 1" kai to "Πρέσα 2" moiazoun 90% san keimeno, alla einai ALLES MIHANES.
// An to systima ta mperdepse, oi vlaves tis mias tha metrousan stin alli kai to
// Pareto tha edeixne lathos "enoxo" - xeirotero apo to na min kanei tipota.
// Gi' auto: an kai ta dyo onomata exoun psifia kai ta psifia diaferoun, DEN tairiazoun.
//
// Kai se kathe periptosi, i protasi PAEI STON XRISTI gia epivevaiosi prin grafei
// otidipote sti vasi.
final class MachineMatcher {

    private MachineMatcher() {
    }

    // Ena pithano tairiasma, me tin "sigouria" tou (0-100)
    record Match(Machine machine, int confidence) {
    }

    static Optional<Match> findBest(String rawName, List<Machine> candidates) {
        if (rawName == null || rawName.isBlank()) {
            return Optional.empty();
        }

        String raw = normalize(rawName);
        String rawIndex = identifyingNumber(raw);
        List<String> rawTokens = tokens(raw);
        if (rawTokens.isEmpty()) {
            return Optional.empty();
        }

        Match best = null;
        // Posoi ypopsifioi exoun to IDIO kalytero score - an einai perissoteroi apo
        // enan, i protasi einai amfisimi (p.x. "Γραμμή 1" me dyo grammes pou lene "1")
        // kai kalytera na min protathei tipota.
        int tiedAtBest = 0;

        for (Machine machine : candidates) {
            int score = 0;

            // 1) Akrivis tautisi me ton kodiko -> apolyti sigouria
            if (machine.getCode() != null && normalize(machine.getCode()).equals(raw)) {
                return Optional.of(new Match(machine, 100));
            }

            // 2) Akrivis tautisi me to onoma
            String name = machine.getName() != null ? normalize(machine.getName()) : "";
            if (!name.isBlank() && name.equals(raw)) {
                return Optional.of(new Match(machine, 100));
            }

            // 3) Merikó tairiasma sto onoma - ME AUSTIRO ELENXO ARITHMOU
            if (!name.isBlank()) {
                score = Math.max(score, partialScore(rawTokens, rawIndex, name));
            }

            // 4) Merikó tairiasma ston kodiko (p.x. "PRS1" mesa sto "7100-EXT-PRS1")
            if (machine.getCode() != null) {
                score = Math.max(score, partialScore(rawTokens, rawIndex, normalize(machine.getCode())));
            }

            if (score <= 0) {
                continue;
            }
            if (best == null || score > best.confidence()) {
                best = new Match(machine, score);
                tiedAtBest = 1;
            } else if (score == best.confidence()) {
                tiedAtBest++;
            }
        }

        if (best == null || best.confidence() < 70) {
            // Katw apo 70 den to protinoume kan - einai pio pithano na mperdepsei
            // ton xristi para na ton voithisei. Kalytera "nea mihani" para lathos antistoixisi.
            return Optional.empty();
        }
        if (tiedAtBest > 1) {
            // Isovathmia -> amfisimo. Apofasizei o xristis.
            return Optional.empty();
        }
        return Optional.of(best);
    }

    private static int partialScore(List<String> rawTokens, String rawIndex, String other) {
        String otherIndex = identifyingNumber(other);

        // O KANONAS ASFALEIAS: an kai ta dyo exoun arithmo taftotitas kai diaferei,
        // DEN einai i idia mihani. "Πρέσα 1" vs "Πρέσα 2" -> telos edo.
        if (rawIndex != null && otherIndex != null && !rawIndex.equals(otherIndex)) {
            return 0;
        }

        List<String> otherTokens = tokens(other);
        if (otherTokens.isEmpty()) {
            return 0;
        }

        // Posa apo ta "kommatia" tou pio SYNTOMOU onomatos yparxoun sto pio makry;
        List<String> shorter = rawTokens.size() <= otherTokens.size() ? rawTokens : otherTokens;
        List<String> longer = rawTokens.size() <= otherTokens.size() ? otherTokens : rawTokens;

        int matched = 0;
        for (String token : shorter) {
            // Ta monopsifia/monogrammata den metrane: to "1" tha tairiaze me
            // OTIDIPOTE periexei to psifio 1 (p.x. "συμπιεστης αερα 1" me "crl1"),
            // pou einai akrivos i epikindyni lathos antistoixisi pou theloume na apofygoume.
            if (token.length() < 2) {
                continue;
            }
            for (String candidate : longer) {
                if (candidate.length() < 2) {
                    continue;
                }
                if (candidate.equals(token) || candidate.contains(token) || token.contains(candidate)) {
                    matched++;
                    break;
                }
            }
        }

        long meaningful = shorter.stream().filter(t -> t.length() >= 2).count();
        if (meaningful == 0) {
            return 0;
        }

        int base = (int) Math.round(100.0 * matched / meaningful);

        if (rawIndex != null && rawIndex.equals(otherIndex)) {
            // Symfonoun kai sto onoma kai ston arithmo -> pio sigouro
            base = Math.min(100, base + 5);
        } else if ((rawIndex == null) != (otherIndex == null)) {
            // To ena leei "Πρέσα" kai to allo "Πρέσα 1". Den kseroume POIA presa ennoei -
            // mporei na yparxoun 3. Riхnoume poly ti sigouria, oste na min protathei
            // katholou kai na apofasisei o xristis.
            base -= 35;
        }

        return Math.max(0, base);
    }

    // Peza, xoris tonous, xoris simeia stixis
    static String normalize(String value) {
        if (value == null) {
            return "";
        }
        String s = value.toLowerCase()
                .replace("ά", "α").replace("έ", "ε").replace("ή", "η").replace("ί", "ι")
                .replace("ό", "ο").replace("ύ", "υ").replace("ώ", "ω")
                .replace("ϊ", "ι").replace("ΐ", "ι").replace("ϋ", "υ").replace("ΰ", "υ")
                .replace("ς", "σ");
        return s.replaceAll("[^a-zα-ω0-9]+", " ").trim().replaceAll("\\s+", " ");
    }

    private static List<String> tokens(String normalized) {
        if (normalized.isBlank()) {
            return List.of();
        }
        return new ArrayList<>(Arrays.asList(normalized.split(" ")));
    }

    // O "arithmos taftotitas" tis mihanis: o TELEFTAIOS arithmos mesa sto onoma.
    //
    // Giati o teleftaios kai oxi oloi: enas kodikos san "7100-EXT-PRS1" exei kai to
    // 7100 (arithmos ergostasiou, koinos se oles tis mihanes) kai to 1 (i sygkekrimeni
    // presa). Auto pou ksexorizei ti mihani einai to DEFTERO. An sygriname olous tous
    // arithmous, to "Πρέσα 1" den tha tairiaze pote me to "7100-EXT-PRS1".
    private static String identifyingNumber(String normalized) {
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("\\d+").matcher(normalized);
        String last = null;
        while (m.find()) {
            last = m.group();
        }
        // Afairoume ta midenika mprosta, oste "01" kai "1" na theorountai idia
        return last == null ? null : last.replaceFirst("^0+(?!$)", "");
    }
}
