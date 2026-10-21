package de.staatsbibliothek.berlin.hsp.nachweis.domain.normdatenerkennung;

import de.staatsbibliothek.berlin.hsp.messaging.common.TEICommon;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.normdaten.NormdatenTyp;
import java.util.List;
import java.util.Map.Entry;
import org.tei_c.ns._1.PersName;

record NormdatenErkennungRequestParam(String xpath, String displayText, String existingId,
                                       NormdatenTyp typ) {

  NormdatenErkennungRequestParam(Entry<String, PersName> entry) {
    this(entry.getKey(), TEICommon.getContentAsString(entry.getValue().getContent()),
        firstReferenceId(entry.getValue().getReves()), NormdatenTyp.Person);
  }

  private static String firstReferenceId(List<String> refs) {
    if (refs == null || refs.isEmpty()) {
      return "";
    }
    var ref = refs.getFirst();
    return ref.substring(ref.lastIndexOf('/') + 1);
  }
}
