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
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

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

        log.info(
                "erzeugeGeloeschtesDokument: kod.id={}, bearbeiter.id={}", kod.getId(), bearbeiter.getId());

        String altSignaturen =
                kod.getAlternativeIdentifikationen().stream()
                        .filter(identifikation -> IdentTyp.ALTSIGNATUR == identifikation.getIdentTyp())
                        .map(Identifikation::getIdent)
                        .collect(Collectors.joining(DELIMITER_ALT_SIG));

        GeloeschtesDokument geloeschtesDokument =
                GeloeschtesDokument.builder()
                        .withDokumentId(kod.getId())
                        .withDokumentObjektTyp(DokumentObjektTyp.HSP_OBJECT)
                        .withGueltigeSignatur(kod.getGueltigeSignatur())
                        .withAlternativeSignaturen(altSignaturen)
                        .withInternePurls(joinInternePURLs(kod.getPURLs()))
                        .withBesitzerId(kod.getGueltigeIdentifikation().getBesitzer().getId())
                        .withBesitzerName(kod.getGueltigeIdentifikation().getBesitzer().getName())
                        .withAufbewahrungsortId(kod.getGueltigeIdentifikation().getAufbewahrungsOrt().getId())
                        .withAufbewahrungsortName(
                                kod.getGueltigeIdentifikation().getAufbewahrungsOrt().getName())
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

        log.info(
                "erzeugeGeloeschtesDokument: beschreibung.id={}, bearbeiter.id={}",
                beschreibung.getId(),
                bearbeiter.getId());

        String altSignaturen =
                beschreibung.getBeschreibungsStruktur().stream()
                        .filter(komponente -> BeschreibungsKomponentenTyp.KOPF == komponente.getTyp())
                        .findFirst()
                        .map(
                                k ->
                                        k.getIdentifikationen().stream()
                                                .filter(i -> IdentTyp.ALTSIGNATUR == i.getIdentTyp())
                                                .map(Identifikation::getIdent)
                                                .collect(Collectors.joining(DELIMITER_ALT_SIG)))
                        .orElse("");

        Identifikation gueltigeIdentifikation = beschreibung.getGueltigeIdentifikation().orElseThrow();

        GeloeschtesDokument geloeschtesDokument =
                GeloeschtesDokument.builder()
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
