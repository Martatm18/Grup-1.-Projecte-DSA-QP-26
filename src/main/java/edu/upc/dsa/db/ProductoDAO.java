package edu.upc.dsa.db;

import edu.upc.dsa.db.util.FactorySession;
import edu.upc.dsa.db.util.Session;
import edu.upc.dsa.models.Producto;
import edu.upc.dsa.models.User;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

public class ProductoDAO {

    public List<Producto> getProductos() {
        Session session = null;

        try {
            session = FactorySession.openSession();
            List<Object> result = session.findAll(Producto.class, new LinkedHashMap<String, Object>());
            List<Producto> productos = new LinkedList<>();

            for (Object producto : result) {
                productos.add((Producto) producto);
            }

            return productos;
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    public Producto getProducto(Integer idProducto) {
        Session session = null;

        try {
            session = FactorySession.openSession();
            return session.get(Producto.class, idProducto);
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    public void comprarProducto(User user, Producto producto) {
        Session session = null;

        try {
            session = FactorySession.openSession();
            session.update(user);
            session.addProductToInventory(user.getId(), producto.getId());
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    public List<Producto> getInventario(String username) {
        Session session = null;

        try {
            session = FactorySession.openSession();
            return session.getInventory(username);
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    public int eliminarProductoInventario(String username, Integer productId, int precio) {
        Session session = null;

        try {
            session = FactorySession.openSession();

            if (!session.removeProductFromInventory(username, productId)) {
                return 404;
            }

            User user = session.get(User.class, "username", username);
            if (user != null) {
                user.addEcts(precio);
                session.update(user);
            }

            return 204;
        } catch (Exception e) {
            e.printStackTrace();
            return 500;
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }
}
