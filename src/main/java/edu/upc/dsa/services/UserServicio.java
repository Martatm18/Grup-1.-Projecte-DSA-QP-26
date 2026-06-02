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
                new TeamMember("Juan", "https://cdn.pixabay.com/photo/2017/07/11/15/51/kermit-2493979_1280.png", 250),
                new TeamMember("Palomo", "https://cdn.pixabay.com/photo/2015/10/05/22/37/blank-profile-picture-973460_960_720.png", 200),
                new TeamMember("Marti", "https://cdn.pixabay.com/photo/2016/11/14/17/39/person-1824144_960_720.png", 180)
        );

        return Response.ok(new TeamResponse("porxinos", members)).build();
    }
}
