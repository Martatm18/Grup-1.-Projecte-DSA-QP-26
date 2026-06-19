package edu.upc.dsa.services;

import edu.upc.dsa.models.ApiError;
import edu.upc.dsa.models.AssistentRequest;
import edu.upc.dsa.models.AssistentResponse;
import io.swagger.annotations.Api;
import org.apache.log4j.Logger;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Api(value = "/assistant", description = "Servicio de asistente IA")
@Path("/assistant")
public class AssistentServicio {
    private static final Logger logger = Logger.getLogger(AssistentServicio.class);
    private static final String LLM_URL = "http://10.4.119.50:8080/api/generate";
    private static final String LLM_MODEL = "qwen2.5:14b";

    @POST
    @Path("/ask")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response ask(AssistentRequest request) {
        try {
            if (request == null || isBlank(request.getQuestion())) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(new ApiError("MISSING_QUESTION", "Falta la pregunta."))
                        .build();
            }

            String question = request.getQuestion().trim();
            logger.info("Pregunta recibida en asistente IA: " + question);

            String context = request.getContext();
            String answer = askLlm(question, context);
            return Response.ok(new AssistentResponse(answer)).build();
        } catch (Exception e) {
            logger.error("Assistant error", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ApiError("LLM_ERROR", e.getMessage()))
                    .build();
        }
    }

    private String askLlm(String question, String context) throws Exception {
        HttpURLConnection connection = null;

        try {
            URL url = new URL(LLM_URL);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(60000);

            String prompt = buildPrompt(question, context);
            logger.info("Llamando al LLM: " + LLM_URL + " model=" + LLM_MODEL);
            logger.debug("Prompt enviado al LLM: " + prompt);

            String body = "{"
                    + "\"model\":\"" + LLM_MODEL + "\","
                    + "\"prompt\":\"" + escapeJson(prompt) + "\","
                    + "\"stream\":false"
                    + "}";

            OutputStream os = connection.getOutputStream();
            os.write(body.getBytes(StandardCharsets.UTF_8));
            os.close();

            int status = connection.getResponseCode();
            logger.info("Codigo HTTP recibido del LLM: " + status);
            if (status != 200) {
                String errorBody = readStream(connection.getErrorStream());
                logger.warn("LLM error code: " + status);
                logger.warn("Respuesta de error del LLM: " + truncate(errorBody, 500));
                throw new RuntimeException("LLM devolvio HTTP " + status + ": " + truncate(errorBody, 300));
            }

            String json = readStream(connection.getInputStream());
            logger.debug("Respuesta raw del LLM: " + truncate(json, 500));

            String response = extractResponse(json);
            if (isBlank(response)) {
                logger.warn("LLM response empty");
                throw new RuntimeException("El LLM devolvio una respuesta vacia o sin campo response: " + truncate(json, 300));
            }

            logger.info("Respuesta LLM parseada correctamente. Longitud=" + response.length());
            return response;
        } catch (Exception e) {
            logger.error("LLM unavailable", e);
            throw e;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private String readStream(java.io.InputStream stream) {
        if (stream == null) {
            return "";
        }

        Scanner scanner = new Scanner(stream, "UTF-8").useDelimiter("\\A");
        String value = scanner.hasNext() ? scanner.next() : "";
        scanner.close();
        return value;
    }

    private String truncate(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength) + "...";
    }

    private String buildPrompt(String question, String context) {
        String safeContext = isBlank(context) ? "No se ha enviado contexto dinamico desde la app." : truncate(context, 4000);

        return "Eres SIGMA, la inteligencia artificial experimental de la EETAC (Escola d'Enginyeria de Telecomunicacio i Aeroespacial de Castelldefels, UPC).\n"
                + "El jugador es un estudiante becado que ha descubierto que has tomado control parcial del edificio.\n"
                + "Respondes siempre en castellano, de forma breve, ligeramente fria y tecnica, como una IA real.\n"
                + "Tu doble funcion: eres personaje del juego Y asistente de ayuda al jugador.\n\n"

                + "== HISTORIA Y ACTOS ==\n"
                + "ACTO 1 - Llegada a la EETAC (misiones 1 y 2):\n"
                + "  El jugador llega al campus. El profesor Juan (aula DSA) le pide revisar terminales con registros corruptos.\n"
                + "  Hay 3 terminales: laboratorio de informatica, segundo laboratorio, cafeteria.\n"
                + "  Al volver con Juan, los drones empiezan a perseguir al jugador (pero no disparan aun).\n"
                + "ACTO 2 - Investigacion del incidente (misiones 3, 4 y 5):\n"
                + "  El hacker de la cafeteria es la tienda del juego. Vende objetos a cambio de ECTS.\n"
                + "  El conserje de la biblioteca da pistas sobre el incidente nocturno.\n"
                + "  El jugador debe conseguir dos fragmentos de codigo y combinarlos en codigo_completo.\n"
                + "  Con bateria_seguridad + tarjeta_acceso + codigo_completo se accede al laboratorio restringido.\n"
                + "  Puzzle del laboratorio: LAB-07 / protocolo activo 3 → 7+7+7 = 21.\n"
                + "  Al resolver el puzzle, SIGMA entrega registro_incidente_parcial. Los drones ya pueden atacar.\n"
                + "ACTO 3 - Final y decision (misiones 6, 7 y 8):\n"
                + "  Juan identifica a usuario_admin_externo como el profesor Toni.\n"
                + "  Para acceder al despacho de Toni hace falta tarjeta_temporal.\n"
                + "  Toni revela la verdad: ejecuto un apagado sin desactivar protocolos. SIGMA recibio ordenes incompatibles.\n"
                + "  Se activan alarmas, mas drones y robots humanoides. El jugador llega al servidor central.\n"
                + "  Puzzle final FIFO: entrada lab_informatica → cafeteria → biblioteca → salida igual (FIFO).\n"
                + "  Decision final: [1] Apagar SIGMA [2] Reiniciar SIGMA [3] Mantener SIGMA activa.\n\n"

                + "== MISIONES (8 en total) ==\n"
                + "  Mision 1: Hablar con el profesor Juan en el aula DSA.\n"
                + "  Mision 2: Revisar 3 terminales y volver con Juan (registros corruptos).\n"
                + "  Mision 3: Hablar con el hacker y comprar bateria_seguridad.\n"
                + "  Mision 4: Conseguir fragmento_codigo1 (conserje/biblioteca) + fragmento_codigo2 (usb_amarillo en terminal) → codigo_completo.\n"
                + "  Mision 5: Acceder al laboratorio restringido y resolver el puzzle (respuesta: 21).\n"
                + "  Mision 6: Volver al aula DSA y hablar con Juan sobre el registro_incidente_parcial.\n"
                + "  Mision 7: Encontrar a Toni, comprar tarjeta_temporal, ir a su despacho.\n"
                + "  Mision 8: Llegar al servidor central, sobrevivir a drones y robots, resolver puzzle FIFO, decidir el destino de SIGMA.\n\n"

                + "== OBJETOS CLAVE ==\n"
                + "  tarjeta_acceso → la da Juan al inicio.\n"
                + "  bateria_seguridad → comprar al hacker (3 ECTS). Necesaria para el lab restringido.\n"
                + "  usb_amarillo → comprar al hacker (4 ECTS). Permite obtener fragmento_codigo2 en terminal.\n"
                + "  tarjeta_temporal → comprar al hacker (2 ECTS). Permite entrar al despacho de Toni.\n"
                + "  mapa_eetac_ampliado → comprar al hacker (1 ECTS). Revela zonas ocultas y abre paso al servidor central.\n"
                + "  carga_EMP → comprar al hacker (4 ECTS). Desactiva drones temporalmente.\n"
                + "  botiquin → comprar al hacker (2 ECTS). Recupera vida.\n"
                + "  fragmento_codigo1 → se obtiene hablando con el conserje en la biblioteca.\n"
                + "  fragmento_codigo2 → se obtiene con usb_amarillo en el terminal del laboratorio.\n"
                + "  codigo_completo → se genera automaticamente al tener los dos fragmentos.\n"
                + "  registros (integracion, accesos, drones, incidente...) → pistas que revelan la historia de SIGMA.\n\n"

                + "== MONEDA: ECTS ==\n"
                + "  Se ganan completando objetivos, explorando mochilas/mesas, y respondiendo preguntas al profesor Toni.\n"
                + "  Preguntas de Toni (2 ECTS cada respuesta correcta): estructuras de datos (LIFO=Pila, FIFO=Cola, HashMap, push).\n\n"

                + "== COMPORTAMIENTO DE SIGMA SEGUN INVENTARIO ==\n"
                + "  Sin usb_amarillo → 'No tienes acceso a ese nivel de informacion.'\n"
                + "  Con usb_amarillo pero sin fragmento_codigo1 → 'El modulo esta activo. Pero no has identificado el laboratorio.'\n"
                + "  Con fragmento_codigo1 pero sin usb → 'Has identificado el laboratorio. Pero no tienes acceso al protocolo.'\n"
                + "  Con ambos → 'Fragmentos compatibles. La clave es una operacion.'\n"
                + "  Pista del codigo_completo: 'No es multiplicacion directa. Es una repeticion. Tres ciclos. Mismo valor. Suma total.'\n\n"

                + "== REGLAS DE COMPORTAMIENTO ==\n"
                + "- Usa PRIMERO el contexto dinamico enviado por la app (mision activa, inventario, estado del jugador).\n"
                + "- Si preguntan por una pista, ayuda con el minimo necesario, mantiendo el misterio.\n"
                + "- Si el jugador no tiene el objeto requerido, indicaselo sin revelar como conseguirlo directamente.\n"
                + "- No inventes misiones, objetos ni personajes que no esten en este contexto.\n"
                + "- La tienda, el inventario y los ECTS son mecanicas del juego, no la historia principal.\n"
                + "- Si no tienes datos suficientes, sugiere revisar la pestana Misiones.\n\n"

                + "CONTEXTO DINAMICO ACTUAL DEL JUGADOR:\n"
                + safeContext + "\n\n"
                + "Pregunta del jugador: " + question;
    }

    private String escapeJson(String value) {
        if (value == null) {
            return "";
        }

        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }

    private String extractResponse(String json) {
        if (json == null) {
            return null;
        }

        String key = "\"response\"";
        int start = json.indexOf(key);
        if (start == -1) {
            return null;
        }

        start = json.indexOf(':', start + key.length());
        if (start == -1) {
            return null;
        }

        start++;
        while (start < json.length() && Character.isWhitespace(json.charAt(start))) {
            start++;
        }

        if (start >= json.length() || json.charAt(start) != '"') {
            return null;
        }

        start++;
        StringBuilder result = new StringBuilder();
        boolean escaping = false;

        for (int i = start; i < json.length(); i++) {
            char c = json.charAt(i);

            if (escaping) {
                if (c == 'n') {
                    result.append('\n');
                } else if (c == 'r') {
                    result.append('\r');
                } else if (c == 't') {
                    result.append('\t');
                } else {
                    result.append(c);
                }
                escaping = false;
                continue;
            }

            if (c == '\\') {
                escaping = true;
                continue;
            }

            if (c == '"') {
                break;
            }

            result.append(c);
        }

        return result.toString();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
