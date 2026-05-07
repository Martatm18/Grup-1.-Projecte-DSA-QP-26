package edu.upc.dsa.services;

import edu.upc.dsa.ProductoManager;
import edu.upc.dsa.ProductoManagerImpl;
import edu.upc.dsa.models.Producto;
import io.swagger.annotations.Api;
import javax.ws.rs.*;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Api(value = "/tienda", description = "Servicio de Tienda SIGMA")
@Path("/tienda")
public class ProductoServicio
{
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
        List<Producto> lista = pm.getListaProductos();
        GenericEntity<List<Producto>> entity = new GenericEntity<List<Producto>>(lista) {};
        return Response.ok(entity).build();
    }

    @POST
    @Path("/comprar/{idProd}/{idUser}")
    public Response comprar(@PathParam("idProd") String idProd, @PathParam("idUser") String idUser)
    {
        int res = pm.comprarProducto(idProd, idUser);
        return Response.status(res).build();
    }
}
