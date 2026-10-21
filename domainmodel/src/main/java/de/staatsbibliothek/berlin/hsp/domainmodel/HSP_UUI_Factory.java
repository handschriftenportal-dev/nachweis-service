package de.staatsbibliothek.berlin.hsp.domainmodel;

import java.util.UUID;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 26.08.22.
 * @version 1.0
 */
public final class HSP_UUI_Factory {

  private static final String PREFIX = "HSP-";

  public static String generate() {
    return PREFIX + UUID.randomUUID();
  }

  public static String generate(byte[] bytes) {
    return PREFIX + UUID.nameUUIDFromBytes(bytes);
  }

}
