package de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.persistence;

import de.staatsbibliothek.berlin.hsp.domainmodel.entities.PublikationTeiSnapshot;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class PublikationTeiSnapshotRepository
    implements CRUDRepository<PublikationTeiSnapshot, String> {
}
