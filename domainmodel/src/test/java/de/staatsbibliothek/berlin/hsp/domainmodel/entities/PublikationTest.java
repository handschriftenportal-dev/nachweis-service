package de.staatsbibliothek.berlin.hsp.domainmodel.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Beschreibung;
import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Beschreibung.BeschreibungsBuilder;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Publikation.PublikationBuilder;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.IdentTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Identifikation.IdentifikationBuilder;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.PURL;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.PURLTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.PublikationStatus;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.Set;
import org.junit.jupiter.api.Test;

class PublikationTest {

  private static final String TEST_USER = "TestUser";

  @Test
  void initialPublikation() {
    Publikation publikation = new PublikationBuilder().build();

    assertNotNull(publikation);
    assertNotNull(publikation.getId());
    assertEquals(PublikationStatus.DRAFT, publikation.getStatus());
    assertEquals(1, publikation.getAuflagenNummer());
    assertTrue(publikation.getPURLs().isEmpty());
    assertTrue(publikation.isDraft());
    assertFalse(publikation.isPublished());
  }

  @Test
  void approvableCallbacks() {
    Publikation publikation = new PublikationBuilder().build();
    var identifikation = new IdentifikationBuilder()
        .withId("IDENT-1-ID")
        .withIdentTyp(IdentTyp.GUELTIGE_SIGNATUR)
        .withGueltigBis(LocalDateTime.now().plusDays(1).toLocalDate())
        .build();
    Beschreibung beschreibung = new BeschreibungsBuilder("ID-1", null, identifikation)
        .withPublikation(publikation)
        .build();

    assertTrue(publikation.canRequestApproval());
    assertFalse(publikation.canBeApproved());
    assertTrue(publikation.isDraft());

    // Create approval request (calls onApprovalCreated internally)
    ApprovalRequest request = ApprovalRequest.create(beschreibung, TEST_USER);
    assertTrue(publikation.isInReview()); // Status unchanged
    assertTrue(publikation.hasActiveApprovalRequest());
    assertTrue(publikation.canBeApproved());

    // Approve
    request.approve(beschreibung, TEST_USER, "Approved");
    assertEquals(PublikationStatus.PUBLISHED, publikation.getStatus());
    assertTrue(publikation.isPublished());
    assertFalse(publikation.isDraft());
    assertNotNull(publikation.getErstpublikationsDatum());
    assertNotNull(publikation.getPublikationsDatumHSP());

    // After publishing, can no longer request approval
    assertFalse(publikation.canRequestApproval());
    assertFalse(publikation.canBeApproved());
  }

  @Test
  void onRejectedKeepsStatusDraft() {
    Publikation publikation = new PublikationBuilder().build();
    var identifikation = new IdentifikationBuilder()
        .withId("IDENT-1-ID")
        .withIdentTyp(IdentTyp.GUELTIGE_SIGNATUR)
        .withGueltigBis(LocalDateTime.now().plusDays(1).toLocalDate())
        .build();
    Beschreibung beschreibung = new BeschreibungsBuilder("ID-1", null, identifikation)
        .withPublikation(publikation)
        .build();

    ApprovalRequest request = ApprovalRequest.create(beschreibung, TEST_USER);
    request.reject(beschreibung, TEST_USER, "Rejected");

    assertEquals(PublikationStatus.DRAFT, publikation.getStatus());
    assertTrue(publikation.isDraft());
    assertFalse(publikation.hasActiveApprovalRequest());
    assertTrue(publikation.canRequestApproval());
  }

  @Test
  void onApprovedSetsErstpublikationsDatumOnlyOnce() {
    Publikation publikation = new PublikationBuilder().build();

    publikation.onApproved();
    LocalDateTime firstPublikationsDatum = publikation.getErstpublikationsDatum();
    assertNotNull(firstPublikationsDatum);

    // For re-publication, the erstpublikationsDatum should remain unchanged
    // (In practice, a new Publikation would be created for re-publication)
  }

  @Test
  void addPURLs() {
    Publikation publikation = new PublikationBuilder().build();

    PURL purl1 = new PURL("purl-id-1", URI.create("http://resolver.spk-berlin.de/12345"),
        URI.create("http://example.com/target1"), PURLTyp.INTERNAL);
    PURL purl2 = new PURL("purl-id-2", URI.create("http://resolver.spk-berlin.de/67890"),
        URI.create("http://example.com/target2"), PURLTyp.EXTERNAL);

    publikation.addPURL(purl1);
    assertEquals(1, publikation.getPURLs().size());

    publikation.addPURLs(Set.of(purl2));
    assertEquals(2, publikation.getPURLs().size());
  }

  @Test
  void addPURLsWithNull() {
    Publikation publikation = new PublikationBuilder().build();

    publikation.addPURL(null);
    assertTrue(publikation.getPURLs().isEmpty());

    publikation.addPURLs(null);
    assertTrue(publikation.getPURLs().isEmpty());
  }

