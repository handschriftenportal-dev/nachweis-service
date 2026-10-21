package de.staatsbibliothek.berlin.hsp.nachweis.domain.publikation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Beschreibung;
import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Beschreibung.BeschreibungsBuilder;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Publikation.PublikationBuilder;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.IdentTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Identifikation;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Identifikation.IdentifikationBuilder;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Lizenz;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.LizenzTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.PURL;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.PURLTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.VerwaltungsTyp;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.purl.PURLBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.purl.PURLException;
import java.lang.reflect.Field;
import java.net.URI;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PublikationPURLServiceTest {

  private PURLBoundary purlBoundary;
  private PublikationPURLService service;

  @BeforeEach
  void setUp() {
    purlBoundary = mock(PURLBoundary.class);
    service = new PublikationPURLService(purlBoundary, true);
  }

  @Test
  void skipsWhenPurlAutogenerateEnabledIsFalse() throws Exception {
    PublikationPURLService disabledService = new PublikationPURLService(purlBoundary, false);
    Beschreibung beschreibung = createBeschreibung();

    disabledService.addNewPURLsToPublication(beschreibung);

    verifyNoInteractions(purlBoundary);
  }

  @Test
  void throwsNullPointerExceptionWhenBeschreibungIsNull() {
    assertThrows(NullPointerException.class, () -> service.addNewPURLsToPublication(null));
  }

  @Test
  void throwsPURLExceptionWhenPublikationIsNull() {
    Beschreibung beschreibung = createBeschreibungWithNullPublikation();

    PURLException ex = assertThrows(PURLException.class,
        () -> service.addNewPURLsToPublication(beschreibung));

    assertTrue(ex.getMessage().contains("No Publikation found"));
  }

  @Test
  void skipsWhenInternalPURLAlreadyExists() throws Exception {
    Beschreibung beschreibung = createBeschreibung();
    PURL internalPurl = new PURL(
        URI.create("https://resolver.example.com/HSP-123"),
        URI.create("https://handschriftenportal.de/search?hspobjectid=HSP-123"),
        PURLTyp.INTERNAL);
    beschreibung.getPublikation().addPURL(internalPurl);

    service.addNewPURLsToPublication(beschreibung);

    verifyNoInteractions(purlBoundary);
  }

  @Test
  void createsAndAddsPURLWhenEnabledAndNoInternalPURLExists() throws Exception {
    Beschreibung beschreibung = createBeschreibung();
    PURL newPurl = new PURL(
        URI.create("https://resolver.example.com/BSB123"),
        URI.create("https://handschriftenportal.de/search?hspobjectid=BSB123"),
        PURLTyp.INTERNAL);

    when(purlBoundary.createNewPURL(beschreibung.getId(), beschreibung.getDokumentObjektTyp()))
        .thenReturn(newPurl);

    service.addNewPURLsToPublication(beschreibung);

    assertTrue(beschreibung.getPublikation().getPURLs().contains(newPurl));
    verify(purlBoundary).createNewPURL(beschreibung.getId(), beschreibung.getDokumentObjektTyp());
  }

  /**
   * Regression pin: if {@code hasInternalPURL()} is ever loosened to {@code hasAnyPURL()}, imports
   * of Beschreibungen with an existing source-institution EXTERNAL PURL would silently stop
   * generating the HSP-side INTERNAL resolver PURL, leaving published Beschreibungen unresolvable
   * via HSP. Asserts that an EXTERNAL PURL does NOT suppress INTERNAL PURL creation.
   */
  @Test
  void createsInternalPURLWhenOnlyExternalPURLExists() throws Exception {
    Beschreibung beschreibung = createBeschreibung();
    PURL externalPurl = new PURL(
        URI.create("https://doi.org/10.1234/source-institution"),
        URI.create("https://doi.org/10.1234/source-institution"),
        PURLTyp.EXTERNAL);
    beschreibung.getPublikation().addPURL(externalPurl);

    PURL newInternalPurl = new PURL(
        URI.create("https://resolver.example.com/HSP-999"),
        URI.create("https://handschriftenportal.de/search?hspobjectid=HSP-999"),
        PURLTyp.INTERNAL);
    when(purlBoundary.createNewPURL(beschreibung.getId(), beschreibung.getDokumentObjektTyp()))
        .thenReturn(newInternalPurl);

    service.addNewPURLsToPublication(beschreibung);

    verify(purlBoundary).createNewPURL(beschreibung.getId(), beschreibung.getDokumentObjektTyp());
    assertTrue(beschreibung.getPublikation().getPURLs().contains(externalPurl),
        "pre-existing EXTERNAL PURL should be preserved");
    assertTrue(beschreibung.getPublikation().getPURLs().contains(newInternalPurl),
        "new INTERNAL PURL should be added alongside the EXTERNAL one");
    assertEquals(2, beschreibung.getPublikation().getPURLs().size(),
        "publikation should end up with both PURLs — EXTERNAL from source + INTERNAL from HSP");
  }

  @Test
  void wrapsExceptionFromPurlBoundaryInPURLException() throws Exception {
    Beschreibung beschreibung = createBeschreibung();

    when(purlBoundary.createNewPURL(eq(beschreibung.getId()), any()))
        .thenThrow(new RuntimeException("connection refused"));

    PURLException ex = assertThrows(PURLException.class,
        () -> service.addNewPURLsToPublication(beschreibung));

    assertTrue(ex.getMessage().contains("Error creating PURL"));
  }

  private Identifikation createIdentifikation() {
    return new IdentifikationBuilder()
        .withIdentTyp(IdentTyp.GUELTIGE_SIGNATUR)
        .withIdent("1-1-1")
        .withBesitzer(new NormdatenReferenz("1", "SBB", "GND1"))
        .withAufbewahrungsOrt(new NormdatenReferenz("2", "Berlin", "GND2"))
        .build();
  }

  private Beschreibung createBeschreibung() {
    var beschreibung = new BeschreibungsBuilder("BESCHREIBUNG1", "KOD1", createIdentifikation())
        .withVerwaltungsTyp(VerwaltungsTyp.EXTERN)
        .withTEIXml("<xml></xml>")
        .build();
    var publikation = new PublikationBuilder(beschreibung.getPublikation())
        .withLizenz(new Lizenz(LizenzTyp.CC_BY))
        .build();
    beschreibung.setPublikation(publikation);
    return beschreibung;
  }

  private Beschreibung createBeschreibungWithNullPublikation() {
    Beschreibung beschreibung = new BeschreibungsBuilder("BESCHREIBUNG1", "KOD1",
        createIdentifikation())
        .withVerwaltungsTyp(VerwaltungsTyp.EXTERN)
        .withTEIXml("<xml></xml>")
        .build();
    try {
      Field field = Beschreibung.class.getDeclaredField("publikation");
      field.setAccessible(true);
      field.set(beschreibung, null);
    } catch (Exception e) {
      throw new RuntimeException("Failed to set publikation to null via reflection", e);
    }
    return beschreibung;
  }
}
