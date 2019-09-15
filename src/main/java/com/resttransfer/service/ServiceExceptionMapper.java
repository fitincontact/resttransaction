package com.resttransfer.service;

import org.apache.log4j.Logger;
import com.resttransfer.exception.ErrorResponse;
import com.resttransfer.exception.CustomException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class ServiceExceptionMapper implements ExceptionMapper<CustomException> {
    private static Logger LOGGER = Logger.getLogger(ServiceExceptionMapper.class);

    public ServiceExceptionMapper() {
    }

    public Response toResponse(CustomException daoException) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Mapping exception to Response....");
        }
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setErrorCode(daoException.getMessage());

        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errorResponse).type(MediaType.APPLICATION_JSON).build();
    }

}
