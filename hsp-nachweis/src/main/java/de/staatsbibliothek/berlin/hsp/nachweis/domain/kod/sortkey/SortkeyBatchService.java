package de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.sortkey;

import static de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.enums.ActivityStreamAction.UPDATE;

import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.KulturObjektDokument;
import de.staatsbibliothek.berlin.hsp.mapper.tei.kod.TEIKulturObjektDokumentCommand;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.KulturObjektDokumentRepository;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.network.DistributedSaveCommand;
import io.quarkus.narayana.jta.runtime.TransactionConfiguration;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class SortkeyBatchService {

  private final KulturObjektDokumentRepository kulturObjektDokumentRepository;

  private final DistributedSaveCommand<KulturObjektDokument> distributedSaveCommand;

  @Transactional(rollbackOn = Exception.class)
  @TransactionConfiguration(timeout = 900)
  public SortKeyBatchResult processChunk(
      List<SortKeyEntry> chunk)
      throws KulturObjektSortkeyUpdateException {

    log.info("Starting SortKey batch processing ({} entries)", chunk.size());

    List<String> unknownUuids = new ArrayList<>();
    int updatedCount = 0;

    List<String> uuids = chunk.stream().map(SortKeyEntry::uuid).toList();

    Map<String, KulturObjektDokument> kodsById =
        kulturObjektDokumentRepository.list("id in ?1", uuids).stream()
            .collect(Collectors.toMap(KulturObjektDokument::getId, Function.identity()));

    for (SortKeyEntry sortKeyEntry : chunk) {
      KulturObjektDokument kod = kodsById.get(sortKeyEntry.uuid());

      if (kod == null) {
        unknownUuids.add(sortKeyEntry.uuid());
      } else if (updateSortkey(kod, sortKeyEntry)) {
        updatedCount++;
      }
    }

    log.info(
        "Finished SortKey batch processing. Updated={}, Unknown={}",
        updatedCount,
        unknownUuids.size());

    return new SortKeyBatchResult(updatedCount, unknownUuids);
  }

  private boolean updateSortkey(KulturObjektDokument kod, SortKeyEntry sortKeyEntry)
      throws KulturObjektSortkeyUpdateException {

    if (kod.getGueltigeIdentifikation() == null) {
      log.warn("KOD without gueltigeIdentifikation for UUID={}", sortKeyEntry.uuid());
      return false;
    }

    if (Objects.equals(kod.getSortKey(), sortKeyEntry.sortkey())) {
      return false;
    }

    kod.getGueltigeIdentifikation().setSortKey(sortKeyEntry.sortkey());

    try {
      TEIKulturObjektDokumentCommand.updateMsIdentifierAndAllAltidentifier(kod);
      distributedSaveCommand.execute(kod, UPDATE);
      return true;

    } catch (Exception exception) {
      throw new KulturObjektSortkeyUpdateException(
          "Sortkey konnte für KOD " + sortKeyEntry.uuid() + " nicht gespeichert werden!",
          exception);
    }
  }
}
