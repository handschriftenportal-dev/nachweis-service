package de.staatsbibliothek.berlin.hsp.nachweis.application.model;

import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Abmessung;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Beschreibstoff;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Buchschmuck;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Entstehungsort;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Entstehungszeit;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Format;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Grundsprache;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Handschriftentyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Musiknotation;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Status;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Titel;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Umfang;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.AttributsReferenzStatusTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.ExterneRessource;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Forschungsdokumentation;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Identifikation;
import java.io.ByteArrayInputStream;
import java.io.Serial;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.Setter;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 26.08.2020.
 * @version 1.0
 */
@Getter
public class KulturObjektDokumentViewModel implements Serializable {

  @Serial
  private static final long serialVersionUID = 3098529522399106936L;

  @Setter
  private String id;

  @Setter
  private String signatur;

  @Setter
  private Set<IdentifikationViewModel> alternativeSignaturen;

  @Setter
  private String teiXML;

  @Setter
  private String bestandhaltendeInstitutionName;

  @Setter
  private String bestandhaltendeInstitutionOrt;

  @Setter
  private Status status;

  @Setter
  private Titel titel;

  @Setter
  private Entstehungszeit entstehungszeit;

  @Setter
  private Entstehungsort entstehungsort;

  @Setter
  private Handschriftentyp handschriftentyp;

  @Setter
  private Beschreibstoff beschreibstoff;

  @Setter
  private Format format;

  @Setter
  private Umfang umfang;

  @Setter
  private Abmessung abmessung;

  @Setter
  private Buchschmuck buchschmuck;

  @Setter
  private Musiknotation musiknotation;

  @Setter
  private Grundsprache grundsprache;

  private List<DigitalisatViewModel> digitalisate;

  private List<BeschreibungsViewModel> beschreibungen;

  @Setter
  private String hspPurl;

  @Setter
  private List<String> externeLinks;

  @Setter
  private List<ExterneRessource> externeRessourcen;

  @Setter
  private List<Forschungsdokumentation> forschungsdokumentationen;

  private AttributsReferenzStatusTyp attributsReferenzStatusTyp;

  public KulturObjektDokumentViewModel() {
  }

  public StreamedContent getTeiFile() {
    if (Objects.nonNull(this.teiXML) && !this.teiXML.isEmpty() && Objects.nonNull(this.id)) {
      return DefaultStreamedContent.builder()
          .name("tei-" + this.id + ".xml")
          .contentType("text/xml")
          .stream(() -> new ByteArrayInputStream(teiXML.getBytes(StandardCharsets.UTF_8)))
          .build();
    } else {
      return null;
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof KulturObjektDokumentViewModel that)) {
      return false;
    }
    return id.equals(that.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }

  public static final class KulturObjektDokumentViewModelBuilder {

    private final List<DigitalisatViewModel> digitalisate = new ArrayList<>();
    private final List<BeschreibungsViewModel> beschreibungen = new ArrayList<>();
    private final List<String> externeLinks = new ArrayList<>();
    private final List<ExterneRessource> externeRessourcen = new ArrayList<>();
    private final List<Forschungsdokumentation> forschungsdokumentationen = new ArrayList<>();
    private String id;
    private String signatur;
    private Set<IdentifikationViewModel> alternativeSignaturen = new HashSet<>();
    private String teiXML;
    private String bestandhaltendeInstitutionName;
    private String bestandhaltendeInstitutionOrt;
    private Status status;
    private Titel titel;
    private Entstehungszeit entstehungszeit;
    private Entstehungsort entstehungsort;
    private Handschriftentyp handschriftentyp;
    private Beschreibstoff beschreibstoff;
    private Umfang umfang;
    private Format format;
    private Abmessung abmessung;
    private Buchschmuck buchschmuck;
    private Musiknotation musiknotation;
    private Grundsprache grundsprache;
    private String hspPurl;
    private AttributsReferenzStatusTyp attributsReferenzStatusTyp;

    private KulturObjektDokumentViewModelBuilder() {
    }

    public static KulturObjektDokumentViewModelBuilder KulturObjektDokumentViewModel() {
      return new KulturObjektDokumentViewModelBuilder();
    }

