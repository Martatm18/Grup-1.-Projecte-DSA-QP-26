package edu.upc.dsa.services;
import io.swagger.annotations.Api;
import org.apache.log4j.Logger;
import edu.upc.dsa.models.Events;
import javax.ws.rs.*;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Arrays;

    @Api(value = "/eventos", description = "Servicio de eventos")
    @Path("/eventos")
public class EventosServicio
{
    private static final Logger logger = Logger.getLogger(EventosServicio.class);

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getEventos() {
        logger.info("GET /eventos");
        List<Events> eventos = Arrays.asList(
                new Events("1", "Halloween", "Evento de otoño", "2026-10-10", "2026-11-02", "https://i.pinimg.com/474x/e2/37/9f/e2379f091849a0abf9e9d56390953cfa.jpg"),
                new Events("2", "Navidad", "Evento de invierno", "2026-12-21", "2027-01-21", "https://cdn-icons-png.flaticon.com/512/6239/6239659.png"),
                new Events("3", "Primavera", "Evento de primavera", "2026-03-21", "2026-06-21", "https://cdn.pixabay.com/photo/2019/03/10/18/10/drawing-4046861_1280.png"),
                new Events("4", "Verano", "Evento de verano", "2026-06-21", "2026-09-21", "https://media.istockphoto.com/id/1124567572/es/vector/icono-de-lindo-sol-plana.jpg?s=612x612&w=0&k=20&c=7RW6qjcVkGhKWvAaOW8H3tODxbJBBp9pzlYuKpeIHIY=")

        );
        GenericEntity<List<Events>> entity = new GenericEntity<List<Events>>(eventos) {};
        return Response.ok(entity).build();
    }

    @POST
    @Path("/inscribir/{idEvento}/{idUser}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response inscribirEvento(
            @PathParam("idEvento") String idEvento,
            @PathParam("idUser") String idUser) {

        logger.info("POST /eventos/inscribir/" + idEvento + "/" + idUser);
        String message = String.format("Usuario %s inscrito en evento %s", idUser, idEvento);
        return Response.ok().entity("{\"mensaje\":\"" + message + "\"}").build();
    }
}
