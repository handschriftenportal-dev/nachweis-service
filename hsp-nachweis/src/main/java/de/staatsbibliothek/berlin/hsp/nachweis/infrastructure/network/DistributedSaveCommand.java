package de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.network;

import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Dokument;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.enums.ActivityStreamAction;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 02.02.24.
 * @version 1.0
 */
public interface DistributedSaveCommand<T extends Dokument> {

  void execute(T dokument, ActivityStreamAction action)
      throws Exception;
}
