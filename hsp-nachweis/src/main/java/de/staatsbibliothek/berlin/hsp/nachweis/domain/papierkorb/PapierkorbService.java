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

package de.staatsbibliothek.berlin.hsp.nachweis.domain.papierkorb;

import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Beschreibung;
import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.KulturObjektDokument;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Bearbeiter;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.GeloeschtesDokument;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.BeschreibungsKomponentenTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.DokumentObjektTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.IdentTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Identifikation;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.PURL;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.PURLTyp;
import io.quarkus.panache.common.Sort;
import io.quarkus.panache.common.Sort.Direction;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import javax.enterprise.context.ApplicationScoped;
import javax.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

/**
 * @author michael.hintersonnleitner@sbb.spk-berlin.de
 * @since 29.11.22
 */

@Slf4j
@ApplicationScoped
public class PapierkorbService implements PapierkorbBoundary {

  private static final String DELIMITER_ALT_SIG = "; ";

  private final GeloeschtesDokumentRepository geloeschtesDokumentRepository;

  public PapierkorbService(GeloeschtesDokumentRepository geloeschtesDokumentRepository) {
    this.geloeschtesDokumentRepository = geloeschtesDokumentRepository;
  }

  @Override
  @Transactional(rollbackOn = Exception.class)
  public String erzeugeGeloeschtesDokument(KulturObjektDokument kod, Bearbeiter bearbeiter) {
    Objects.requireNonNull(kod, "kod is required");
    Objects.requireNonNull(bearbeiter, "bearbeiter is required");

    log.info("erzeugeGeloeschtesDokument: kod.id={}, bearbeiter.id={}", kod.getId(), bearbeiter.getId());

    String altSignaturen = kod.getAlternativeIdentifikationen().stream()
        .filter(identifikation -> IdentTyp.ALTSIGNATUR == identifikation.getIdentTyp())
        .map(Identifikation::getIdent)
        .collect(Collectors.joining(DELIMITER_ALT_SIG));

    GeloeschtesDokument geloeschtesDokument = GeloeschtesDokument.builder()
        .withDokumentId(kod.getId())
        .withDokumentObjektTyp(DokumentObjektTyp.HSP_OBJECT)
        .withGueltigeSignatur(kod.getGueltigeSignatur())
        .withAlternativeSignaturen(altSignaturen)
        .withInternePurls(joinInternePURLs(kod.getPURLs()))
        .withBesitzerId(kod.getGueltigeIdentifikation().getBesitzer().getId())
        .withBesitzerName(kod.getGueltigeIdentifikation().getBesitzer().getName())
        .withAufbewahrungsortId(kod.getGueltigeIdentifikation().getAufbewahrungsOrt().getId())
        .withAufbewahrungsortName(kod.getGueltigeIdentifikation().getAufbewahrungsOrt().getName())
        .withTeiXML(kod.getTeiXML())
        .withLoeschdatum(LocalDateTime.now())
        .withBearbeiterId(bearbeiter.getId())
        .withBearbeiterName(bearbeiter.getName())
        .build();

    geloeschtesDokumentRepository.save(geloeschtesDokument);

    return geloeschtesDokument.getId();
  }

  @Override
  @Transactional(rollbackOn = Exception.class)
  public String erzeugeGeloeschtesDokument(Beschreibung beschreibung, Bearbeiter bearbeiter) {
    Objects.requireNonNull(beschreibung, "beschreibung is required");
    Objects.requireNonNull(bearbeiter, "bearbeiter is required");

    log.info("erzeugeGeloeschtesDokument: beschreibung.id={}, bearbeiter.id={}", beschreibung.getId(),
        bearbeiter.getId());

    String altSignaturen = beschreibung.getBeschreibungsStruktur()
        .stream()
        .filter(komponente -> BeschreibungsKomponentenTyp.KOPF == komponente.getTyp())
        .findFirst()
        .map(k -> k.getIdentifikationen().stream()
            .filter(i -> IdentTyp.ALTSIGNATUR == i.getIdentTyp())
            .map(Identifikation::getIdent)
            .collect(Collectors.joining(DELIMITER_ALT_SIG))
        )
        .orElse("");

    Identifikation gueltigeIdentifikation = beschreibung.getGueltigeIdentifikation().orElseThrow();

    GeloeschtesDokument geloeschtesDokument = GeloeschtesDokument.builder()
        .withDokumentId(beschreibung.getId())
        .withDokumentObjektTyp(beschreibung.getDokumentObjektTyp())
        .withGueltigeSignatur(gueltigeIdentifikation.getIdent())
        .withAlternativeSignaturen(altSignaturen)
        .withInternePurls(joinInternePURLs(beschreibung.getPURLs()))
        .withBesitzerId(gueltigeIdentifikation.getBesitzer().getId())
        .withBesitzerName(gueltigeIdentifikation.getBesitzer().getName())
        .withAufbewahrungsortId(gueltigeIdentifikation.getAufbewahrungsOrt().getId())
        .withAufbewahrungsortName(gueltigeIdentifikation.getAufbewahrungsOrt().getName())
        .withTeiXML(beschreibung.getTeiXML())
        .withLoeschdatum(LocalDateTime.now())
        .withBearbeiterId(bearbeiter.getId())
        .withBearbeiterName(bearbeiter.getName())
        .build();

    geloeschtesDokumentRepository.save(geloeschtesDokument);
    return geloeschtesDokument.getId();
  }

  @Override
  @Transactional(rollbackOn = Exception.class)
  public List<GeloeschtesDokument> findAllGeloeschteDokumente() {
    return geloeschtesDokumentRepository
        .findAll(Sort.by("loeschdatum", Direction.Descending))
        .list();
  }

  String joinInternePURLs(Set<PURL> purls) {
    return purls.stream()
        .filter(purl -> PURLTyp.INTERNAL == purl.getTyp())
        .map(PURL::getPurl)
        .filter(Objects::nonNull)
        .map(URI::toASCIIString)
        .collect(Collectors.joining(" "));
  }

}
