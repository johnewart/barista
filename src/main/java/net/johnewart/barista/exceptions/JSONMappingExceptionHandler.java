package net.johnewart.barista.exceptions;

import com.fasterxml.jackson.databind.JsonMappingException;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JSONMappingExceptionHandler implements ExceptionMapper<JsonMappingException> {

    private Logger LOG = LoggerFactory.getLogger(JSONMappingExceptionHandler.class);

    @Override
    public Response toResponse(JsonMappingException e) {
        // Deserialize issues
        String message = e.getMessage();

        String bogusDataPatternStr = ".*core.(\\w+)\\[\"(\\w+)\"\\].*";
        Pattern bogusDataPattern = Pattern.compile(bogusDataPatternStr, Pattern.DOTALL);
        Matcher bogusDataMatcher = bogusDataPattern.matcher(message);

        LOG.debug("JSON exception: ", e);
        String errorMessage = "Unhandled parsing error";

        if (message.startsWith("Unrecognized")) {
            Pattern p = Pattern.compile(".*Unrecognized field \"(.*?)\".*", Pattern.DOTALL | Pattern.UNICODE_CHARACTER_CLASS);
            Matcher m = p.matcher(message);
            if(m.matches()) {
                errorMessage = "Invalid key " + m.group(1) + " in request body";
            }
        } else if (bogusDataMatcher.matches()) {
            String className = bogusDataMatcher.group(1);
            String property = bogusDataMatcher.group(2);
            switch(property) {
                case "run_list":
                    errorMessage = "Field 'run_list' is not a valid run list";
                    break;

                case "env_run_lists":
                    errorMessage = "Field 'env_run_lists' contains invalid run lists";
                    break;

                case "override":
                case "automatic":
                case "normal":
                case "default":
                case "default_attributes":
                case "override_attributes":
                    errorMessage = String.format("Field '%s' is not a hash", property);
                    break;
            }
        } else {
            LOG.error("Unhandled JSON exception: " + e.getMessage());
        }

        return ChefAPIException.buildResponse(400, errorMessage);
    }
}
