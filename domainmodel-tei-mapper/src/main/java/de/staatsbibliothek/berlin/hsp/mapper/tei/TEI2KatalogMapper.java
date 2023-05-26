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

package de.staatsbibliothek.berlin.hsp.mapper.tei;

import static de.staatsbibliothek.berlin.hsp.mapper.tei.TEIValues.IDNO_TYPE_HSK;
import static de.staatsbibliothek.berlin.hsp.mapper.tei.TEIValues.IDNO_TYPE_HSP;
import static de.staatsbibliothek.berlin.hsp.mapper.tei.TEIValues.PTR_TYPE_HSP;
import static de.staatsbibliothek.berlin.hsp.mapper.tei.TEIValues.PTR_TYPE_THUMBNAIL;

import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Katalog;
import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Katalog.KatalogBuilder;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Digitalisat;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.DigitalisatTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
import de.staatsbibliothek.berlin.hsp.mapper.api.Mapper;
import de.staatsbibliothek.berlin.hsp.mapper.api.exceptions.HSPMapperException;
import de.staatsbibliothek.berlin.hsp.mapper.tei.domainfactories.AutorenFactory;
import de.staatsbibliothek.berlin.hsp.messaging.common.TEICommon;
import de.staatsbibliothek.berlin.hsp.messaging.objectfactory.TEIObjectFactory;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tei_c.ns._1.Body;
import org.tei_c.ns._1.Date;
import org.tei_c.ns._1.FileDesc;
import org.tei_c.ns._1.Idno;
import org.tei_c.ns._1.Licence;
import org.tei_c.ns._1.Ptr;
import org.tei_c.ns._1.PublicationStmt;
import org.tei_c.ns._1.Publisher;
import org.tei_c.ns._1.TEI;
import org.tei_c.ns._1.Title;
import org.tei_c.ns._1.TitleStmt;

/**
 * @author michael.hintersonnleitner@sbb.spk-berlin.de
 * @since 24.02.22
 */
public class TEI2KatalogMapper implements Mapper<TEI, Katalog> {

  private static final String KOERPERSCHAFT_TYPE_NAME = "CorporateBody";

  private static final Logger log = LoggerFactory.getLogger(TEIQuery.class);

  public static FileDesc getFileDesc(TEI tei) throws Exception {
    return TEICommon.findFirst(FileDesc.class, tei)
        .orElseThrow(() -> new HSPMapperException("tei contains no fileDesc"));
  }

  public static PublicationStmt getPublicationStmt(FileDesc fileDesc) throws Exception {
    return TEICommon.findFirst(PublicationStmt.class, fileDesc)
        .orElseThrow(() -> new HSPMapperException("tei contains no publicationStmt"));
  }

  public static Body getBody(TEI tei) throws Exception {
    return TEICommon.findFirst(Body.class, tei)
        .orElseThrow(() -> new HSPMapperException("tei contains no body"));
  }

  public static Optional<String> getIdnoForType(List<Idno> idnos, String idnoType) {
    if (Objects.isNull(idnos) || idnos.isEmpty()) {
      return Optional.empty();
    }

    return idnos.stream()
        .filter(idno -> Objects.nonNull(idno.getType()) && idno.getType().equals(idnoType))
        .findFirst()
        .map(idno -> TEICommon.getContentAsString(idno.getContent()))
        .map(StringUtils::normalizeSpace);
  }

  public static String getTitel(FileDesc fileDesc) throws Exception {
    Optional<TitleStmt> titleStmt = TEICommon.findFirst(TitleStmt.class, fileDesc);
    if (titleStmt.isPresent()) {
      return TEICommon.findFirst(Title.class, titleStmt.get())
          .map(title -> TEICommon.getContentAsString(title.getContent()))
          .map(StringUtils::normalizeSpace)
          .orElse("");
    } else {
      return "";
    }
  }

