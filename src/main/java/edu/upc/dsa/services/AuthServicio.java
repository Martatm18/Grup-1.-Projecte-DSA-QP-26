package edu.upc.dsa.services;

import edu.upc.dsa.ProductoManager;
import edu.upc.dsa.ProductoManagerImpl;
import edu.upc.dsa.db.UserDAO;
import edu.upc.dsa.models.AuthRequest;
import edu.upc.dsa.models.User;
import io.swagger.annotations.Api;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

@Api(value = "/auth", description = "Servicio de autenticacion")
@Path("/auth")
public class AuthServicio
{
    private ProductoManager pm;
    private UserDAO userDAO;

    public AuthServicio()
    {
        this.pm = ProductoManagerImpl.getInstance();
        this.userDAO = new UserDAO();
    }

    @POST
    @Path("/register")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response register(AuthRequest request)
    {
        if (request == null || isBlank(request.getId()) || isBlank(request.getPassword()) || isBlank(request.getEmail()))
        {
            return badRequest("MISSING_REQUIRED_FIELDS");
        }

        if (!isValidEmail(request.getEmail()))
        {
            return badRequest("INVALID_EMAIL");
        }

        List<String> missingPasswordRules = passwordMissingRules(request.getPassword());
        if (!missingPasswordRules.isEmpty())
        {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("WEAK_PASSWORD:" + String.join(",", missingPasswordRules))
                    .build();
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
            return Response.status(Response.Status.CONFLICT).entity("USERNAME_EXISTS").build();
        }
        if (status == UserDAO.EMAIL_EXISTS)
        {
            return Response.status(Response.Status.CONFLICT).entity("EMAIL_EXISTS").build();
        }

        return Response.status(status).entity("REGISTER_ERROR").build();
    }

    @POST
    @Path("/login")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response login(AuthRequest request)
    {
        if (request == null || isBlank(request.getPassword()) || (isBlank(request.getId()) && isBlank(request.getEmail())))
        {
            return badRequest("MISSING_REQUIRED_FIELDS");
        }

        User user;
        if (!isBlank(request.getEmail()))
        {
            user = userDAO.loginUserByEmail(request.getEmail().trim().toLowerCase(), request.getPassword());
        }
        else
        {
            user = pm.loginUser(request.getId(), request.getPassword());
        }

        if (user == null)
        {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        user = pm.getUser(user.getId());
        return Response.ok(user).build();
    }

    // Nuevo endpoint: login por email
    @POST
    @Path("/login-by-email")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response loginByEmail(AuthRequest request)
    {
        if (request == null || isBlank(request.getEmail()) || isBlank(request.getPassword()))
        {
            return badRequest("MISSING_REQUIRED_FIELDS");
        }

        User user = userDAO.loginUserByEmail(request.getEmail().trim().toLowerCase(), request.getPassword());
        if (user == null)
        {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        user = pm.getUser(user.getId());
        return Response.ok(user).build();
    }

    private Response badRequest(String message)
    {
        return Response.status(Response.Status.BAD_REQUEST).entity(message).build();
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
        User user = pm.getUser(idUser);
        if (user == null)
        {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        return Response.ok(user).build();
    }

    @PUT
    @Path("/usuarios/{idUser}/avatar/{avatar}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateAvatar(@PathParam("idUser") String idUser, @PathParam("avatar") String avatar)
    {
        if (isBlank(idUser))
        {
            return badRequest("MISSING_USER");
        }

        String cleanAvatar = cleanAvatar(avatar);
        if (cleanAvatar == null)
        {
            return badRequest("INVALID_AVATAR");
        }

        int status = userDAO.updateAvatar(idUser.trim(), cleanAvatar);
        if (status != 204)
        {
            return Response.status(status).build();
        }

        return Response.ok(pm.getUser(idUser.trim())).build();
    }

    private String cleanAvatar(String avatar)
    {
        if (avatar == null)
        {
            return null;
        }

        String value = avatar.trim();
        return value.matches("avatar_[1-7]") ? value : null;
    }
}
