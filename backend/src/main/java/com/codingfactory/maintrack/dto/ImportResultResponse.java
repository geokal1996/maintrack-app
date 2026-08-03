package com.codingfactory.maintrack.dto;

import java.util.ArrayList;
import java.util.List;

// To apotelesma enos import: poses grammes diavastikan, poses perasan,
// poses prosperastikan (idi yparxoun) kai poies apotixan me poio logo.
public class ImportResultResponse {

    private int totalRows;
    private int imported;
    private int skipped;
    private List<RowError> errors = new ArrayList<>();

    public void addError(int rowNumber, String message) {
        this.errors.add(new RowError(rowNumber, message));
    }

    public int getTotalRows() {
        return totalRows;
    }

    public void setTotalRows(int totalRows) {
        this.totalRows = totalRows;
    }

    public int getImported() {
        return imported;
    }

    public void setImported(int imported) {
        this.imported = imported;
    }

    public int getSkipped() {
        return skipped;
    }

    public void setSkipped(int skipped) {
        this.skipped = skipped;
    }

    public int getFailed() {
        return errors.size();
    }

    public List<RowError> getErrors() {
        return errors;
    }

    public void setErrors(List<RowError> errors) {
        this.errors = errors;
    }

    // Ena lathos se sigkekrimeni grammi tou Excel
    public static class RowError {
        private int row;
        private String message;

        public RowError() {
        }

        public RowError(int row, String message) {
            this.row = row;
            this.message = message;
        }

        public int getRow() {
            return row;
        }

        public void setRow(int row) {
            this.row = row;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
}
