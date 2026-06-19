package edu.upc.dsa.models;

import java.util.List;

public class CanCompleteObjective {
    private boolean canComplete;
    private String code;
    private String message;
    private List<String> missingRequirements;

    public CanCompleteObjective() {}

    public CanCompleteObjective(boolean canComplete, String code, String message, List<String> missingRequirements) {
        this.canComplete = canComplete;
        this.code = code;
        this.message = message;
        this.missingRequirements = missingRequirements;
    }

    public boolean isCanComplete() { return canComplete; }
    public void setCanComplete(boolean canComplete) { this.canComplete = canComplete; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public List<String> getMissingRequirements() { return missingRequirements; }
    public void setMissingRequirements(List<String> missingRequirements) { this.missingRequirements = missingRequirements; }
}
