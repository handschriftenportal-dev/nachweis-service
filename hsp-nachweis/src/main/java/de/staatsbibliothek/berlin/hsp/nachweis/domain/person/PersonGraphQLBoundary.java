package de.staatsbibliothek.berlin.hsp.nachweis.domain.person;

import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.normdaten.graphql.person.PersonPageResult;

/**
 * @author Munhal.Mammadov@sbb.spk-berlin.de
 * @since 24.03.2026
 */

public interface PersonGraphQLBoundary {

  PersonPageResult findPersons(int page, int pageSize, PersonFilter filter);

}
