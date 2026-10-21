package de.staatsbibliothek.berlin.oaipmh.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import de.staatsbibliothek.berlin.oaipmh.model.Item;
import de.staatsbibliothek.berlin.oaipmh.model.OaiItem;
import io.gdcc.xoai.dataprovider.exceptions.handler.HandlerException;
import io.gdcc.xoai.dataprovider.exceptions.handler.IdDoesNotExistException;
import io.gdcc.xoai.dataprovider.model.ItemIdentifier;
import io.gdcc.xoai.dataprovider.model.MetadataFormat;
import io.gdcc.xoai.dataprovider.repository.ResultsPage;
import io.gdcc.xoai.model.oaipmh.ResumptionToken.ValueBuilder;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

@QuarkusTest
class ItemRepositoryImplTest {

  @InjectMock
  ClientItemRepository clientItemRepository;

  @Test
  void testGetItemIdentifier() throws IdDoesNotExistException {
    when(clientItemRepository.findById("1"))
        .thenReturn(
            Optional.of(new OaiItem("1", "teiXML", Instant.parse("2022-11-29T00:00:00Z"), "Set1")));
    ItemRepositoryImpl itemRepositoryImpl = new ItemRepositoryImpl(clientItemRepository);

    Item itemIdentifier = (Item) itemRepositoryImpl.getItemIdentifier("1");

    assertEquals("1", itemIdentifier.getIdentifier());
    assertEquals(Instant.parse("2022-11-29T00:00:00Z"), itemIdentifier.getDatestamp());
    assertEquals("Set1", itemIdentifier.getSets().getFirst().getSpec());
  }

  @Test
  void testGetItemIdentifierNotFound() {
    when(clientItemRepository.findById("1")).thenReturn(Optional.empty());

    ItemRepositoryImpl itemRepositoryImpl = new ItemRepositoryImpl(clientItemRepository);
    assertThrows(
        IdDoesNotExistException.class,
        () -> {
          itemRepositoryImpl.getItemIdentifier("1");
        });
  }

  @Test
  void testGetItem() throws HandlerException {
    when(clientItemRepository.findById("1"))
        .thenReturn(
            Optional.of(new OaiItem("1", "teiXML", Instant.parse("2022-11-29T00:00:00Z"), "Set1")));

    ItemRepositoryImpl itemRepositoryImpl = new ItemRepositoryImpl(clientItemRepository);
    Item item = (Item) itemRepositoryImpl.getItem("1", new MetadataFormat());

    assertEquals("1", item.getIdentifier());
    assertEquals(Instant.parse("2022-11-29T00:00:00Z"), item.getDatestamp());
    assertEquals("Set1", item.getSets().getFirst().getSpec());
  }

  @Test
  void testGetItemNotFound() {
    when(clientItemRepository.findById("1")).thenReturn(Optional.empty());

    ItemRepositoryImpl itemRepositoryImpl = new ItemRepositoryImpl(clientItemRepository);
    assertThrows(
        IdDoesNotExistException.class,
        () -> {
          itemRepositoryImpl.getItem("1", new MetadataFormat());
        });
  }

  @Test
  void testGetItemIdentifiers() {
    when(clientItemRepository.findWithFilters(null, null, null, 1, 0))
        .thenReturn(
            List.of(new OaiItem("1", "teiXML", Instant.parse("2022-11-29T00:00:00Z"), "Set1")));
    when(clientItemRepository.countWithFilters(null, null, null)).thenReturn(2L);

    ItemRepositoryImpl itemRepositoryImpl = new ItemRepositoryImpl(clientItemRepository);
    ResultsPage<ItemIdentifier> itemIdentifiers =
        itemRepositoryImpl.getItemIdentifiers(
            null,
            new MetadataFormat().withPrefix("oai_dc"),
            1,
            new ValueBuilder().withMetadataPrefix("oai_dc").withOffset(0L).build());

    assertEquals(1, itemIdentifiers.getList().size());
    assertEquals(2, itemIdentifiers.getTotal());
    assertTrue(itemIdentifiers.hasMore());
  }

