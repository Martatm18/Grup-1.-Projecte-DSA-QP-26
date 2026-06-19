package edu.upc.dsa.db;

import edu.upc.dsa.db.DBUtils;
import edu.upc.dsa.db.util.QueryHelper;
import edu.upc.dsa.models.Equipo;
import edu.upc.dsa.models.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

public class EquipoDAO {
    private final UserDAO userDAO = new UserDAO();

    public List<Equipo> getEquipos() {
        List<Equipo> equipos = new LinkedList<>();

        try (Connection conn = DBUtils.getConnection();
             PreparedStatement pstm = conn.prepareStatement(QueryHelper.createSelectEquipos());
             ResultSet rs = pstm.executeQuery()) {

            while (rs.next()) {
                equipos.add(new Equipo(
                        rs.getString("id"),
                        rs.getString("nombre"),
                        rs.getString("descripcion")
                ));
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return equipos;
    }

    public Equipo getEquipo(String id) {
        try (Connection conn = DBUtils.getConnection();
             PreparedStatement pstm = conn.prepareStatement(QueryHelper.createSelectEquipoById())) {

            pstm.setString(1, id);
            try (ResultSet rs = pstm.executeQuery()) {
                if (rs.next()) {
                    return new Equipo(
                            rs.getString("id"),
                            rs.getString("nombre"),
                            rs.getString("descripcion")
                    );
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return null;
    }

    public boolean isUsuarioEnGrupo(String idGrupo, String idUser) {
        try (Connection conn = DBUtils.getConnection();
             PreparedStatement pstm = conn.prepareStatement(QueryHelper.createSelectGrupoUsuario())) {

            pstm.setString(1, idGrupo);
            pstm.setString(2, idUser);
            try (ResultSet rs = pstm.executeQuery()) {
                return rs.next();
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public int unirseAGrupo(String idGrupo, String idUser) {
        if (idGrupo == null || idGrupo.trim().isEmpty() || idUser == null || idUser.trim().isEmpty()) {
            return 400;
        }

        Equipo equipo = getEquipo(idGrupo.trim());
        if (equipo == null) {
            return 404;
        }

        User user = userDAO.getUser(idUser.trim());
        if (user == null) {
            return 404;
        }

        if (isUsuarioEnGrupo(idGrupo.trim(), idUser.trim())) {
            return 409;
        }

        try (Connection conn = DBUtils.getConnection();
             PreparedStatement pstm = conn.prepareStatement(QueryHelper.createInsertGrupoUsuario())) {

            pstm.setString(1, idGrupo.trim());
            pstm.setString(2, idUser.trim());
            pstm.executeUpdate();
            return 201;

        } catch (SQLException e) {
            if (e.getMessage() != null && e.getMessage().contains("Duplicate")) {
                return 409;
            }
            throw new RuntimeException(e);
        }
    }
}
