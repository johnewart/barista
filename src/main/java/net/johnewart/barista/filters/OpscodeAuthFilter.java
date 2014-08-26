package net.johnewart.barista.filters;

import net.johnewart.barista.exceptions.ChefAPIException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Quality of Service Filter.
 * <p/>
 * This filter limits the number of active requests to the number set by the "maxRequests" init parameter (default 10).
 * If more requests are received, they are suspended and placed on priority queues.  Priorities are determined by
 * the {@link #getPriority(javax.servlet.ServletRequest)} method and are a value between 0 and the value given by the "maxPriority"
 * init parameter (default 10), with higher values having higher priority.
 * </p><p>
 * This filter is ideal to prevent wasting threads waiting for slow/limited
 * resources such as a JDBC connection pool.  It avoids the situation where all of a
 * containers thread pool may be consumed blocking on such a slow resource.
 * By limiting the number of active threads, a smaller thread pool may be used as
 * the threads are not wasted waiting.  Thus more memory may be available for use by
 * the active threads.
 * </p><p>
 * Furthermore, this filter uses a priority when resuming waiting requests. So that if
 * a container is under load, and there are many requests waiting for resources,
 * the {@link #getPriority(javax.servlet.ServletRequest)} method is used, so that more important
 * requests are serviced first.     For example, this filter could be deployed with a
 * maxRequest limit slightly smaller than the containers thread pool and a high priority
 * allocated to admin users.  Thus regardless of load, admin users would always be
 * able to access the web application.
 * </p><p>
 * The maxRequest limit is policed by a {@link java.util.concurrent.Semaphore} and the filter will wait a short while attempting to acquire
 * the semaphore. This wait is controlled by the "waitMs" init parameter and allows the expense of a suspend to be
 * avoided if the semaphore is shortly available.  If the semaphore cannot be obtained, the request will be suspended
 * for the default suspend period of the container or the valued set as the "suspendMs" init parameter.
 * </p><p>
 * If the "managedAttr" init parameter is set to true, then this servlet is set as a {@link javax.servlet.ServletContext} attribute with the
 * filter name as the attribute name.  This allows context external mechanism (eg JMX via {@link org.eclipse.jetty.server.handler.ContextHandler#MANAGED_ATTRIBUTES}) to
 * manage the configuration of the filter.
 * </p>
 */

public class OpscodeAuthFilter implements Filter {
    private final Logger LOG = LoggerFactory.getLogger(OpscodeAuthFilter.class);
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
        HttpServletRequest req = (HttpServletRequest) request;
        String userid = req.getHeader(X_OPS_USERID);
        String signature = req.getHeader(X_OPS_SIGN);
        String authString = req.getHeader(X_OPS_AUTHORIZATION);
        String timestamp = req.getHeader(X_OPS_TIMESTAMP);
        String contentHash = req.getHeader(X_OPS_CONTENT_HASH);

        try {
            if (signature == null || !signature.startsWith("algorithm=sha1;version=1.0")) {
                throw new ErrorCodeException(400);
            }

            if (userid == null || !userid.equals("admin") ||
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
