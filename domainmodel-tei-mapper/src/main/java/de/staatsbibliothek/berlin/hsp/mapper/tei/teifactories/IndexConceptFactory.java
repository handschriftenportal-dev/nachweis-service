package de.staatsbibliothek.berlin.hsp.mapper.tei.teifactories;

import static de.staatsbibliothek.berlin.hsp.mapper.tei.teifactories.IndexFactory.setChangeID;
import static de.staatsbibliothek.berlin.hsp.mapper.tei.teifactories.IndexFactory.setIndexSourceAndCopyOf;

import de.staatsbibliothek.berlin.hsp.domainmodel.entities.AttributsReferenz;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import org.tei_c.ns._1.Index;
import org.tei_c.ns._1.Term;

public abstract class IndexConceptFactory<T extends AttributsReferenz> {

  protected IndexConceptFactory() {
  }

  abstract String getIndexType();

  abstract void addTermsToIndex(Index index, T attributsReferenz);


  protected void build(T attributsReferenz, List<Index> result) {
    if (Objects.isNull(attributsReferenz)) {
      return;
    }
    Index index = createIndex(attributsReferenz);
    addTermsToIndex(index, attributsReferenz);
    result.add(index);
  }

  protected Index createIndex(T attributsReferenz) {
    Index index = new Index();
    index.setIndexName(getIndexType());
    setChangeID(index, attributsReferenz);
    setIndexSourceAndCopyOf(index, attributsReferenz.getReferenzId(),
        attributsReferenz.getReferenzURL());
    return index;
  }

  protected Term createTerm(String type, String content) {
    Objects.requireNonNull(type, "type is required!");
    Term term = new Term();
    term.setType(type);
    if (Objects.nonNull(content) && !content.isEmpty()) {
      term.getContent().add(content);
    }
    return term;
  }

  protected Term createTextTerm(String type, String text) {
    if (text == null || text.isBlank()) {
      return createTerm(type, null);
    }
    return createTerm(type, text);
  }

  protected Term createEmptyTerm(String type) {
    return createTerm(type, null);
  }

  protected List<Term> createNormdatenTerms(String type,
      List<NormdatenReferenz> normdatenReferenzen) {
    return createNormdatenTerms(type, normdatenReferenzen, true);
  }

  protected List<Term> createNormdatenTerms(String type,
      List<NormdatenReferenz> normdatenReferenzen,
      boolean withContent) {
    if (normdatenReferenzen == null || normdatenReferenzen.isEmpty()) {
      return List.of(createEmptyTerm(type));
    }

    return Stream.ofNullable(normdatenReferenzen)
        .flatMap(Collection::stream)
        .map(n -> createNormdatenTerm(n, type, withContent))
        .toList();
  }

  protected Term createNormdatenTerm(NormdatenReferenz normdatenReferenz, String type,
      boolean withContent) {
    if (normdatenReferenz == null || normdatenReferenz.getId() == null) {
      return createEmptyTerm(type);
    }
    Term term = new Term();
    term.setType(type);

    var id = normdatenReferenz.getId();
    if (id != null && !id.isBlank()) {
      term.setKey(id);
    }
    var uri = normdatenReferenz.getUri();
    if (uri != null && !uri.isBlank()) {
      term.getReves().add(uri);
    }
    var name = normdatenReferenz.getName();
    if (withContent && name != null && !name.isBlank()) {
      term.getContent().add(name);
    }
    return term;
  }

  protected Term createNormdatenTerm(NormdatenReferenz normdatenReferenz, String type) {
    return createNormdatenTerm(normdatenReferenz, type, true);
  }

  protected Term createNormdatenTermNoContent(NormdatenReferenz normdatenReferenz, String type) {
    return createNormdatenTerm(normdatenReferenz, type, false);
  }

  protected void addTermToIndex(Index index, Term term) {
    if (index != null && term != null) {
      index.getTermsAndIndices().add(term);
    }
  }
}
