package de.staatsbibliothek.berlin.oaipmh.dataprovider;

import io.gdcc.xoai.dataprovider.model.Context;
import io.gdcc.xoai.dataprovider.model.MetadataFormat;
import lombok.Getter;

public class DataProviderContext {

  @Getter
  private final Context context = Context.context();

  private DataProviderContext() {
  }

  public static final class DataProviderContextBuilder {

    private final DataProviderContext dataProviderContext = new DataProviderContext();

    public DataProviderContextBuilder addMetadataFormat(MetadataFormat format) {
      dataProviderContext.context.withMetadataFormat(format);
      return this;
    }

    public DataProviderContext build() {
      return dataProviderContext;
    }
  }
}
