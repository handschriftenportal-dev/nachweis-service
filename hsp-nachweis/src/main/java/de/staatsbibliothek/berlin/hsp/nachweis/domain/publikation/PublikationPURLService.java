package de.staatsbibliothek.berlin.hsp.nachweis.domain.publikation;

import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Beschreibung;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Publikation;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.PURL;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.purl.PURLBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.purl.PURLException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 * Creates internal PURLs for a Beschreibung during the publication workflow.
 *
 * <p>Should only be used by {@link PublikationsService}.
 */
@Slf4j
@ApplicationScoped
public class PublikationPURLService {

  private static final String BESCHREIBUNG_MUST_NOT_BE_NULL = "Beschreibung must not be null";

  private PURLBoundary purlBoundary;
  private boolean purlAutogenerateEnabled;

  protected PublikationPURLService() {
  }

  @Inject
  public PublikationPURLService(
      PURLBoundary purlBoundary,
      @ConfigProperty(name = "purl.autogenerate.enabled", defaultValue = "true")
      boolean purlAutogenerateEnabled) {
    this.purlBoundary = purlBoundary;
    this.purlAutogenerateEnabled = purlAutogenerateEnabled;
  }

  /**
   * Creates an internal PURL for the given Beschreibung and attaches it to its Publikation, unless
   * auto-generation is disabled or an internal PURL already exists.
   *
   * @param beschreibung the Beschreibung to create a PURL for
   * @throws PURLException if Publikation is missing or PURL creation fails
   */
  public void addNewPURLsToPublication(Beschreibung beschreibung) throws PURLException {
    Objects.requireNonNull(beschreibung, BESCHREIBUNG_MUST_NOT_BE_NULL);

    if (!this.purlAutogenerateEnabled) {
      log.info("PURL auto-generation is disabled - skipping PURL creation for Beschreibung {}",
          beschreibung.getId());
      return;
    }

    Publikation publikation = beschreibung.getPublikation();
    if (publikation == null) {
      throw new PURLException("No Publikation found for Beschreibung " + beschreibung.getId());
    }

    if (publikation.hasInternalPURL()) {
      log.info("Internal PURL already exists for Beschreibung {}", beschreibung.getId());
      return;
    }

    try {
      PURL purl = purlBoundary
          .createNewPURL(beschreibung.getId(), beschreibung.getDokumentObjektTyp());
      publikation.addPURL(purl);
      log.info("Created internal PURL for publishing Beschreibung {} (type: {})",
          beschreibung.getId(), beschreibung.getDokumentObjektTyp());
    } catch (Exception e) {
      log.error("Error creating PURL for Beschreibung {}", beschreibung.getId(), e);
      throw new PURLException("Error creating PURL for Beschreibung " + beschreibung.getId(), e);
    }
  }
}
