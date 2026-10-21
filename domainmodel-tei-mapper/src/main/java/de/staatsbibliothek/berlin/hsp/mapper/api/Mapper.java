package de.staatsbibliothek.berlin.hsp.mapper.api;

/**
 * @author Piotr.Czarnecki@sbb.spk-berlin.de
 * @since 20.04.20
 */
public interface Mapper<T, V> {

  V map(T source) throws Exception;

}
