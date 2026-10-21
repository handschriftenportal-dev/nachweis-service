package de.staatsbibliothek.berlin.hsp.mapper.api.factory;

import de.staatsbibliothek.berlin.hsp.mapper.api.exceptions.HSPMapperException;

/**
 * Created by konrad.eichstaedt@sbb.spk-berlin.de on 12.09.2019.
 */
public interface ElementFactory<T, L, G> {

  T build(L local, G global) throws HSPMapperException;
}
