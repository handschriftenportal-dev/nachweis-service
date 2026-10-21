package de.staatsbibliothek.berlin.oaipmh.model;

import java.time.Instant;

public record OaiItem(String id, String teiXML, Instant registrierungsDatum, String setSpec) {}
