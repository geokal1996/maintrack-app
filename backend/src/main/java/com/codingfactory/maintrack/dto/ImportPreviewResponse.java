package com.codingfactory.maintrack.dto;

import java.util.List;

// Auto pou epistrefoume otan o xristis dialexei arxeio, PRIN kanei tin eisagogi.
// Periexei ti "vrikame" mesa, oste to frontend na tou deixei tis stiles tou kai
// na ton afisei na tis antistoixisei.
public class ImportPreviewResponse {

    // "MAINTRACK_TEMPLATE", "SAP_IW29" i "UNKNOWN"
    private String detectedFormat;

    // Ta onomata ton stilon, opos akrivos einai sto arxeio tou xristi
    private List<String> headers;

    // Oi protes grammes, gia na dei o xristis ti periexei i kathe stili
    private List<List<String>> sampleRows;

    // Poses grammes dedomenon exei synolika to arxeio
    private int totalRows;

    // I protasi mas: mantepsame poia stili einai ti, me vasi to onoma tis.
    // O xristis mporei na tin allaxei prin patisei "Eisagogi".
    private ColumnMappingRequest suggestedMapping;

    public String getDetectedFormat() {
        return detectedFormat;
    }

    public void setDetectedFormat(String detectedFormat) {
        this.detectedFormat = detectedFormat;
    }

    public List<String> getHeaders() {
        return headers;
    }

    public void setHeaders(List<String> headers) {
        this.headers = headers;
    }

    public List<List<String>> getSampleRows() {
        return sampleRows;
    }

    public void setSampleRows(List<List<String>> sampleRows) {
        this.sampleRows = sampleRows;
    }

    public int getTotalRows() {
        return totalRows;
    }

    public void setTotalRows(int totalRows) {
        this.totalRows = totalRows;
    }

    public ColumnMappingRequest getSuggestedMapping() {
        return suggestedMapping;
    }

    public void setSuggestedMapping(ColumnMappingRequest suggestedMapping) {
        this.suggestedMapping = suggestedMapping;
    }
}
