package de.staatsbibliothek.berlin.hsp.nachweis.application.controller;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 21.08.2020.
 * @version 1.0
 */
public class StreamReader {

  public static String readStream(InputStream in) throws IOException {
    byte[] data = new byte[1024];
    int r;
    try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
      while ((r = in.read(data)) > 0) {
        out.write(data, 0, r);
      }
      return out.toString(StandardCharsets.UTF_8);
    }
  }

}
