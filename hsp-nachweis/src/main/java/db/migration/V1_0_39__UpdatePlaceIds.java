package db.migration;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.PreparedStatement;
import java.sql.Statement;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Java-based Flyway migration to update Place NormdatenReferenz IDs.
 * <p>
 * This migration: 1. Creates a temporary table and loads Place ID mappings from CSV 2. Temporarily
 * adds ON UPDATE CASCADE to all foreign keys referencing normdatenreferenz 3. Updates the
 * normdatenreferenz.id values (CASCADE propagates to all FKs) 4. Restores original FK constraints
 * without CASCADE
 */
public class V1_0_39__UpdatePlaceIds extends BaseJavaMigration {

  private static final Logger logger = LoggerFactory.getLogger(V1_0_39__UpdatePlaceIds.class);

  @Override
  public Integer getChecksum() {
    return 1;
  }

  @Override
  public void migrate(Context context) throws Exception {
    logger.info("Starting Place ID migration...");

    try (Statement stmt = context.getConnection().createStatement()) {
      // Set timeouts for long-running operations
      stmt.execute("SET statement_timeout = '18000000'"); // 300 minutes
      stmt.execute("SET lock_timeout = '1200000'"); // 5 minutes

      // Step 1: Create temporary table for mappings
      stmt.execute("""
          CREATE TEMPORARY TABLE place_id_mappings (
              old_id VARCHAR(255) NOT NULL,
              new_id VARCHAR(255) NOT NULL,
              PRIMARY KEY (old_id)
          )
          """);
      logger.info("Created temporary table: place_id_mappings");
    }

    // Step 2: Load CSV data from resources
    loadCsvData(context);

    // Step 3: Create indexes for performance
    try (Statement stmt = context.getConnection().createStatement()) {
      stmt.execute("CREATE INDEX idx_temp_place_new_id ON place_id_mappings(new_id)");
      logger.info("Created index on place_id_mappings table");
    }

    // Step 4: Drop all FK constraints and re-add with ON UPDATE CASCADE
    dropForeignKeyConstraints(context);
    addCascadeForeignKeyConstraints(context);

    // Step 5: Update normdatenreferenz.id (CASCADE handles all FK updates)
    int updatedCount = updatePlaceIds(context);
    logger.info("Updated {} Place normdatenreferenz records", updatedCount);

    // Step 6: Restore original FK constraints without CASCADE
    dropForeignKeyConstraints(context);
    addOriginalForeignKeyConstraints(context);

    // Step 7: Verify no orphaned foreign keys
    verifyNoOrphanedReferences(context);

    // Step 8: Update Place IDs in teixml columns
    updateKodTeiXml(context);
    updateBeschreibungTeiXml(context);
    updateKatalogTeiXml(context);

    // Step 9: Clean up temporary table
    try (Statement stmt = context.getConnection().createStatement()) {
      stmt.execute("DROP TABLE IF EXISTS place_id_mappings");
    }

    logger.info("Place ID migration completed successfully");
  }

  private void loadCsvData(Context context) throws Exception {
    var csvStream = getClass().getResourceAsStream("/db/migration/data/place_id_mapping.csv");
    if (csvStream == null) {
      throw new IllegalStateException(
          "CSV file not found: /db/migration/data/place_id_mapping.csv");
    }

    logger.info("Loading Place ID mappings from CSV");

    try (BufferedReader reader = new BufferedReader(
        new InputStreamReader(csvStream, StandardCharsets.UTF_8));
        PreparedStatement pstmt = context.getConnection().prepareStatement(
            "INSERT INTO place_id_mappings (old_id, new_id) VALUES (?, ?)")) {

      // Skip header line
      String line = reader.readLine();
      if (line == null || !line.contains("old_id")) {
        throw new IllegalStateException("CSV file missing header line or invalid format");
      }

      int totalRows = 0;
      int lineNumber = 1;

      while ((line = reader.readLine()) != null) {
        lineNumber++;
        line = line.trim();

        if (line.isEmpty()) {
          continue;
        }

        String[] parts = line.split(",");
        if (parts.length != 2) {
          logger.error("Warning: Skipping malformed line {}: {}", lineNumber, line);
          continue;
        }

        String oldId = parts[0].trim();
        String newId = parts[1].trim();

        if (!oldId.startsWith("NORM-") || !newId.startsWith("NORM-")) {
          logger.error(
              "Warning: Skipping line {} - IDs must start with NORM-", lineNumber);
          continue;
        }

        pstmt.setString(1, oldId);
        pstmt.setString(2, newId);
        pstmt.addBatch();
        totalRows++;
      }

      pstmt.executeBatch();
      logger.info("Loaded {} Place ID mappings from CSV", totalRows);
    }

  }

