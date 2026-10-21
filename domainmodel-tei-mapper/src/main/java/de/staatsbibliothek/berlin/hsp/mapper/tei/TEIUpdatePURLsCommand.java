package de.staatsbibliothek.berlin.hsp.mapper.tei;

import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.PURL;
import de.staatsbibliothek.berlin.hsp.mapper.tei.teifactories.PtrFactory;
import java.util.Objects;
import java.util.Set;
import org.tei_c.ns._1.Ptr;
import org.tei_c.ns._1.TEI;

/**
 * @author michael.hintersonnleitner@sbb.spk-berlin.de
 * @since 21.09.22
 */
public interface TEIUpdatePURLsCommand {

  static void updatePURLs(TEI tei, Set<PURL> purls) throws Exception {
    Objects.requireNonNull(tei);
    Objects.requireNonNull(purls);

    TEIQuery.queryForFileDescPupPlaceElement(tei)
        .ifPresent(pubPlace -> {
          pubPlace.getContent()
              .removeIf(content -> content instanceof Ptr && PtrFactory.TYPE.equals(((Ptr) content).getType()));

          purls.stream()
              .filter(Objects::nonNull)
              .forEach(purl -> pubPlace.getContent().add(PtrFactory.build(purl)));
        });
  }
}
