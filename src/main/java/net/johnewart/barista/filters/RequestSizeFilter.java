package net.johnewart.barista.filters;

import javax.servlet.*;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


public class RequestSizeFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (request.getContentLength() > 1000000) {
            ((HttpServletResponse) response).sendError(HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE);
        //} else if (request.getContentLength() == 99999999) {
          //  ((HttpServletResponse) response).sendError(400);
        } else {
            chain.doFilter(request, response);
        }

    }

    @Override
    public void destroy() {

    }


}