  private void dropForeignKeyConstraints(Context context) throws Exception {
    logger.info("Drop all normdatenreferenz foreign key constraints");
    try (Statement stmt = context.getConnection().createStatement()) {
      // bearbeiter table
      stmt.execute(
          "ALTER TABLE bearbeiter DROP CONSTRAINT IF EXISTS fk_normdatenreferenz_bearbeiter");
      stmt.execute(
          "ALTER TABLE bearbeiter DROP CONSTRAINT IF EXISTS fk_normdatenreferenz_bearbeiter_person");

      // beschreibungsdokument table
      stmt.execute(
          "ALTER TABLE beschreibungsdokument DROP CONSTRAINT IF EXISTS fk_normdatenreferenz_beschreibungssprache");

      // beschreibungsdokument_autoren table
      stmt.execute(
          "ALTER TABLE beschreibungsdokument_autoren DROP CONSTRAINT IF EXISTS fk_autoren_beschreibung");

      // beschreibungsdokument_normdatenreferenz table
      stmt.execute(
          "ALTER TABLE beschreibungsdokument_normdatenreferenz DROP CONSTRAINT IF EXISTS fk_normdatenreferenz_beschreibungsbeteiligte");

      // beschreibungskomponentekopf_grundsprachen table
      stmt.execute(
          "ALTER TABLE beschreibungskomponentekopf_grundsprachen DROP CONSTRAINT IF EXISTS fk_normdatenreferenz_beschreibungsgrundsprache");

      // digitalisat table
      stmt.execute(
          "ALTER TABLE digitalisat DROP CONSTRAINT IF EXISTS fk_normdatenreferenz_digitalisatentstehungsort");
      stmt.execute(
          "ALTER TABLE digitalisat DROP CONSTRAINT IF EXISTS fk_normdatenreferenz_digitalisateinrichtung");

      // digitalisat_normdatenreferenz table
      stmt.execute(
          "ALTER TABLE digitalisat_normdatenreferenz DROP CONSTRAINT IF EXISTS fk_normdatenreferenz_digitalisatkonzepte");

      // identifikation table
      stmt.execute(
          "ALTER TABLE identifikation DROP CONSTRAINT IF EXISTS fk_normdatenreferenz_identifikationbesitzer");
      stmt.execute(
          "ALTER TABLE identifikation DROP CONSTRAINT IF EXISTS fk_normdatenreferenz_identifikationaufbewahrungsort");

      // katalog table
      stmt.execute("ALTER TABLE katalog DROP CONSTRAINT IF EXISTS fk_katalog_verlag");

      // katalog_autoren table
      stmt.execute("ALTER TABLE katalog_autoren DROP CONSTRAINT IF EXISTS fk_katalog_autoren");

      // normdatenreferenz_identifikator table
      stmt.execute(
          "ALTER TABLE normdatenreferenz_identifikator DROP CONSTRAINT IF EXISTS fk_nr_identifikator_normdatenreferenz");

      // normdatenreferenz_variantername table
      stmt.execute(
          "ALTER TABLE normdatenreferenz_variantername DROP CONSTRAINT IF EXISTS fk_nr_variantername_normdatenreferenz");
    }
  }

