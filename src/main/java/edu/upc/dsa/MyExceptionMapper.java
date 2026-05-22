package edu.upc.dsa;

import edu.upc.dsa.models.ApiError;
import org.apache.log4j.Logger;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class MyExceptionMapper implements ExceptionMapper<Exception> {
    private static final Logger logger = Logger.getLogger(MyExceptionMapper.class);

    @Override
    public Response toResponse(Exception ex) {
        if (ex instanceof NotFoundException) {
            return build(Response.Status.NOT_FOUND, "NOT_FOUND", "Recurso no encontrado.");
        }

        if (ex instanceof WebApplicationException) {
            WebApplicationException webException = (WebApplicationException) ex;
            int status = webException.getResponse().getStatus();
            return Response.status(status)
                    .entity(new ApiError("REQUEST_ERROR", "La peticion no se ha podido procesar."))
                    .build();
        }

        logger.error("Unexpected server error", ex);
        return build(Response.Status.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR", "Error interno del servidor.");
    }

    private Response build(Response.Status status, String code, String message) {
        return Response.status(status)
                .entity(new ApiError(code, message))
                .build();
    }
}
