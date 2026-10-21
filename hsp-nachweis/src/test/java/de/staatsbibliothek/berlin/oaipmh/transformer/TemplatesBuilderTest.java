package de.staatsbibliothek.berlin.oaipmh.transformer;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.Map;
import javax.xml.transform.Transformer;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.junit.jupiter.api.Test;

class TemplatesBuilderTest {

  private static final String IDENTITY_XSLT =
      """
          <?xml version="1.0" encoding="UTF-8"?>
          <xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
            <xsl:output method="xml" indent="no" omit-xml-declaration="yes"/>
            <xsl:template match="/">
              <xsl:copy-of select="."/>
            </xsl:template>
          </xsl:stylesheet>
          """;

  private static final String EXTRACT_XSLT =
      """
          <?xml version="1.0" encoding="UTF-8"?>
          <xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
            <xsl:output method="xml" indent="no" omit-xml-declaration="yes"/>
            <xsl:template match="/">
              <result><xsl:value-of select="/root/name"/></result>
            </xsl:template>
          </xsl:stylesheet>
          """;

  private static final String DOCUMENT_XSLT =
      """
          <?xml version="1.0" encoding="UTF-8"?>
          <xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
            <xsl:output method="xml" indent="no" omit-xml-declaration="yes"/>
            <xsl:variable name="lookup" select="document('data.xml')"/>
            <xsl:template match="/">
              <result>
                <fromInput><xsl:value-of select="/root/key"/></fromInput>
                <fromLookup><xsl:value-of select="$lookup/data/value"/></fromLookup>
              </result>
            </xsl:template>
          </xsl:stylesheet>
          """;

  private static final String LOOKUP_XML = "<data><value>resolved-value</value></data>";

  private static final String INPUT_XML = "<root><name>hello</name><key>K1</key></root>";

  private static final String INVALID_XSLT = "<not-a-stylesheet>broken</not-a-stylesheet>";

  @Test
  void testCompile_withValidXslt_producesWorkingTransformer() throws Exception {
    Transformer transformer = new TemplatesBuilder(EXTRACT_XSLT).compile().build();

    assertNotNull(transformer);

    String output = transform(transformer, INPUT_XML);
    assertTrue(output.contains("<result>hello</result>"),
        "Transformer should extract <name> value");
  }

  @Test
  void testCompile_withInvalidXslt_throwsTemplateCompilationException() {
    TemplatesBuilder builder = new TemplatesBuilder(INVALID_XSLT);

    assertThrows(TemplateCompilationException.class, builder::compile);
  }

  @Test
  void testCompile_producesReusableTemplates() throws Exception {
    TemplatesBuilder.TransformerBuilder transformerBuilder =
        new TemplatesBuilder(EXTRACT_XSLT).compile();

    assertNotNull(transformerBuilder);

    Transformer t1 = transformerBuilder.build();
    Transformer t2 = transformerBuilder.build();

    String output1 = transform(t1, INPUT_XML);
    String output2 = transform(t2, "<root><name>world</name></root>");

    assertTrue(output1.contains("<result>hello</result>"),
        "First transformer should produce correct output");
    assertTrue(output2.contains("<result>world</result>"),
        "Second transformer should independently produce correct output");
  }

  @Test
  void testCompile_withResolverResources_resolvesDocumentFunction() throws Exception {
    Transformer transformer =
        new TemplatesBuilder(DOCUMENT_XSLT, Map.of("data.xml", LOOKUP_XML)).compile().build();

    String output = transform(transformer, INPUT_XML);

    assertTrue(output.contains("<fromInput>K1</fromInput>"),
        "Should contain value from input document");
    assertTrue(output.contains("<fromLookup>resolved-value</fromLookup>"),
        "Should contain value resolved via document() from URI resolver");
  }

  @Test
  void testCompile_withResolverResources_producesReusableTemplates() throws Exception {
    TemplatesBuilder.TransformerBuilder transformerBuilder =
        new TemplatesBuilder(DOCUMENT_XSLT, Map.of("data.xml", LOOKUP_XML)).compile();

    Transformer t1 = transformerBuilder.build();
    Transformer t2 = transformerBuilder.build();

    String output1 = transform(t1, INPUT_XML);
    String output2 = transform(t2, "<root><key>K2</key></root>");

    assertTrue(output1.contains("<fromLookup>resolved-value</fromLookup>"),
        "First transformer should resolve document() via URI resolver");
    assertTrue(output2.contains("<fromLookup>resolved-value</fromLookup>"),
        "Second transformer from same Templates should also resolve document()");
    assertTrue(output1.contains("<fromInput>K1</fromInput>"),
        "First transformer should read input key");
    assertTrue(output2.contains("<fromInput>K2</fromInput>"),
        "Second transformer should read its own input key");
  }

  @Test
  void testCompile_unknownHrefInResolver_failsDuringTransform() {
    // XSLT references 'data.xml' but we only provide 'other.xml' in the resolver map.
    // Saxon should fail because it cannot resolve 'data.xml' and secure processing
    // blocks external access, so we expect an error during transformation.
    TemplatesBuilder.TransformerBuilder transformerBuilder =
        assertDoesNotThrow(
            () -> new TemplatesBuilder(DOCUMENT_XSLT, Map.of("other.xml", "<unused/>")).compile(),
            "Compile should succeed; document() is resolved lazily");

    Transformer transformer = assertDoesNotThrow(transformerBuilder::build,
        "Build should succeed; document() is resolved at transform time");

    // Transformation should fail because 'data.xml' cannot be resolved.
    assertThrows(javax.xml.transform.TransformerException.class,
        () -> transform(transformer, INPUT_XML),
        "Transform should fail when document('data.xml') cannot be resolved");
  }

  @Test
  void testCompile_emptyResolverResourcesMap_basicXsltStillWorks() throws Exception {
    Transformer transformer = new TemplatesBuilder(EXTRACT_XSLT, Map.of()).compile().build();

    assertNotNull(transformer);

    String output = transform(transformer, INPUT_XML);
    assertTrue(output.contains("<result>hello</result>"),
        "Empty resolver map should not interfere with basic XSLT");
  }

  private String transform(Transformer transformer, String inputXml) throws Exception {
    StringWriter writer = new StringWriter();
    transformer.transform(
        new StreamSource(new StringReader(inputXml)),
        new StreamResult(writer));
    return writer.toString();
  }
}
