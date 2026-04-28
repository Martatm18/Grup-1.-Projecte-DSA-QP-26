package edu.upc.dsa;
import java.util.List;
import edu.upc.dsa.models.Producto;
import edu.upc.dsa.models.User;

public interface ProductoManager
{
    // Para obtener todos los productos que hay en la tienda
    public List<Producto> getListaProductos();

    // Para realizar la transacción de compra
    public int comprarProducto(String idProducto, String idUser);

    // Para registrar un nuevo usuario en el sistema
    public void addUser(String id, String nombre);

    // Para buscar un usuario por su ID
    public User getUser(String idUser);

}