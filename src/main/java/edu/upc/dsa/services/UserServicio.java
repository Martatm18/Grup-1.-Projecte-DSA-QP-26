package edu.upc.dsa.services;

import edu.upc.dsa.db.DBUtils;
import edu.upc.dsa.db.util.QueryHelper;
import edu.upc.dsa.models.ApiError;
import edu.upc.dsa.models.TeamInfo;
import org.apache.log4j.Logger;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Path("/user")
public class UserServicio {

    private static final Logger logger = Logger.getLogger(UserServicio.class);

    @GET
    @Path("/{idUser}/team")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUserTeam(@PathParam("idUser") String idUser) {
        if (idUser == null || idUser.trim().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ApiError("MISSING_USER", "Falta el usuario.")).build();
        }

        try (Connection conn = DBUtils.getConnection()) {
            String groupId = null;
            String groupName = null;
            try (PreparedStatement ps = conn.prepareStatement(QueryHelper.createSelectGrupoDeUsuario())) {
                ps.setString(1, idUser.trim());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        groupId = rs.getString("id");
                        groupName = rs.getString("nombre");
                    }
                }
            }

            if (groupId == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(new ApiError("NO_TEAM", "El usuario no pertenece a ningún equipo.")).build();
            }

            List<TeamInfo.TeamMember> members = new ArrayList<>();
            try (PreparedStatement ps = conn.prepareStatement(QueryHelper.createSelectMiembrosGrupo())) {
                ps.setString(1, groupId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        members.add(new TeamInfo.TeamMember(
                                rs.getString("name"),
                                rs.getString("avatar"),
                                rs.getInt("ects")
                        ));
                    }
                }
            }

            return Response.ok(new TeamInfo(groupName, members)).build();

        } catch (SQLException e) {
            logger.error("Error getUserTeam para " + idUser, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ApiError("ERROR", e.getMessage())).build();
        }
    }
}
