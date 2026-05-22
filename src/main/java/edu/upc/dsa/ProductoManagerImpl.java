package edu.upc.dsa;
import edu.upc.dsa.models.*;
import java.util.*;
import edu.upc.dsa.db.GameStateDAO;
import edu.upc.dsa.db.ProductoDAO;
import edu.upc.dsa.db.UserDAO;

public class ProductoManagerImpl implements ProductoManager
{
    private static ProductoManager instance;
    private ProductoDAO productoDAO;
    private GameStateDAO gameStateDAO;
    private UserDAO userDAO;

    private ProductoManagerImpl() {
        this.productoDAO = new ProductoDAO();
        this.gameStateDAO = new GameStateDAO();
        this.userDAO = new UserDAO();
    }

    public static ProductoManager getInstance()
    {
        if (instance == null) instance = new ProductoManagerImpl();
        return instance;
    }

    @Override
    public List<Producto> getListaProductos()
    {
        return productoDAO.getProductos();
    }

    @Override
    public int comprarProducto(String idProducto, String idUser)
    {
        if (idProducto == null || idUser == null) {
            return 400;
        }

        Integer productId;
        try {
            productId = Integer.parseInt(idProducto);
        } catch (NumberFormatException e) {
            return 400;
        }

        User u = userDAO.getUser(idUser.trim());
        Producto p = productoDAO.getProducto(productId);

        if (u == null || p == null)
        {
            return 404;
        }

        if (u.getEcts() >= p.getPrecio())
        {
            u.subtractEcts(p.getPrecio());
            u.addObjeto(p);
            productoDAO.comprarProducto(u, p);
            return 201;
        }

        return 402;
    }

    @Override
    public void addUser(String id, String nombre)
    {
        registerUser(id, nombre, "", null);
    }

    @Override
    public int registerUser(String id, String nombre, String password, String email)
    {
        if (id == null || id.trim().isEmpty() || password == null || password.trim().isEmpty())
        {
            return 400;
        }

        String cleanId = id.trim();
        String cleanName = nombre == null || nombre.trim().isEmpty() ? cleanId : nombre.trim();
        String cleanEmail = email == null || email.trim().isEmpty() ? null : email.trim().toLowerCase();

        return userDAO.registerUser(cleanId, cleanName, password, cleanEmail);
    }

    @Override
    public User loginUser(String id, String password)
    {
        if (id == null || password == null)
        {
            return null;
        }

        User user = userDAO.loginUser(id.trim(), password);
        loadUserData(user);

        return user;
    }

    @Override
    public User getUser(String idUser) {
        if (idUser == null || idUser.trim().isEmpty())
        {
            return null;
        }

        User user = userDAO.getUser(idUser.trim());
        loadUserData(user);

        return user;
    }

    private void loadUserData(User user) {
        if (user != null) {
            user.setInventario(productoDAO.getInventario(user.getId()));
            user.setGameState(gameStateDAO.getOrCreateGameState(user.getId()));
        }
    }

    @Override
    public int eliminarProductoInventario(String idProducto, String idUser) {
        if (idProducto == null || idUser == null) {
            return 400;
        }

        Integer productId;
        try {
            productId = Integer.parseInt(idProducto);
        } catch (NumberFormatException e) {
            return 400;
        }

        User user = userDAO.getUser(idUser.trim());
        Producto producto = productoDAO.getProducto(productId);

        if (user == null || producto == null) {
            return 404;
        }

        // Pasar el precio para que ProductoDAO devuelva los ECTS al usuario
        return productoDAO.eliminarProductoInventario(user.getId(), productId, producto.getPrecio());
    }
}