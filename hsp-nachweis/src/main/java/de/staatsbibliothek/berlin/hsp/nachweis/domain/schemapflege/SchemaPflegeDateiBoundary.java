package de.staatsbibliothek.berlin.hsp.nachweis.domain.schemapflege;

import java.io.InputStream;
import java.util.Optional;
import java.util.Set;

/**
 * @author michael.hintersonnleitner@sbb.spk-berlin.de
 * @since 29.03.2022
 */

public interface SchemaPflegeDateiBoundary {

  Optional<SchemaPflegeDatei> findById(String id);

  Set<SchemaPflegeDatei> findAll();

  boolean upload(String id, String version, String bearbeiterName, InputStream datei);
}
