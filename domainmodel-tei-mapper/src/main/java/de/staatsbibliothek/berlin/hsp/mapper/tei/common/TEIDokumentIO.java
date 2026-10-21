package de.staatsbibliothek.berlin.hsp.mapper.tei.common;

import de.staatsbibliothek.berlin.hsp.mapper.tei.TEICommandException;
import de.staatsbibliothek.berlin.hsp.messaging.objectfactory.TEIObjectFactory;
import jakarta.xml.bind.JAXBException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import org.tei_c.ns._1.TEI;

/**
 * Unmarshals, changes and marshals TEI documents of any type.
 *
 * <p>{@code editIfChanged} and {@code read} confine the mutable JAXB tree to the lambda they
 * invoke; {@code unmarshal} and {@code marshal} hand it out, so the caller owns its lifecycle.</p>
 */
public final class TEIDokumentIO {

  private TEIDokumentIO() {
  }

  /**
   * @param editCallback changes the document in place and returns true when it changed something —
   *                     false skips the marshal
   * @return the new TEI-XML, or empty when the change reported no modification
   */
  public static Optional<String> editIfChanged(String teiXML, Predicate<TEI> editCallback) {

    Objects.requireNonNull(editCallback, "editCallback is required");

    TEI tei = unmarshal(teiXML);

    return editCallback.test(tei)
        ? Optional.of(marshal(tei))
        : Optional.empty();
  }

  /**
   * @param readerCallback must not let the document escape its own return value
   */
  public static <T> T read(String teiXML, Function<TEI, T> readerCallback) {

    Objects.requireNonNull(readerCallback, "leser is required");

    return readerCallback.apply(unmarshal(teiXML));
  }

  /**
   * @throws TEICommandException when the XML is unparsable or contains no TEI element
   */
  public static TEI unmarshal(String teiXML) {

    Objects.requireNonNull(teiXML, "teiXML is required");

    try (InputStream inputStream =
        new ByteArrayInputStream(teiXML.getBytes(StandardCharsets.UTF_8))) {

      return TEIObjectFactory.unmarshalOne(inputStream)
          .orElseThrow(() -> new TEICommandException("TEI-XML contains no TEI element"));

    } catch (JAXBException | IOException exception) {
      throw new TEICommandException("Unable to read TEI-XML", exception);
    }
  }

  /**
   * @throws TEICommandException when the document cannot be serialized
   */
  public static String marshal(TEI tei) {

    Objects.requireNonNull(tei, "tei is required");

    try {
      return TEIObjectFactory.marshal(tei);

    } catch (JAXBException exception) {
      throw new TEICommandException("Unable to write TEI-XML", exception);
    }
  }
}
