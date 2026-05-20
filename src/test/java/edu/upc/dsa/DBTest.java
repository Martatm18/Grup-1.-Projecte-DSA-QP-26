package edu.upc.dsa;

import edu.upc.dsa.db.DBUtils;
import edu.upc.dsa.models.Producto;
import edu.upc.dsa.models.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class DBTest {

    private static final String TEST_USER = "test_shop";
    private static final String TEST_NAME = "Usuario Tienda";
    private static final String TEST_PASSWORD = "Test1234!";
    private static final String TEST_EMAIL = "test_shop@gmail.com";

    public static void main(String[] args) {
        ProductoManager pm = ProductoManagerImpl.getInstance();

        System.out.println("========== TEST TIENDA + INVENTORY ==========");

        prepareTestUser(pm);

        User userBefore = pm.getUser(TEST_USER);
        printUserEcts("ECTS iniciales", userBefore);

        List<Producto> productos = pm.getListaProductos();
        printShop(productos);

        if (productos.isEmpty()) {
            System.out.println("No hay productos en shop. Inserta productos antes de ejecutar el test.");
            return;
        }

        Producto producto = productos.get(0);
        System.out.println("\nComprando producto:");
        printProduct(producto);

        int buyStatus = pm.comprarProducto(String.valueOf(producto.getId()), TEST_USER);
        System.out.println("Resultado compra: " + buyStatus + " (esperado 201)");

        User userAfterBuy = pm.getUser(TEST_USER);
        printUserEcts("ECTS despues de comprar", userAfterBuy);
        printInventory(TEST_USER);

        System.out.println("\nEliminando una unidad del inventario:");
        removeOneProductFromInventory(TEST_USER, producto.getId());

        User userAfterDelete = pm.getUser(TEST_USER);
        printUserEcts("ECTS despues de eliminar objeto", userAfterDelete);
        printInventory(TEST_USER);

        System.out.println("\n========== FIN TEST ==========");
    }

    private static void prepareTestUser(ProductoManager pm) {
        int registerStatus = pm.registerUser(TEST_USER, TEST_NAME, TEST_PASSWORD, TEST_EMAIL);
        System.out.println("Registro usuario test: " + registerStatus + " (201 nuevo, 409 si ya existia)");

        resetTestUserData();
    }

    private static void resetTestUserData() {
        try (Connection conn = DBUtils.getConnection()) {
            try (PreparedStatement pstm = conn.prepareStatement("DELETE FROM inventory WHERE username=?")) {
                pstm.setString(1, TEST_USER);
                pstm.executeUpdate();
            }

            try (PreparedStatement pstm = conn.prepareStatement("UPDATE users SET ects=100 WHERE username=?")) {
                pstm.setString(1, TEST_USER);
                pstm.executeUpdate();
            }

            System.out.println("Usuario test preparado: inventory vacio y 100 ECTS.");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static void printShop(List<Producto> productos) {
        System.out.println("\n--- PRODUCTOS EN SHOP ---");
        for (Producto p : productos) {
            printProduct(p);
        }
    }

    private static void printProduct(Producto p) {
        System.out.println(
                "id=" + p.getId() +
                " | name=" + p.getNombre() +
                " | price=" + p.getPrecio() +
                " | description=" + p.getDescripcion()
        );
    }

    private static void printUserEcts(String title, User user) {
        if (user == null) {
            System.out.println(title + ": usuario no encontrado");
            return;
        }

        System.out.println(title + ": " + user.getEcts());
    }

    private static void printInventory(String username) {
        System.out.println("\n--- INVENTORY DE " + username + " ---");

        String sql = "SELECT s.id, s.name, s.price, i.quantity " +
                "FROM inventory i " +
                "INNER JOIN shop s ON s.id = i.product_id " +
                "WHERE i.username=?";

        try (Connection conn = DBUtils.getConnection();
             PreparedStatement pstm = conn.prepareStatement(sql)) {

            pstm.setString(1, username);

            try (ResultSet rs = pstm.executeQuery()) {
                boolean hasItems = false;

                while (rs.next()) {
                    hasItems = true;
                    System.out.println(
                            "product_id=" + rs.getInt("id") +
                            " | name=" + rs.getString("name") +
                            " | price=" + rs.getInt("price") +
                            " | quantity=" + rs.getInt("quantity")
                    );
                }

                if (!hasItems) {
                    System.out.println("Inventario vacio.");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static void removeOneProductFromInventory(String username, Integer productId) {
        try (Connection conn = DBUtils.getConnection()) {
            int quantity = getInventoryQuantity(conn, username, productId);

            if (quantity <= 0) {
                System.out.println("No se puede eliminar: el usuario no tiene ese producto.");
                return;
            }

            if (quantity == 1) {
                try (PreparedStatement pstm = conn.prepareStatement(
                        "DELETE FROM inventory WHERE username=? AND product_id=?")) {
                    pstm.setString(1, username);
                    pstm.setInt(2, productId);
                    pstm.executeUpdate();
                }
            } else {
                try (PreparedStatement pstm = conn.prepareStatement(
                        "UPDATE inventory SET quantity=quantity-1 WHERE username=? AND product_id=?")) {
                    pstm.setString(1, username);
                    pstm.setInt(2, productId);
                    pstm.executeUpdate();
                }
            }

            System.out.println("Producto eliminado del inventory: product_id=" + productId);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static int getInventoryQuantity(Connection conn, String username, Integer productId) throws SQLException {
        try (PreparedStatement pstm = conn.prepareStatement(
                "SELECT quantity FROM inventory WHERE username=? AND product_id=?")) {
            pstm.setString(1, username);
            pstm.setInt(2, productId);

            try (ResultSet rs = pstm.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("quantity");
                }
            }
        }

        return 0;
    }
}
