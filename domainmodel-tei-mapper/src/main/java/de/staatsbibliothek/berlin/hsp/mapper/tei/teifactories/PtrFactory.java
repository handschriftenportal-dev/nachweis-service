package de.staatsbibliothek.berlin.hsp.mapper.tei.teifactories;

import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.PURL;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.PURLTyp;
import org.tei_c.ns._1.Ptr;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 26.08.22.
 * @version 1.0
 */
public class PtrFactory {

  public static final String TYPE = "purl";
  public static final String SUBTYPE = "hsp";

  public static Ptr build(PURL purl) {
    Ptr ptr = new Ptr();
    ptr.setId(purl.getId());
    ptr.getTargets().add(purl.getPurl().toASCIIString());
    ptr.setType(TYPE);
    if (PURLTyp.INTERNAL == purl.getTyp()) {
      ptr.setSubtype(SUBTYPE);
    }

    return ptr;
  }

}
