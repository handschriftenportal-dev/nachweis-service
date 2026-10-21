package de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.stork;

import io.smallrye.stork.Stork;
import io.smallrye.stork.api.ServiceInstance;
import jakarta.enterprise.context.ApplicationScoped;
import java.net.URI;
import java.util.Objects;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Resolves {@code stork://service-name/path} URIs to actual {@code http(s)://host:port/path} URIs
 * via SmallRye Stork service discovery.
 */
@ApplicationScoped
public class StorkDiscovery {

  private static final Logger logger = LoggerFactory.getLogger(StorkDiscovery.class);

  private static boolean isStorkScheme(URI uri) {
    if (uri == null || uri.getScheme() == null) {
      return false;
    }
    String scheme = uri.getScheme();
    return Stork.STORK.equalsIgnoreCase(scheme) || (Stork.STORK + "s").equalsIgnoreCase(scheme);
  }

  /**
   * Resolves a {@code stork://service-name/path} URI to the actual service URL.
   *
   * @param uri the stork URI (e.g., {@code stork://normdaten-service/rest})
   * @return the resolved URI with actual host:port, or the original URI if not a stork URI or
   * resolution fails
   */
  public URI resolveUri(URI uri) {
    if (!isStorkScheme(uri)) {
      return uri;
    }
    String serviceName = uri.getHost();
    if (serviceName == null || serviceName.isBlank()) {
      return uri;
    }

    try {
      ServiceInstance instance = selectInstance(serviceName);
      URI resolvedUri = createResolvedUri(uri, instance);
      logger.debug("Resolved stork service {} to {}", serviceName, resolvedUri);
      return resolvedUri;
    } catch (RuntimeException e) {
      logger.error("Error resolving stork service {}", serviceName, e);
      return uri;
    }
  }

  ServiceInstance selectInstance(String serviceName) {
    return Stork.getInstance()
        .getService(serviceName)
        .selectInstance()
        .await().indefinitely();
  }

  URI createResolvedUri(URI uri, ServiceInstance instance) {
    boolean secure = instance.isSecure() || "storks".equalsIgnoreCase(uri.getScheme());
    String scheme = secure ? "https" : "http";
    String path = Objects.toString(uri.getRawPath(), "");
    
    StringBuilder sb = new StringBuilder(scheme)
        .append("://")
        .append(instance.getHost())
        .append(":")
        .append(instance.getPort())
        .append(path);

    if (StringUtils.isNotBlank(uri.getRawQuery())) {
      sb.append("?").append(uri.getRawQuery());
    }
    if (StringUtils.isNotBlank(uri.getRawFragment())) {
      sb.append("#").append(uri.getRawFragment());
    }

    return URI.create(sb.toString());
  }
}
