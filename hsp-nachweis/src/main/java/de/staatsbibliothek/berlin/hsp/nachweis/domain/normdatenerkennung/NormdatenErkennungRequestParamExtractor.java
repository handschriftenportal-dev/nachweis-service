package de.staatsbibliothek.berlin.hsp.nachweis.domain.normdatenerkennung;

import de.staatsbibliothek.berlin.hsp.mapper.tei.TEIQuery;
import de.staatsbibliothek.berlin.hsp.messaging.objectfactory.TEIObjectFactory;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.xml.bind.JAXBException;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.apache.commons.lang.StringUtils;

@ApplicationScoped
public class NormdatenErkennungRequestParamExtractor {

  List<NormdatenErkennungRequestParam> extract(String teiXml) throws JAXBException {
    if (StringUtils.isBlank(teiXml)) {
      return List.of();
    }
    var tei = TEIObjectFactory.unmarshalOne(
            new ByteArrayInputStream(teiXml.getBytes(StandardCharsets.UTF_8)))
        .orElse(null);
    if (tei == null) {
      return List.of();
    }
    return TEIQuery
        .findAllPersNamesWithPath(tei)
        .entrySet()
        .stream()
        .filter(e -> StringUtils.isBlank(e.getValue().getKey()))
        .map(NormdatenErkennungRequestParam::new)
        .toList();
  }
}
