package de.staatsbibliothek.berlin.hsp.mapper.tei.domainfactories;


import de.staatsbibliothek.berlin.hsp.domainmodel.entities.BeschreibungsKomponenteKopf;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.BeschreibungsKomponenteKopf.BeschreibungsKomponenteKopfBuilder;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Grundsprache;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.KulturObjektTyp;
import de.staatsbibliothek.berlin.hsp.mapper.api.exceptions.HSPMapperException;
import de.staatsbibliothek.berlin.hsp.mapper.api.factory.ElementFactory;
import de.staatsbibliothek.berlin.hsp.messaging.common.TEICommon;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import org.tei_c.ns._1.Index;
import org.tei_c.ns._1.MsDesc;
import org.tei_c.ns._1.ObjectDesc;
import org.tei_c.ns._1.PhysDesc;
import org.tei_c.ns._1.TEI;
import org.tei_c.ns._1.Term;

/**
 * @author Piotr.Czarnecki@sbb.spk-berlin.de
 * @since 22.04.20
 */
public class BeschreibungsKomponenteKopfFactory implements
    ElementFactory<BeschreibungsKomponenteKopf, MsDesc, TEI> {

  //msDesc/head/title/text()
  static String buildTitleString(MsDesc local) {

    final AtomicReference<ArrayList<Object>> allTitles = new AtomicReference<>(
        new ArrayList<>());
    if (Objects.nonNull(local)) {
      local.getHeads()
          .stream()
          .flatMap(head -> head.getContent().stream())
          .filter(index -> index instanceof Index)
          .filter(index -> ((Index) index).getIndexName().equals("norm_title"))
          .findFirst()
          .map(object -> (Index) object)
          .flatMap(normTitle -> normTitle.getTermsAndIndices()
              .stream()
              .findFirst()
              .map(object -> (Term) object))
          .ifPresent(
              term -> allTitles.set((ArrayList<Object>) term.getContent()));
    }

    return TEICommon.getContentAsString(allTitles.get());
  }

  @Override
  public BeschreibungsKomponenteKopf build(MsDesc local, TEI global)
      throws HSPMapperException {

    KulturObjektTyp kulturObjektTyp = findKulturObjektTyp(local);
    Grundsprache grundsprache = new GrundspracheFactory().build(local, global);

    return new BeschreibungsKomponenteKopfBuilder()
        .withId(UUID.randomUUID().toString())
        .withKulturObjektTyp(kulturObjektTyp)
        .withTitel(buildTitleString(local))
        .withGrundsprachen(
            grundsprache != null ? new HashSet<>(
                grundsprache.getNormdatenReferenzen())
                : new HashSet<>())
        .build();
  }

  KulturObjektTyp findKulturObjektTyp(MsDesc local) {
    final AtomicReference<KulturObjektTyp> kulturObjektTyp = new AtomicReference<>();

    local.getMsContentsAndPhysDescsAndHistories()
        .stream()
        .filter(o -> o instanceof PhysDesc)
        .map(o -> (PhysDesc) o)
        .map(PhysDesc::getObjectDesc)
        .filter(Objects::nonNull)
        .map(ObjectDesc::getForm).findFirst()
        .ifPresent(s -> kulturObjektTyp.set(
            KulturObjektTyp.getKulturObjektTypFromString(s)));

    return kulturObjektTyp.get();
  }
}
