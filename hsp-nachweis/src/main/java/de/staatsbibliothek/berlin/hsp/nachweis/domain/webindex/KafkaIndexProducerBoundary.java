package de.staatsbibliothek.berlin.hsp.nachweis.domain.webindex;

import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.KulturObjektDokument;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.exceptions.ActivityStreamsException;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.ActivityStream;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.enums.ActivityStreamAction;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 27.10.22.
 * @version 1.0
 */
public interface KafkaIndexProducerBoundary {
  ActivityStream sendKulturobjektDokumentAsActivityStreamMessage(KulturObjektDokument kod, ActivityStreamAction action, boolean compressed, String actor) throws ActivityStreamsException;

  ActivityStream sendRemoveAllAsActivityStreamMessage(String actor) throws ActivityStreamsException;
}
