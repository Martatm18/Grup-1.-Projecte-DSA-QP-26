package edu.upc.dsa.models;

public class SigmaResponseWithProgress {
    private String respuesta;
    private boolean objectiveCompleted;
    private ObjectiveResult objectiveProgress;

    public SigmaResponseWithProgress() {}

    public SigmaResponseWithProgress(String respuesta, ObjectiveResult objectiveProgress) {
        this.respuesta = respuesta;
        this.objectiveCompleted = true;
        this.objectiveProgress = objectiveProgress;
    }

    public String getRespuesta() { return respuesta; }
    public void setRespuesta(String respuesta) { this.respuesta = respuesta; }

    public boolean isObjectiveCompleted() { return objectiveCompleted; }
    public void setObjectiveCompleted(boolean objectiveCompleted) { this.objectiveCompleted = objectiveCompleted; }

    public ObjectiveResult getObjectiveProgress() { return objectiveProgress; }
    public void setObjectiveProgress(ObjectiveResult objectiveProgress) { this.objectiveProgress = objectiveProgress; }
}
