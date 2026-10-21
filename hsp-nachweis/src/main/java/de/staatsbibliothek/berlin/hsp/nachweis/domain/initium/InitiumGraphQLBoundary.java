package de.staatsbibliothek.berlin.hsp.nachweis.domain.initium;

import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.normdaten.graphql.initium.InitiumPageResult;

/**
 * @author Munhal.Mammadov@sbb.spk-berlin.de
 * @since 28.11.2025
 */

public interface InitiumGraphQLBoundary {

  InitiumPageResult findInitien(int page, int pageSize, InitiumFilter filter);

}
