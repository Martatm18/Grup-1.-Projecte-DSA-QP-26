package edu.upc.dsa.services;

import edu.upc.dsa.ProductoManager;
import edu.upc.dsa.ProductoManagerImpl;
import edu.upc.dsa.db.GameStateDAO;
import edu.upc.dsa.db.UserDAO;
import edu.upc.dsa.models.ApiError;
import edu.upc.dsa.models.AuthRequest;
import edu.upc.dsa.models.Mission;
import edu.upc.dsa.models.User;
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
            if (request == null || isBlank(request.getId()) || isBlank(request.getPassword()) || isBlank(request.getEmail()))
            {
                return badRequest("MISSING_REQUIRED_FIELDS", "Rellena usuario, correo y contrasena.");
            }

            if (!isValidEmail(request.getEmail()))
            {
                return badRequest("INVALID_EMAIL", "Introduce un correo electronico valido.");
            }

            List<String> missingPasswordRules = passwordMissingRules(request.getPassword());
            if (!missingPasswordRules.isEmpty())
            {
                return badRequest("WEAK_PASSWORD:" + String.join(",", missingPasswordRules), "La contrasena no cumple los requisitos minimos.");
            }

            int status = pm.registerUser(request.getId(), request.getNombre(), request.getPassword(), request.getEmail());
            if (status == 201)
            {
                String avatar = cleanAvatar(request.getAvatar());
                if (avatar != null)
                {
                    userDAO.updateAvatar(request.getId().trim(), avatar);
                }
                return Response.status(Response.Status.CREATED).entity(pm.getUser(request.getId().trim())).build();
            }
            if (status == UserDAO.USERNAME_EXISTS)
            {
                return error(Response.Status.CONFLICT, "USERNAME_EXISTS", "Ese nombre de usuario ya existe.");
            }
            if (status == UserDAO.EMAIL_EXISTS)
            {
                return error(Response.Status.CONFLICT, "EMAIL_EXISTS", "Ese correo electronico ya esta registrado.");
            }

            return error(status, "REGISTER_ERROR", "No se ha podido crear la cuenta.");
        } catch (Exception e) {
            logger.error("Register error", e);
            return serverError();
        }
    }

    @POST
    @Path("/login")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response login(AuthRequest request)
    {
        try {
            if (request == null || isBlank(request.getPassword()) || (isBlank(request.getId()) && isBlank(request.getEmail())))
            {
                return badRequest("MISSING_REQUIRED_FIELDS", "Rellena usuario o correo y contrasena.");
            }

            User user;
            if (!isBlank(request.getEmail()))
            {
                if (!isValidEmail(request.getEmail())) {
                    return badRequest("INVALID_EMAIL", "Introduce un correo electronico valido.");
                }
                user = userDAO.loginUserByEmail(request.getEmail().trim().toLowerCase(), request.getPassword());
            }
            else
            {
                user = pm.loginUser(request.getId(), request.getPassword());
            }

            if (user == null)
            {
                return error(Response.Status.UNAUTHORIZED, "INVALID_CREDENTIALS", "Usuario, correo o contrasena incorrectos.");
            }

            user = pm.getUser(user.getId());
            return Response.ok(user).build();
        } catch (Exception e) {
            logger.error("Login error", e);
            return serverError();
        }
    }

    // Nuevo endpoint: login por email
    @POST
    @Path("/login-by-email")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response loginByEmail(AuthRequest request)
    {
        try {
            if (request == null || isBlank(request.getEmail()) || isBlank(request.getPassword()))
            {
                return badRequest("MISSING_REQUIRED_FIELDS", "Rellena correo y contrasena.");
            }

            if (!isValidEmail(request.getEmail())) {
                return badRequest("INVALID_EMAIL", "Introduce un correo electronico valido.");
            }

            User user = userDAO.loginUserByEmail(request.getEmail().trim().toLowerCase(), request.getPassword());
            if (user == null)
            {
                return error(Response.Status.UNAUTHORIZED, "INVALID_CREDENTIALS", "Correo o contrasena incorrectos.");
            }

            user = pm.getUser(user.getId());
            return Response.ok(user).build();
        } catch (Exception e) {
            logger.error("Login by email error", e);
            return serverError();
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
            User user = pm.getUser(idUser);
            if (user == null)
            {
                return error(Response.Status.NOT_FOUND, "USER_NOT_FOUND", "Usuario no encontrado.");
            }

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
            if (isBlank(idUser)) {
                return badRequest("MISSING_USER", "Falta el identificador de usuario.");
            }

            User user = pm.getUser(idUser.trim());
            if (user == null)
            {
                return error(Response.Status.NOT_FOUND, "USER_NOT_FOUND", "Usuario no encontrado.");
            }

            java.util.Map<String, Object> payload = new java.util.HashMap<>();
            payload.put("idUser", user.getId());
            payload.put("ects", user.getEcts());

            return Response.ok(payload).build();
        } catch (Exception e) {
            logger.error("Get user ECTS error", e);
            return serverError();
        }
    }

    @GET
    @Path("/ranking")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getRanking()
    {
        try {
            List<User> ranking = userDAO.getRanking();
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
            List<Mission> missions = gameStateDAO.getMissionsWithObjectives();
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
            if (isBlank(idUser))
            {
                return badRequest("MISSING_USER", "Falta el usuario.");
            }

            String cleanAvatar = cleanAvatar(avatar);
            if (cleanAvatar == null)
            {
                return badRequest("INVALID_AVATAR", "Avatar no valido.");
            }

            int status = userDAO.updateAvatar(idUser.trim(), cleanAvatar);
            if (status == 404)
            {
                return error(Response.Status.NOT_FOUND, "USER_NOT_FOUND", "Usuario no encontrado.");
            }
            if (status != 204)
            {
                return serverError();
            }

            return Response.ok(pm.getUser(idUser.trim())).build();
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
}
