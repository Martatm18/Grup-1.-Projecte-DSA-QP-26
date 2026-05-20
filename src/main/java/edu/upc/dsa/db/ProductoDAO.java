package edu.upc.dsa.db;

import edu.upc.dsa.db.util.FactorySession;
import edu.upc.dsa.db.util.Session;
import edu.upc.dsa.models.Producto;
import edu.upc.dsa.models.User;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

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
            return (Producto) session.get(Producto.class, idProducto);
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
        List<Producto> inventario = new LinkedList<>();

        String sql = "SELECT s.id, s.name, s.description, s.price, i.quantity " +
                "FROM inventory i " +
                "INNER JOIN shop s ON s.id = i.product_id " +
                "WHERE i.username=?";

        try (Connection conn = DBUtils.getConnection();
             PreparedStatement pstm = conn.prepareStatement(sql)) {

            pstm.setString(1, username);

            try (ResultSet rs = pstm.executeQuery()) {
                while (rs.next()) {
                    Producto producto = new Producto(
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getString("description"),
                            rs.getInt("price")
                    );

                    int quantity = rs.getInt("quantity");
                    for (int i = 0; i < quantity; i++) {
                        inventario.add(producto);
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return inventario;
    }
}
