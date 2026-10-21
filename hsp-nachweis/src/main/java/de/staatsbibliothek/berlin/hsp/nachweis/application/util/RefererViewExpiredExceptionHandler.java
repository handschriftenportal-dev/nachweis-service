package de.staatsbibliothek.berlin.hsp.nachweis.application.util;

import jakarta.faces.context.ExceptionHandler;
import jakarta.faces.context.FacesContext;
import java.net.URI;
import java.net.URISyntaxException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.omnifaces.exceptionhandler.ViewExpiredExceptionHandler;
import org.omnifaces.util.FacesLocal;

/**
 * Extends the OmniFaces {@link ViewExpiredExceptionHandler} to keep the view parameters on refresh.
 * A JSF postback is sent to the bare view path, so the default refresh loses the query string of
 * the page (e.g. {@code ?kodid=...}). The referer header still carries the full page URL, so the
 * redirect targets its path and query instead. Falls back to the default refresh when no usable
 * referer is present.
 */
@Slf4j
public class RefererViewExpiredExceptionHandler extends ViewExpiredExceptionHandler {

  public RefererViewExpiredExceptionHandler(ExceptionHandler wrapped) {
    super(wrapped);
  }

  @Override
  protected void handleSuppressedException(FacesContext context, Throwable suppressedException) {
    super.handleSuppressedException(context, suppressedException);

    String referer = FacesLocal.getRequestHeader(context, "Referer");
    if (StringUtils.isBlank(referer)) {
      return;
    }

    try {
      URI refererURI = new URI(referer);
      String path = refererURI.getRawPath();
      // Require a plain absolute path: a path starting with "//" or "/\" would be treated as a
      // protocol-relative URL by the browser and open a redirect to a foreign host.
      if (StringUtils.isBlank(path) || !path.startsWith("/")
          || path.startsWith("//") || path.startsWith("/\\")) {
        return;
      }

      // Redirect to path and query only: keeps the target on this server even if the header
      // was tampered with.
      String query = refererURI.getRawQuery();
      FacesLocal.redirect(context, StringUtils.isBlank(query) ? path : path + "?" + query);
    } catch (URISyntaxException e) {
      log.warn("Ignoring invalid referer header '{}' while handling ViewExpiredException",
          referer, e);
    }
  }
}
