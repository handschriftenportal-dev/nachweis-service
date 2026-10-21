package de.staatsbibliothek.berlin.oaipmh.dataprovider;

import de.staatsbibliothek.berlin.oaipmh.config.ConfigMapper;
import de.staatsbibliothek.berlin.oaipmh.config.Configuration;
import de.staatsbibliothek.berlin.oaipmh.repository.ClientItemRepository;
import de.staatsbibliothek.berlin.oaipmh.repository.ClientSetRepository;
import de.staatsbibliothek.berlin.oaipmh.repository.ItemRepositoryImpl;
import de.staatsbibliothek.berlin.oaipmh.repository.SetRepositoryImpl;
import io.gdcc.xoai.xml.XmlWriter;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Provider;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.Map;
import javax.xml.stream.XMLStreamException;

@ApplicationScoped
public class DataProvider {

  private final io.gdcc.xoai.dataprovider.repository.Repository repository;
  private final Provider<DataProviderContext> dataProviderContextProvider;

  @Inject
  public DataProvider(
      ClientItemRepository clientItemRepository,
      ClientSetRepository clientSetRepository,
      Configuration configuration,
      Provider<DataProviderContext> dataProviderContextProvider) {
    this.dataProviderContextProvider = dataProviderContextProvider;
    repository =
        new io.gdcc.xoai.dataprovider.repository.Repository(
            ConfigMapper.getOAIRepositoryConfiguration(configuration));
    repository.withSetRepository(new SetRepositoryImpl(clientSetRepository));
    repository.withItemRepository(new ItemRepositoryImpl(clientItemRepository));
  }

  public String handleRequest(Map<String, String[]> parameterMap) throws XMLStreamException {
    var provider = new io.gdcc.xoai.dataprovider.DataProvider(
        dataProviderContextProvider.get().getContext(), repository);
    final var oaiPmh = provider.handle(parameterMap);
    OutputStream responseStream = new ByteArrayOutputStream();
    try (XmlWriter xmlWriter = new XmlWriter(responseStream, repository.getConfiguration())) {
      xmlWriter.write(oaiPmh);
      xmlWriter.flush();
      return responseStream.toString();
    }
  }
}
