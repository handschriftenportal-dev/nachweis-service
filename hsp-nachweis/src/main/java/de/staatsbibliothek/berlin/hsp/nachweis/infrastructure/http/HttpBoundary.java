package de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.http;

import java.io.IOException;
import java.net.URI;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;

/**
 * @author michael.hintersonnleitner@sbb.spk-berlin.de
 * @since 20.07.22
 */
public interface HttpBoundary {

  Optional<String> loadBodyFromURL(URI targetURL)
      throws IOException, InterruptedException, NoSuchAlgorithmException, KeyManagementException;
}
