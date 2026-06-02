package edu.upc.dsa.services;

import edu.upc.dsa.models.TeamMember;
import edu.upc.dsa.models.TeamResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.log4j.Logger;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Arrays;
import java.util.List;

@Api(value = "/user", description = "Servicio de usuarios")
@Path("/user")
public class UserServicio
{
    private static final Logger logger = Logger.getLogger(UserServicio.class);

    @GET
    @Path("/{idUser}/team")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Consulta los miembros del equipo de un usuario")
    public Response getUserTeam(
            @ApiParam(value = "ID del usuario", required = true) @PathParam("idUser") String idUser)
    {
        logger.info("EJ2 - Consulta dummy del equipo para el usuario: " + idUser);
        System.out.println("EJ2 - GET /user/" + idUser + "/team");

        List<TeamMember> members = Arrays.asList(
                new TeamMember("Marta", "avatar_3", 250),
                new TeamMember("Carla", "avatar_4", 200),
                new TeamMember("Marti", "avatar_2", 380),
                new TeamMember("Hector", "avatar_5", 180)
        );

        return Response.ok(new TeamResponse("Grup1", members)).build();
    }
}
