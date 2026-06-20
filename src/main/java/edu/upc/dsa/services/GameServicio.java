package edu.upc.dsa.services;

import edu.upc.dsa.db.GameProgressDAO;
import edu.upc.dsa.db.util.FactorySession;
import edu.upc.dsa.db.util.Session;
import edu.upc.dsa.models.ApiError;
import edu.upc.dsa.models.CanCompleteObjective;
import edu.upc.dsa.models.ObjectiveResult;
import edu.upc.dsa.models.Producto;
import edu.upc.dsa.models.UserGameState;
import io.swagger.annotations.Api;
import org.apache.log4j.Logger;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Api(value = "/game", description = "Progreso de misiones y puzzles")
@Path("/game")
public class GameServicio {

    private static final Logger logger = Logger.getLogger(GameServicio.class);
    private final GameProgressDAO dao = new GameProgressDAO();

    // Preguntas de Toni: pregunta → respuesta correcta (normalizada)
    private static final Map<Integer, String[]> TONI_PREGUNTAS = new HashMap<>();
    static {
        TONI_PREGUNTAS.put(1, new String[]{"¿Qué estructura de datos sigue el principio LIFO (Last In, First Out)?", "pila"});
        TONI_PREGUNTAS.put(2, new String[]{"¿Qué estructura de datos sigue el principio FIFO (First In, First Out)?", "cola"});
        TONI_PREGUNTAS.put(3, new String[]{"¿Qué operación añade un elemento a una pila?", "push"});
        TONI_PREGUNTAS.put(4, new String[]{"¿Qué estructura de datos asocia claves con valores?", "hashmap"});
    }

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

            Map<String, Object> result = new HashMap<>();
            result.put("used", slug);
            if ("botiquin".equals(slug)) {
                session.updateHealth(username, 30);
                result.put("effect", "health+30");
            } else if ("carga_emp".equals(slug)) {
                result.put("effect", "emp_activated"); // Unity gestiona el efecto visual
            } else {
                result.put("effect", "consumed");
            }
            UserGameState state = session.getEstadoJugador(username);
            result.put("health", state != null ? state.getHealth() : null);
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
     * GET /game/toni/pregunta/{id}
     * Devuelve la pregunta de Toni (1-4).
     */
    @GET
    @Path("/toni/pregunta/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getToniPregunta(@PathParam("id") int id) {
        String[] qa = TONI_PREGUNTAS.get(id);
        if (qa == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ApiError("NOT_FOUND", "Pregunta no encontrada.")).build();
        }
        Map<String, Object> resp = new HashMap<>();
        resp.put("id", id);
        resp.put("pregunta", qa[0]);
        return Response.ok(resp).build();
    }

    /**
     * POST /game/{username}/toni/pregunta/{id}/responder
     * Body: { "respuesta": "Pila" }
     * Si es correcta, da 2 ECTS. Devuelve { correcto, ects_ganados, ects_total }.
     */
    @POST
    @Path("/{username}/toni/pregunta/{id}/responder")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response responderToni(
            @PathParam("username") String username,
            @PathParam("id") int id,
            PuzzleRequest body) {
        String[] qa = TONI_PREGUNTAS.get(id);
        if (qa == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ApiError("NOT_FOUND", "Pregunta no encontrada.")).build();
        }
        String respuesta = body.getRespuesta();
        boolean correcto = respuesta != null && normalize(respuesta).equals(qa[1]);

        Session session = null;
        try {
            session = FactorySession.openSession();
            if (correcto) {
                session.darEcts(username, 2);
            }
            UserGameState state = session.getEstadoJugador(username);
            Map<String, Object> result = new HashMap<>();
            result.put("correcto", correcto);
            result.put("ects_ganados", correcto ? 2 : 0);
            result.put("ects_total", state != null ? state.getHealth() : 0); // health reutilizado, ver abajo
            // Obtener ects actuales del usuario
            edu.upc.dsa.models.User user = session.get(edu.upc.dsa.models.User.class, username);
            result.put("ects_total", user != null ? user.getEcts() : 0);
            return Response.ok(result).build();
        } catch (Exception e) {
            logger.error("Error responder Toni", e);
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
