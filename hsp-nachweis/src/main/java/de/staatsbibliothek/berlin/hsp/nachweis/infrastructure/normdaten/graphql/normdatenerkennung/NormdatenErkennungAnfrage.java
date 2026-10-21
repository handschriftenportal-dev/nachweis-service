package de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.normdaten.graphql.normdatenerkennung;

import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.normdaten.NormdatenTyp;
import org.eclipse.microprofile.graphql.Name;

@Name("ErkennungsAnfrageInput")
public record NormdatenErkennungAnfrage(String id, String name, NormdatenTyp typ) {

}
