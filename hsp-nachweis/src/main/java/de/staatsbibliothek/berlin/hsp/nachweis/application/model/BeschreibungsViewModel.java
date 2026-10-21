package de.staatsbibliothek.berlin.hsp.nachweis.application.model;

import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Beschreibung;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Bearbeiter;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Publikation;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.DokumentObjektTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.PURLTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.PublikationStatus;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.VerwaltungsTyp;
import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.Setter;

/**
 * Created by konrad.eichstaedt@sbb.spk-berlin.de on 11.06.2019.
 */
@Getter
@Setter
public class BeschreibungsViewModel implements Serializable {

  @Serial
  private static final long serialVersionUID = -8499789047921037458L;
  private String importJobID;
  private String id;
  private String titel;
  private String signatur;
  private String externeID;
  private String preconditionResult;
  private boolean preconditionState = false;
  private String teiXML;
  private boolean selected;
  private String kodID;
  private String kodSignatur;
  private String bestandhaltendeInstitutionName;
  private String bestandhaltendeInstitutionOrt;
  private PublikationViewModel publikation;
  private List<NormdatenReferenzViewModel> beteiligte;
  private VerwaltungsTyp verwaltungsTyp;
  private LocalDateTime erstellungsDatum;
  private LocalDateTime aenderungsDatum;
  private NormdatenReferenzViewModel beschreibungsSprache;
  private LizenzViewModel lizenz;
  private List<BeschreibungsAutorView> autoren;
  private boolean ausGedrucktemKatalog;
  private String hspPurl;
  private List<String> externeLinks;
  private LocalDateTime datumErstpublikation;
  private Set<String> bearbeiterNames;
  private Set<String> bearbeiterIds;

  public BeschreibungsViewModel() {
  }

  public BeschreibungsViewModel(BeschreibungsViewBuilder beschreibungsViewBuilder) {
    this.importJobID = beschreibungsViewBuilder.importJobID;
    this.id = beschreibungsViewBuilder.id;
    this.titel = beschreibungsViewBuilder.titel;
    this.signatur = beschreibungsViewBuilder.signatur;
    this.externeID = beschreibungsViewBuilder.externeID;
    this.preconditionResult = beschreibungsViewBuilder.kodView;
    this.teiXML = beschreibungsViewBuilder.teiXML;
    this.bestandhaltendeInstitutionName = beschreibungsViewBuilder.bestandhaltendeInstitutionName;
    this.bestandhaltendeInstitutionOrt = beschreibungsViewBuilder.bestandhaltendeInstitutionOrt;
    this.publikation = beschreibungsViewBuilder.publikation;
    this.beteiligte = beschreibungsViewBuilder.beteiligte;
    this.verwaltungsTyp = beschreibungsViewBuilder.verwaltungsTyp;
    this.kodID = beschreibungsViewBuilder.kodID;
    this.kodSignatur = beschreibungsViewBuilder.kodSignatur;
    this.aenderungsDatum = beschreibungsViewBuilder.aenderungsDatum;
    this.erstellungsDatum = beschreibungsViewBuilder.erstellungsDatum;
    this.beschreibungsSprache = beschreibungsViewBuilder.beschreibungsSprache;
    this.lizenz = beschreibungsViewBuilder.lizenz;
    this.autoren = beschreibungsViewBuilder.autoren;
    this.ausGedrucktemKatalog = beschreibungsViewBuilder.ausGedrucktemKatalog;
    this.hspPurl = beschreibungsViewBuilder.hspPurl;
    this.externeLinks = beschreibungsViewBuilder.externeLinks;
    this.datumErstpublikation = beschreibungsViewBuilder.datumErstpublikation;
    this.bearbeiterIds = beschreibungsViewBuilder.bearbeiter.stream()
        .map(Bearbeiter::getId)
        .collect(Collectors.toCollection(LinkedHashSet::new));
    this.bearbeiterNames = beschreibungsViewBuilder.bearbeiter.stream()
        .map(Bearbeiter::getFullName)
        .collect(Collectors.toCollection(LinkedHashSet::new));
  }

