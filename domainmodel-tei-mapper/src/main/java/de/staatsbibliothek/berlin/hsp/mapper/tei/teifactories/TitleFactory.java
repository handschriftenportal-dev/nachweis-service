package de.staatsbibliothek.berlin.hsp.mapper.tei.teifactories;

import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Identifikation;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
import java.util.Optional;
import org.tei_c.ns._1.Title;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 19.05.2020.
 * @version 1.0
 * <p>
 * Class to create TEI Element Title based on HSP Domainmodel Identifikation
 */
public class TitleFactory {

  public static final String KOD_TITLE_PREFIX = "Kulturobjektdokument von ";

  static Title build(Identifikation identifikation) {

    Title title = new Title();

    Optional.ofNullable(identifikation).map(Identifikation::getBesitzer).map(
        NormdatenReferenz::getName).ifPresent(
        nameKoerperschaft -> Optional.of(identifikation).map(Identifikation::getAufbewahrungsOrt)
            .ifPresent(ort -> {
              if (identifikation.getIdent() != null) {
                title.getContent().clear();
                title.getContent().add(
                    KOD_TITLE_PREFIX + ort.getName()
                        + ", " + nameKoerperschaft + ", " + identifikation.getIdent());
              }
            }));

    return title;

  }

}
