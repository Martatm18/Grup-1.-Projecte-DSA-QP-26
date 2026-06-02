package edu.upc.dsa.services;

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
import java.util.Arrays;
import java.util.List;

@Api(value = "/grupos", description = "Servicio dummy de grupos")
@Path("/grupos")
public class GruposServicio
{
    private static final Logger logger = Logger.getLogger(GruposServicio.class);

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getGrupos()
    {
        logger.info("GET /grupos - listado dummy de equipos");

        List<Equipo> equipos = Arrays.asList(
                new Equipo("1", "Equipo Alpha", "Escuadron principal de exploracion SIGMA"),
                new Equipo("2", "Equipo Beta", "Unidad de soporte academico y recursos"),
                new Equipo("3", "Equipo Gamma", "Grupo tactico para misiones de laboratorio")
        );

        GenericEntity<List<Equipo>> entity = new GenericEntity<List<Equipo>>(equipos) {};
        return Response.ok(entity).build();
    }

    @POST
    @Path("/{idGrupo}/usuarios/{idUser}")
    public Response unirseAGrupo(@PathParam("idGrupo") String idGrupo, @PathParam("idUser") String idUser)
    {
        logger.info("POST /grupos/" + idGrupo + "/usuarios/" + idUser
                + " - usuario anadido a grupo dummy");
        return Response.ok().build();
    }
}
