package edu.upc.dsa.services;

import edu.upc.dsa.db.GameProgressDAO;
import edu.upc.dsa.models.ApiError;
import edu.upc.dsa.models.CanCompleteObjective;
import edu.upc.dsa.models.ObjectiveResult;
import edu.upc.dsa.models.UserGameState;
import io.swagger.annotations.Api;
import org.apache.log4j.Logger;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Api(value = "/game", description = "Progreso de misiones y puzzles")
@Path("/game")
public class GameServicio {

    private static final Logger logger = Logger.getLogger(GameServicio.class);
    private final GameProgressDAO dao = new GameProgressDAO();

    @POST
    @Path("/{username}/objetivo/{objectiveId}/completar")
    @Produces(MediaType.APPLICATION_JSON)
    public Response completarObjetivo(
            @PathParam("username") String username,
            @PathParam("objectiveId") int objectiveId) {
        try {
            ObjectiveResult result = dao.completarObjetivo(username, objectiveId);
            return Response.ok(result).build();
        } catch (RuntimeException e) {
            logger.error("Error completar objetivo", e);
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ApiError("ERROR", e.getMessage())).build();
        }
    }

    @GET
    @Path("/{username}/objetivo/{objectiveId}/puede-completar")
    @Produces(MediaType.APPLICATION_JSON)
    public Response puedeCompletarObjetivo(
            @PathParam("username") String username,
            @PathParam("objectiveId") int objectiveId) {
        try {
            CanCompleteObjective result = dao.puedeCompletarObjetivo(username, objectiveId);
            return Response.ok(result).build();
        } catch (RuntimeException e) {
            logger.error("Error validar objetivo", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ApiError("ERROR", e.getMessage())).build();
        }
    }

    @POST
    @Path("/{username}/puzzle/{puzzleId}/resolver")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response resolverPuzzle(
            @PathParam("username") String username,
            @PathParam("puzzleId") int puzzleId,
            PuzzleRequest body) {
        try {
            ObjectiveResult result = dao.resolverPuzzle(username, puzzleId, body.getRespuesta());
            return Response.ok(result).build();
        } catch (RuntimeException e) {
            if ("RESPUESTA_INCORRECTA".equals(e.getMessage())) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(new ApiError("RESPUESTA_INCORRECTA", "La respuesta no es correcta.")).build();
            }
            logger.error("Error resolver puzzle", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ApiError("ERROR", e.getMessage())).build();
        }
    }

    @GET
    @Path("/{username}/estado")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getEstado(@PathParam("username") String username) {
        try {
            UserGameState state = dao.getEstado(username);
            if (state == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(new ApiError("NOT_FOUND", "Estado de juego no encontrado.")).build();
            }
            return Response.ok(state).build();
        } catch (Exception e) {
            logger.error("Error get estado", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ApiError("ERROR", e.getMessage())).build();
        }
    }

    public static class PuzzleRequest {
        private String respuesta;
        public String getRespuesta() { return respuesta; }
        public void setRespuesta(String respuesta) { this.respuesta = respuesta; }
    }
}
