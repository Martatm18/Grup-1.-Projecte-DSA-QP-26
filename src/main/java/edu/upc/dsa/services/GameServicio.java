package edu.upc.dsa.services;

import edu.upc.dsa.db.GameProgressDAO;
import edu.upc.dsa.db.util.FactorySession;
import edu.upc.dsa.db.util.Session;
import edu.upc.dsa.models.ApiError;
import edu.upc.dsa.models.CanCompleteObjective;
import edu.upc.dsa.models.ObjectiveResult;
import edu.upc.dsa.models.Producto;
import edu.upc.dsa.models.Puzzle;
import edu.upc.dsa.models.ToniPregunta;
import edu.upc.dsa.models.ToniRespuestaResult;
import edu.upc.dsa.models.UseItemResult;
import edu.upc.dsa.models.UserGameState;
import io.swagger.annotations.Api;
import org.apache.log4j.Logger;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Arrays;
import java.util.List;

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

    /**
     * POST /game/{username}/dano
     * Body: { "cantidad": 20 }
     * Reduce la vida del jugador. GREATEST(0, health - cantidad).
     * Devuelve el estado actualizado.
     */
    @POST
    @Path("/{username}/dano")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response recibirDano(
            @PathParam("username") String username,
            CantidadRequest body) {
        return aplicarDeltaSalud(username, -(Math.abs(body.getCantidad())));
    }

    /**
     * POST /game/{username}/curar
     * Body: { "cantidad": 30 }
     * Aumenta la vida del jugador hasta max_health.
     * Si tiene botiquin en el inventario, lo consume automáticamente.
     */
    @POST
    @Path("/{username}/curar")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response curar(
            @PathParam("username") String username,
            CantidadRequest body) {
        Session session = null;
        try {
            session = FactorySession.openSession();
            // Buscar botiquin en inventario y consumirlo
            List<Producto> inv = session.getInventory(username);
            for (Producto p : inv) {
                String slug = p.getSlug();
                String key = (slug != null && !slug.isEmpty()) ? slug : normalize(p.getNombre());
                if ("botiquin".equals(key)) {
                    session.removeProductFromInventory(username, p.getId());
                    break;
                }
            }
        } catch (Exception e) {
            logger.warn("No se pudo consumir botiquin de " + username + ": " + e.getMessage());
        } finally {
            if (session != null) session.close();
        }
        return aplicarDeltaSalud(username, Math.abs(body.getCantidad()));
    }

    /**
     * POST /game/{username}/usar/{slug}
     * Consume un objeto del inventario (por slug) y aplica su efecto.
     * Efectos implementados: botiquin (+30 vida), carga_emp (sin efecto servidor — Unity lo gestiona).
     */
    @POST
    @Path("/{username}/usar/{slug}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response usarObjeto(
            @PathParam("username") String username,
            @PathParam("slug") String slug) {
        Session session = null;
        try {
            session = FactorySession.openSession();
            List<Producto> inv = session.getInventory(username);
            Producto target = null;
            for (Producto p : inv) {
                String pSlug = p.getSlug();
                String key = (pSlug != null && !pSlug.isEmpty()) ? pSlug : normalize(p.getNombre());
                if (slug.equals(key)) {
                    target = p;
                    break;
                }
            }
            if (target == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(new ApiError("NOT_IN_INVENTORY", "El objeto '" + slug + "' no está en el inventario.")).build();
            }
            session.removeProductFromInventory(username, target.getId());

            UseItemResult result = new UseItemResult();
            result.setUsed(slug);
            if ("botiquin".equals(slug)) {
                session.updateHealth(username, 30);
                result.setEffect("health+30");
            } else if ("carga_emp".equals(slug)) {
                result.setEffect("emp_activated");
            } else {
                result.setEffect("consumed");
            }
            UserGameState state = session.getEstadoJugador(username);
            result.setHealth(state != null ? state.getHealth() : null);

            try {
                ObjectiveResult progress = dao.autoCompletarUso(username, slug);
                if (progress != null) {
                    result.setObjectiveCompleted(true);
                    result.setObjectiveProgress(progress);
                }
            } catch (Exception e) {
                logger.warn("No se pudo auto-completar objetivo de uso para " + username + "/" + slug, e);
            }

            return Response.ok(result).build();
        } catch (Exception e) {
            logger.error("Error usar objeto " + slug + " para " + username, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ApiError("ERROR", e.getMessage())).build();
        } finally {
            if (session != null) session.close();
        }
    }

    /**
     * GET /game/toni/pregunta
     * Devuelve una pregunta aleatoria de Toni (sin solución).
     */
    @GET
    @Path("/toni/pregunta")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getToniPregunta() {
        Session session = null;
        try {
            session = FactorySession.openSession();
            List<Puzzle> puzzles = session.getPuzzlesByMission(7);
            if (puzzles.isEmpty()) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(new ApiError("NOT_FOUND", "No hay preguntas disponibles.")).build();
            }
            Puzzle p = puzzles.get(new java.util.Random().nextInt(puzzles.size()));
            return Response.ok(new ToniPregunta(p.getId(), p.getTitle(), p.getDescription())).build();
        } catch (Exception e) {
            logger.error("Error get pregunta Toni", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ApiError("ERROR", e.getMessage())).build();
        } finally {
            if (session != null) session.close();
        }
    }

    /**
     * POST /game/{username}/toni/puzzle/{puzzleId}/responder
     * Body: { "respuesta": "Pila" }
     * Si es correcta, da 2 ECTS. Devuelve { correcto, ects_ganados, ects_total }.
     */
    @POST
    @Path("/{username}/toni/puzzle/{puzzleId}/responder")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response responderToni(
            @PathParam("username") String username,
            @PathParam("puzzleId") int puzzleId,
            PuzzleRequest body) {
        Session session = null;
        try {
            session = FactorySession.openSession();
            Puzzle puzzle = session.getPuzzleById(puzzleId);
            if (puzzle == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(new ApiError("NOT_FOUND", "Pregunta no encontrada.")).build();
            }
            boolean correcto = dao.isCorrectSolutionPublic(puzzle.getSolution(), body.getRespuesta());
            if (correcto) {
                session.darEcts(username, 2);
            }
            edu.upc.dsa.models.User user = session.get(edu.upc.dsa.models.User.class, username);
            return Response.ok(new ToniRespuestaResult(correcto, correcto ? 2 : 0, user != null ? user.getEcts() : 0)).build();
        } catch (Exception e) {
            logger.error("Error responder Toni puzzle " + puzzleId, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ApiError("ERROR", e.getMessage())).build();
        } finally {
            if (session != null) session.close();
        }
    }

    private Response aplicarDeltaSalud(String username, int delta) {
        Session session = null;
        try {
            session = FactorySession.openSession();
            session.updateHealth(username, delta);
            UserGameState state = session.getEstadoJugador(username);
            return Response.ok(state).build();
        } catch (Exception e) {
            logger.error("Error actualizar salud de " + username, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ApiError("ERROR", e.getMessage())).build();
        } finally {
            if (session != null) session.close();
        }
    }

    private String normalize(String value) {
        if (value == null) return "";
        return java.text.Normalizer.normalize(value.trim().toLowerCase(), java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replaceAll("[^a-z0-9]+", "_")
                .replaceAll("^_+|_+$", "");
    }

    public static class PuzzleRequest {
        private String respuesta;
        public String getRespuesta() { return respuesta; }
        public void setRespuesta(String respuesta) { this.respuesta = respuesta; }
    }

    public static class CantidadRequest {
        private int cantidad;
        public int getCantidad() { return cantidad; }
        public void setCantidad(int cantidad) { this.cantidad = cantidad; }
    }
}
