package de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects;

public enum AttributsTyp {
  TITEL("norm_title"),
  BESCHREIBSTOFF("norm_material"),
  UMFANG("norm_measure"),
  ABMESSUNG("norm_dimensions"),
  FORMAT("norm_format"),
  ENTSTEHUNGSORT("norm_origPlace"),
  ENTSTEHUNGSZEIT("norm_origDate"),
  GRUNDSPRACHE("norm_textLang"),
  HANDSCHRIFTENTYP("norm_form"),
  STATUS("norm_status"),
  BUCHSCHMUCK("norm_decoration"),
  MUSIKNOTATION("norm_musicNotation");

  private final String indexName;

  AttributsTyp(String indexName) {
    this.indexName = indexName;
  }

  public String getIndexName() {
    return indexName;
  }
}
