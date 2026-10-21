package de.staatsbibliothek.berlin.hsp.mapper.tei.domainfactories;

import static de.staatsbibliothek.berlin.hsp.messaging.common.TEICommon.getContentAsString;

import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.IdentTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Identifikation;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Identifikation.IdentifikationBuilder;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
import de.staatsbibliothek.berlin.hsp.mapper.api.exceptions.HSPMapperException;
import de.staatsbibliothek.berlin.hsp.mapper.api.factory.ElementFactory;
import de.staatsbibliothek.berlin.hsp.mapper.tei.TEIQuery;
import de.staatsbibliothek.berlin.hsp.messaging.common.TEICommon;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import org.tei_c.ns._1.Idno;
import org.tei_c.ns._1.MsDesc;
import org.tei_c.ns._1.MsIdentifier;
import org.tei_c.ns._1.TEI;

/**
 * @author Piotr.Czarnecki@sbb.spk-berlin.de
 * @since 22.04.20
 */
public class IdentifikationFactory implements ElementFactory<Identifikation, MsDesc, TEI> {

  /**
   * @param msDesc JAXB Element class which contains the description
   * @param tei    JAXB Element
   * @return Business Objekt which represents the Minimaldaten
   * @throws HSPMapperException Class
   */
  @Override
  public Identifikation build(MsDesc msDesc, TEI tei) throws HSPMapperException {

    MsIdentifier msIdentifier = msDesc.getMsIdentifier();
    NormdatenReferenz besitzer = findBesitzer(msIdentifier);
    NormdatenReferenz aufbewahrungsOrt = findAufbewahrungsOrt(msIdentifier);

    String ident = getIdent(msIdentifier);
    String sortKey = getSortKey(msIdentifier);

    Set<String> sammlungIds = findSammlungIds(msIdentifier);

    return new IdentifikationBuilder()
        .withId(UUID.randomUUID().toString())
        .withAufbewahrungsOrt(aufbewahrungsOrt)
        .withBesitzer(besitzer)
        .withIdent(ident)
        .withIdentTyp(IdentTyp.GUELTIGE_SIGNATUR)
        .withSammlungIDs(sammlungIds)
        .withSortKey(sortKey)
        .build();
  }

  /**
   * @param msIdentifier JAXB Element
   * @return the Signature as Ident of MsDesc/msIdentifier/idno
   */

  String getIdent(MsIdentifier msIdentifier) {

    StringBuilder stringBuilder = new StringBuilder();

    Optional.ofNullable(msIdentifier).map(MsIdentifier::getIdnos).stream()
        .flatMap(Collection::stream)
        .forEach(i -> stringBuilder.append(getContentAsString(i.getContent(), " ; ")));

    return stringBuilder.toString();
  }

  private String getSortKey(MsIdentifier msIdentifier) {
    return Optional.ofNullable(msIdentifier)
        .map(MsIdentifier::getIdnos)
        .stream()
        .flatMap(Collection::stream)
        .map(Idno::getSortKey)
        .filter(Objects::nonNull)
        .findFirst()
        .orElse(null);
  }

  NormdatenReferenz findBesitzer(MsIdentifier msIdentifier) {

    final var koerperschaftAtomicReference = new AtomicReference<NormdatenReferenz>();

    Optional.ofNullable(msIdentifier).map(MsIdentifier::getRepository)
        .ifPresent(repository -> koerperschaftAtomicReference.set(new NormdatenReferenz(
            repository.getKey(),
            TEICommon.getContentAsString(repository.getContent()),
            TEIQuery.findGNDIDinListOfStrings(repository.getReves()))));

    return koerperschaftAtomicReference.get();
  }


  NormdatenReferenz findAufbewahrungsOrt(MsIdentifier msIdentifier) {

    final AtomicReference<NormdatenReferenz> ort = new AtomicReference<>();

    Optional.ofNullable(msIdentifier).map(MsIdentifier::getSettlement)
        .ifPresent(settlement -> ort.set(new NormdatenReferenz(settlement.getKey(),
            TEICommon.getContentAsString(settlement.getContent()),
            TEIQuery.findGNDIDinListOfStrings(settlement.getReves()))));

    return ort.get();
  }

  Set<String> findSammlungIds(MsIdentifier msIdentifier) {
    return Optional.ofNullable(msIdentifier)
        .map(MsIdentifier::getCollections)
        .stream()
        .flatMap(java.util.Collection::stream)
        .map(collection -> TEICommon.getContentAsString(collection.getContent()))
        .filter(Objects::nonNull)
        .collect(Collectors.toSet());
  }
}
