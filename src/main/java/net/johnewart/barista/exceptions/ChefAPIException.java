package net.johnewart.barista.exceptions;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Map;

public class ChefAPIException extends WebApplicationException {
    public ChefAPIException(List<String> exceptions) {
        this(400, exceptions);
    }

    public ChefAPIException(String exception) {
        this(400, exception);
    }

    public ChefAPIException(int code, List<String> exceptions) {
        super(buildResponse(code, exceptions));
    }

    public ChefAPIException(int code, String exception) {
        super(buildResponse(code, exception));
    }

    public static Response buildResponse(int code, String exception) {
        return buildResponse(code, ImmutableList.of(exception));
    }

    public static Response buildResponse(int code, List<String> exceptions) {
        Map<String, List<String>> errorMap = ImmutableMap.of("error", exceptions);
        Response response =  Response.status(code).entity(errorMap).build();
        if(code >= 500 && code < 600) {
            throw new WebApplicationException(response);
        } else {
            return response;
        }
    }

}
