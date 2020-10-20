package com.polykhel.ssa.config.locale;

import org.springframework.context.i18n.LocaleContext;
import org.springframework.context.i18n.TimeZoneAwareLocaleContext;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;
import org.springframework.web.util.WebUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Locale;
import java.util.TimeZone;

/**
 * This class will check if a double quote has been added, if so it will remove it.
 */
public class AngularCookieLocaleResolver extends CookieLocaleResolver {

    /**
     * Constant <code>QUOTE="%22"</code>
     */
    public static final String QUOTE = "%22";

    /**
     * {@inheritDoc}
     */
    @Override
    public Locale resolveLocale(HttpServletRequest request) {
        parseAngularCookieIfNecessary(request);
        return (Locale) request.getAttribute(LOCALE_REQUEST_ATTRIBUTE_NAME);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LocaleContext resolveLocaleContext(HttpServletRequest request) {
        parseAngularCookieIfNecessary(request);
        return new TimeZoneAwareLocaleContext() {
            @Override
            public TimeZone getTimeZone() {
                return (TimeZone) request.getAttribute(TIME_ZONE_REQUEST_ATTRIBUTE_NAME);
            }

            @Override
            public Locale getLocale() {
                return (Locale) request.getAttribute(LOCALE_REQUEST_ATTRIBUTE_NAME);
            }
        };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addCookie(HttpServletResponse response, String cookieValue) {
        super.addCookie(response, quote(cookieValue));
    }

    private String quote(String cookieValue) {
        return QUOTE + cookieValue + QUOTE;
    }

    private void parseAngularCookieIfNecessary(HttpServletRequest request) {
        if (request.getAttribute(LOCALE_REQUEST_ATTRIBUTE_NAME) == null) {
            Cookie cookie = WebUtils.getCookie(request, getCookieName());
            Locale locale = null;
            TimeZone timeZone = null;
            if (cookie != null) {
                String value = cookie.getValue();

                value = StringUtils.replace(value, QUOTE, "");

                String localePart = value;
                String timeZonePart = null;
                int spaceIndex = localePart.indexOf(' ');
                if (spaceIndex != -1) {
                    localePart = value.substring(0, spaceIndex);
                    timeZonePart = value.substring(spaceIndex + 1);
                }
                locale =
                    !"-".equals(localePart)
                        ? StringUtils.parseLocaleString(localePart.replace('-', '_'))
                        : null;
                if (timeZonePart != null) {
                    timeZone = StringUtils.parseTimeZoneString(timeZonePart);
                }
                if (logger.isTraceEnabled()) {
                    logger.trace(
                        "Parsed cookie value["
                            + cookie.getValue()
                            + "] into locale '"
                            + locale
                            + "'"
                            + (timeZone != null ? " and time zone '" + timeZone.getID() + "'" : ""));
                }
            }
            request.setAttribute(
                LOCALE_REQUEST_ATTRIBUTE_NAME, locale != null ? locale : determineDefaultLocale(request));
            request.setAttribute(
                TIME_ZONE_REQUEST_ATTRIBUTE_NAME,
                timeZone != null ? timeZone : determineDefaultTimeZone(request));
        }
    }
}
