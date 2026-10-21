package de.staatsbibliothek.berlin.oaipmh.model;

import io.gdcc.xoai.dataprovider.model.Set;
import io.gdcc.xoai.model.oaipmh.results.record.Metadata;
import java.time.Instant;
import java.util.List;

public class Item implements io.gdcc.xoai.dataprovider.model.Item {

  private final Metadata metaData;
  private final String identifier;
  private final Instant dateStamp;
  private final List<Set> sets;

  public Item(Metadata metaData, String identifier, Instant dateStamp, List<Set> sets) {
    this.metaData = metaData;
    this.identifier = identifier;
    this.dateStamp = dateStamp;
    this.sets = sets;
  }

  @Override
  public Metadata getMetadata() {
    return metaData;
  }

  @Override
  public String getIdentifier() {
    return identifier;
  }

  @Override
  public Instant getDatestamp() {
    return dateStamp;
  }

  @Override
  public List<Set> getSets() {
    return sets;
  }

  @Override
  public boolean isDeleted() {
    return false;
  }
}
