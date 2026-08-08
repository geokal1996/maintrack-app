package com.codingfactory.maintrack.service;

import com.codingfactory.maintrack.dto.ColumnMappingRequest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

// O ColumnGuesser prosymplironi ta dropdowns tis antistoixisis stilon.
// Den xreiazetai na einai teleios - o xristis to elenxei kai to diorthonei.
// Auta pou PREPEI na douleuoun einai i ellinikes epikefalides me tonous kai
// teliko sigma, giati ekei akrivos eixame ena "aorato" bug: to normalize()
// metetrepe to teliko ς se σ stin epikefalida ALLA OXI sti lexi-kleidi,
// opote den tairiaze POTE tipota kai kaneis den to katalavaine.
class ColumnGuesserTest {

    @Test
    void anagnorizeiOlesTisStilesTouDikouMasYpodeigmatos() {
        ColumnMappingRequest m = ColumnGuesser.guess(List.of(
                "Αρ. Γνωστοποίησης", "Κωδικός Μηχανής", "Τίτλος Βλάβης", "Περιγραφή",
                "Σοβαρότητα", "Κατάσταση", "Τεχνικός (username)", "Ενέργεια Συντήρησης",
                "Χρόνος Διακοπής (λεπτά)"));

        assertThat(m.getExternalRef()).isEqualTo(0);
        assertThat(m.getMachineCode()).isEqualTo(1);
        assertThat(m.getTitle()).isEqualTo(2);
        assertThat(m.getDescription()).isEqualTo(3);
        assertThat(m.getSeverity()).isEqualTo(4);
        assertThat(m.getStatus()).isEqualTo(5);
        assertThat(m.getTechnician()).isEqualTo(6);
        assertThat(m.getAction()).isEqualTo(7);
        assertThat(m.getDowntime()).isEqualTo(8);
    }

    @Test
    void anagnorizeiTiMonadaTouXronouDiakopisApoTinEpikefalida() {
        ColumnMappingRequest minutes = ColumnGuesser.guess(List.of("Χρόνος Διακοπής (λεπτά)"));
        assertThat(minutes.getDowntimeUnit()).isEqualTo("MINUTES");

        ColumnMappingRequest hours = ColumnGuesser.guess(List.of("Ώρες εκτός λειτουργίας"));
        assertThat(hours.getDowntimeUnit()).isEqualTo("HOURS");
    }

    @Test
    void douleveiKaiMeElefthereEpikefalidesXeirokinitouArxeiou() {
        // Etsi grafei o proistamenos to diko tou Excel - alles lexeis, idio noima
        ColumnMappingRequest m = ColumnGuesser.guess(List.of(
                "Ημερομηνία", "Μηχάνημα", "Πρόβλημα", "Ώρες εκτός λειτουργίας", "Ποιος το έφτιαξε"));

        assertThat(m.getDate()).isEqualTo(0);
        assertThat(m.getMachineCode()).isEqualTo(1);
        assertThat(m.getTitle()).isEqualTo(2);
        assertThat(m.getDowntime()).isEqualTo(3);
        assertThat(m.getDowntimeUnit()).isEqualTo("HOURS");

        // To "Ποιος το έφτιαξε" DEN to anagnorizei - kai einai entaxei.
        // Kalytera na min mantepsei, para na valei lathos stili kai na perasoun
        // oi vlaves se lathos texniko.
        assertThat(m.getTechnician()).isNull();
    }

    @Test
    void kamiaStiliDenXrisimopoieitaiDyoFores() {
        ColumnMappingRequest m = ColumnGuesser.guess(List.of(
                "Κωδικός Μηχανής", "Όνομα Μηχανής", "Τίτλος"));

        assertThat(m.getMachineCode()).isEqualTo(0);
        assertThat(m.getMachineName()).isEqualTo(1);
        assertThat(m.getTitle()).isEqualTo(2);
    }

    @Test
    void toNormalizeVgazeiTonousTelikoSigmaKaiParentheseis() {
        assertThat(ColumnGuesser.normalize("Χρόνος (min)")).isEqualTo("χρονοσmin");
        assertThat(ColumnGuesser.normalize("Ώρες εκτός")).isEqualTo("ωρεσεκτοσ");
        assertThat(ColumnGuesser.normalize("Αρ. Γνωστοποίησης")).isEqualTo("αργνωστοποιησησ");
    }
}
