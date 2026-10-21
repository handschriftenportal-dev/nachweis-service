package de.staatsbibliothek.berlin.hsp.nachweis.domain.common;

import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.TEIDokument;
import java.util.Objects;

public class TEIUtility {

  private TEIUtility() {
  }

  public static boolean isTEINullOrEmpty(TEIDokument tei) {
    return Objects.isNull(tei) || Objects.isNull(tei.getId()) || Objects.isNull(
        tei.getTeiXML()) || tei.getTeiXML().isEmpty();
  }
}