    public KulturObjektDokumentViewModelBuilder withId(String id) {
      this.id = id;
      return this;
    }

    public KulturObjektDokumentViewModelBuilder withSignatur(String signatur) {
      this.signatur = signatur;
      return this;
    }

    public KulturObjektDokumentViewModelBuilder withAlternativeSignaturen(
        Set<Identifikation> alternativeSignaturen) {
      this.alternativeSignaturen = alternativeSignaturen.stream().map(identifikation ->
          new IdentifikationViewModel(
              identifikation.getId(),
              identifikation.getIdent(),
              identifikation.getAufbewahrungsOrt() != null ? identifikation.getAufbewahrungsOrt()
                  .getName() : "",
              identifikation.getBesitzer() != null ? identifikation.getBesitzer().getName() : ""
          )
      ).collect(Collectors.toSet());
      return this;
    }

    public KulturObjektDokumentViewModelBuilder addAlternativeSignatur(
        IdentifikationViewModel alternativeSignatur) {
      this.alternativeSignaturen.add(alternativeSignatur);
      return this;
    }

    public KulturObjektDokumentViewModelBuilder withTeiXML(String teiXML) {
      this.teiXML = teiXML;
      return this;
    }

    public KulturObjektDokumentViewModelBuilder withBestandhaltendeInstitutionName(
        String bestandhaltendeInstitutionName) {
      this.bestandhaltendeInstitutionName = bestandhaltendeInstitutionName;
      return this;
    }

    public KulturObjektDokumentViewModelBuilder withBestandhaltendeInstitutionOrt(
        String bestandhaltendeInstitutionOrt) {
      this.bestandhaltendeInstitutionOrt = bestandhaltendeInstitutionOrt;
      return this;
    }

    public KulturObjektDokumentViewModelBuilder withStatus(Status status) {
      this.status = status;
      return this;
    }

    public KulturObjektDokumentViewModelBuilder withTitel(Titel titel) {
      this.titel = titel;
      return this;
    }

    public KulturObjektDokumentViewModelBuilder withEntstehungszeit(
        Entstehungszeit entstehungszeit) {
      this.entstehungszeit = entstehungszeit;
      return this;
    }

    public KulturObjektDokumentViewModelBuilder withEntstehungsort(Entstehungsort entstehungsort) {
      this.entstehungsort = entstehungsort;
      return this;
    }

    public KulturObjektDokumentViewModelBuilder withHandschriftentyp(
        Handschriftentyp handschriftentyp) {
      this.handschriftentyp = handschriftentyp;
      return this;
    }

    public KulturObjektDokumentViewModelBuilder withBeschreibstoff(Beschreibstoff beschreibstoff) {
      this.beschreibstoff = beschreibstoff;
      return this;
    }

    public KulturObjektDokumentViewModelBuilder withAbmessung(Abmessung abmessung) {
      this.abmessung = abmessung;
      return this;
    }

    public KulturObjektDokumentViewModelBuilder withFormat(Format format) {
      this.format = format;
      return this;
    }

    public KulturObjektDokumentViewModelBuilder withUmfang(Umfang umfang) {
      this.umfang = umfang;
      return this;
    }

    public KulturObjektDokumentViewModelBuilder withBuchschmuck(Buchschmuck buchschmuck) {
      this.buchschmuck = buchschmuck;
      return this;
    }

    public KulturObjektDokumentViewModelBuilder withMusiknotation(Musiknotation musiknotation) {
      this.musiknotation = musiknotation;
      return this;
    }

    public KulturObjektDokumentViewModelBuilder withGrundSprache(Grundsprache grundSprache) {
      this.grundsprache = grundSprache;
      return this;
    }

    public KulturObjektDokumentViewModelBuilder addDigitalisat(
        DigitalisatViewModel digitalisatViewModel) {
      this.digitalisate.add(digitalisatViewModel);
      return this;
    }

    public KulturObjektDokumentViewModelBuilder withDigitalisats(
        List<DigitalisatViewModel> digitalisatViewModel) {
      this.digitalisate.addAll(digitalisatViewModel);
      return this;
    }

