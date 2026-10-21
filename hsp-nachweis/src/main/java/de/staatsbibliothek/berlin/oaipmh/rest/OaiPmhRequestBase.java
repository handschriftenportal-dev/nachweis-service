package de.staatsbibliothek.berlin.oaipmh.rest;

import java.util.HashMap;
import java.util.Map;

public abstract class OaiPmhRequestBase {

  public abstract String getVerb();

  public abstract String getIdentifier();

  public abstract String getMetadataPrefix();

  public abstract String getFrom();

  public abstract String getUntil();

  public abstract String getSet();

  public abstract String getResumptionToken();

  public Map<String, String[]> toMap() {
    Map<String, String[]> map = new HashMap<>();

    map.put("verb", new String[]{getVerb()});

    if (getIdentifier() != null) {
      map.put("identifier", new String[]{getIdentifier()});
    }
    if (getMetadataPrefix() != null) {
      map.put("metadataPrefix", new String[]{getMetadataPrefix()});
    }
    if (getFrom() != null) {
      map.put("from", new String[]{getFrom()});
    }
    if (getUntil() != null) {
      map.put("until", new String[]{getUntil()});
    }
    if (getSet() != null) {
      map.put("set", new String[]{getSet()});
    }
    if (getResumptionToken() != null) {
      map.put("resumptionToken", new String[]{getResumptionToken()});
    }
    return map;
  }
}
