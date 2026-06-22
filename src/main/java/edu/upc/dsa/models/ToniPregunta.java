package edu.upc.dsa.models;

public class ToniPregunta {
    private int id;
    private String titulo;
    private String pregunta;

    public ToniPregunta() {}

    public ToniPregunta(int id, String titulo, String pregunta) {
        this.id = id;
        this.titulo = titulo;
        this.pregunta = pregunta;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getPregunta() { return pregunta; }
    public void setPregunta(String pregunta) { this.pregunta = pregunta; }
}