  public static String getLizenzURI(PublicationStmt publicationStmt) throws Exception {
    return TEICommon.findFirst(Licence.class, publicationStmt)
        .map(licence -> TEICommon.getContentAsString(licence.getTargets()))
        .map(StringUtils::normalizeSpace)
        .orElse("");
  }

  public static NormdatenReferenz getVerlag(PublicationStmt publicationStmt) throws Exception {
    return TEICommon.findFirst(Publisher.class, publicationStmt)
        .map(publisher -> TEICommon.getContentAsString(publisher.getContent()))
        .map(StringUtils::normalizeSpace)
        .filter(publisher -> !publisher.isEmpty())
        .map(publisher -> new NormdatenReferenz(UUID.randomUUID().toString(), publisher, "",
            KOERPERSCHAFT_TYPE_NAME))
        .orElse(null);
  }

  public static String getPtrTarget(List<Ptr> ptrs, String type) {
    if (Objects.isNull(ptrs) || ptrs.isEmpty() || Objects.isNull(type)) {
      return "";
    }
    return ptrs.stream()
        .filter(ptr -> type.equals(ptr.getType()))
        .findFirst()
        .map(ptr -> TEICommon.getContentAsString(ptr.getTargets()))
        .map(StringUtils::normalizeSpace)
        .filter(ptrTargets -> !ptrTargets.isEmpty())
        .orElse("");
  }

  public static Digitalisat getDigitalisat(PublicationStmt publicationStmt) throws Exception {
    List<Ptr> prts = new ArrayList<>();
    TEICommon.findAll(Ptr.class, publicationStmt, prts);

    Digitalisat.Builder builder = Digitalisat.DigitalisatBuilder()
        .withID(UUID.randomUUID().toString())
        .withManifest(getPtrTarget(prts, PTR_TYPE_HSP))
        .withThumbnail(getPtrTarget(prts, PTR_TYPE_THUMBNAIL))
        .withTyp(DigitalisatTyp.KOMPLETT_VOM_ORIGINAL);

    return builder.build();
  }

  public static String getPublikationsJahr(PublicationStmt publicationStmt) throws Exception {
    return TEICommon.findFirst(Date.class, publicationStmt)
        .map(date -> TEICommon.getContentAsString(date.getContent()))
        .map(StringUtils::normalizeSpace)
        .filter(date -> !date.isEmpty())
        .orElse("");
  }

  @Override
  public Katalog map(TEI tei) throws Exception {
    if (Objects.isNull(tei)) {
      throw new HSPMapperException("TEI tei is null");
    }

    FileDesc fileDesc = getFileDesc(tei);
    PublicationStmt publicationStmt = getPublicationStmt(fileDesc);

    List<Idno> idnos = new ArrayList<>();
    TEICommon.findAll(Idno.class, publicationStmt, idnos);

    String hskId = getIdnoForType(idnos, IDNO_TYPE_HSK)
        .orElseThrow(() -> new HSPMapperException("tei contains no idno of type " + IDNO_TYPE_HSK));

    String id = getIdnoForType(idnos, IDNO_TYPE_HSP)
        .orElse(
            TEIValues.UUID_PREFIX + UUID.nameUUIDFromBytes(hskId.getBytes(StandardCharsets.UTF_8)));

    LocalDateTime now = LocalDateTime.now();

    KatalogBuilder builder = new Katalog.KatalogBuilder()
        .withHSKID(hskId)
        .withId(id)
        .withTitle(getTitel(fileDesc))
        .withVerlag(getVerlag(publicationStmt))
        .withDigitalisat(getDigitalisat(publicationStmt))
        .withLizenzURI(getLizenzURI(publicationStmt))
        .withPublikationsJahr(getPublikationsJahr(publicationStmt))
        .withTEIXML(TEIObjectFactory.marshal(tei))
        .withErstelldatum(now)
        .withAenderungsdatum(now);

    AutorenFactory.buildAutoren(Optional.ofNullable(fileDesc)).stream().forEach(builder::addAutor);

    return builder.build();
  }
}
