package com.codingfactory.maintrack.service;

import com.codingfactory.maintrack.model.FaultSeverity;
import com.codingfactory.maintrack.model.FaultStatus;

import java.time.LocalDateTime;

// "Endiamesi" morfi mias grammis, koini kai gia to diko mas template kai gia to SAP.
//
// Giati yparxei: ta dyo arxeia exoun TELEIOS DIAFORETIKES stiles. Anti na grapsoume
// dyo fores ti logiki apothikefsis, kathe morfi metafrazetai PROTA se auto to
// antikeimeno, kai meta yparxei ENA kommati kodika pou apothikevei. Etsi an
// avrio prostethei kai trito format, allazei mono o "metafrastis".
class ImportRow {

    String externalRef;
    String machineCode;
    String machineName;
    String machineArea;
    String title;
    String description;
    FaultSeverity severity;
    FaultStatus status;
    String technicianUsername;
    String actionDescription;
    Integer downtimeMinutes;
    LocalDateTime resolvedAt;

    // An mia grammi prepei na agnoithei entelos (p.x. SAP gnostopoiisi me simaia
    // diagrafis DLFL), ti simadevoume etsi anti na petaxoume exception.
    boolean skip;
    String skipReason;

    static ImportRow skipped(String reason) {
        ImportRow row = new ImportRow();
        row.skip = true;
        row.skipReason = reason;
        return row;
    }
}
