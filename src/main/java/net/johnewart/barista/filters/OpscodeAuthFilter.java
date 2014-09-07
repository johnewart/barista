package net.johnewart.barista.filters;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;


public class OpscodeAuthFilter implements Filter {
    private final String X_OPS_SIGN = "X-Ops-Sign";
    private final String X_OPS_USERID = "X-Ops-Userid";
    private final String X_OPS_TIMESTAMP = "X-Ops-Timestamp";
    private final String X_OPS_CONTENT_HASH = "X-Ops-Content-Hash";
    private final String X_OPS_AUTHORIZATION = "X-Ops-Authorization-";


    @Override
    public void init(FilterConfig filterConfig) {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpReq = (HttpServletRequest) request;
        String userid = httpReq.getHeader(X_OPS_USERID);
        String signature = httpReq.getHeader(X_OPS_SIGN);
        String authString = httpReq.getHeader(X_OPS_AUTHORIZATION);
        String timestamp = httpReq.getHeader(X_OPS_TIMESTAMP);
        String contentHash = httpReq.getHeader(X_OPS_CONTENT_HASH);



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
