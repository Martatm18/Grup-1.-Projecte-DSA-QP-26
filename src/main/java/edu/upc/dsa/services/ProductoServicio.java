package edu.upc.dsa.services;

import edu.upc.dsa.ProductoManager;
import edu.upc.dsa.ProductoManagerImpl;
import edu.upc.dsa.db.GameProgressDAO;
import edu.upc.dsa.models.ApiError;
import edu.upc.dsa.models.ObjectiveResult;
import edu.upc.dsa.models.Producto;
import edu.upc.dsa.models.SigmaObject;
import edu.upc.dsa.db.ProductoDAO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.apache.log4j.Logger;
import javax.ws.rs.*;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Arrays;
import java.util.List;

@Api(value = "/tienda", description = "Servicio de Tienda SIGMA")
@Path("/tienda")
public class ProductoServicio
{
    private static final Logger logger = Logger.getLogger(ProductoServicio.class);

    private ProductoManager pm;
    private GameProgressDAO gameProgressDAO;
    private ProductoDAO productoDAO;

    public ProductoServicio()
    {
        this.pm = ProductoManagerImpl.getInstance();
        this.gameProgressDAO = new GameProgressDAO();
        this.productoDAO = new ProductoDAO();
    }

    @GET
    @Path("/objetos")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getObjetosMision()
    {
        try {
            List<SigmaObject> lista = productoDAO.getObjetosMision(null);
            GenericEntity<List<SigmaObject>> entity = new GenericEntity<List<SigmaObject>>(lista) {};
            return Response.ok(entity).build();
        } catch (Exception e) {
            logger.error("Mission objects error", e);
            return serverError();
        }
    }

    @GET
    @Path("/objetos/{idUser}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getObjetosMisionUsuario(@PathParam("idUser") String idUser)
    {
        try {
            if (idUser == null || idUser.trim().isEmpty()) {
                return error(Response.Status.BAD_REQUEST, "INVALID_USER_ID", "Identificador obligatorio.");
            }

            List<SigmaObject> lista = productoDAO.getObjetosMision(idUser.trim());
            GenericEntity<List<SigmaObject>> entity = new GenericEntity<List<SigmaObject>>(lista) {};
            return Response.ok(entity).build();
        } catch (Exception e) {
            logger.error("Mission objects by user error", e);
            return serverError();
        }
    }

    @GET
    @Path("/objeto/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getObjeto(@PathParam("id") int id)
    {
        try {
            List<SigmaObject> lista = productoDAO.getObjetosMision(null);
            for (SigmaObject obj : lista) {
                if (obj.getId() != null && obj.getId() == id) {
                    return Response.ok(obj).build();
                }
            }
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ApiError("NOT_FOUND", "Objeto no encontrado.")).build();
        } catch (Exception e) {
            logger.error("Get objeto error", e);
            return serverError();
        }
    }

    @GET
    @Path("/productos")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getProductos()
    {
        try {
            List<Producto> lista = pm.getListaProductos();
            GenericEntity<List<Producto>> entity = new GenericEntity<List<Producto>>(lista) {};
            return Response.ok(entity).build();
        } catch (Exception e) {
            logger.error("Shop products error", e);
            return serverError();
        }
    }

    @GET
    @Path("/inventario/{idUser}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiResponses({
            @ApiResponse(code = 200, message = "Inventario conseguido", response = Producto.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "User Not Found")
    })
    public Response getInventario(
            @ApiParam(value = "ID del usuario", required = true) @PathParam("idUser") String idUser)
    {
        try {
            if (idUser == null || idUser.trim().isEmpty()) {
                return error(Response.Status.BAD_REQUEST, "INVALID_USER_ID", "Identificador obligatorio.");
            }

            // 1. Buscamos el usuario utilizando el ID que manda Android
            edu.upc.dsa.models.User user = pm.getUser(idUser.trim());
            if (user == null) {
                return error(Response.Status.NOT_FOUND, "USER_NOT_FOUND", "Usuario no encontrado.");
            }

            List<Producto> inventario = user.getInventario();

            // Si el inventario viene null, evitamos que rompa el GenericEntity pasándolo a lista vacía
            if (inventario == null) {
                inventario = new java.util.ArrayList<Producto>();
            }

            GenericEntity<List<Producto>> entity = new GenericEntity<List<Producto>>(inventario) {};
            return Response.ok(entity).build();
        } catch (Exception e) {
            logger.error("Get inventory error", e);
            return serverError();
        }
    }



    @POST
    @Path("/comprar/{idProd}/{idUser}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response comprar(@PathParam("idProd") String idProd, @PathParam("idUser") String idUser)
    {
        try {
            int status = pm.comprarProducto(idProd, idUser);
            if (status == 201) {
                ObjectiveResult progress = null;
                try {
                    progress = gameProgressDAO.autoCompletarCompra(idUser.trim(), Integer.parseInt(idProd));
                } catch (Exception e) {
                    logger.warn("Compra realizada, pero no se pudo auto-completar objetivo de compra", e);
                }

                if (progress != null) {
                    return Response.status(Response.Status.CREATED).entity(progress).build();
                }

                return Response.status(Response.Status.CREATED).build();
            }
            if (status == 400) {
                return error(Response.Status.BAD_REQUEST, "INVALID_PURCHASE", "Producto o usuario no validos.");
            }
            if (status == 402) {
                return error(402, "INSUFFICIENT_ECTS", "No tienes suficientes ECTS.");
            }
            if (status == 404) {
                return error(Response.Status.NOT_FOUND, "PRODUCT_OR_USER_NOT_FOUND", "Producto o usuario no encontrado.");
            }

            return serverError();
        } catch (Exception e) {
            logger.error("Buy product error", e);
            return serverError();
        }
    }

    @DELETE
    @Path("/inventario/{idProd}/{idUser}")
    public Response eliminarInventario(@PathParam("idProd") String idProd, @PathParam("idUser") String idUser)
    {
        try {
            int status = pm.eliminarProductoInventario(idProd, idUser);
            if (status == 204) {
                return Response.noContent().build();
            }
            if (status == 400) {
                return error(Response.Status.BAD_REQUEST, "INVALID_INVENTORY_REQUEST", "Producto o usuario no validos.");
            }
            if (status == 404) {
                return error(Response.Status.NOT_FOUND, "INVENTORY_ITEM_NOT_FOUND", "El producto no existe en el inventario.");
            }

            return serverError();
        } catch (Exception e) {
            logger.error("Delete inventory error", e);
            return serverError();
        }
    }

    private Response error(Response.Status status, String code, String message)
    {
        return Response.status(status)
                .entity(new ApiError(code, message))
                .build();
    }

    private Response error(int status, String code, String message)
    {
        return Response.status(status)
                .entity(new ApiError(code, message))
                .build();
    }

    private Response serverError()
    {
        return error(Response.Status.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR", "Error interno del servidor.");
    }
}
