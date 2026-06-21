package edu.upc.dsa.models;

import java.util.List;

public class SigmaResponseWithProgress {
    private String respuesta;
    private boolean objectiveCompleted;
    private ObjectiveResult objectiveProgress;
    private List<String> registrosObtenidos;

    public SigmaResponseWithProgress() {}

    public SigmaResponseWithProgress(String respuesta, ObjectiveResult objectiveProgress, List<String> registros) {
        this.respuesta = respuesta;
        this.objectiveCompleted = true;
        this.objectiveProgress = objectiveProgress;
        this.registrosObtenidos = registros;
    }

    public String getRespuesta() { return respuesta; }
    public void setRespuesta(String respuesta) { this.respuesta = respuesta; }

    public boolean isObjectiveCompleted() { return objectiveCompleted; }
    public void setObjectiveCompleted(boolean objectiveCompleted) { this.objectiveCompleted = objectiveCompleted; }

    public ObjectiveResult getObjectiveProgress() { return objectiveProgress; }
    public void setObjectiveProgress(ObjectiveResult objectiveProgress) { this.objectiveProgress = objectiveProgress; }

    public List<String> getRegistrosObtenidos() { return registrosObtenidos; }
    public void setRegistrosObtenidos(List<String> registrosObtenidos) { this.registrosObtenidos = registrosObtenidos; }
}