    public KulturObjektDokumentViewModelBuilder withBeschreibungen(
        List<BeschreibungsViewModel> beschreibungenViewModel) {
      this.beschreibungen.addAll(beschreibungenViewModel);
      return this;
    }

    public KulturObjektDokumentViewModelBuilder addBeschreibung(
        BeschreibungsViewModel beschreibungenViewModel) {
      this.beschreibungen.add(beschreibungenViewModel);
      return this;
    }

    public KulturObjektDokumentViewModelBuilder withHspPurl(String hspPurl) {
      this.hspPurl = hspPurl;
      return this;
    }

    public KulturObjektDokumentViewModelBuilder withExterneLinks(List<String> externeLinks) {
      this.externeLinks.clear();
      if (Objects.nonNull(externeLinks)) {
        this.externeLinks.addAll(externeLinks);
      }
      return this;
    }

    public KulturObjektDokumentViewModelBuilder addExternenLink(String externerLink) {
      this.externeLinks.add(externerLink);
      return this;
    }

    public KulturObjektDokumentViewModelBuilder withExterneRessourcen(
        List<ExterneRessource> externeRessourcen) {
      this.externeRessourcen.clear();
      if (Objects.nonNull(externeRessourcen)) {
        this.externeRessourcen.addAll(externeRessourcen);
      }
      return this;
    }

    public KulturObjektDokumentViewModelBuilder withForschungsdokumentationen(
        List<Forschungsdokumentation> forschungsdokumentationen) {
      this.forschungsdokumentationen.clear();
      if (Objects.nonNull(forschungsdokumentationen)) {
        this.forschungsdokumentationen.addAll(forschungsdokumentationen);
      }
      return this;
    }

    public KulturObjektDokumentViewModelBuilder addAttributsReferenzStatusTyp(
        AttributsReferenzStatusTyp attributsReferenzStatusTyp) {
      this.attributsReferenzStatusTyp = attributsReferenzStatusTyp;
      return this;
    }

    public KulturObjektDokumentViewModel build() {
      KulturObjektDokumentViewModel kulturObjektDokumentViewModel = new KulturObjektDokumentViewModel();
      kulturObjektDokumentViewModel.setId(id);
      kulturObjektDokumentViewModel.setSignatur(signatur);
      kulturObjektDokumentViewModel.setAlternativeSignaturen(alternativeSignaturen);
      kulturObjektDokumentViewModel.setTeiXML(teiXML);
      kulturObjektDokumentViewModel.setBestandhaltendeInstitutionName(
          bestandhaltendeInstitutionName);
      kulturObjektDokumentViewModel.setBestandhaltendeInstitutionOrt(bestandhaltendeInstitutionOrt);
      kulturObjektDokumentViewModel.setStatus(status);
      kulturObjektDokumentViewModel.setTitel(titel);
      kulturObjektDokumentViewModel.setEntstehungszeit(entstehungszeit);
      kulturObjektDokumentViewModel.setEntstehungsort(entstehungsort);
      kulturObjektDokumentViewModel.setHandschriftentyp(handschriftentyp);
      kulturObjektDokumentViewModel.setBeschreibstoff(beschreibstoff);
      kulturObjektDokumentViewModel.setAbmessung(abmessung);
      kulturObjektDokumentViewModel.setFormat(format);
      kulturObjektDokumentViewModel.setUmfang(umfang);
      kulturObjektDokumentViewModel.setBuchschmuck(buchschmuck);
      kulturObjektDokumentViewModel.setMusiknotation(musiknotation);
      kulturObjektDokumentViewModel.setGrundsprache(grundsprache);
      kulturObjektDokumentViewModel.digitalisate = digitalisate;
      kulturObjektDokumentViewModel.beschreibungen = beschreibungen;
      kulturObjektDokumentViewModel.hspPurl = hspPurl;
      kulturObjektDokumentViewModel.externeLinks = externeLinks;
      kulturObjektDokumentViewModel.setExterneRessourcen(externeRessourcen);
      kulturObjektDokumentViewModel.setForschungsdokumentationen(forschungsdokumentationen);
      kulturObjektDokumentViewModel.attributsReferenzStatusTyp = attributsReferenzStatusTyp;
      return kulturObjektDokumentViewModel;
    }
  }
}
