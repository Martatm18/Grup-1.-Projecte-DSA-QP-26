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

    // Para registrar un nuevo usuario con password y email
    public int registerUser(String id, String nombre, String password, String email);

    // Para validar credenciales
    public User loginUser(String id, String password);

    // Para buscar un usuario por su ID
    public User getUser(String idUser);

    // Para eliminar un producto del inventario de un usuario
    public int eliminarProductoInventario(String idProducto, String idUser);
}
