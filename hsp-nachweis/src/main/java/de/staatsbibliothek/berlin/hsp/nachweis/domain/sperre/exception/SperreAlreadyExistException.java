package de.staatsbibliothek.berlin.hsp.nachweis.domain.sperre.exception;

import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Sperre;
import java.util.List;

/**
 * Signalize that one or more of the provided documents to lock are already locked
 *
 * @author Piotr.Czarnecki@sbb.spk-berlin.de
 * @since 17.08.21
 */
public class SperreAlreadyExistException extends RuntimeException {

  private static final long serialVersionUID = -8116674838144495146L;

  private List<Sperre> sperren;

  public SperreAlreadyExistException(String message, List<Sperre> sperren) {
    super(message);
    this.sperren = sperren;
  }

  public List<Sperre> getSperren() {
    return this.sperren;
  }

}