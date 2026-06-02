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

            String answer = askLlm(question);
            return Response.ok(new AssistentResponse(answer)).build();
        } catch (Exception e) {
            logger.error("Assistant error", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ApiError("INTERNAL_SERVER_ERROR", "Error interno del servidor."))
                    .build();
        }
    }

    private String askLlm(String question) {
        HttpURLConnection connection = null;

        try {
            URL url = new URL(LLM_URL);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(60000);

            String prompt = buildPrompt(question);
            String body = "{"
                    + "\"model\":\"" + LLM_MODEL + "\","
                    + "\"prompt\":\"" + escapeJson(prompt) + "\","
                    + "\"stream\":false"
                    + "}";

            OutputStream os = connection.getOutputStream();
            os.write(body.getBytes(StandardCharsets.UTF_8));
            os.close();

            int status = connection.getResponseCode();
            if (status != 200) {
                logger.warn("LLM error code: " + status);
                return generateDummyAnswer(question);
            }

            Scanner scanner = new Scanner(connection.getInputStream(), "UTF-8").useDelimiter("\\A");
            String json = scanner.hasNext() ? scanner.next() : "";
            scanner.close();

            String response = extractResponse(json);
            if (isBlank(response)) {
                logger.warn("LLM response empty, using dummy answer");
                return generateDummyAnswer(question);
            }

            return response;
        } catch (Exception e) {
            logger.warn("LLM unavailable, using dummy answer", e);
            return generateDummyAnswer(question);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private String buildPrompt(String question) {
        return "Eres un asistente de ayuda para la app SigmaDSA. "
                + "Responde en castellano, de forma breve y clara. "
                + "Ayuda al usuario a pasar pantallas o resolver dudas frecuentes de la app. "
                + "Contexto de la app: hay login, registro, tienda, ECTS como moneda, compras e inventario. "
                + "No inventes funciones que no existan. "
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

    private String generateDummyAnswer(String question) {
        String q = question.toLowerCase();

        if (q.contains("comprar") || q.contains("tienda") || q.contains("producto")) {
            return "Para comprar un producto entra en la tienda, revisa tus ECTS y pulsa COMPRAR en el objeto que quieras.";
        }

        if (q.contains("inventario") || q.contains("objeto")) {
            return "Para ver tus objetos comprados pulsa el boton Ver inventario en la pantalla de tienda.";
        }

        if (q.contains("ects") || q.contains("creditos") || q.contains("moneda")) {
            return "Los ECTS son la moneda del juego. Sirven para comprar objetos y desbloquear pistas.";
        }

        if (q.contains("login") || q.contains("entrar") || q.contains("sesion")) {
            return "Para entrar, introduce tu usuario y contrasena en la pantalla de acceso y pulsa ENTRAR AL SISTEMA.";
        }

        if (q.contains("registrar") || q.contains("registro") || q.contains("cuenta")) {
            return "Para crear una cuenta, ve a REGISTRO, completa los campos obligatorios y pulsa CREAR USUARIO.";
        }

        return "Revisa la pantalla actual, lee las pistas disponibles y usa los botones principales para avanzar.";
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
