package de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.solr;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.impl.Http2SolrClient;
import org.apache.solr.common.SolrInputDocument;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.MountableFile;

/**
 * Integration test for {@link SolrAdminClient} against a real Solr leader/follower pair.
 * <p>
 * Two throwaway containers share a network like in production: the leader carries a
 * ReplicationHandler leader configuration, the follower polls the same core name on the leader
 * (network alias {@code solr-leader}), see {@code src/test/resources/solr-replication-config}. The
 * admin clients are built exactly like in {@code SolrClientProducer}. Runs on demand:
 * {@code mvn test -Dtest=SolrAdminClientIT}.
 */
class SolrAdminClientIT {

  // Matches the Solr version of the test environment (stage/prod run 8.11.2).
  static final String SOLR_IMAGE = "solr:9.4.1";
  // Leader and follower serve the same core name, as in production.
  static final String CORE = "hsp-replication-test";
  static final String LEADER_ALIAS = "solr-leader";
  static final int SOLR_PORT = 8983;

  static Network network;
  static GenericContainer<?> leaderSolr;
  static GenericContainer<?> followerSolr;
  static Http2SolrClient leaderSolrClient;
  static Http2SolrClient followerSolrClient;
  static Http2SolrClient leaderCoreClient;
  static Http2SolrClient followerCoreClient;
  static SolrAdminClient leaderClient;
  static SolrAdminClient followerClient;

  @BeforeAll
  static void setUp() {
    network = Network.newNetwork();
    leaderSolr = createSolrContainer("leader").withNetworkAliases(LEADER_ALIAS);
    leaderSolr.start();
    followerSolr = createSolrContainer("follower")
        // enable the follower role and point it at the leader via the production system
        // properties; the poll interval is short to keep the end-to-end test fast.
        // Solr 9 rejects the follower's leaderUrl unless it is on the URL allowlist.
        .withEnv("SOLR_OPTS", "-Dreplication.slave.enable=true"
            + " -Dreplication.slave.masterHost=" + LEADER_ALIAS
            + " -Dreplication.slave.masterPort=" + SOLR_PORT
            + " -Dreplication.pollInterval=00:00:01"
            + " -Dsolr.disable.allowUrls=true");
    followerSolr.start();

    String leaderUrl = solrUrl(leaderSolr);
    String followerUrl = solrUrl(followerSolr);
    leaderSolrClient = createHttp2Client(leaderUrl);
    followerSolrClient = createHttp2Client(followerUrl);
    leaderClient = new SolrAdminClient(leaderSolrClient, CORE);
    followerClient = new SolrAdminClient(followerSolrClient, CORE);
    leaderCoreClient = new Http2SolrClient.Builder(leaderUrl + "/" + CORE).build();
    followerCoreClient = new Http2SolrClient.Builder(followerUrl + "/" + CORE).build();
  }

  static Http2SolrClient createHttp2Client(String baseUrl) {
    return new Http2SolrClient.Builder(baseUrl)
        .withConnectionTimeout(5_000, TimeUnit.MILLISECONDS)
        .withRequestTimeout(10_000, TimeUnit.MILLISECONDS)
        .build();
  }

  static GenericContainer<?> createSolrContainer(String configset) {
    return new GenericContainer<>(SOLR_IMAGE)
        .withNetwork(network)
        .withExposedPorts(SOLR_PORT)
        .withCopyFileToContainer(
            MountableFile.forClasspathResource("solr-replication-config/" + configset),
            "/opt/configset")
        .withCommand("solr-precreate", CORE, "/opt/configset")
        .waitingFor(
            Wait.forHttp("/solr/" + CORE + "/select?q=*:*")
                .forStatusCode(200)
                .withStartupTimeout(Duration.ofMinutes(2)));
  }

  static String solrUrl(GenericContainer<?> container) {
    return "http://" + container.getHost() + ":" + container.getMappedPort(SOLR_PORT) + "/solr";
  }

  @AfterAll
  static void tearDown() {
    if (leaderCoreClient != null) {
      leaderCoreClient.close();
    }
    if (followerCoreClient != null) {
      followerCoreClient.close();
    }
    if (leaderSolrClient != null) {
      leaderSolrClient.close();
    }
    if (followerSolrClient != null) {
      followerSolrClient.close();
    }
    if (followerSolr != null) {
      followerSolr.stop();
    }
    if (leaderSolr != null) {
      leaderSolr.stop();
    }
    if (network != null) {
      network.close();
    }
  }

  @Test
  void testLeaderReplication_disableAndEnable() throws Exception {
    assertTrue(leaderClient.isReplicationEnabled(), "leader must start with replication enabled");

    leaderClient.disableReplication();
    assertFalse(leaderClient.isReplicationEnabled());

    leaderClient.enableReplication();
    assertTrue(leaderClient.isReplicationEnabled());
  }

  @Test
  void testFollowerPolling_disableAndEnable() throws Exception {
    assertTrue(followerClient.isPollingEnabled(), "follower must start with polling enabled");

    followerClient.disablePoll();
    assertFalse(followerClient.isPollingEnabled());

    followerClient.enablePoll();
    assertTrue(followerClient.isPollingEnabled());
  }

  @Test
  void testIsPollingEnabled_onCoreWithoutFollower_returnsFalse() throws Exception {
    // a leader-only core has no follower section in its replication details - the same shape a
    // single solr instance (e.g. the testsystem) presents to the follower client
    assertFalse(leaderClient.isPollingConfigured());
    assertFalse(leaderClient.isPollingEnabled());

    assertTrue(followerClient.isPollingConfigured());
  }

  @Test
  void testDisablePoll_onLeaderCore_throws() {
    // solr answers HTTP 200 but without status=OK for poll commands on a non-follower core
    assertThrows(SolrServiceException.class, () -> leaderClient.disablePoll());
  }

  @Test
  void testReplication_endToEnd_disablePollStopsFollower() throws Exception {
    // baseline: a document committed on the leader reaches the follower via polling
    String firstDocument = indexDocumentOnLeader();
    assertTrue(waitForDocumentOnFollower(firstDocument, Duration.ofSeconds(30)),
        "follower must replicate the leader index while polling is enabled");

    followerClient.disablePoll();
    try {
      String secondDocument = indexDocumentOnLeader();
      // several poll intervals (00:00:01) pass without the follower fetching anything
      Thread.sleep(4_000);
      assertEquals(0, countDocumentsOnFollower(secondDocument),
          "follower must not replicate while polling is disabled");

      followerClient.enablePoll();
      assertTrue(waitForDocumentOnFollower(secondDocument, Duration.ofSeconds(30)),
          "follower must catch up after polling is enabled again");
    } finally {
      followerClient.enablePoll();
    }
  }

  private String indexDocumentOnLeader() throws Exception {
    String id = UUID.randomUUID().toString();
    SolrInputDocument document = new SolrInputDocument();
    document.addField("id", id);
    leaderCoreClient.add(document);
    leaderCoreClient.commit();
    return id;
  }

  private long countDocumentsOnFollower(String id) throws Exception {
    return followerCoreClient.query(new SolrQuery("id:" + id)).getResults().getNumFound();
  }

  private boolean waitForDocumentOnFollower(String id, Duration timeout) throws Exception {
    long deadline = System.currentTimeMillis() + timeout.toMillis();
    while (System.currentTimeMillis() < deadline) {
      if (countDocumentsOnFollower(id) > 0) {
        return true;
      }
      Thread.sleep(500);
    }
    return false;
  }
}
