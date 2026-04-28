package edu.upc.dsa.models;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlTransient;

public class User
{
    private String id;
    private String nombre;
    private String password;
    private int ects;
    private List<Producto> inventario;

    public User() {}

    public User(String id, String nombre)
    {
        this(id, nombre, "");
    }

    public User(String id, String nombre, String password)
    {
        this.id = id;
        this.nombre = nombre;
        this.password = password;
        this.ects = 100;
        this.inventario = new ArrayList<>();
    }

    public int getEcts()
    {
        return this.ects;
    }

    public void setEcts(int ects)
    {
        this.ects = ects;
    }

    public void addEcts(int cantidad)
    {
        this.ects += cantidad;
    }

    public void subtractEcts(int cantidad)
    {
        this.ects -= cantidad;
    }

    public List<Producto> getInventario()
    {
        return inventario;
    }

    public void setInventario(List<Producto> inventario)
    {
        this.inventario = inventario;
    }

    public void addObjeto(Producto p)
    {
        this.inventario.add(p);
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

    @XmlTransient
    public String getPassword()
    {
        return password;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }
}
