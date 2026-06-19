package edu.upc.dsa.services;

import edu.upc.dsa.db.EquipoDAO;
import edu.upc.dsa.models.Equipo;
import io.swagger.annotations.Api;
import org.apache.log4j.Logger;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Api(value = "/grupos", description = "Servicio de grupos")
@Path("/grupos")
public class GruposServicio
{
    private static final Logger logger = Logger.getLogger(GruposServicio.class);
    private final EquipoDAO equipoDAO = new EquipoDAO();

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getGrupos()
    {
        logger.info("GET /grupos - listado de equipos");

        List<Equipo> equipos = equipoDAO.getEquipos();
        GenericEntity<List<Equipo>> entity = new GenericEntity<List<Equipo>>(equipos) {};
        return Response.ok(entity).build();
    }

    @POST
    @Path("/{idGrupo}/usuarios/{idUser}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response unirseAGrupo(@PathParam("idGrupo") String idGrupo, @PathParam("idUser") String idUser)
    {
        logger.info("POST /grupos/" + idGrupo + "/usuarios/" + idUser + " - intento de union a grupo");

        int status = equipoDAO.unirseAGrupo(idGrupo, idUser);
        switch (status) {
            case 201:
                return Response.status(Response.Status.CREATED).build();
            case 400:
                return Response.status(Response.Status.BAD_REQUEST).entity("Faltan idGrupo o idUser").build();
            case 404:
                return Response.status(Response.Status.NOT_FOUND).entity("Grupo o usuario no encontrado").build();
            case 409:
                return Response.status(Response.Status.CONFLICT).entity("Usuario ya pertenece al grupo").build();
            default:
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error interno al unir al grupo").build();
        }
    }
}
