package com.codingfactory.maintrack.dto;

import java.util.List;

// Oi protaseis antistoixisis mihanon, PRIN ginei i eisagogi.
// O xristis tis vlepei se pinaka kai tis epivevaionei i tis allazei.
public class MachineMatchResponse {

    private List<MachineMatch> matches;

    public MachineMatchResponse() {
    }

    public MachineMatchResponse(List<MachineMatch> matches) {
        this.matches = matches;
    }

    public List<MachineMatch> getMatches() {
        return matches;
    }

    public void setMatches(List<MachineMatch> matches) {
        this.matches = matches;
    }

    public static class MachineMatch {
        // Pos einai grammeno sto arxeio tou xristi (p.x. "Πρέσα 1")
        private String rawName;
        // Se poses grammes emfanizetai
        private int rowCount;
        // I protasi mas (null = "nea mihani")
        private Long machineId;
        private String machineCode;
        private String machineName;
        // Poso sigouri einai i protasi (0-100)
        private int confidence;

        public MachineMatch() {
        }

        public MachineMatch(String rawName, int rowCount) {
            this.rawName = rawName;
            this.rowCount = rowCount;
        }

        public String getRawName() {
            return rawName;
        }

        public void setRawName(String rawName) {
            this.rawName = rawName;
        }

        public int getRowCount() {
            return rowCount;
        }

        public void setRowCount(int rowCount) {
            this.rowCount = rowCount;
        }

        public Long getMachineId() {
            return machineId;
        }

        public void setMachineId(Long machineId) {
            this.machineId = machineId;
        }

        public String getMachineCode() {
            return machineCode;
        }

        public void setMachineCode(String machineCode) {
            this.machineCode = machineCode;
        }

        public String getMachineName() {
            return machineName;
        }

        public void setMachineName(String machineName) {
            this.machineName = machineName;
        }

        public int getConfidence() {
            return confidence;
        }

        public void setConfidence(int confidence) {
            this.confidence = confidence;
        }
    }
}
