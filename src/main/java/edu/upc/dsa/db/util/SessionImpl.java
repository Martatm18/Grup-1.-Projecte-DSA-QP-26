package edu.upc.dsa.db.util;

import edu.upc.dsa.models.Producto;
import edu.upc.dsa.models.User;

import java.sql.*;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

public class SessionImpl implements Session {
    private final Connection conn;

    public SessionImpl(Connection conn) {
        this.conn = conn;
    }

    @Override
    public void save(Object entity) {
        if (entity instanceof User) {
            saveUser((User) entity);
        }
    }

    @Override
    public void update(Object entity) {
        if (entity instanceof User) {
            updateUser((User) entity);
        }
    }

    private void saveUser(User user) {
        String sql = QueryHelper.createInsertUser();

        try (PreparedStatement pstm = conn.prepareStatement(sql)) {
            pstm.setString(1, user.getId());
            pstm.setString(2, user.getNombre());
            pstm.setString(3, user.getPassword());
            pstm.setString(4, user.getEmail());
            pstm.setInt(5, user.getEcts());
            pstm.setString(6, user.getAvatar());

            pstm.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void updateUser(User user) {
        String sql = QueryHelper.createUpdateUserEcts();

        try (PreparedStatement pstm = conn.prepareStatement(sql)) {
            pstm.setInt(1, user.getEcts());
            pstm.setString(2, user.getId());

            pstm.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void addProductToInventory(String username, Integer productId) {
        String sql = QueryHelper.createUpsertInventory();

        try (PreparedStatement pstm = conn.prepareStatement(sql)) {
            pstm.setString(1, username);
            pstm.setInt(2, productId);

            pstm.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Object> findAll(Class theClass, LinkedHashMap<String, Object> params) {
        List<Object> result = new LinkedList<>();
        String sql = QueryHelper.createSelectFindAll(theClass, params);

        System.out.println("QUERY ORM: " + sql);

        try (PreparedStatement pstm = conn.prepareStatement(sql)) {
            int i = 1;
            for (Object value : params.values()) {
                System.out.println("Param " + i + ": " + value);
                pstm.setObject(i++, value);
            }

            ResultSet rs = pstm.executeQuery();

            while (rs.next()) {
                if (theClass == User.class) {
                    result.add(buildUser(rs));
                }
                else if (theClass == Producto.class) {
                    result.add(buildProducto(rs));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return result;
    }

    @Override
    public Object get(Class theClass, Object id) {
        LinkedHashMap<String, Object> params = new LinkedHashMap<>();
        params.put("id", id);

        List<Object> result = findAll(theClass, params);

        if (result.isEmpty()) {
            return null;
        }

        return result.get(0);
    }

    private User buildUser(ResultSet rs) throws SQLException {
        User user = new User(
                rs.getString("username"),
                rs.getString("name"),
                rs.getString("password")
        );

        user.setEmail(rs.getString("email"));
        user.setEcts(rs.getInt("ects"));
        user.setAvatar(rs.getString("avatar"));

        return user;
    }

    private Producto buildProducto(ResultSet rs) throws SQLException {
        return new Producto(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getString("description"),
                rs.getInt("price")
        );
    }

    @Override
    public void close() {
        try {
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
