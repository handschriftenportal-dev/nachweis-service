package de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.purl;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.ext.MessageBodyReader;
import jakarta.ws.rs.ext.Provider;

/**
 * @author michael.hintersonnleitner@sbb.spk-berlin.de
 * @since 28.10.22
 */

@Provider
@Consumes(MediaType.TEXT_PLAIN)
public class SBBPURLGenResponseReader implements MessageBodyReader<SBBPURLGenResponse> {

  private ObjectMapper mapper;

  @Inject
  public SBBPURLGenResponseReader(ObjectMapper mapper) {
    this.mapper = mapper;
  }

  @Override
  public boolean isReadable(Class<?> type, Type genericType,
      Annotation[] annotations, MediaType mediaType) {
    return type == SBBPURLGenResponse.class && MediaType.TEXT_PLAIN_TYPE.equals(mediaType);
  }

  @Override
  public SBBPURLGenResponse readFrom(Class<SBBPURLGenResponse> type, Type genericType, Annotation[] annotations,
      MediaType mediaType, MultivaluedMap<String, String> httpHeaders,
      InputStream entityStream) throws IOException, WebApplicationException {

    return mapper.readValue(entityStream, SBBPURLGenResponse.class);
  }
}