  private void addCascadeForeignKeyConstraints(Context context) throws Exception {
    logger.info("Adding foreign key constraints with ON UPDATE CASCADE");
    try (Statement stmt = context.getConnection().createStatement()) {
      stmt.execute("""
          ALTER TABLE bearbeiter ADD CONSTRAINT fk_normdatenreferenz_bearbeiter
              FOREIGN KEY (institution_id) REFERENCES normdatenreferenz (id) ON UPDATE CASCADE
          """);
      stmt.execute("""
          ALTER TABLE bearbeiter ADD CONSTRAINT fk_normdatenreferenz_bearbeiter_person
              FOREIGN KEY (person_id) REFERENCES normdatenreferenz (id) ON UPDATE CASCADE
          """);
      stmt.execute("""
          ALTER TABLE beschreibungsdokument ADD CONSTRAINT fk_normdatenreferenz_beschreibungssprache
              FOREIGN KEY (beschreibungssprache_id) REFERENCES normdatenreferenz (id) ON UPDATE CASCADE
          """);
      stmt.execute("""
          ALTER TABLE beschreibungsdokument_autoren ADD CONSTRAINT fk_autoren_beschreibung
              FOREIGN KEY (autoren_id) REFERENCES normdatenreferenz (id) ON UPDATE CASCADE
          """);
      stmt.execute("""
          ALTER TABLE beschreibungsdokument_normdatenreferenz ADD CONSTRAINT fk_normdatenreferenz_beschreibungsbeteiligte
              FOREIGN KEY (beschreibungsbeteiligte_id) REFERENCES normdatenreferenz (id) ON UPDATE CASCADE
          """);
      stmt.execute("""
          ALTER TABLE beschreibungskomponentekopf_grundsprachen ADD CONSTRAINT fk_normdatenreferenz_beschreibungsgrundsprache
              FOREIGN KEY (grundsprachen_id) REFERENCES normdatenreferenz (id) ON UPDATE CASCADE
          """);
      stmt.execute("""
          ALTER TABLE digitalisat ADD CONSTRAINT fk_normdatenreferenz_digitalisatentstehungsort
              FOREIGN KEY (entstehungsort_id) REFERENCES normdatenreferenz (id) ON UPDATE CASCADE
          """);
      stmt.execute("""
          ALTER TABLE digitalisat ADD CONSTRAINT fk_normdatenreferenz_digitalisateinrichtung
              FOREIGN KEY (digitalisierendeeinrichtung_id) REFERENCES normdatenreferenz (id) ON UPDATE CASCADE
          """);
      stmt.execute("""
          ALTER TABLE digitalisat_normdatenreferenz ADD CONSTRAINT fk_normdatenreferenz_digitalisatkonzepte
              FOREIGN KEY (digitalisatskonzepte_id) REFERENCES normdatenreferenz (id) ON UPDATE CASCADE
          """);
      stmt.execute("""
          ALTER TABLE identifikation ADD CONSTRAINT fk_normdatenreferenz_identifikationbesitzer
              FOREIGN KEY (besitzer_id) REFERENCES normdatenreferenz (id) ON UPDATE CASCADE
          """);
      stmt.execute("""
          ALTER TABLE identifikation ADD CONSTRAINT fk_normdatenreferenz_identifikationaufbewahrungsort
              FOREIGN KEY (aufbewahrungsort_id) REFERENCES normdatenreferenz (id) ON UPDATE CASCADE
          """);
      stmt.execute("""
          ALTER TABLE katalog ADD CONSTRAINT fk_katalog_verlag
              FOREIGN KEY (verlag_id) REFERENCES normdatenreferenz (id) ON UPDATE CASCADE
          """);
      stmt.execute("""
          ALTER TABLE katalog_autoren ADD CONSTRAINT fk_katalog_autoren
              FOREIGN KEY (autoren_id) REFERENCES normdatenreferenz (id) ON UPDATE CASCADE
          """);
      stmt.execute("""
          ALTER TABLE normdatenreferenz_identifikator ADD CONSTRAINT fk_nr_identifikator_normdatenreferenz
              FOREIGN KEY (normdatenreferenz_id) REFERENCES normdatenreferenz (id) ON UPDATE CASCADE
          """);
      stmt.execute("""
          ALTER TABLE normdatenreferenz_variantername ADD CONSTRAINT fk_nr_variantername_normdatenreferenz
              FOREIGN KEY (normdatenreferenz_id) REFERENCES normdatenreferenz (id) ON UPDATE CASCADE
          """);
    }
  }

  private int updatePlaceIds(Context context) throws Exception {
    try (Statement stmt = context.getConnection().createStatement()) {
      return stmt.executeUpdate("""
          UPDATE normdatenreferenz n
          SET id = m.new_id
          FROM place_id_mappings m
          WHERE n.id = m.old_id
            AND n.typename = 'Place'
          """);
    }
  }

