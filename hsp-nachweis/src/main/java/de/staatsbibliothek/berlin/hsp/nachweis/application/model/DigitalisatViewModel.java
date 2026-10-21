package de.staatsbibliothek.berlin.hsp.nachweis.application.model;

import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Digitalisat;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
import java.io.Serial;
import java.io.Serializable;
import java.net.URI;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

/**
 * @author konradigitalisat.eichstaedt@sbb.spk-berlin.de on 06.10.2020.
 * @version 1.0
 */

@Getter
@Setter
@Builder
@NoArgsConstructor
@ToString
@AllArgsConstructor
@Slf4j
public class DigitalisatViewModel implements Serializable {

  public static final String DATE_TIME_FORMAT = "dd.MM.yyyy";
  @Serial
  private static final long serialVersionUID = -2958547584046678919L;
  private String id;
  private String manifestURL;
  private String alternativeUrl;
  private String thumbnail;
  private NormdatenReferenz ort;
  private NormdatenReferenz einrichtung;
  private LocalDate digitalisierungsdatum;
  private LocalDate erstellungsdatum;
  private LocalDate publikationsDatum;
  @Default
  private List<NormdatenReferenz> digitalisatTypKonzepte = new ArrayList<>();

  public static DigitalisatViewModel map(Digitalisat digitalisat) {
    log.info("Map Digitalisat {}", digitalisat);
    if (Objects.isNull(digitalisat)) {
      return null;
    }
    List<NormdatenReferenz> list = new ArrayList<>(digitalisat.getDigitalisatsKonzepte());
    list.sort(Comparator.comparing(NormdatenReferenz::getTypeName));
    return DigitalisatViewModel.builder()
        .id(digitalisat.getId())
        .manifestURL(
            Optional.ofNullable(digitalisat.getManifestURL()).map(URI::toASCIIString).orElse(null))
        .thumbnail(
            Optional.ofNullable(digitalisat.getThumbnailURL()).map(URI::toASCIIString).orElse(null))
        .alternativeUrl(Optional.ofNullable(digitalisat.getAlternativeURL()).map(URI::toASCIIString)
            .orElse(null))
        .erstellungsdatum(digitalisat.getErstellungsDatum())
        .digitalisierungsdatum(digitalisat.getDigitalisierungsDatum())
        .publikationsDatum(digitalisat.getPublikationsDatum())
        .ort(digitalisat.getEntstehungsOrt())
        .einrichtung(digitalisat.getDigitalisierendeEinrichtung())
        .digitalisatTypKonzepte(digitalisat.getDigitalisatsKonzepte().stream()
            .sorted(Comparator.comparing(NormdatenReferenz::getTypeName))
            .toList())
        .build();
  }

  /**
   * Returns whether this and the other Digitalisat reference the same source — via the manifest or the alternative URL.
   * Used for de-duplication during the Digitalisat import; deliberately not {@code equals}, since only the URLs are
   * compared (not id and the remaining fields).
   */
  public boolean hasSameUrl(DigitalisatViewModel other) {
    if (other == null) {
      return false;
    }
    return (manifestURL != null && manifestURL.equals(other.manifestURL))
        || (alternativeUrl != null && alternativeUrl.equals(other.alternativeUrl));
  }

  /**
   * Like {@link #hasSameUrl(DigitalisatViewModel)}, but against a domain {@link Digitalisat} — useful for
   * de-duplication during the import without spreading the URL-comparison logic across the service.
   */
  public boolean hasSameUrl(Digitalisat other) {
    return other != null && hasSameUrl(map(other));
  }

  /**
   * Builds a domain {@link Digitalisat} from this view model (counterpart to {@link #map(Digitalisat)}). The creation
   * date is set to the current day.
   */
  public Digitalisat toDigitalisat() {
    return Digitalisat.DigitalisatBuilder()
        .withID(id)
        .withManifest(manifestURL)
        .withAlternativeUrl(alternativeUrl)
        .withThumbnail(thumbnail)
        .withEntstehungsort(ort)
        .withDigitalisierendeEinrichtung(einrichtung)
        .withDigitalisierungsdatum(digitalisierungsdatum)
        .withErstellungsdatum(LocalDate.now(ZoneId.systemDefault()))
        .withPublikationsdatum(publikationsDatum)
        .addDigitalisatsKonzepte(new HashSet<>(digitalisatTypKonzepte))
        .build();
  }
}
