package edu.upc.dsa.models;

public class Producto
{
    // Atributos básicos de un producto de la tienda
    private Integer id;
    private String nombre;
    private String descripcion;
    private int precio;
    private String slug;

    // Constructor vacío necesario para que los frameworks de JSON funcionen
    public Producto() {}

    // Constructor para inicializar productos rápidamente
    public Producto(Integer id, String nombre, String descripcion, int precio)
    {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precio = precio;
    }

    // Getters y Setters: permiten leer y modificar los datos desde otras clases
    public Integer getId()
    {
        return id;
    }
    public void setId(Integer id)
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
    public int getPrecio()
    {
        return precio;
    }
    public void setPrecio(int precio)
    {
        this.precio = precio;
    }
    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }
}
