package de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects;

/**
 * Created by konrad.eichstaedt@sbb.spk-berlin.de on 30.04.2019.
 */

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Beschreibung;
import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.KulturObjektDokument;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME)
@JsonSubTypes(value = {
    @JsonSubTypes.Type(value = KulturObjektDokument.class, name = "KulturObjektDokument"),

    @JsonSubTypes.Type(value = Beschreibung.class, name = "Beschreibung")
})
public interface Dokument {

}
