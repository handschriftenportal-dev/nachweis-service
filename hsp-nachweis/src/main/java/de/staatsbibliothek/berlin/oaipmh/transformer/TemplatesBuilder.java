package de.staatsbibliothek.berlin.oaipmh.transformer;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import javax.xml.XMLConstants;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamSource;

public class TemplatesBuilder {

  private static final String SAXON_FACTORY = "net.sf.saxon.TransformerFactoryImpl";

  private final StreamSource xsltStream;
  private final Map<String, String> resolverResources;

  public TemplatesBuilder(String xslt) {
    this(xslt, Map.of());
  }

  public TemplatesBuilder(String xslt, Map<String, String> resolverResources) {
    this.xsltStream =
        new StreamSource(new ByteArrayInputStream(xslt.getBytes(StandardCharsets.UTF_8)));
    this.resolverResources = resolverResources;
  }

  public TransformerBuilder compile() {
    try {
      TransformerFactory factory = createFactory();
      return new TransformerBuilder(factory.newTemplates(xsltStream), factory.getURIResolver());
    } catch (TransformerConfigurationException e) {
      throw new TemplateCompilationException("Failed to compile XSLT templates", e);
    }
  }

  private TransformerFactory createFactory() throws TransformerConfigurationException {
    TransformerFactory factory = TransformerFactory.newInstance(SAXON_FACTORY,
        getClass().getClassLoader());
    factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
    factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
    factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");

    if (!resolverResources.isEmpty()) {
      factory.setURIResolver(
          (href, base) -> {
            String content = resolverResources.get(href);
            if (content != null) {
              return new StreamSource(
                  new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)));
            }
            return null;
          });
    }

    return factory;
  }

  public static class TransformerBuilder {

    private final Templates templates;
    private final URIResolver uriResolver;

    public TransformerBuilder(Templates templates, URIResolver uriResolver) {
      this.templates = templates;
      this.uriResolver = uriResolver;
    }

    public Transformer build() {
      try {
        Transformer t = templates.newTransformer();
        if (uriResolver != null) {
          t.setURIResolver(uriResolver);
        }
        return t;
      } catch (TransformerConfigurationException e) {
        throw new TemplateCompilationException("Failed to create transformer from templates", e);
      }
    }

  }
}
