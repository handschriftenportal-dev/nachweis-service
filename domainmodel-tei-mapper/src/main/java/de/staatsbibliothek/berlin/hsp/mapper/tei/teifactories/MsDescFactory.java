package de.staatsbibliothek.berlin.hsp.mapper.tei.teifactories;

import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Beschreibung;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.BeschreibungsKomponenteKopf;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.GlobaleBeschreibungsKomponente;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.DokumentObjektTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.IdentTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Identifikation;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Identifikator;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.tei_c.ns._1.MsDesc;
import org.tei_c.ns._1.Title;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 07.04.2022.
 * @version 1.0
 */
public class MsDescFactory {

  private MsDescFactory() {
  }

  static void buildWithInitialBeschreibung(MsDesc msDesc, Beschreibung beschreibung) {
    if (msDesc == null || beschreibung == null) {
      return;
    }

    msDesc.setId(beschreibung.getId());
    msDesc.setType(DokumentObjektTyp.HSP_DESCRIPTION.toString());

    if (beschreibung.getBeschreibungsTyp() != null) {
      msDesc.setSubtype(beschreibung.getBeschreibungsTyp().toString());
    }

    if (beschreibung.getVerwaltungsTyp() != null) {
      msDesc.setStatus(beschreibung.getVerwaltungsTyp().name().toLowerCase());
    }

    if (beschreibung.getBeschreibungsSprache() != null) {
      msDesc.setLang(beschreibung.getBeschreibungsSprache()
          .getIdentifikatorByTypeName("ISO_639-1")
          .stream().findFirst()
          .map(Identifikator::getText)
          .orElse(null));
    }

    for (GlobaleBeschreibungsKomponente globaleBeschreibungsKomponente : beschreibung
        .getBeschreibungsStruktur()) {
      if (globaleBeschreibungsKomponente instanceof BeschreibungsKomponenteKopf kopf) {
        Optional<Identifikation> gueltigeSignatur = beschreibung.getGueltigeIdentifikation();

        Set<Identifikation> alternativeIdentifier = kopf
            .getIdentifikationen().stream()
            .filter(i -> !IdentTyp.GUELTIGE_SIGNATUR.equals(i.getIdentTyp()))
            .collect(Collectors.toCollection(LinkedHashSet::new));

        gueltigeSignatur.ifPresent(signatur -> msDesc.setMsIdentifier(
            MsIdentifierFactory.build(signatur, alternativeIdentifier)));

        msDesc.getHeads().stream().findFirst()
            .flatMap(h -> h.getContent().stream().filter(Title.class::isInstance).findFirst())
            .ifPresent(title -> {
              ((Title) title).getContent().clear();
              ((Title) title).getContent().add(kopf.getTitel());
            });
      }
    }
  }
}
