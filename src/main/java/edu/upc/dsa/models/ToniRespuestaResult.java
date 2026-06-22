package edu.upc.dsa.models;

public class ToniRespuestaResult {
    private boolean correcto;
    private int ects_ganados;
    private int ects_total;

    public ToniRespuestaResult() {}

    public ToniRespuestaResult(boolean correcto, int ects_ganados, int ects_total) {
        this.correcto = correcto;
        this.ects_ganados = ects_ganados;
        this.ects_total = ects_total;
    }

    public boolean isCorrecto() { return correcto; }
    public void setCorrecto(boolean correcto) { this.correcto = correcto; }

    public int getEcts_ganados() { return ects_ganados; }
    public void setEcts_ganados(int ects_ganados) { this.ects_ganados = ects_ganados; }

    public int getEcts_total() { return ects_total; }
    public void setEcts_total(int ects_total) { this.ects_total = ects_total; }
}