  private void addOriginalForeignKeyConstraints(Context context) throws Exception {
    logger.info("Restoring original foreign key constraints (without CASCADE)");
    try (Statement stmt = context.getConnection().createStatement()) {
      stmt.execute("""
          ALTER TABLE bearbeiter ADD CONSTRAINT fk_normdatenreferenz_bearbeiter
              FOREIGN KEY (institution_id) REFERENCES normdatenreferenz (id)
          """);
      stmt.execute("""
          ALTER TABLE bearbeiter ADD CONSTRAINT fk_normdatenreferenz_bearbeiter_person
              FOREIGN KEY (person_id) REFERENCES normdatenreferenz (id)
          """);
      stmt.execute("""
          ALTER TABLE beschreibungsdokument ADD CONSTRAINT fk_normdatenreferenz_beschreibungssprache
              FOREIGN KEY (beschreibungssprache_id) REFERENCES normdatenreferenz (id)
          """);
      stmt.execute("""
          ALTER TABLE beschreibungsdokument_autoren ADD CONSTRAINT fk_autoren_beschreibung
              FOREIGN KEY (autoren_id) REFERENCES normdatenreferenz (id)
          """);
      stmt.execute("""
          ALTER TABLE beschreibungsdokument_normdatenreferenz ADD CONSTRAINT fk_normdatenreferenz_beschreibungsbeteiligte
              FOREIGN KEY (beschreibungsbeteiligte_id) REFERENCES normdatenreferenz (id)
          """);
      stmt.execute("""
          ALTER TABLE beschreibungskomponentekopf_grundsprachen ADD CONSTRAINT fk_normdatenreferenz_beschreibungsgrundsprache
              FOREIGN KEY (grundsprachen_id) REFERENCES normdatenreferenz (id)
          """);
      stmt.execute("""
          ALTER TABLE digitalisat ADD CONSTRAINT fk_normdatenreferenz_digitalisatentstehungsort
              FOREIGN KEY (entstehungsort_id) REFERENCES normdatenreferenz (id)
          """);
      stmt.execute("""
          ALTER TABLE digitalisat ADD CONSTRAINT fk_normdatenreferenz_digitalisateinrichtung
              FOREIGN KEY (digitalisierendeeinrichtung_id) REFERENCES normdatenreferenz (id)
          """);
      stmt.execute("""
          ALTER TABLE digitalisat_normdatenreferenz ADD CONSTRAINT fk_normdatenreferenz_digitalisatkonzepte
              FOREIGN KEY (digitalisatskonzepte_id) REFERENCES normdatenreferenz (id)
          """);
      stmt.execute("""
          ALTER TABLE identifikation ADD CONSTRAINT fk_normdatenreferenz_identifikationbesitzer
              FOREIGN KEY (besitzer_id) REFERENCES normdatenreferenz (id)
          """);
      stmt.execute("""
          ALTER TABLE identifikation ADD CONSTRAINT fk_normdatenreferenz_identifikationaufbewahrungsort
              FOREIGN KEY (aufbewahrungsort_id) REFERENCES normdatenreferenz (id)
          """);
      stmt.execute("""
          ALTER TABLE katalog ADD CONSTRAINT fk_katalog_verlag
              FOREIGN KEY (verlag_id) REFERENCES normdatenreferenz (id)
          """);
      stmt.execute("""
          ALTER TABLE katalog_autoren ADD CONSTRAINT fk_katalog_autoren
              FOREIGN KEY (autoren_id) REFERENCES normdatenreferenz (id)
          """);
      stmt.execute("""
          ALTER TABLE normdatenreferenz_identifikator ADD CONSTRAINT fk_nr_identifikator_normdatenreferenz
              FOREIGN KEY (normdatenreferenz_id) REFERENCES normdatenreferenz (id)
          """);
      stmt.execute("""
          ALTER TABLE normdatenreferenz_variantername ADD CONSTRAINT fk_nr_variantername_normdatenreferenz
              FOREIGN KEY (normdatenreferenz_id) REFERENCES normdatenreferenz (id)
          """);
    }
  }

