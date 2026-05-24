package edu.upc.dsa.services;

import edu.upc.dsa.ProductoManager;
import edu.upc.dsa.ProductoManagerImpl;
import edu.upc.dsa.models.ApiError;
import edu.upc.dsa.models.Producto;
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
import java.util.List;

@Api(value = "/tienda", description = "Servicio de Tienda SIGMA")
@Path("/tienda")
public class ProductoServicio
{
    private static final Logger logger = Logger.getLogger(ProductoServicio.class);

    private ProductoManager pm;

    public ProductoServicio()
    {
        this.pm = ProductoManagerImpl.getInstance();
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

            edu.upc.dsa.models.User user = pm.getUser(idUser.trim());
            if (user == null) {
                return error(Response.Status.NOT_FOUND, "USER_NOT_FOUND", "Usuario no encontrado.");
            }

            List<Producto> inventario = user.getInventario();
            
            // CAMBIO CRÍTICO: Evita NullPointerException si el inventario está vacío en la BD
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

    @GET
    @Path("/inventario/{idUser}/producto/{idProd}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiResponses({
            @ApiResponse(code = 200, message = "Producto de inventario recuperado", response = Producto.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Usuario o producto no encontrado")
    })
    public Response getInventarioProducto(
            @ApiParam(value = "ID del usuario", required = true) @PathParam("idUser") String idUser,
            @ApiParam(value = "ID del producto", required = true) @PathParam("idProd") String idProd)
    {
        try {
            if (idUser == null || idUser.trim().isEmpty() || idProd == null || idProd.trim().isEmpty()) {
                return error(Response.Status.BAD_REQUEST, "INVALID_REQUEST", "ID de usuario y producto son obligatorios.");
            }

            edu.upc.dsa.models.User user = pm.getUser(idUser.trim());
            if (user == null) {
                return error(Response.Status.NOT_FOUND, "USER_NOT_FOUND", "Usuario no encontrado.");
            }

            List<Producto> inventario = user.getInventario();
            List<Producto> items = new java.util.ArrayList<>();
            for (Producto producto : inventario) {
                if (String.valueOf(producto.getId()).equals(idProd.trim())) {
                    items.add(producto);
                }
            }

            if (items.isEmpty()) {
                return error(Response.Status.NOT_FOUND, "INVENTORY_PRODUCT_NOT_FOUND", "Producto no encontrado en el inventario.");
            }

            GenericEntity<List<Producto>> entity = new GenericEntity<List<Producto>>(items) {};
            return Response.ok(entity).build();
        } catch (Exception e) {
            logger.error("Get inventory product error", e);
            return serverError();
        }
    }

    @POST
    @Path("/comprar/{idProd}/{idUser}")
    public Response comprar(@PathParam("idProd") String idProd, @PathParam("idUser") String idUser)
    {
        try {
            int status = pm.comprarProducto(idProd, idUser);
            if (status == 201) {
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
