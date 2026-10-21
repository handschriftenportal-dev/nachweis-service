package de.staatsbibliothek.berlin.hsp.nachweis.domain.sperre;

import static de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.SperreDokumentTyp.BESCHREIBUNG;
import static de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.SperreDokumentTyp.KOD;

import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Bearbeiter;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Sperre;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Sperre.SperreBuilder;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.SperreEintrag;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.SperreTyp;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.bearbeiter.BearbeiterRepository;
import jakarta.transaction.Transactional;
import jakarta.transaction.Transactional.TxType;
import java.time.LocalDateTime;
import java.util.Random;

/**
 * @author Piotr.Czarnecki@sbb.spk-berlin.de
 * @since 01.09.21
 */
public class SperreCommon {

  public static final String USER_NAME = "b-pc101";

  @Transactional(TxType.REQUIRES_NEW)
  Sperre getTestSperre(BearbeiterRepository bearbeiterRepository, SperreTyp sperreTyp) {
    int start = new Random().nextInt();
    String uniqueUserName = USER_NAME;
    Bearbeiter bearbeiter = bearbeiterRepository.findById(uniqueUserName);
    if (bearbeiter == null) {
      bearbeiter = new Bearbeiter(uniqueUserName, uniqueUserName);
      bearbeiter = bearbeiterRepository.save(bearbeiter);
    }

      SperreBuilder sperreBuilder =
              Sperre.newBuilder()
                      .withBearbeiter(bearbeiter)
                      .withSperreGrund("Import")
                      .withSperreTyp(sperreTyp)
                      .withStartDatum(LocalDateTime.now());

    for (int i = start, k = start + 10; i < k; i++) {
        sperreBuilder.addSperreEintrag(
                new SperreEintrag("ckm-" + i, (i % 2 == 0) ? BESCHREIBUNG : KOD));
    }
    return sperreBuilder.build();
  }
}
