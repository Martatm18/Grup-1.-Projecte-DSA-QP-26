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

        return "Eres el asistente de ayuda de SigmaDSA, una web/app de juego academico ambientado en EETAC/UPC.\n"
                + "Responde siempre en castellano, breve y claro.\n"
                + "Tu trabajo es ayudar a pasar pantallas, entender misiones, tienda, inventario, ECTS, login y registro.\n"
                + "Reglas importantes:\n"
                + "- Usa primero el CONTEXTO REAL que te envia la app.\n"
                + "- Si preguntan por una mision, objetivo, producto o inventario, responde solo con lo que aparezca en el contexto.\n"
                + "- No inventes misiones, pasos, tutoriales ni funciones que no aparezcan en el contexto.\n"
                + "- Si el contexto no contiene la respuesta, di que no tienes datos suficientes y sugiere revisar la pestana Misiones.\n\n"
                + "Contexto fijo de la app:\n"
                + "- Hay login y registro.\n"
                + "- La tienda usa ECTS como moneda.\n"
                + "- Los objetos comprados aparecen en inventario.\n"
                + "- La web tiene secciones Tienda, Ranking, Misiones y Asistente IA.\n\n"
                + "CONTEXTO REAL ACTUAL:\n"
                + safeContext + "\n\n"
                + "Pregunta del usuario: " + question;
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