  public boolean isIntern() {
    return this.verwaltungsTyp == VerwaltungsTyp.INTERN;
  }

  public boolean isExtern() {
    return this.verwaltungsTyp == VerwaltungsTyp.EXTERN;
  }

  public boolean isPublished() {
    if (this.publikation == null) {
      return false;
    }
    return this.publikation.status() == PublikationStatus.PUBLISHED;
  }

  /**
   * Checks if this Beschreibung can be edited. A Beschreibung is editable if: - it has no
   * Publikation, or - it has a Publikation in DRAFT status without an active approval request
   */
  public boolean isEditable() {
    if (publikation == null) {
      return true;
    }
    return publikation.status() == PublikationStatus.DRAFT
        && !publikation.hasActiveApprovalRequest();
  }

  /**
   * Checks if a new approval request can be created for this Beschreibung.
   */
  public boolean canRequestApproval() {
    return publikation != null && publikation.canRequestApproval();
  }

  public String getFormattedAutoren() {
    var formattedAutoren = Optional.ofNullable(autoren)
        .stream()
        .flatMap(Collection::stream)
        .map(BeschreibungsAutorView::preferredName)
        .collect(Collectors.joining("; "));
    return shorten(formattedAutoren);
  }

  public String getFormattedTitel() {
    return shorten(titel);
  }

  private String shorten(String input) {
    return shorten(input, 60);
  }

  private String shorten(String input, int length) {
    if (input.length() > length) {
      return input.substring(0, length + 1).trim() + "...";
    }
    return input;
  }

  public boolean getAusGedrucktemKatalog() {
    return ausGedrucktemKatalog;
  }

