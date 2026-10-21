package de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.importservice;

import jakarta.ws.rs.core.MediaType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jboss.resteasy.reactive.PartType;
import org.jboss.resteasy.reactive.RestForm;

/**
 * @author michael.hintersonnleitner@sbb.spk-berlin.de
 * @since 28.03.22
 */

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SchemaPflegeDateiMultipartBody {

    @RestForm("version")
    @PartType(MediaType.TEXT_PLAIN)
    private String version;

    @RestForm("bearbeiterName")
    @PartType(MediaType.TEXT_PLAIN)
    private String bearbeiterName;

    @RestForm("datei")
    @PartType(MediaType.APPLICATION_OCTET_STREAM)
    private byte[] datei;
}
