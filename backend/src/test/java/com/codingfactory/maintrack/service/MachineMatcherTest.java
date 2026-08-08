package com.codingfactory.maintrack.service;

import com.codingfactory.maintrack.model.Machine;
import com.codingfactory.maintrack.model.MachineStatus;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

// O MachineMatcher einai to pio "epikindino" kommati tis efarmogis: an antistoixisei
// lathos mihani, oi vlaves tis mias metrane stin alli kai TO PARETO DEIXNEI LATHOS
// ENOXO. Kai to xeirotero, kaneis den to katalavainei - to diagramma fainetai mia xara.
//
// Gi' auto ta tests edo den elenxoun mono "vriskei to sosto", alla kyrios
// "DEN protinei otan den einai sigouro".
class MachineMatcherTest {

    private Machine machine(String code, String name) {
        return new Machine(code, name, "Δοκιμαστική Περιοχή", MachineStatus.OPERATIONAL);
    }

    private final List<Machine> catalog = List.of(
            machine("CRL1", "Γραμμή Ψυχρής Έλασης 1"),
            machine("CRL2", "Γραμμή Ψυχρής Έλασης 2"),
            machine("SAW1", "Πριόνι Κοπής Ράβδων 1"),
            machine("CMP2", "Συμπιεστής Αέρα 2"),
            machine("7100-EXT-PRS1", "ΠΡΕΣΑ ΔΙΕΛΑΣΗΣ 1")
    );

    @Test
    void akrivisKodikosDineiApolytiSigouria() {
        Optional<MachineMatcher.Match> match = MachineMatcher.findBest("CRL1", catalog);

        assertThat(match).isPresent();
        assertThat(match.get().machine().getCode()).isEqualTo("CRL1");
        assertThat(match.get().confidence()).isEqualTo(100);
    }

    @Test
    void toOnomaTairiazeiAkomaKiMePezaKaiTonous() {
        // O proistamenos grafei "γραμμη ψυχρησ ελασησ 1" - xoris tonous, me teliko sigma
        Optional<MachineMatcher.Match> match = MachineMatcher.findBest("γραμμη ψυχρησ ελασησ 1", catalog);

        assertThat(match).isPresent();
        assertThat(match.get().machine().getCode()).isEqualTo("CRL1");
    }

    @Test
    void diaforetikosArithmosDenTairiazeiPote() {
        // "Γραμμή Ψυχρής Έλασης 2" moiazei 90% me tin "1" san keimeno.
        // Prepei na paei stin CRL2, POTE stin CRL1.
        Optional<MachineMatcher.Match> match = MachineMatcher.findBest("Γραμμή Ψυχρής Έλασης 2", catalog);

        assertThat(match).isPresent();
        assertThat(match.get().machine().getCode()).isEqualTo("CRL2");
    }

    @Test
    void monopsifioDenTairiazeiMeKodikoPouTyxainiNaExeiToIdioPsifio() {
        // To "1" tou "Συμπιεστής αέρα 1" DEN prepei na tairiaxei me to "1" mesa sto "CRL1".
        // An to ekane, oi vlaves tou sympiesti tha metrousan sti grammi elasis.
        Optional<MachineMatcher.Match> match = MachineMatcher.findBest("Συμπιεστής αέρα 1", catalog);

        // I CMP2 einai "Συμπιεστής Αέρα 2" - allos arithmos, ara den tairiazei oute auti.
        // Sosto apotelesma: kammia protasi, apofasizei o xristis.
        assertThat(match).isEmpty();
    }

    @Test
    void oKodikosTouSapAnagnorizetaiApoToTeleftaioNoumero() {
        // "Πρέσα 1" -> "7100-EXT-PRS1". To 7100 einai kodikos ergostasiou (koinos),
        // to 1 einai i sygkekrimeni presa. Sygkrinoume to TELEFTAIO noumero.
        Optional<MachineMatcher.Match> match = MachineMatcher.findBest("ΠΡΕΣΑ ΔΙΕΛΑΣΗΣ 1", catalog);

        assertThat(match).isPresent();
        assertThat(match.get().machine().getCode()).isEqualTo("7100-EXT-PRS1");
    }

    @Test
    void agnostoOnomaDenProtineiTipota() {
        Optional<MachineMatcher.Match> match = MachineMatcher.findBest("Ανυψωτικό Παλετών 7", catalog);

        assertThat(match).isEmpty();
    }

    @Test
    void kenoOnomaDenSkaeiKaiDenProtineiTipota() {
        assertThat(MachineMatcher.findBest(null, catalog)).isEmpty();
        assertThat(MachineMatcher.findBest("   ", catalog)).isEmpty();
    }

    @Test
    void toNormalizeVgazeiTonousKaiTelikoSigma() {
        assertThat(MachineMatcher.normalize("Γραμμή Ψυχρής Έλασης")).isEqualTo("γραμμη ψυχρησ ελασησ");
        assertThat(MachineMatcher.normalize("Χρόνος (min)")).isEqualTo("χρονοσ min");
    }
}
