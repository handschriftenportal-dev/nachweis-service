package de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.solr;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import java.time.Duration;
import java.util.Map;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.MountableFile;

/**
 * Starts a throwaway Solr container for {@link SolrSuchAdapterIT}, pre-creating the core from the
 * project's own configset ({@code src/main/resources/solr-config/conf}) so the index fields and
 * facets match production. The container URL/core are injected into the {@code solr.*} config
 * properties read by {@link SolrSuchAdapter}, overriding the placeholder values in
 * application-test.yml.
 *
 * <p>The production {@code conf} directory is mounted (so the managed-schema and all analyzer
 * resource files behave like production), but its Solr 8-era {@code solrconfig.xml} is overlaid
 * with a minimal Solr 9 config ({@code solr-test-config/solrconfig.xml}); the production one
 * references classes removed/modularized in Solr 9 and fails core load. The stale top-level
 * {@code core.properties} (name=hsp-nachweis, schema=schema.xml) is intentionally ignored so
 * {@code solr-precreate} generates a fresh core descriptor for the test core name.
 */
public class SolrTestResource implements QuarkusTestResourceLifecycleManager {

  // Matches the Solr version of the test environment (stage/prod run 8.11.2).
  private static final String SOLR_IMAGE = "solr:9.4.1";
  private static final String CORE = "hsp-nachweis-dev-unittest";
  private static final int SOLR_PORT = 8983;

  private GenericContainer<?> solr;

  @Override
  public Map<String, String> start() {
    solr =
        new GenericContainer<>(SOLR_IMAGE)
            .withExposedPorts(SOLR_PORT)
            .withCopyFileToContainer(
                MountableFile.forClasspathResource("solr-config/conf"), "/opt/hsp-configset/conf")
            .withCopyFileToContainer(
                MountableFile.forClasspathResource("solr-test-config/solrconfig.xml"),
                "/opt/hsp-configset/conf/solrconfig.xml")
            .withCommand("solr-precreate", CORE, "/opt/hsp-configset")
            .waitingFor(
                Wait.forHttp("/solr/" + CORE + "/select?q=*:*")
                    .forStatusCode(200)
                    .withStartupTimeout(Duration.ofMinutes(2)));
    solr.start();

    String url = "http://" + solr.getHost() + ":" + solr.getMappedPort(SOLR_PORT) + "/solr";
    return Map.of(
        "solr.url.leader", url,
        "solr.url.follower", url,
        "solr.core.nachweis", CORE);
  }

  @Override
  public void stop() {
    if (solr != null) {
      solr.stop();
    }
  }
}