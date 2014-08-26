package net.johnewart.barista.filters;

import org.eclipse.jetty.continuation.Continuation;
import org.eclipse.jetty.continuation.ContinuationListener;
import org.eclipse.jetty.continuation.ContinuationSupport;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.ws.rs.WebApplicationException;
import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * Quality of Service Filter.
 * <p/>
 * This filter limits the number of active requests to the number set by the "maxRequests" init parameter (default 10).
 * If more requests are received, they are suspended and placed on priority queues.  Priorities are determined by
 * the {@link #getPriority(ServletRequest)} method and are a value between 0 and the value given by the "maxPriority"
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
 * the {@link #getPriority(ServletRequest)} method is used, so that more important
 * requests are serviced first.     For example, this filter could be deployed with a
 * maxRequest limit slightly smaller than the containers thread pool and a high priority
 * allocated to admin users.  Thus regardless of load, admin users would always be
 * able to access the web application.
 * </p><p>
 * The maxRequest limit is policed by a {@link Semaphore} and the filter will wait a short while attempting to acquire
 * the semaphore. This wait is controlled by the "waitMs" init parameter and allows the expense of a suspend to be
 * avoided if the semaphore is shortly available.  If the semaphore cannot be obtained, the request will be suspended
 * for the default suspend period of the container or the valued set as the "suspendMs" init parameter.
 * </p><p>
 * If the "managedAttr" init parameter is set to true, then this servlet is set as a {@link ServletContext} attribute with the
 * filter name as the attribute name.  This allows context external mechanism (eg JMX via {@link ContextHandler#MANAGED_ATTRIBUTES}) to
 * manage the configuration of the filter.
 * </p>
 */

public class RequestSizeFilter implements Filter {
    private final Logger LOG = LoggerFactory.getLogger(RequestSizeFilter.class);

    @Override
    public void init(FilterConfig filterConfig) {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (request.getContentLength() > 1000000) {
            ((HttpServletResponse) response).sendError(HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE);
        } else if (request.getContentLength() == 0) {
            ((HttpServletResponse) response).sendError(400);
        } else {
            chain.doFilter(request, response);
        }

    }

    @Override
    public void destroy() {

    }


}
