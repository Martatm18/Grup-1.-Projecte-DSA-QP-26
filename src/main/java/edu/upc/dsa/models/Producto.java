package edu.upc.dsa.models;

public class Producto
{
    // Atributos básicos de un producto de la tienda
    private String id;
    private String nombre;
    private String descripcion;
    private int precio; // El coste en la moneda del juego (ECTS)

    // Constructor vacío necesario para que los frameworks de JSON funcionen
    public Producto() {}

    // Constructor para inicializar productos rápidamente
    public Producto(String id, String nombre, String descripcion, int precio)
    {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precio = precio;
    }

    // Getters y Setters: permiten leer y modificar los datos desde otras clases
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
    public int getPrecio()
    {
        return precio;
    }
}
