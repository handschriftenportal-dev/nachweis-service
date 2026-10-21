package de.staatsbibliothek.berlin.hsp.nachweis.domain.webindex;

import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@ToString
public class IndexJob {

  private final String actorName;
  private final IndexJobDocumentType indexJobDocumentType;
  private final String documentId;

  public IndexJob(String documentId, String actorName, IndexJobDocumentType indexJobDocumentType) {
    this.documentId = documentId;
    this.actorName = actorName;
    this.indexJobDocumentType = indexJobDocumentType;
  }

  public IndexJob(String actorName, IndexJobDocumentType indexJobDocuments) {
    this(null, actorName, indexJobDocuments);
  }

  public boolean isSingleDocumentIndexJob() {
    return documentId != null;
  }

  public boolean isKodIndexJob() {
    return IndexJobDocumentType.KOD.equals(indexJobDocumentType);
  }

  public boolean isKatalogIndexJob() {
    return IndexJobDocumentType.KATALOG.equals(indexJobDocumentType);
  }

  public boolean isAllDocumentTypeIndexJob() {
    return IndexJobDocumentType.ALL.equals(indexJobDocumentType);
  }
}
