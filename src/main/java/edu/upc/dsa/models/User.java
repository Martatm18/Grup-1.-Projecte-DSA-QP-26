package edu.upc.dsa.models;
import java.util.ArrayList;
import java.util.List;

public class User
{
    private String id;
    private String nombre;
    private int ects; // Saldo actual de créditos ECTS
    private List<Producto> inventario; // Lista de objetos que el usuario ha comprado

    public User(String id, String nombre)
    {
        this.id = id;
        this.nombre = nombre;
        this.ects = 100; //El personaje tendrá 100ECTS para empezar a comprar
        this.inventario = new ArrayList<>(); //Empezamos con la mochila vacía
    }

    public int getEcts()
    {
        return this.ects;
    }

    public void addEcts(int cantidad)
    {
        this.ects += cantidad;
    }

    // Resta el precio del producto al saldo del usuario
    public void subtractEcts(int cantidad)
    {
        this.ects -= cantidad;
    }

    public List<Producto> getInventario()
    {
        return inventario;
    }

    // Añade un producto a la mochila del usuario
    public void addObjeto(Producto p)
    {
        this.inventario.add(p);
    }

    public String getId()
    {
        return id;
    }
}
