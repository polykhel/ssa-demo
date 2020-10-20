package com.polykhel.ssa.web.filter;

import com.polykhel.ssa.config.CoreProperties;

import javax.servlet.*;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * This filter is used in production to put HTTP cache headers with a long (4 years default)
 * expiration time.
 */
public class CachingHttpHeadersFilter implements Filter {

    public static final int DEFAULT_DAYS_TO_LIVE = 1461;
    public static final long DEFAULT_SECONDS_TO_LIVE = TimeUnit.DAYS.toMillis(DEFAULT_DAYS_TO_LIVE);
    private final CoreProperties coreProperties;
    private long cacheTimeToLive = DEFAULT_SECONDS_TO_LIVE;

    public CachingHttpHeadersFilter(CoreProperties coreProperties) {
        this.coreProperties = coreProperties;
    }

    /**
     * {@inheritDoc
     */
    @Override
    public void init(FilterConfig filterConfig) {
        cacheTimeToLive =
            TimeUnit.DAYS.toMillis(coreProperties.getHttp().getCache().getTimeToLiveInDays());
    }

    /**
     * {@inheritDoc
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
        throws IOException, ServletException {
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        httpResponse.setHeader("Cache-Control", "max-age=" + cacheTimeToLive);
        httpResponse.setHeader("Pragma", "cache");

        // setting expires header for proxy caching
        httpResponse.setDateHeader("Expires", cacheTimeToLive + System.currentTimeMillis());

        chain.doFilter(request, response);
    }

    /**
     * {@inheritDoc
     */
    @Override
    public void destroy() {
        // do nothing
    }
}
