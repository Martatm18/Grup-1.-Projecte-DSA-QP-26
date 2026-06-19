package edu.upc.dsa.models;

public class SigmaObject {
    private Integer id;
    private String nombre;
    private String descripcion;
    private String tipo;
    private String contenido;
    private boolean obtenido;

    public SigmaObject() {}

    public SigmaObject(Integer id, String nombre, String descripcion, String tipo, String contenido, boolean obtenido) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.tipo = tipo;
        this.contenido = contenido;
        this.obtenido = obtenido;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public String getContenido() { return contenido; }
    public void setContenido(String contenido) { this.contenido = contenido; }

    public boolean isObtenido() { return obtenido; }
    public void setObtenido(boolean obtenido) { this.obtenido = obtenido; }
}