  public String getFormattedBearbeiter() {
    return String.join("; ", bearbeiterNames);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof BeschreibungsViewModel that)) {
      return false;
    }
    return Objects.equals(id, that.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }

  @Override
  public String toString() {
    return "BeschreibungsViewModel{" +
        "importJobID='" + importJobID + '\'' +
        ", id='" + id + '\'' +
        ", titel='" + titel + '\'' +
        ", signatur='" + signatur + '\'' +
        ", externeID='" + externeID + '\'' +
        ", kodView='" + preconditionResult + '\'' +
        ", kodID='" + kodID + '\'' +
        ", selected=" + selected +
        ", kodGefunden=" + preconditionState +
        ", bestandhaltendeInstitutionName='" + bestandhaltendeInstitutionName + '\'' +
        ", bestandhaltendeInstitutionOrt='" + bestandhaltendeInstitutionOrt + '\'' +
        ", publikation='" + publikation + '\'' +
        ", beteiligte='" + ((beteiligte == null) ? "" + '\''
        : Arrays.toString(beteiligte.toArray()) + '\'') +
        ", verwaltuntsTyp='" + verwaltungsTyp + '\'' +
        ", anderungsDatum='" + aenderungsDatum + '\'' +
        ", erstellungsDatum='" + erstellungsDatum + '\'' +
        ", beschreibungsSprache='" + beschreibungsSprache + '\'' +
        ", lizenz='" + lizenz + '\'' +
        ", autoren='" + autoren + '\'' +
        ", hspPurl='" + hspPurl + '\'' +
        ", externeLinks='" + externeLinks + '\'' +
        ", datumErstpublikation='" + datumErstpublikation + '\'' +
        ", bearbeiterNames='" + bearbeiterNames + '\'' +
        '}';
  }

  public static class BeschreibungsViewBuilder {

    private final List<BeschreibungsAutorView> autoren = new ArrayList<>();
    private final List<String> externeLinks = new ArrayList<>();
    private final List<Bearbeiter> bearbeiter = new ArrayList<>();
    private String importJobID;
    private String id;
    private String titel;
    private String signatur;
    private String externeID;
    private String kodView;
    private String teiXML;
    private String bestandhaltendeInstitutionName;
    private String bestandhaltendeInstitutionOrt;
    private PublikationViewModel publikation;
    private List<NormdatenReferenzViewModel> beteiligte;
    private VerwaltungsTyp verwaltungsTyp;
    private String kodSignatur = "";
    private String kodID;
    private LocalDateTime aenderungsDatum;
    private LocalDateTime erstellungsDatum;
    private NormdatenReferenzViewModel beschreibungsSprache;
    private LizenzViewModel lizenz;
    private boolean ausGedrucktemKatalog;
    private String hspPurl;
    private LocalDateTime datumErstpublikation;

    public BeschreibungsViewBuilder() {
    }

    /**
     * Initialisiert den Builder mit den Werten aus der übergebenen Beschreibung. Es werden nur die
     * Werte übernommen, die auch in der Beschreibung vorhanden sind. Alle anderen Werte werden auf
     * ihren Default-Wert gesetzt (z.B. null oder leere Liste). KOD Signatur und Beabreiter werden
     * nicht uebernommen, da diese Informationen nicht direkt in der Beschreibung vorhanden sind,
     * sondern aus der Datenbank geladen werden muessen. Sie sollten daher explizit gesetzt werden,
     * wenn sie benötigt werden.
     *
     * @param beschreibung Beschreibung, deren Werte für die Initialisierung des Builders verwendet
     *                     werden sollen. Darf nicht null sein.
     */
    public BeschreibungsViewBuilder(Beschreibung beschreibung) {
      Objects.requireNonNull(beschreibung, "Beschreibung darf nicht null sein");
      this.id = beschreibung.getId();
      this.titel = beschreibung.getTitel().orElse("");
      this.teiXML = beschreibung.getTeiXML();
      this.kodID = beschreibung.getKodID();
      this.erstellungsDatum = beschreibung.getErstellungsDatum();
      this.aenderungsDatum = beschreibung.getAenderungsDatum();
      this.publikation = PublikationViewModel.fromEntity(beschreibung.getPublikation());
      this.beteiligte = beschreibung.getBeschreibungsBeteiligte().stream()
          .map(NormdatenReferenzViewModel::from)
          .toList();
      this.verwaltungsTyp = beschreibung.getVerwaltungsTyp();
      this.beschreibungsSprache = NormdatenReferenzViewModel.from(
          beschreibung.getBeschreibungsSprache());
      this.ausGedrucktemKatalog = DokumentObjektTyp.HSP_DESCRIPTION_RETRO.equals(
          beschreibung.getDokumentObjektTyp());

      beschreibung.getGueltigeIdentifikation().ifPresent(identifikation -> {
        this.signatur = identifikation.getIdent();
        Optional.ofNullable(identifikation.getBesitzer())
            .map(NormdatenReferenz::getName)
            .ifPresent(name -> this.bestandhaltendeInstitutionName = name);
        Optional.ofNullable(identifikation.getAufbewahrungsOrt())
            .map(NormdatenReferenz::getName)
            .ifPresent(name -> this.bestandhaltendeInstitutionOrt = name);
      });

      this.lizenz = Optional.ofNullable(beschreibung.getLizenz())
          .map(l -> new LizenzViewModel(
              l.getUris() != null ? new LinkedHashSet<>(l.getUris()) : null,
              l.getBeschreibungsText()))
          .orElse(null);

      this.autoren.addAll(
          beschreibung.getAutoren().stream()
              .map(normdatenReferenz -> new BeschreibungsAutorView(
                  normdatenReferenz.getName(),
                  normdatenReferenz.getGndID()))
              .toList());

      this.hspPurl = beschreibung.getPURLs().stream()
          .filter(purl -> PURLTyp.INTERNAL == purl.getTyp())
          .findFirst()
          .map(purl -> purl.getPurl().toASCIIString())
          .orElse(null);

      this.externeLinks.addAll(
          beschreibung.getPURLs().stream()
              .filter(purl -> PURLTyp.INTERNAL != purl.getTyp())
              .map(purl -> purl.getPurl().toASCIIString())
              .toList());

      this.datumErstpublikation = Optional.ofNullable(beschreibung.getPublikation())
          .map(Publikation::getErstpublikationsDatum)
          .orElse(null);
    }

    public BeschreibungsViewBuilder withID(String id) {

      this.id = id;
      return this;
    }

    public BeschreibungsViewBuilder withImportJobID(String importJobID) {

      this.importJobID = importJobID;
      return this;
    }

    public BeschreibungsViewBuilder withTitel(String titel) {

      this.titel = titel;
      return this;
    }

    public BeschreibungsViewBuilder withSignatur(String signatur) {

      this.signatur = signatur;
      return this;
    }

    public BeschreibungsViewBuilder withExterneID(String externeID) {

      this.externeID = externeID;
      return this;
    }

    public BeschreibungsViewBuilder withKODView(String kodView) {

      this.kodView = kodView;
      return this;
    }

    public BeschreibungsViewBuilder withTEIXML(String teiXML) {
      this.teiXML = teiXML;
      return this;
    }

    public BeschreibungsViewBuilder withPublikation(PublikationViewModel publikation) {
      this.publikation = publikation;
      return this;
    }

    public BeschreibungsViewBuilder withBeteiligte(List<NormdatenReferenz> beteiligte) {
      this.beteiligte = beteiligte == null ? null
          : beteiligte.stream().map(NormdatenReferenzViewModel::from).toList();
      return this;
    }

    public BeschreibungsViewBuilder withBestandhaltendeInstitutionName(
        String bestandhaltendeInstitutionName) {
      this.bestandhaltendeInstitutionName = bestandhaltendeInstitutionName;
      return this;
    }

    public BeschreibungsViewBuilder withBestandhaltendeInstitutionOrt(
        String bestandhaltendeInstitutionOrt) {
      this.bestandhaltendeInstitutionOrt = bestandhaltendeInstitutionOrt;
      return this;
    }

    public BeschreibungsViewBuilder withVerwaltungsTyp(VerwaltungsTyp verwaltungsTyp) {
      this.verwaltungsTyp = verwaltungsTyp;
      return this;
    }

    public BeschreibungsViewBuilder withKODID(String kodID) {
      this.kodID = kodID;
      return this;
    }

    public BeschreibungsViewBuilder withKODSignatur(String signatur) {
      this.kodSignatur = signatur;
      return this;
    }

    public BeschreibungsViewBuilder withAenderungsDatum(LocalDateTime aenderungsDatum) {
      this.aenderungsDatum = aenderungsDatum;
      return this;
    }

    public BeschreibungsViewBuilder withErstellungsDatum(LocalDateTime erstellungsDatum) {
      this.erstellungsDatum = erstellungsDatum;
      return this;
    }

    public BeschreibungsViewBuilder withBeschreibungsSprache(
        NormdatenReferenz beschreibungsSprache) {
      this.beschreibungsSprache = NormdatenReferenzViewModel.from(beschreibungsSprache);
      return this;
    }

    public BeschreibungsViewBuilder withLizenz(LizenzViewModel lizenz) {
      this.lizenz = lizenz;
      return this;
    }

    public BeschreibungsViewBuilder withAutoren(List<BeschreibungsAutorView> autoren) {
      this.autoren.addAll(autoren);
      return this;
    }

    public BeschreibungsViewBuilder withGedrucktemKatalog(boolean ausGedrucktemKatalog) {
      this.ausGedrucktemKatalog = ausGedrucktemKatalog;
      return this;
    }

    public BeschreibungsViewBuilder withHspPurl(String hspPurl) {
      this.hspPurl = hspPurl;
      return this;
    }

    public BeschreibungsViewBuilder withExterneLinks(List<String> externeLinks) {
      this.externeLinks.clear();
      if (Objects.nonNull(externeLinks)) {
        this.externeLinks.addAll(externeLinks);
      }
      return this;
    }

    public BeschreibungsViewBuilder addExternenLink(String externerLink) {
      this.externeLinks.add(externerLink);
      return this;
    }

    public BeschreibungsViewBuilder withDatumErstpublikation(LocalDateTime datumErstpublikation) {
      this.datumErstpublikation = datumErstpublikation;
      return this;
    }

    public BeschreibungsViewBuilder withBearbeiter(Collection<Bearbeiter> bearbeiter) {
      this.bearbeiter.addAll(bearbeiter);
      return this;
    }

    public BeschreibungsViewModel build() {
      return new BeschreibungsViewModel(this);
    }

  }
}
