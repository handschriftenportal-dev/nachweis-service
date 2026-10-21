package de.staatsbibliothek.berlin.hsp.nachweis.application.controller.oaipmh;

import de.staatsbibliothek.berlin.oaipmh.rest.OaiPmhRequestBase;
import jakarta.ws.rs.FormParam;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OaiPmhPostRequest extends OaiPmhRequestBase {

  @FormParam("verb")
  @Parameter(
      description =
          "The verb defines the type of OAI-PMH request. The verb argument is required. "
              + "Possible values are: Identify, ListMetadataFormats, ListSets, GetRecord, ListIdentifiers, ListRecords.")
  private String verb;

  @FormParam("identifier")
  @Parameter(description = "The unique identifier of the item.")
  private String identifier;

  @FormParam("metadataPrefix")
  @Parameter(description = "The metaDataPrefix defines the metadata format of the records.")
  private String metadataPrefix;

  @FormParam("from")
  @Parameter(
      description =
          "An optional argument with a UTCdatetime value, which specifies a lower bound for datestamp-based selective harvesting.")
  private String from;

  @FormParam("until")
  @Parameter(
      description =
          "An optional argument with a UTCdatetime value, which specifies an upper bound for datestamp-based selective harvesting.")
  private String until;

  @FormParam("set")
  @Parameter(
      description =
          "set an optional argument with a setSpec value , which specifies set criteria for selective harvesting.")
  private String set;

  @FormParam("resumptionToken")
  @Parameter(
      description =
          "The value of the resumptionToken argument is an opaque value returned by the server in a previous ListIdentifiers request."
              + " The resumptionToken argument is only used within the context of a single resumption of a list."
              + " The value of the resumptionToken argument is an implementation detail of the repository and should not be assumed to have any particular structure or interpretation.")
  private String resumptionToken;
}
