package de.staatsbibliothek.berlin.oaipmh.repository;

import de.staatsbibliothek.berlin.oaipmh.model.Item;
import de.staatsbibliothek.berlin.oaipmh.model.ItemIdentifier;
import de.staatsbibliothek.berlin.oaipmh.model.OaiItem;
import io.gdcc.xoai.dataprovider.exceptions.handler.HandlerException;
import io.gdcc.xoai.dataprovider.exceptions.handler.IdDoesNotExistException;
import io.gdcc.xoai.dataprovider.filter.ScopedFilter;
import io.gdcc.xoai.dataprovider.model.MetadataFormat;
import io.gdcc.xoai.dataprovider.model.Set;
import io.gdcc.xoai.dataprovider.repository.ItemRepository;
import io.gdcc.xoai.dataprovider.repository.ResultsPage;
import io.gdcc.xoai.model.oaipmh.ResumptionToken;
import io.gdcc.xoai.model.oaipmh.results.record.Metadata;
import io.gdcc.xoai.xml.CopyElement;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class ItemRepositoryImpl implements ItemRepository {

  private final ClientItemRepository repository;

  public ItemRepositoryImpl(ClientItemRepository repository) {
    this.repository = repository;
  }

  @Override
  public io.gdcc.xoai.dataprovider.model.Item getItemIdentifier(String identifier)
      throws IdDoesNotExistException {

    var doc = repository.findById(identifier);

    if (doc.isEmpty()) {
      throw new IdDoesNotExistException();
    }

    var item = doc.get();
    return new Item(
        new Metadata(item.teiXML()),
        item.id(),
        item.registrierungsDatum(),
        List.of(new Set(item.setSpec())));
  }

  @Override
  public io.gdcc.xoai.dataprovider.model.Item getItem(String identifier, MetadataFormat format)
      throws HandlerException {
    var doc = repository.findById(identifier);
    return mapItem(doc.orElseThrow(IdDoesNotExistException::new), format);
  }

  @Override
  public ResultsPage<io.gdcc.xoai.dataprovider.model.ItemIdentifier> getItemIdentifiers(
      List<ScopedFilter> filters,
      MetadataFormat metadataFormat,
      int maxResponseLength,
      ResumptionToken.Value resumptionToken) {

    var items = getItems(resumptionToken, maxResponseLength);
    var mappeditems =
        items.items().stream()
            .map(
                item ->
                    (io.gdcc.xoai.dataprovider.model.ItemIdentifier)
                        new ItemIdentifier(
                            item.id(),
                            item.registrierungsDatum(),
                            List.of(new Set(item.setSpec())),
                            false))
            .toList();

    return new ResultsPage<>(
        resumptionToken,
        mappeditems.size() == maxResponseLength
            && items.totalCount != resumptionToken.getOffset() + maxResponseLength,
        mappeditems,
        (int) items.totalCount);
  }

  @Override
  public ResultsPage<io.gdcc.xoai.dataprovider.model.Item> getItems(
      List<ScopedFilter> filters,
      MetadataFormat metadataFormat,
      int maxResponseLength,
      ResumptionToken.Value resumptionToken) {

    var items = getItems(resumptionToken, maxResponseLength);

    List<io.gdcc.xoai.dataprovider.model.Item> mappeditems = items.items().stream()
        .map(item -> mapItem(item, metadataFormat))
        .toList();

    return new ResultsPage<>(
        resumptionToken,
        mappeditems.size() == maxResponseLength
            && items.totalCount != resumptionToken.getOffset() + maxResponseLength,
        mappeditems,
        (int) items.totalCount);
  }

  protected io.gdcc.xoai.dataprovider.model.Item mapItem(OaiItem item, MetadataFormat format) {
    boolean isNativeFormat = format.getTransformer() == null;
    Metadata metadata =
        isNativeFormat
            ? new Metadata(new CopyElement(
            new ByteArrayInputStream(item.teiXML().getBytes(StandardCharsets.UTF_8))))
            : new Metadata(item.teiXML());

    return new Item(metadata, item.id(), item.registrierungsDatum(),
        List.of(new Set(item.setSpec())));
  }

  private ItemResult getItems(ResumptionToken.Value resumptionToken, int maxResponseLength) {
    var from = resumptionToken.getFrom();
    var until = resumptionToken.getUntil();
    var setSpec = resumptionToken.getSetSpec();
    var offset = (int) resumptionToken.getOffset();

    var totalCount = repository.countWithFilters(from, until, setSpec);
    return new ItemResult(
        repository.findWithFilters(from, until, setSpec, maxResponseLength, offset), totalCount);
  }

  private record ItemResult(List<OaiItem> items, long totalCount) {

  }
}
