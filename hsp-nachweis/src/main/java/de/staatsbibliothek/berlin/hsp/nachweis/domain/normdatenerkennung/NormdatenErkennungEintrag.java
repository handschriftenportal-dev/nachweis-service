package de.staatsbibliothek.berlin.hsp.nachweis.domain.normdatenerkennung;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Bearbeiter;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.normdaten.NormdatenTyp;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.normdaten.Treffer;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.normdaten.graphql.normdatenerkennung.NormdatenErkennungAnfrage;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.normdaten.graphql.normdatenerkennung.NormdatenErkennungErgebnis;
import io.smallrye.graphql.client.GraphQLError;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@Entity
@Table(name = "normdaten_erkennung_eintrag")
public class NormdatenErkennungEintrag {

  private static final ObjectMapper MAPPER = new ObjectMapper();

  @Id
  @Column(length = 64)
  private String id;

  @Column(columnDefinition = "TEXT", nullable = false)
  private String xpath;

  @Enumerated(EnumType.STRING)
  @Column(name = "normdaten_typ", nullable = false, length = 32)
  private NormdatenTyp normdatenTyp;

  @Column(name = "display_text", columnDefinition = "TEXT", nullable = false)
  private String displayText;

  @Enumerated(EnumType.STRING)
  @Column(name = "selection_type", nullable = false, length = 32)
  private NormdatenErkennungAuswahlTyp selectionType;

  @Column(name = "request_params", columnDefinition = "jsonb")
  private String requestParams;

  @Column(columnDefinition = "jsonb")
  private String response;

  @Column(name = "error_message", columnDefinition = "TEXT")
  private String errorMessage;

  @Column(name = "selected_match_index")
  private Integer selectedMatchIndex;

  @Column(name = "manual_identifier", length = 128)
  private String manualIdentifier;

  @ManyToOne(fetch = FetchType.EAGER,
      cascade = {CascadeType.DETACH, CascadeType.REFRESH})
  @JoinColumn(name = "reviewed_by_id")
  private Bearbeiter reviewedBy;

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;

  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  @Transient
  private NormdatenErkennungErgebnis ergebnis;

  protected NormdatenErkennungEintrag() {
  }

  public NormdatenErkennungEintrag(
      String xpath,
      NormdatenTyp normdatenTyp,
      String displayText
  ) {
    this.id = UUID.randomUUID().toString();
    this.xpath = xpath;
    this.normdatenTyp = normdatenTyp;
    this.displayText = displayText;
    this.selectionType = NormdatenErkennungAuswahlTyp.NONE;
    this.createdAt = LocalDateTime.now();
  }

  private void initializeSelection(NormdatenErkennungErgebnis ergebnis) {
    if (ergebnis == null || ergebnis.getEmpfehlung() == null) {
      return;
    }
    this.selectionType = NormdatenErkennungAuswahlTyp.RECOMMENDATION;
  }

  public NormdatenErkennungErgebnis getErgebnis() {
    if (ergebnis == null && response != null) {
      try {
        ergebnis = MAPPER.readValue(response, NormdatenErkennungErgebnis.class);
      } catch (JsonProcessingException e) {
        log.error("Could not deserialize NormdatenErkennungErgebnis", e);
        return null;
      }
    }
    return ergebnis;
  }

  public void setErgebnis(NormdatenErkennungErgebnis ergebnis) {
    this.ergebnis = ergebnis;
    try {
      this.response = MAPPER.writeValueAsString(ergebnis);
      initializeSelection(ergebnis);
    } catch (JsonProcessingException e) {
      log.error("Could not serialize NormdatenErkennungErgebnis", e);
      this.errorMessage = e.getMessage();
    }
  }

  public Treffer getSelectedTreffer() {
    var ergebnis = getErgebnis();
    if (ergebnis == null) {
      return null;
    }
    return switch (getSelectionType()) {
      case RECOMMENDATION -> ergebnis.getEmpfehlung();
      case MATCH -> {
        if (getSelectedMatchIndex() == null) {
          yield null;
        }
        var treffer = ergebnis.getTreffer();
        yield (treffer != null && getSelectedMatchIndex() < treffer.size())
            ? treffer.get(getSelectedMatchIndex())
            : null;
      }
      default -> null;
    };
  }

  public NormdatenErkennungAnfrage getAnfrage() {
    if (requestParams == null) {
      return null;
    }
    try {
      return MAPPER.readValue(requestParams, NormdatenErkennungAnfrage.class);
    } catch (JsonProcessingException e) {
      log.error("Could not deserialize NormdatenErkennungAnfrage", e);
      return null;
    }
  }

  public void setAnfrage(NormdatenErkennungAnfrage anfrage) {
    try {
      this.requestParams = MAPPER.writeValueAsString(anfrage);
    } catch (JsonProcessingException e) {
      log.error("Could not serialize anfrage", e);
    }
  }

  public void setErrorMessage(String error) {
    this.errorMessage = error;
  }

  public void setErrorMessage(List<GraphQLError> errors) {
    this.errorMessage = errors.stream()
        .map(GraphQLError::getMessage)
        .collect(Collectors.joining(", "));
  }

  public void setErrorMessage(Exception e) {
    this.errorMessage = e.getMessage();
  }

  public void setSelectionNone(Bearbeiter reviewer) {
    this.selectionType = NormdatenErkennungAuswahlTyp.NONE;
    this.selectedMatchIndex = null;
    this.manualIdentifier = null;
    this.reviewedBy = reviewer;
    this.updatedAt = LocalDateTime.now();
  }

  public void setSelectionRecommendation(Bearbeiter reviewer) {
    if (getErgebnis() == null || getErgebnis().getEmpfehlung() == null) {
      return;
    }
    this.selectionType = NormdatenErkennungAuswahlTyp.RECOMMENDATION;
    this.selectedMatchIndex = null;
    this.reviewedBy = reviewer;
    this.updatedAt = LocalDateTime.now();
  }

  public void setSelectionMatchIndex(int index, Bearbeiter reviewer) {
    if (index < 0
        || getErgebnis() == null
        || getErgebnis().getTreffer() == null
        || getErgebnis().getTreffer().size() <= index) {
      return;
    }
    this.selectionType = NormdatenErkennungAuswahlTyp.MATCH;
    this.selectedMatchIndex = index;
    this.reviewedBy = reviewer;
    this.updatedAt = LocalDateTime.now();
  }

  public void setSelectionManualIdentifier(String manualIdentifier, Bearbeiter reviewer) {
    this.selectionType = NormdatenErkennungAuswahlTyp.MANUAL;
    this.manualIdentifier = manualIdentifier;
    this.reviewedBy = reviewer;
    this.updatedAt = LocalDateTime.now();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof NormdatenErkennungEintrag)) {
      return false;
    }
    return Objects.equals(id, ((NormdatenErkennungEintrag) o).id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }

}
