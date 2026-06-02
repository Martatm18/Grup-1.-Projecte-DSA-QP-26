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
import java.util.Arrays;
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
            List<Producto> lista = getFallbackProductos();
            GenericEntity<List<Producto>> entity = new GenericEntity<List<Producto>>(lista) {};
            return Response.ok(entity).build();
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
            List<Producto> inventario = new java.util.ArrayList<Producto>();
            GenericEntity<List<Producto>> entity = new GenericEntity<List<Producto>>(inventario) {};
            return Response.ok(entity).build();
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
            return Response.status(Response.Status.CREATED).build();
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

    private List<Producto> getFallbackProductos()
    {
        return Arrays.asList(
                new Producto(1, "Carga EMP", "Pulso electromagnetico portatil", 4),
                new Producto(2, "USB amarillo", "Dispositivo cifrado con fragmento de codigo", 4),
                new Producto(3, "Tarjeta temporal", "Acceso a despachos", 2),
                new Producto(4, "Botiquin", "Sube 50% de vida", 2),
                new Producto(5, "Bateria de seguridad", "Para abrir laboratorio", 3),
                new Producto(6, "Ampliacion del mapa", "Ver zonas ocultas", 1)
        );
    }
}
