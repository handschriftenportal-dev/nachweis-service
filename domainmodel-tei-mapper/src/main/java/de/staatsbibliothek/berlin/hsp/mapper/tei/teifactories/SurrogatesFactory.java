/*
 * MIT License
 *
 * Copyright (c) 2023 Staatsbibliothek zu Berlin - Preußischer Kulturbesitz
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package de.staatsbibliothek.berlin.hsp.mapper.tei.teifactories;

import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Digitalisat;
import de.staatsbibliothek.berlin.hsp.mapper.tei.TEIValues;
import java.time.format.DateTimeFormatter;
import java.util.List;
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

      digitalisate.stream().forEach(digitalisat -> {

        Bibl bibl = new Bibl();

        if (digitalisat.getId() != null && !digitalisat.getId().isEmpty()) {
          Idno idno = new Idno();
          idno.getContent().add(digitalisat.getId());
          bibl.getContent().add(idno);
        }

        if (digitalisat.getManifestURL() != null && !digitalisat.getManifestURL().toASCIIString().isEmpty()) {
          Ref ref = new Ref();
          ref.getTargets().add(digitalisat.getManifestURL().toASCIIString());
          if (digitalisat.getTyp() != null) {
            ref.setSubtype(digitalisat.getTyp().toString());
            ref.setType(TEIValues.REF_TYPE_MANIFEST);
          }
          bibl.getContent().add(ref);
        }

        if (digitalisat.getAlternativeURL() != null && !digitalisat.getAlternativeURL().toASCIIString()
            .isEmpty()) {
          Ref ref = new Ref();
          if (digitalisat.getTyp() != null) {
            ref.setSubtype(digitalisat.getTyp().toString());
            ref.setType(TEIValues.REF_TYPE_OTHER);
          }

          ref.getTargets().add(digitalisat.getAlternativeURL().toASCIIString());
          bibl.getContent().add(ref);
        }

        if (digitalisat.getThumbnailURL() != null && !digitalisat.getThumbnailURL().toASCIIString().isEmpty()) {
          Ref ref = new Ref();
          ref.setType(TEIValues.REF_TYPE_THUMBNAIL);
          ref.getTargets().add(digitalisat.getThumbnailURL().toASCIIString());
          bibl.getContent().add(ref);
        }

        if (digitalisat.getDigitalisierungsDatum() != null) {
          Date datum = new Date();
          datum.setWhen(
              digitalisat.getDigitalisierungsDatum().format(DateTimeFormatter.ofPattern(TEIValues.LOCAL_DATE_FORMAT)));
          datum.setType(TEIValues.DATE_DIGITIZED);
          datum.getContent().add(
              digitalisat.getDigitalisierungsDatum()
                  .format(DateTimeFormatter.ofPattern(TEIValues.LOCAL_DATE_FORMAT_LABEL)));
          bibl.getContent().add(datum);
        }

        if (digitalisat.getErstellungsDatum() != null) {
          Date datum = new Date();
          datum.setWhen(
              digitalisat.getErstellungsDatum().format(DateTimeFormatter.ofPattern(TEIValues.LOCAL_DATE_FORMAT)));
          datum.setType(TEIValues.DATE_ISSUED);
          datum.getContent().add(
              digitalisat.getErstellungsDatum().format(DateTimeFormatter.ofPattern(TEIValues.LOCAL_DATE_FORMAT_LABEL)));
          bibl.getContent().add(datum);
        }

        if (digitalisat.getEntstehungsOrt() != null) {
          PlaceName placeName = new PlaceName();
          placeName.getReves().add(digitalisat.getEntstehungsOrt().getId());
          placeName.getContent().add(digitalisat.getEntstehungsOrt().getName());
          bibl.getContent().add(placeName);
        }

        if (digitalisat.getDigitalisierendeEinrichtung() != null) {
          OrgName orgName = new OrgName();
          orgName.getReves().add(digitalisat.getDigitalisierendeEinrichtung().getId());
          orgName.getContent().add(digitalisat.getDigitalisierendeEinrichtung().getName());

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
