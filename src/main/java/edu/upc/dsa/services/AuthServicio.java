package edu.upc.dsa.services;

import edu.upc.dsa.ProductoManager;
import edu.upc.dsa.ProductoManagerImpl;
import edu.upc.dsa.models.AuthRequest;
import edu.upc.dsa.models.User;
import io.swagger.annotations.Api;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Api(value = "/auth", description = "Servicio de autenticacion")
@Path("/auth")
public class AuthServicio
{
    private ProductoManager pm;

    public AuthServicio()
    {
        this.pm = ProductoManagerImpl.getInstance();
    }

    @POST
    @Path("/register")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response register(AuthRequest request)
    {
        if (request == null)
        {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        int status = pm.registerUser(request.getId(), request.getNombre(), request.getPassword());
        if (status == 201)
        {
            return Response.status(Response.Status.CREATED).entity(pm.getUser(request.getId().trim())).build();
        }

        return Response.status(status).build();
    }

    @POST
    @Path("/login")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response login(AuthRequest request)
    {
        if (request == null)
        {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        User user = pm.loginUser(request.getId(), request.getPassword());
        if (user == null)
        {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        return Response.ok(user).build();
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
}
