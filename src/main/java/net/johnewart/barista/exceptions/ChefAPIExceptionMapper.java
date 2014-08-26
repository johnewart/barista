package net.johnewart.barista.exceptions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class ChefAPIExceptionMapper implements ExceptionMapper<ChefAPIException> {

    private final Logger LOG = LoggerFactory.getLogger(ChefAPIExceptionMapper.class);

    @Override
    public Response toResponse(ChefAPIException e) {
        LOG.debug("CUSTOM RESPONSE MAPPER!");
        Response defaultResponse = Response
                .serverError()
                .entity("WTF")
                .build();

        return defaultResponse;    }
}
