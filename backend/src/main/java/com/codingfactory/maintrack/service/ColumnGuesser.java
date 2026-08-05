package com.codingfactory.maintrack.service;

import com.codingfactory.maintrack.dto.ColumnMappingRequest;

import java.util.*;

// "Mantevei" poia stili tou arxeiou tou xristi einai ti, koitontas to ONOMA tis.
//
// Den einai teleio kai den xreiazetai na einai: to apotelesma einai apla i
// PROSYMPLIROSI ton dropdowns sto frontend. O xristis to vlepei kai to diorthonei
// an xreiazetai. Skopos einai na min xreiazetai na ta dialexei ola apo tin arxi.
final class ColumnGuesser {

    private ColumnGuesser() {
    }

    // Gia kathe diko mas pedio, oi lexeis-kleidia pou psaxnoume stis epikefalides.
    // I seira exei simasia: to proto pedio pou tairiazei "kleidonei" ti stili,
    // gi' auto ta pio eidika (p.x. kodikos mihanis) mpainoun PRIN ta pio genika.
    private static final LinkedHashMap<String, List<String>> KEYWORDS = new LinkedHashMap<>();

    static {
        KEYWORDS.put("externalRef", List.of(
                "γνωστοποιη", "notification", "notif", "αριθμοςβλαβης", "κωδικοςβλαβης",
                "externalref", "ref", "ticket"));

        KEYWORDS.put("machineCode", List.of(
                "κωδικοςμηχαν", "μηχανη", "μηχανημα", "εξοπλισμ", "equipment", "machine",
                "flocaffected", "functionallocation", "floc", "asset", "τοποθεσια"));

        KEYWORDS.put("machineName", List.of(
                "ονομαμηχαν", "περιγραφηεξοπλισμ", "machinename", "equipmentdescription",
                "λειτουργικηπεριοχη"));

        KEYWORDS.put("title", List.of(
                "τιτλος", "προβλημα", "βλαβη", "συμπτωμα", "θεμα", "shorttext",
                "faulttitle", "issue", "problem", "συντομηπεριγραφη"));

        KEYWORDS.put("severity", List.of(
                "σοβαροτητα", "κρισιμοτητα", "προτεραιοτητα", "severity", "priority", "criticality"));

        KEYWORDS.put("status", List.of(
                "κατασταση", "status", "systemstatus", "state", "στατους"));

        KEYWORDS.put("technician", List.of(
                "τεχνικος", "υπευθυνος", "χειριστης", "reportedby", "createdby", "changedby",
                "username", "technician", "user", "χρηστης", "ονοματεπωνυμο"));

        KEYWORDS.put("downtime", List.of(
                "χρονοςδιακοπ", "διακοπ", "ωρεςεκτος", "downtime", "breakdowndur", "duration",
                "διαρκεια", "νεκροςχρονος", "stoppage"));

        KEYWORDS.put("action", List.of(
                "ενεργεια", "εργασια", "αποκαταστ", "επισκευη", "action", "workdone", "remedy"));

        KEYWORDS.put("description", List.of(
                "περιγραφη", "λεπτομερ", "σχολια", "παρατηρησ", "description", "longtext",
                "details", "notes", "comments"));
    }

    static ColumnMappingRequest guess(List<String> headers) {
        ColumnMappingRequest mapping = new ColumnMappingRequest();

        List<String> normalized = headers.stream().map(ColumnGuesser::normalize).toList();
        Set<Integer> used = new HashSet<>();

        for (Map.Entry<String, List<String>> entry : KEYWORDS.entrySet()) {
            Integer column = findBest(normalized, entry.getValue(), used);
            if (column == null) {
                continue;
            }
            used.add(column);
            apply(mapping, entry.getKey(), column);
        }

        // An i stili tou xronou diakopis milaei gia "ores", to simeionoume oste
        // na ginei i metatropi se lepta kata tin eisagogi.
        if (mapping.getDowntime() != null) {
            String h = normalized.get(mapping.getDowntime());
            if (h.contains("ωρ") || h.contains("hour") || h.contains("(h)")) {
                mapping.setDowntimeUnit("HOURS");
            }
        }

        return mapping;
    }

    // Vriskei tin kalyteri diathesimi stili gia mia lista lexeon-kleidion.
    // Protimame tin akrivi tautisi· an den yparxei, tin proti pou PERIEXEI ti lexi.
    //
    // PROSOXI: pername KAI tis lexeis-kleidia apo tin normalize(). Alliws mia lexi
    // grammeni edo san "ωρεςεκτος" den tha tairiaze pote me tin epikefalida
    // "Ώρες εκτός", pou meta tin kanonikopoiisi ginetai "ωρεσεκτοσ" (teliko ς -> σ).
    private static Integer findBest(List<String> normalized, List<String> keywords, Set<Integer> used) {
        List<String> keys = keywords.stream().map(ColumnGuesser::normalize).toList();

        for (String keyword : keys) {
            for (int i = 0; i < normalized.size(); i++) {
                if (!used.contains(i) && normalized.get(i).equals(keyword)) {
                    return i;
                }
            }
        }
        for (String keyword : keys) {
            for (int i = 0; i < normalized.size(); i++) {
                if (!used.contains(i) && !normalized.get(i).isBlank() && normalized.get(i).contains(keyword)) {
                    return i;
                }
            }
        }
        return null;
    }

    private static void apply(ColumnMappingRequest m, String field, int column) {
        switch (field) {
            case "externalRef" -> m.setExternalRef(column);
            case "machineCode" -> m.setMachineCode(column);
            case "machineName" -> m.setMachineName(column);
            case "title" -> m.setTitle(column);
            case "description" -> m.setDescription(column);
            case "severity" -> m.setSeverity(column);
            case "status" -> m.setStatus(column);
            case "technician" -> m.setTechnician(column);
            case "action" -> m.setAction(column);
            case "downtime" -> m.setDowntime(column);
            default -> { /* agnosto pedio - den kanoume tipota */ }
        }
    }

    // Peza, xoris tonous, xoris kena/teleies - oste "Κωδικός Μηχανής" kai
    // "κωδικος μηχανης" na theorountai to idio.
    static String normalize(String header) {
        if (header == null) {
            return "";
        }
        String s = header.toLowerCase()
                .replace("ά", "α").replace("έ", "ε").replace("ή", "η").replace("ί", "ι")
                .replace("ό", "ο").replace("ύ", "υ").replace("ώ", "ω")
                .replace("ϊ", "ι").replace("ΐ", "ι").replace("ϋ", "υ").replace("ΰ", "υ")
                .replace("ς", "σ");
        return s.replaceAll("[\\s._\\-/]", "");
    }
}
