package edu.upc.dsa.models;

public class Equipo
{
    private String id;
    private String nombre;
    private String descripcion;
    private int miembros;

    public Equipo()
    {
    }

    public Equipo(String id, String nombre, String descripcion)
    {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
    }

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public String getNombre()
    {
        return nombre;
    }

    public void setNombre(String nombre)
    {
        this.nombre = nombre;
    }

    public String getDescripcion()
    {
        return descripcion;
    }

    public void setDescripcion(String descripcion)
    {
        this.descripcion = descripcion;
    }

    public int getMiembros() { return miembros; }
    public void setMiembros(int miembros) { this.miembros = miembros; }
}
