package de.staatsbibliothek.berlin.hsp.mapper.tei.teifactories;

import static de.staatsbibliothek.berlin.hsp.mapper.tei.TEIValues.ROLE_AUTHOR;

import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Lizenz;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
import de.staatsbibliothek.berlin.hsp.messaging.common.TEICommon;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import org.tei_c.ns._1.Availability;
import org.tei_c.ns._1.Date;
import org.tei_c.ns._1.FileDesc;
import org.tei_c.ns._1.Licence;
import org.tei_c.ns._1.P;
import org.tei_c.ns._1.PersName;
import org.tei_c.ns._1.RespStmt;
import org.tei_c.ns._1.TitleStmt;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 07.04.2022.
 * @version 1.0
 */
public class FileDescFactory {

  static void buildWithLicenseAndAutor(Optional<FileDesc> fileDesc, Lizenz lizenz,
      Set<NormdatenReferenz> autoren) {

    if (fileDesc.isPresent()) {

      Optional<RespStmt> respStmt = Optional.empty();

      try {
        respStmt = TEICommon.findFirst(RespStmt.class, fileDesc.get());
      } catch (Exception e) {
        //No Handling needed
      } finally {
        if (respStmt.isEmpty()) {
          respStmt = Optional.of(new RespStmt());
        }
      }

      if (fileDesc.get().getTitleStmt() == null) {
        TitleStmt titleStmt = new TitleStmt();
        titleStmt.getAuthorsAndEditorsAndRespStmts().add(respStmt.get());
        fileDesc.get().setTitleStmt(titleStmt);
      }

      Optional.ofNullable(fileDesc.get().getPublicationStmt()).ifPresent(
          publicationStmt -> publicationStmt.getPublishersAndDistributorsAndAuthorities()
              .removeIf(pubObject -> pubObject instanceof Date));

      updateLicence(fileDesc.get(), lizenz);

      if (!autoren.isEmpty()) {
        respStmt.get().getRespsAndNamesAndOrgNames().removeIf(resp -> resp instanceof PersName);
        addAutorenToRespsStatement(autoren, respStmt.get());
      }

    }
  }

  static void addAutorenToRespsStatement(Set<NormdatenReferenz> autoren, RespStmt respStmtNew) {
    respStmtNew.getRespsAndNamesAndOrgNames().addAll(autoren.stream().map(autor -> {
      PersName persName = new PersName();
      persName.setKey(autor.getId());
      persName.getRoles().add(ROLE_AUTHOR);
      if (autor.getGndID() != null) {
        persName.getReves().add(autor.getGndID());
      }
      persName.getContent().add(autor.getName());
      return persName;
    }).toList());
  }

  static void updateLicence(FileDesc fileDesc, Lizenz lizenz) {
    if (Objects.isNull(fileDesc) || Objects.isNull(fileDesc.getPublicationStmt()) || Objects.isNull(
        lizenz)) {
      return;
    }

    Optional<Licence> licence = fileDesc.getPublicationStmt()
        .getPublishersAndDistributorsAndAuthorities().stream()
        .filter(Availability.class::isInstance).findFirst().map(Availability.class::cast).flatMap(
            availability -> availability.getLicencesAndPSAndAbs().stream()
                .filter(Licence.class::isInstance).findFirst().map(Licence.class::cast));

    if (licence.isPresent()) {
      licence.get().getTargets().clear();
      if (Objects.nonNull(lizenz.getUris()) && !lizenz.getUris().isEmpty()) {
        licence.get().getTargets().addAll(lizenz.getUris());
      }
      if (Objects.nonNull(lizenz.getBeschreibungsText())) {
        Optional<P> firstP = licence.get().getContent().stream().filter(P.class::isInstance)
            .findFirst().map(P.class::cast);

        if (firstP.isPresent()) {
          firstP.get().getContent().clear();
          firstP.get().getContent().add(lizenz.getBeschreibungsText());
        } else {
          P p = new P();
          p.getContent().add(lizenz.getBeschreibungsText());
          licence.get().getContent().add(p);
        }
      }
    }
  }

}
