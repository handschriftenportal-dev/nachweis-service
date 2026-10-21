package de.staatsbibliothek.berlin.oaipmh.model;

import io.gdcc.xoai.dataprovider.model.Set;
import java.time.Instant;
import java.util.List;

public class ItemIdentifier implements io.gdcc.xoai.dataprovider.model.ItemIdentifier {

  private final String id;
  private final Instant datestamp;
  private final List<Set> sets;
  private final boolean deleted;

  public ItemIdentifier(String id, Instant datestamp, List<Set> sets, boolean deleted) {
    this.id = id;
    this.datestamp = datestamp;
    this.sets = sets;
    this.deleted = deleted;
  }

  @Override
  public String getIdentifier() {
    return id;
  }

  @Override
  public Instant getDatestamp() {
    return datestamp;
  }

  @Override
  public List<Set> getSets() {
    return sets;
  }

  @Override
  public boolean isDeleted() {
    return deleted;
  }
}
