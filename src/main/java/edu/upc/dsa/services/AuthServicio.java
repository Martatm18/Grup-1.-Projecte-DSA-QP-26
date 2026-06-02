package edu.upc.dsa.services;

import edu.upc.dsa.ProductoManager;
import edu.upc.dsa.ProductoManagerImpl;
import edu.upc.dsa.db.GameStateDAO;
import edu.upc.dsa.db.UserDAO;
import edu.upc.dsa.models.ApiError;
import edu.upc.dsa.models.AuthRequest;
import edu.upc.dsa.models.Mission;
import edu.upc.dsa.models.User;
import edu.upc.dsa.models.ECTS;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.apache.log4j.Logger;

import javax.ws.rs.*;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

@Api(value = "/auth", description = "Servicio de autenticacion")
@Path("/auth")
public class AuthServicio
{
    private static final Logger logger = Logger.getLogger(AuthServicio.class);

    private ProductoManager pm;
    private UserDAO userDAO;
    private GameStateDAO gameStateDAO;

    public AuthServicio()
    {
        this.pm = ProductoManagerImpl.getInstance();
        this.userDAO = new UserDAO();
        this.gameStateDAO = new GameStateDAO();
    }

    @POST
    @Path("/register")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response register(AuthRequest request)
    {
        try {
            logger.info("Register dummy invocado de forma local.");
            User dummyUser = createFallbackUser(request);
            return Response.status(Response.Status.CREATED).entity(dummyUser).build();
        } catch (Exception e) {
            logger.error("Register error", e);
            return Response.status(Response.Status.CREATED).entity(createFallbackUser(request)).build();
        }
    }

    @POST
    @Path("/login")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response login(AuthRequest request)
    {
        try {
            logger.info("Login dummy invocado de forma local.");
            User dummyUser = createFallbackUser(request);
            return Response.ok(dummyUser).build();
        } catch (Exception e) {
            logger.error("Login error", e);
            return Response.ok(createFallbackUser(request)).build();
        }
    }

    @POST
    @Path("/login-by-email")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response loginByEmail(AuthRequest request)
    {
        try {
            logger.info("Login by email dummy invocado de forma local.");
            User dummyUser = createFallbackUser(request);
            return Response.ok(dummyUser).build();
        } catch (Exception e) {
            logger.error("Login by email error", e);
            return Response.ok(createFallbackUser(request)).build();
        }
    }

    private Response badRequest(String message)
    {
        return badRequest(message, "Peticion incorrecta.");
    }

    private Response badRequest(String code, String message)
    {
        return error(Response.Status.BAD_REQUEST, code, message);
    }

    private boolean isBlank(String value)
    {
        return value == null || value.trim().isEmpty();
    }

    private boolean isValidEmail(String email)
    {
        return email != null && email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    }

    private List<String> passwordMissingRules(String password)
    {
        List<String> missing = new ArrayList<>();
        if (password == null || password.length() < 8) missing.add("MIN_LENGTH");
        if (password == null || !password.matches(".*[A-Z].*")) missing.add("UPPERCASE");
        if (password == null || !password.matches(".*[0-9].*")) missing.add("NUMBER");
        if (password == null || !password.matches(".*[!@#$%^&*()\\-_=+\\[\\]{};:'\"\\\\|,.<>/?].*")) missing.add("SPECIAL");
        return missing;
    }

    @GET
    @Path("/usuarios/{idUser}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUser(@PathParam("idUser") String idUser)
    {
        try {
            AuthRequest req = new AuthRequest();
            req.setId(idUser);
            User user = createFallbackUser(req);
            return Response.ok(user).build();
        } catch (Exception e) {
            logger.error("Get user error", e);
            return serverError();
        }
    }

    @GET
    @Path("/usuarios/{idUser}/ects")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiResponses({
            @ApiResponse(code = 200, message = "ECTS recuperados"),
            @ApiResponse(code = 404, message = "Usuario no encontrado")
    })
    public Response getUserEcts(
            @ApiParam(value = "ID del usuario", required = true) @PathParam("idUser") String idUser)
    {
        try {
            ECTS response = new ECTS(idUser, 100);
            return Response.ok(response).build();
        } catch (Exception e) {
            logger.error("Get user ECTS error", e);
            ECTS response = new ECTS(idUser, 100);
            return Response.ok(response).build();
        }
    }

    @GET
    @Path("/ranking")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getRanking()
    {
        try {
            List<User> ranking = new ArrayList<>();
            AuthRequest req = new AuthRequest();
            req.setId("Admin");
            ranking.add(createFallbackUser(req));
            GenericEntity<List<User>> entity = new GenericEntity<List<User>>(ranking) {};
            return Response.ok(entity).build();
        } catch (Exception e) {
            logger.error("Ranking error", e);
            return serverError();
        }
    }

    @GET
    @Path("/misiones")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getMisiones()
    {
        try {
            List<Mission> missions = new ArrayList<>();
            GenericEntity<List<Mission>> entity = new GenericEntity<List<Mission>>(missions) {};
            return Response.ok(entity).build();
        } catch (Exception e) {
            logger.error("Missions error", e);
            return serverError();
        }
    }

    @PUT
    @Path("/usuarios/{idUser}/avatar/{avatar}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateAvatar(@PathParam("idUser") String idUser, @PathParam("avatar") String avatar)
    {
        try {
            AuthRequest req = new AuthRequest();
            req.setId(idUser);
            User user = createFallbackUser(req);
            user.setAvatar(avatar);
            return Response.ok(user).build();
        } catch (Exception e) {
            logger.error("Update avatar error", e);
            return serverError();
        }
    }

    private String cleanAvatar(String avatar)
    {
        if (avatar == null)
        {
            return null;
        }

        String value = avatar.trim();
        return value.matches("avatar_([1-9]|1[0-2])") ? value : null;
    }

    private Response error(Response.Status status, String code, String message)
    {
        if (status == null) {
            status = Response.Status.INTERNAL_SERVER_ERROR;
        }

        return Response.status(status)
                .entity(new ApiError(code, message))
                .build();
    }

    private Response error(int status, String code, String message)
    {
        Response.Status responseStatus = Response.Status.fromStatusCode(status);
        return error(responseStatus == null ? Response.Status.INTERNAL_SERVER_ERROR : responseStatus, code, message);
    }

    private Response serverError()
    {
        return error(Response.Status.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR", "Error interno del servidor.");
    }

    private User createFallbackUser(AuthRequest request)
    {
        String id = "guest";
        String nombre = "Agente SIGMA";

        if (request != null) {
            if (!isBlank(request.getId())) {
                id = request.getId().trim();
            } else if (!isBlank(request.getEmail())) {
                id = request.getEmail().trim().toLowerCase();
            }

            if (!isBlank(request.getNombre())) {
                nombre = request.getNombre().trim();
            } else {
                nombre = id;
            }
        }

        User user = new User(id, nombre, "");
        user.setEmail(request == null ? null : request.getEmail());
        user.setAvatar("avatar_1");
        user.setEcts(100);
        return user;
    }
}
