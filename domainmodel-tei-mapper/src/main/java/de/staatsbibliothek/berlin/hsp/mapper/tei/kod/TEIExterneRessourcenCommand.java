package de.staatsbibliothek.berlin.hsp.mapper.tei.kod;

import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.ExterneRessource;
import de.staatsbibliothek.berlin.hsp.mapper.tei.TEICommandException;
import de.staatsbibliothek.berlin.hsp.mapper.tei.common.TEIDokumentIO;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Adds, removes, and reads externe Ressourcen on the TEI-XML of a KulturObjektDokument.
 *
 * <p>Returns the new TEI-XML rather than mutating an aggregate — deciding when the KOD changes is
 * the caller's job.</p>
 */
public final class TEIExterneRessourcenCommand {

  private TEIExterneRessourcenCommand() {
  }

  /**
   * @return the TEI-XML with the ressource appended, or empty when it was already present
   * @throws TEICommandException when the TEI-XML is unparsable or holds no additional element
   */
  public static Optional<String> addExterneRessource(
      String teiXML,
      ExterneRessource externeRessource) {

    if (Objects.isNull(teiXML) || teiXML.isBlank()) {
      throw new TEICommandException("KulturObjektDokument contains no TEI-XML");
    }

    return TEIDokumentIO.editIfChanged(teiXML,
        tei -> ExterneRessourcenListBiblMapper.addRessource(tei, externeRessource));
  }

  /**
   * @return the TEI-XML without the ressource, or empty when it was not present — which includes a
   * KOD that has no TEI-XML at all, since it cannot hold the ressource either
   * @throws TEICommandException when the TEI-XML is unparsable or holds no additional element
   */
  public static Optional<String> removeExterneRessource(
      String teiXML,
      ExterneRessource externeRessource) {

    if (Objects.isNull(teiXML) || teiXML.isBlank()) {
      return Optional.empty();
    }

    return TEIDokumentIO.editIfChanged(teiXML,
        tei -> ExterneRessourcenListBiblMapper.removeRessource(tei, externeRessource));
  }

  /**
   * @return the externe Ressourcen in document order, the first one being the default; empty when
   * the KOD has no TEI-XML
   * @throws TEICommandException when the TEI-XML is unparsable or holds no additional element
   */
  public static List<ExterneRessource> readExterneRessourcen(String teiXML) {
    if (Objects.isNull(teiXML) || teiXML.isBlank()) {
      return List.of();
    }
    return TEIDokumentIO.read(teiXML, ExterneRessourcenListBiblMapper::readRessourcen);
  }
}
