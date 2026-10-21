package de.staatsbibliothek.berlin.hsp.nachweis.domain.normdatenerkennung;

import java.util.List;
import java.util.UUID;
import org.tei_c.ns._1.Name;
import org.tei_c.ns._1.Resp;
import org.tei_c.ns._1.RespStmt;

public record TEINormdatenErkennungRespStmts(RespStmt idReplacement, RespStmt nameReplacement) {

  private static final String ZUWEISUNG_GND_ID = "Zuweisung einer GND-ID durch";
  private static final String ERGAENZUNG_KEY = "Ergänzung von @key durch";
  private static final String ALGORITHMUS = "Normdatenerkennungsalgorithmus des HSP";

  public TEINormdatenErkennungRespStmts() {
    this(buildRespStmt(ZUWEISUNG_GND_ID, ALGORITHMUS),
        buildRespStmt(ERGAENZUNG_KEY, ALGORITHMUS));
  }

  public TEINormdatenErkennungRespStmts(String bearbeiterName) {
    this(buildRespStmt(ZUWEISUNG_GND_ID, bearbeiterName),
        buildRespStmt(ERGAENZUNG_KEY, bearbeiterName));
  }

  private static RespStmt buildRespStmt(String respText, String performerName) {
    RespStmt respStmt = new RespStmt();
    respStmt.setId("HSP-" + UUID.randomUUID());
    Resp resp = new Resp();
    resp.getContent().add(respText);
    Name name = new Name();
    name.getContent().add(performerName);
    respStmt.getRespsAndNamesAndOrgNames().addAll(List.of(resp, name));
    return respStmt;
  }
}
