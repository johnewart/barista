package net.johnewart.barista.auth;


import com.google.common.base.Optional;
import com.sun.jersey.api.core.HttpContext;
import com.sun.jersey.api.model.Parameter;
import com.sun.jersey.core.spi.component.ComponentContext;
import com.sun.jersey.core.spi.component.ComponentScope;
import com.sun.jersey.server.impl.inject.AbstractHttpContextInjectable;
import com.sun.jersey.spi.inject.Injectable;
import com.sun.jersey.spi.inject.InjectableProvider;
import io.dropwizard.auth.Auth;
import io.dropwizard.auth.AuthenticationException;
import io.dropwizard.auth.Authenticator;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.eclipse.jetty.util.B64Code;
import org.eclipse.jetty.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class ChefAuthProvider<T> implements InjectableProvider<Auth, Parameter> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ChefAuthProvider.class);

    private static class BasicAuthInjectable<T> extends AbstractHttpContextInjectable<T> {
        private static final String PREFIX = "Basic";
        private static final String CHALLENGE_FORMAT = PREFIX + " realm=\"%s\"";

        private final Authenticator<String, T> authenticator;
        private final boolean required;

        private final String X_OPS_SIGN = "X-Ops-Sign";
        private final String X_OPS_USERID = "X-Ops-Userid";
        private final String X_OPS_TIMESTAMP = "X-Ops-Timestamp";
        private final String X_OPS_CONTENT_HASH = "X-Ops-Content-Hash";
        private final String X_OPS_AUTHORIZATION = "X-Ops-Authorization-";


        private BasicAuthInjectable(Authenticator<String, T> authenticator,
                                    boolean required) {
            this.authenticator = authenticator;
            this.required = required;
        }

        @Override
        public T getValue(HttpContext c) {
            final String header = c.getRequest().getHeaderValue(HttpHeaders.AUTHORIZATION);

            String userid = c.getRequest().getHeaderValue(X_OPS_USERID);
            String signature = c.getRequest().getHeaderValue(X_OPS_SIGN);
            String authString = c.getRequest().getHeaderValue(X_OPS_AUTHORIZATION);
            String timestamp = c.getRequest().getHeaderValue(X_OPS_TIMESTAMP);
            String contentHash = c.getRequest().getHeaderValue(X_OPS_CONTENT_HASH);

            try {
                // Throw 400
                /*if (signature == null || !signature.matches(".*version=1.0.*")) {
                    throw new WebApplicationException(Response.Status.BAD_REQUEST);
                }  */

                if(signature != null ) {
                    try {

                        String body = c.getRequest().getEntity(String.class);
                        LOGGER.debug("BODY: " + body);
                        String computedContentHash = new String(Base64.encodeBase64(DigestUtils.sha1(body)));

                        LOGGER.debug(
                                String.format("Computed hash: '%s' / Request hash '%s'", computedContentHash, contentHash)
                        );

                        if(!computedContentHash.equals(contentHash)) {
                            throw new WebApplicationException(Response.Status.BAD_REQUEST);
                        }
                    } catch (Exception e) {
                        LOGGER.debug("Can't cast request entity to string: " + e.toString());
                    }
                }

                /*
                // Throw 401
                if (userid == null || !userid.equals("admin") ||
                        timestamp == null || timestamp.isEmpty() ||
                        contentHash == null || contentHash.isEmpty())
                {
                    throw new WebApplicationException(Response.Status.UNAUTHORIZED);
                } */



                final Optional<T> result = authenticator.authenticate(userid);
                if (result.isPresent()) {
                    return result.get();
                }
            } catch (IllegalArgumentException e) {
                LOGGER.debug("Error decoding credentials", e);
            } catch (AuthenticationException e) {
                LOGGER.warn("Error authenticating credentials", e);
                throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
            }

            return null;
        }
    }

    private final Authenticator<String, T> authenticator;

    /**
     * Creates a new BasicAuthProvider with the given {@link Authenticator} and realm.
     *
     * @param authenticator the authenticator which will take the {@link BasicCredentials} and
     *                      convert them into instances of {@code T}
     */
    public ChefAuthProvider(Authenticator<String, T> authenticator) {
        this.authenticator = authenticator;

    }

    @Override
    public ComponentScope getScope() {
        return ComponentScope.PerRequest;
    }

    @Override
    public Injectable<?> getInjectable(ComponentContext ic, Auth a, Parameter c) {
        return new BasicAuthInjectable<>(authenticator, a.required());
    }
}
