package de.staatsbibliothek.berlin.hsp.nachweis;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;
import org.eclipse.microprofile.openapi.annotations.OpenAPIDefinition;
import org.eclipse.microprofile.openapi.annotations.info.Contact;
import org.eclipse.microprofile.openapi.annotations.info.Info;
import org.eclipse.microprofile.openapi.annotations.info.License;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 21.11.2019.
 * @version 1.0
 */
@OpenAPIDefinition(
    tags = {
        @Tag(name = "bearbeiter", description = "This service provides an API REST HTTP Interface for managing Bearbeiter"),
        @Tag(name = "beschreibungen", description = "This service provides an API REST HTTP Interface for managing Beschreibung"),
        @Tag(name = "indexKods", description = "This service provides an API REST HTTP Interface for indexing one or many KulturObjektDokumente"),
        @Tag(name = "indexJobStatus", description = "This service provides an API REST HTTP Interface for get the status of indexing the kods")
    },
    info = @Info(
        title = "HSP Nachweiservice of Staatsbibliothek to Berlin",
        version = "0.8.1",

        contact = @Contact(
            name = "Information and Datamanagement Department 2.2",
            url = "https://staatsbibliothek-berlin.de",
            email = "SBB-IDM2.2 <SBB-IDM2.2@sbb.spk-berlin.de>"),
        license = @License(
            name = "Apache 2.0",
            url = "http://www.apache.org/licenses/LICENSE-2.0.html"))
)
@ApplicationPath("/rest")
public class ApplicationConfig extends Application {

}
