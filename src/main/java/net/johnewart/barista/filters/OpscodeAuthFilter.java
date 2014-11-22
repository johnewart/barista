package net.johnewart.barista.filters;

import net.johnewart.barista.core.User;
import net.johnewart.barista.data.UserDAO;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Enumeration;


public class OpscodeAuthFilter implements Filter {
    private static final Logger LOG = LoggerFactory.getLogger(OpscodeAuthFilter.class);

    private final String X_OPS_SIGN = "X-Ops-Sign";
    private final String X_OPS_USERID = "X-Ops-Userid";
    private final String X_OPS_TIMESTAMP = "X-Ops-Timestamp";
    private final String X_OPS_CONTENT_HASH = "X-Ops-Content-Hash";
    private final String X_OPS_AUTH_PREFIX = "X-Ops-Authorization-";

    private final UserDAO userDAO;

    public OpscodeAuthFilter(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    @Override
    public void init(FilterConfig filterConfig) {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpReq = (HttpServletRequest) request;
        String userid = httpReq.getHeader(X_OPS_USERID);
        String signature = httpReq.getHeader(X_OPS_SIGN);
        String timestamp = httpReq.getHeader(X_OPS_TIMESTAMP);
        String contentHash = httpReq.getHeader(X_OPS_CONTENT_HASH);
        StringBuilder authBuilder = new StringBuilder();
        Enumeration<String> enumeration = httpReq.getHeaderNames();

        while(enumeration.hasMoreElements()) {
            String header = enumeration.nextElement();
            if(header.startsWith(X_OPS_AUTH_PREFIX)) {
                authBuilder.append(httpReq.getHeader(header));
            }
        }

        String authString = authBuilder.toString();
        LOG.debug("Auth String: " + authString);

        try {
            if (signature == null || !signature.matches(".*version=1.0.*")) {
                throw new ErrorCodeException(400);
            }

            if (userid == null ||
                timestamp == null || timestamp.isEmpty() ||
                contentHash == null || contentHash.isEmpty())
            {
                throw new ErrorCodeException(401);
            }

            User user = userDAO.getByName(userid);
            if (user == null) {
                throw new ErrorCodeException(401);
            }

            String data = new String(Base64.decodeBase64(authString));
            LOG.debug("DATA: " + data);


            chain.doFilter(request, response);
        } catch (ErrorCodeException ex) {
            ((HttpServletResponse) response).sendError(ex.errorCode);
        }

    }

    @Override
    public void destroy() {

    }

    class ErrorCodeException extends Exception {
        public final int errorCode;

        public ErrorCodeException(int errorCode) {
            this.errorCode = errorCode;
        }
    }


}