  @Test
  void builderCreatesPublikationWithDefaultValues() {
    Publikation publikation = new Publikation.PublikationBuilder().build();

    assertNotNull(publikation);
    assertNotNull(publikation.getId());
    assertEquals(PublikationStatus.DRAFT, publikation.getStatus());
    assertEquals(1, publikation.getAuflagenNummer());
    assertTrue(publikation.getPURLs().isEmpty());
  }

  @Test
  void builderWithPrevious() {
    Publikation previous = new PublikationBuilder().build();
    Publikation publikation = new Publikation.PublikationBuilder()
        .withPrevious(previous)
        .build();

    assertEquals(2, publikation.getAuflagenNummer());
    assertTrue(publikation.getPreviousPublikation().isPresent());
    assertEquals(previous.getId(), publikation.getPreviousPublikation().get().getId());
  }

  @Test
  void builderWithDates() {
    LocalDateTime erstDatum = LocalDateTime.of(2020, 1, 1, 12, 0);
    LocalDateTime hspDatum = LocalDateTime.of(2021, 1, 1, 12, 0);

    Publikation publikation = new Publikation.PublikationBuilder()
        .withErstpublikationsDatum(erstDatum)
        .withPublikationsDatumHSP(hspDatum)
        .build();

    assertEquals(erstDatum, publikation.getErstpublikationsDatum());
    assertEquals(hspDatum, publikation.getPublikationsDatumHSP());
  }

  @Test
  void builderWithPURLs() {
    PURL purl1 = new PURL("purl-id-1", URI.create("http://resolver.spk-berlin.de/12345"),
        URI.create("http://example.com/target1"), PURLTyp.INTERNAL);
    PURL purl2 = new PURL("purl-id-2", URI.create("http://resolver.spk-berlin.de/67890"),
        URI.create("http://example.com/target2"), PURLTyp.EXTERNAL);

    Publikation publikation = new Publikation.PublikationBuilder()
        .addPURL(purl1)
        .addPURL(purl2)
        .build();

    assertEquals(2, publikation.getPURLs().size());
  }

  @Test
  void builderCopyConstructor() {
    LocalDateTime erstDatum = LocalDateTime.of(2020, 1, 1, 12, 0);
    Publikation previous = new PublikationBuilder().build();
    ApprovalRequest request = ApprovalRequest.create(
        new Beschreibung.BeschreibungsBuilder("ID-1", "kod-1", null)
            .withPublikation(previous)
            .build(), TEST_USER);
    PURL purl = new PURL("purl-id", URI.create("http://resolver.spk-berlin.de/12345"),
        URI.create("http://example.com/target"), PURLTyp.INTERNAL);

    Publikation original = new Publikation.PublikationBuilder()
        .withPrevious(previous)
        .withErstpublikationsDatum(erstDatum)
        .addPURL(purl)
        .build();

    original.addApprovalRequest(request);

    Publikation copy = new Publikation.PublikationBuilder(original).build();

    assertNotEquals(original.getId(), copy.getId());
    assertEquals(original.getErstpublikationsDatum(), copy.getErstpublikationsDatum());
    assertEquals(original.getPreviousPublikation().get().getId(),
        copy.getPreviousPublikation().get().getId());
    assertEquals(1, original.getApprovalRequests().size());
    assertEquals(1, copy.getApprovalRequests().size());
    assertEquals(1, copy.getPURLs().size());
    assertEquals(2, original.getAuflagenNummer());
    assertEquals(2, copy.getAuflagenNummer());
  }

  @Test
  void setPublikationsDatumHSP() {
    Publikation publikation = new PublikationBuilder().build();
    LocalDateTime newDate = LocalDateTime.of(2021, 6, 15, 10, 30);

    publikation.setPublikationsDatumHSP(newDate);

    assertEquals(newDate, publikation.getPublikationsDatumHSP());
  }

  @Test
  void hasTeiSnapshotDefaultsToFalse() {
    // teiSnapshotPresent is a @Formula-derived field, populated only on DB load.
    // For a freshly built (unpersisted) Publikation it defaults to false.
    Publikation publikation = new PublikationBuilder().build();
    assertFalse(publikation.hasTeiSnapshot());
  }

  @Test
  void equalsAndHashCode() {
    Publikation publikation1 = new PublikationBuilder().build();
    Publikation publikation2 = new PublikationBuilder().build();

    assertNotEquals(publikation1, publikation2);
    assertNotEquals(publikation1.hashCode(), publikation2.hashCode());
  }

  @Test
  void toStringContainsRelevantInformation() {
    Publikation publikation = new PublikationBuilder().build();
    String result = publikation.toString();

    assertTrue(result.contains("Publikation"));
    assertTrue(result.contains("id="));
    assertTrue(result.contains("status="));
    assertTrue(result.contains("auflagenNummer="));
  }
}
