package edu.upc.dsa.services;

import edu.upc.dsa.db.util.FactorySession;
import edu.upc.dsa.db.util.Session;
import edu.upc.dsa.models.Dialogue;
import org.apache.log4j.Logger;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Path("/dialogues")
public class DialogueServicio {

    private static final Logger logger = Logger.getLogger(DialogueServicio.class);

    /**
     * GET /dialogues/{npc_id}
     *
     * Params opcionales:
     *   ?mission=<id>     — filtra por misión
     *   ?trigger=<cond>   — filtra por trigger_condition (default, post_compra, pide_registros, ...)
     *
     * Sin params devuelve todos los diálogos del NPC ordenados por sequence_order.
     */
    @GET
    @Path("/{npc_id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDialogues(
            @PathParam("npc_id") String npcId,
            @QueryParam("mission") Integer missionId,
            @QueryParam("trigger") String trigger) {

        Session session = null;
        try {
            session = FactorySession.openSession();
            List<Dialogue> dialogues = session.getDialogues(npcId, missionId, trigger);
            if (dialogues.isEmpty()) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"error\":\"No se encontraron dialogos para " + npcId + "\"}")
                        .build();
            }
            return Response.ok(dialogues).build();
        } catch (Exception e) {
            logger.error("Error obteniendo dialogos de " + npcId, e);
            return Response.serverError()
                    .entity("{\"error\":\"Error interno\"}")
                    .build();
        } finally {
            if (session != null) session.close();
        }
    }
}
