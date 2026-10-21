package de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.kodregistrieren;

import java.util.List;

public record IdnoRegistrierung(String gueltigeSignatur,
                                String sortKey,
                                List<String> altSignaturen) {

}