  private void verifyNoOrphanedReferences(Context context) throws Exception {
    try (Statement stmt = context.getConnection().createStatement()) {
      var rs = stmt.executeQuery("""
          SELECT COUNT(*) FROM identifikation i
          WHERE i.aufbewahrungsort_id IS NOT NULL
            AND NOT EXISTS (SELECT 1 FROM normdatenreferenz WHERE id = i.aufbewahrungsort_id)
          """);
      rs.next();
      int orphanCount = rs.getInt(1);
      if (orphanCount > 0) {
        throw new IllegalStateException(
            "Found " + orphanCount + " orphaned identifikation.aufbewahrungsort_id references");
      }

      rs = stmt.executeQuery("""
          SELECT COUNT(*) FROM identifikation i
          WHERE i.besitzer_id IS NOT NULL
            AND NOT EXISTS (SELECT 1 FROM normdatenreferenz WHERE id = i.besitzer_id)
          """);
      rs.next();
      orphanCount = rs.getInt(1);
      if (orphanCount > 0) {
        throw new IllegalStateException(
            "Found " + orphanCount + " orphaned identifikation.besitzer_id references");
      }
    }
    logger.info("Verification passed: No orphaned foreign keys detected");
  }

  private void updateKodTeiXml(Context context) throws Exception {
    logger.info("Updating KulturObjektDokument teixml references");
    try (Statement stmt = context.getConnection().createStatement()) {
      stmt.execute("""
          DO $$
          DECLARE
              mapping RECORD;
              total_updated INTEGER := 0;
              row_updated INTEGER;
          BEGIN
              FOR mapping IN SELECT old_id, new_id FROM place_id_mappings LOOP
                  UPDATE kulturobjektdokument
                  SET teixml = REPLACE(teixml, mapping.old_id, mapping.new_id)
                  WHERE teixml LIKE '%' || mapping.old_id || '%';
          
                  GET DIAGNOSTICS row_updated = ROW_COUNT;
                  total_updated := total_updated + row_updated;
              END LOOP;
              RAISE NOTICE 'Updated % KulturObjektDokument teixml occurrences', total_updated;
          END $$
          """);
    }
    logger.info("Finished - Updaing KulturObjektDokument teixml references");

  }

  private void updateBeschreibungTeiXml(Context context) throws Exception {
    logger.info("Updating Beschreibung teixml references");
    try (Statement stmt = context.getConnection().createStatement()) {
      stmt.execute("""
          DO $$
          DECLARE
              mapping RECORD;
              total_updated INTEGER := 0;
              row_updated INTEGER;
          BEGIN
              FOR mapping IN SELECT old_id, new_id FROM place_id_mappings LOOP
                  UPDATE beschreibungsdokument
                  SET teixml = REPLACE(teixml, mapping.old_id, mapping.new_id)
                  WHERE teixml LIKE '%' || mapping.old_id || '%';
          
                  GET DIAGNOSTICS row_updated = ROW_COUNT;
                  total_updated := total_updated + row_updated;
              END LOOP;
              RAISE NOTICE 'Updated % KulturObjektDokument teixml occurrences', total_updated;
          END $$
          """);
    }
    logger.info("Finished - Updating Beschreibung teixml references");
  }

  private void updateKatalogTeiXml(Context context) throws Exception {
    logger.info("Updating Katalog teixml references");
    try (Statement stmt = context.getConnection().createStatement()) {
      stmt.execute("""
          DO $$
          DECLARE
              mapping RECORD;
              total_updated INTEGER := 0;
              row_updated INTEGER;
          BEGIN
              FOR mapping IN SELECT old_id, new_id FROM place_id_mappings LOOP
                  UPDATE katalog
                  SET tei_xml = REPLACE(tei_xml, mapping.old_id, mapping.new_id)
                  WHERE tei_xml LIKE '%' || mapping.old_id || '%';
          
                  GET DIAGNOSTICS row_updated = ROW_COUNT;
                  total_updated := total_updated + row_updated;
              END LOOP;
              RAISE NOTICE 'Updated % KulturObjektDokument teixml occurrences', total_updated;
          END $$
          """);
    }
    logger.info("Finished - Updating Katalog teixml references");
  }
}
