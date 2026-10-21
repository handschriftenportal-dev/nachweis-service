package de.staatsbibliothek.berlin.hsp.mapper.tei.kod;

import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Forschungsdokumentation;
import de.staatsbibliothek.berlin.hsp.mapper.tei.TEICommandException;
import de.staatsbibliothek.berlin.hsp.mapper.tei.common.TEIDokumentIO;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/** Adds, removes, and reads Forschungsdokumentation in the TEI XML of a KulturObjektDokument.
 * @author Munhal.Mammadov@sbb.spk-berlin.de
 * @since 01.09.2026
 */
public final class TEIForschungsdokumentationCommand {

  private TEIForschungsdokumentationCommand() {
  }

  /**
   * @return the updated TEI XML, or empty when a Forschungsdokumentation with the same non-empty
   * link already exists
   * @throws TEICommandException when the TEI XML is empty, unparsable or has no additional element
   */
  public static Optional<String> addForschungsdokumentation(
      String teiXML,
      Forschungsdokumentation forschungsdokumentation) {

    if (Objects.isNull(teiXML) || teiXML.isBlank()) {
      throw new TEICommandException("KulturObjektDokument contains no TEI-XML");
    }

    return TEIDokumentIO.editIfChanged(
        teiXML,
        tei -> ForschungsdokumentationListBiblMapper.addForschungsdokumentation(
            tei,
        forschungsdokumentation));
  }

  /**
   * @return the updated TEI XML, or empty when the Forschungsdokumentation was not present
   * @throws TEICommandException when the TEI XML is unparsable or has no additional element
   */
  public static Optional<String> removeForschungsdokumentation(
      String teiXML,
      Forschungsdokumentation forschungsdokumentation) {

    if (Objects.isNull(teiXML) || teiXML.isBlank()) {
      return Optional.empty();
    }

    return TEIDokumentIO.editIfChanged(
        teiXML,
        tei -> ForschungsdokumentationListBiblMapper.removeForschungsdokumentation(
            tei,
            forschungsdokumentation));
  }

  /**
   * @return the Forschungsdokumentationen in document order; empty when the KOD has no TEI-XML
   * @throws TEICommandException when the TEI-XML is unparsable or holds no additional element
   */
  public static List<Forschungsdokumentation> readForschungsdokumentationen(String teiXML) {
    if (Objects.isNull(teiXML) || teiXML.isBlank()) {
      return List.of();
    }
    return TEIDokumentIO.read(
        teiXML, ForschungsdokumentationListBiblMapper::readForschungsdokumentationen);
  }
}
