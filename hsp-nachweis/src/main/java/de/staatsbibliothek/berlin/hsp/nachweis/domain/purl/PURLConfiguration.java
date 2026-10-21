package de.staatsbibliothek.berlin.hsp.nachweis.domain.purl;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;
import io.smallrye.config.WithName;

/**
 * Configuration interface for the whole {@code purl.*} configuration tree.
 */
@ConfigMapping(prefix = "purl")
public interface PURLConfiguration {

  PURLTargetConfiguration target();

  PURLAutogenerateConfiguration autogenerate();

  PURLResolverConfiguration resolver();

  /**
   * Configuration interface for PURL target URL templates ({@code purl.target.*}).
   */
  interface PURLTargetConfiguration {

    @WithName("kod")
    String kodTemplate();

    @WithName("beschreibung")
    String beschreibungTemplate();

    @WithName("beschreibung_retro")
    String beschreibungRetroTemplate();
  }

  /**
   * Configuration interface for the automatic PURL generation ({@code purl.autogenerate.*}).
   */
  interface PURLAutogenerateConfiguration {

    @WithDefault("true")
    boolean enabled();
  }

  /**
   * Configuration interface for the PURL resolver file generation ({@code purl.resolver.*}).
   */
  interface PURLResolverConfiguration {

    @WithDefault("/tmp/")
    String directory();

    @WithDefault("hsp_purl.txt")
    String txtFileName();

    @WithDefault("hsp_purl.dbm")
    String dbmFileName();

    @WithDefault("httxt2dbm -f DB -i {0} -o {1}")
    String command();

    @WithDefault("https://resolver.staatsbibliothek-berlin.de/")
    String url();

    @WithDefault("false")
    boolean enabled();

    @WithDefault("off")
    @WithName("scheduler-every")
    String schedulerEvery();
  }
}
