package de.staatsbibliothek.berlin.hsp.mapper.tei.teifactories;

import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Digitalisat;
import de.staatsbibliothek.berlin.hsp.mapper.tei.TEIValues;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.tei_c.ns._1.Bibl;
import org.tei_c.ns._1.Date;
import org.tei_c.ns._1.Idno;
import org.tei_c.ns._1.OrgName;
import org.tei_c.ns._1.PlaceName;
import org.tei_c.ns._1.Ref;
import org.tei_c.ns._1.Surrogates;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 06.10.2020.
 * @version 1.0
 */
public class SurrogatesFactory {

  public static Surrogates build(List<Digitalisat> digitalisate) {

    Surrogates surrogates = new Surrogates();

    if (digitalisate != null && !digitalisate.isEmpty()) {
      AtomicInteger counter = new AtomicInteger(1);
      digitalisate.forEach(digitalisat -> {
        var currentCounter = counter.getAndIncrement();
        Bibl bibl = new Bibl();
        bibl.setId(
            digitalisat.getId().contains("HSP") ? digitalisat.getId() : TEIValues.UUID_PREFIX + digitalisat.getId());

        if (currentCounter == 1) {
          bibl.setDefault("true");
        }

        if (digitalisat.getId() != null && !digitalisat.getId().isEmpty()) {
          Idno idno = new Idno();
          idno.getContent().add(digitalisat.getId());
          bibl.getContent().add(idno);
        }

        if (digitalisat.getManifestURL() != null && !digitalisat.getManifestURL().toASCIIString()
            .isEmpty()) {
          Ref ref = new Ref();
          ref.getTargets().add(digitalisat.getManifestURL().toASCIIString());
          ref.setType(TEIValues.REF_TYPE_MANIFEST);
          bibl.getContent().add(ref);
        }

        if (digitalisat.getAlternativeURL() != null && !digitalisat.getAlternativeURL()
            .toASCIIString()
            .isEmpty()) {
          Ref ref = new Ref();
          ref.setType(TEIValues.REF_TYPE_OTHER);
          ref.getTargets().add(digitalisat.getAlternativeURL().toASCIIString());
          bibl.getContent().add(ref);
        }

        if (digitalisat.getThumbnailURL() != null && !digitalisat.getThumbnailURL().toASCIIString()
            .isEmpty()) {
          Ref ref = new Ref();
          ref.setType(TEIValues.REF_TYPE_THUMBNAIL);
          ref.getTargets().add(digitalisat.getThumbnailURL().toASCIIString());
          bibl.getContent().add(ref);
        }

        if (digitalisat.getDigitalisatsKonzepte() != null) {
          digitalisat.getDigitalisatsKonzepte().forEach(normdatenReferenz -> {
            Ref ref = new Ref();
            ref.setType(normdatenReferenz.getTypeName());
            ref.getTargets().add(normdatenReferenz.getUri());
            bibl.getContent().add(ref);
          });
        }

        if (digitalisat.getDigitalisierungsDatum() != null) {
          Date datum = new Date();
          datum.setWhen(
              digitalisat.getDigitalisierungsDatum()
                  .format(DateTimeFormatter.ofPattern(TEIValues.LOCAL_DATE_FORMAT)));
          datum.setType(TEIValues.DIGITIZED);
          datum.getContent().add(
              digitalisat.getDigitalisierungsDatum()
                  .format(DateTimeFormatter.ofPattern(TEIValues.LOCAL_DATE_FORMAT_LABEL)));
          bibl.getContent().add(datum);
        }

        if (digitalisat.getErstellungsDatum() != null) {
          Date datum = new Date();
          datum.setWhen(
              digitalisat.getErstellungsDatum()
                  .format(DateTimeFormatter.ofPattern(TEIValues.LOCAL_DATE_FORMAT)));
          datum.setType(TEIValues.DATE_ISSUED);
          datum.getContent().add(
              digitalisat.getErstellungsDatum()
                  .format(DateTimeFormatter.ofPattern(TEIValues.LOCAL_DATE_FORMAT_LABEL)));
          bibl.getContent().add(datum);
        }

        if (digitalisat.getPublikationsDatum() != null) {
          Date datum = new Date();
          datum.setWhen(
              digitalisat.getPublikationsDatum()
                  .format(DateTimeFormatter.ofPattern(TEIValues.LOCAL_DATE_FORMAT)));
          datum.setType(TEIValues.DATE_PUBLISHED);
          datum.getContent().add(
              digitalisat.getPublikationsDatum()
                  .format(DateTimeFormatter.ofPattern(TEIValues.LOCAL_DATE_FORMAT_LABEL)));
          bibl.getContent().add(datum);
        }

        if (digitalisat.getEntstehungsOrt() != null) {
          PlaceName placeName = new PlaceName();
          placeName.getReves().add(digitalisat.getEntstehungsOrt().getId());
          placeName.getContent().add(digitalisat.getEntstehungsOrt().getName());
          placeName.setType(TEIValues.TYPE_DIGITIZING);
          bibl.getContent().add(placeName);
        }

        if (digitalisat.getDigitalisierendeEinrichtung() != null) {
          OrgName orgName = new OrgName();
          orgName.getReves().add(digitalisat.getDigitalisierendeEinrichtung().getId());
          orgName.getContent().add(digitalisat.getDigitalisierendeEinrichtung().getName());
          orgName.setType(TEIValues.TYPE_DIGITIZING);
          bibl.getContent().add(orgName);
        }

        if (bibl.getContent().size() > 0) {
          surrogates.getContent().add(bibl);
        }

      });

    }

    return surrogates;
  }

}