  @Test
  void testGetItems() {
    when(clientItemRepository.findWithFilters(null, null, null, 1, 0))
        .thenReturn(
            List.of(new OaiItem("1", "teiXML", Instant.parse("2022-11-29T00:00:00Z"), "Set1")));
    when(clientItemRepository.countWithFilters(null, null, null)).thenReturn(2L);

    ItemRepositoryImpl itemRepositoryImpl = new ItemRepositoryImpl(clientItemRepository);
    ResultsPage<io.gdcc.xoai.dataprovider.model.Item> itemIdentifiers =
        itemRepositoryImpl.getItems(
            null,
            new MetadataFormat().withPrefix("oai_dc"),
            1,
            new ValueBuilder().withMetadataPrefix("oai_dc").withOffset(0L).build());

    assertEquals(1, itemIdentifiers.getList().size());
    assertEquals(2, itemIdentifiers.getTotal());
    assertTrue(itemIdentifiers.hasMore());
  }

  @Test
  void testGetItemWithTransformerFormat() throws HandlerException {
    when(clientItemRepository.findById("1"))
        .thenReturn(
            Optional.of(new OaiItem("1", "teiXML", Instant.parse("2022-11-29T00:00:00Z"), "Set1")));

    ItemRepositoryImpl itemRepositoryImpl = new ItemRepositoryImpl(clientItemRepository);
    MetadataFormat format =
        MetadataFormat.metadataFormat("oai_dc").withTransformer(MetadataFormat.identity());
    Item item = (Item) itemRepositoryImpl.getItem("1", format);

    assertEquals("1", item.getIdentifier());
    assertEquals(Instant.parse("2022-11-29T00:00:00Z"), item.getDatestamp());
    assertEquals(1, item.getSets().size());
    assertEquals("Set1", item.getSets().getFirst().getSpec());
    assertTrue(item.getMetadata().needsProcessing());
  }

  @Test
  void testGetItemWithNativeFormat() throws HandlerException {
    when(clientItemRepository.findById("1"))
        .thenReturn(
            Optional.of(new OaiItem("1", "teiXML", Instant.parse("2022-11-29T00:00:00Z"), "Set1")));

    ItemRepositoryImpl itemRepositoryImpl = new ItemRepositoryImpl(clientItemRepository);
    MetadataFormat format = MetadataFormat.metadataFormat("oai_tei");
    Item item = (Item) itemRepositoryImpl.getItem("1", format);

    assertEquals("1", item.getIdentifier());
    assertEquals(Instant.parse("2022-11-29T00:00:00Z"), item.getDatestamp());
    assertEquals(1, item.getSets().size());
    assertEquals("Set1", item.getSets().getFirst().getSpec());
    assertFalse(item.getMetadata().needsProcessing());
  }

  @Test
  void testMapItem() {
    OaiItem oaiItem = new OaiItem("1", "teiXML", Instant.parse("2022-11-29T00:00:00Z"), "Set1");
    ItemRepositoryImpl itemRepositoryImpl = new ItemRepositoryImpl(clientItemRepository);
    io.gdcc.xoai.dataprovider.model.Item item = itemRepositoryImpl.mapItem(oaiItem,
        MetadataFormat.metadataFormat("oai_tei"));

    assertEquals("1", item.getIdentifier());
    assertEquals(Instant.parse("2022-11-29T00:00:00Z"), item.getDatestamp());
    assertEquals("Set1", item.getSets().getFirst().getSpec());
    assertFalse(item.getMetadata().needsProcessing());

    io.gdcc.xoai.dataprovider.model.Item item2 = itemRepositoryImpl.mapItem(oaiItem,
        MetadataFormat.metadataFormat("oai_dc").withTransformer(MetadataFormat.identity()));
    assertTrue(item2.getMetadata().needsProcessing());

  }
}
