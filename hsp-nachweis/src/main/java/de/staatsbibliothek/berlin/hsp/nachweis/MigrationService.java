package de.staatsbibliothek.berlin.hsp.nachweis;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.flywaydb.core.Flyway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Piotr.Czarnecki@sbb.spk-berlin.de
 * @since 17.12.20
 */
@ApplicationScoped
public class MigrationService {

  private static final Logger logger = LoggerFactory.getLogger(MigrationService.class);

  @Inject
  Flyway flyway;

  public void checkMigration() {
    // This will print 1.0.0
    logger.info("Database version {}", flyway.info().current().getVersion());
  }

}
