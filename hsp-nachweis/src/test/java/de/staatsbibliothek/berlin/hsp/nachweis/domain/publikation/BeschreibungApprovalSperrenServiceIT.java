package de.staatsbibliothek.berlin.hsp.nachweis.domain.publikation;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.TransactionRequiredException;
import jakarta.transaction.TransactionalException;
import org.junit.jupiter.api.Test;

/**
 * Transaction-contract integration test for {@link BeschreibungApprovalSperrenService}. Verifies
 * that {@link BeschreibungApprovalSperrenService#releaseApprovalSperre(String)} enforces its
 * {@code @Transactional(MANDATORY)} annotation by failing loudly when invoked outside an active
 * transaction. Prevents a regression where the annotation is accidentally downgraded and the Sperre
 * release silently runs in its own transaction, breaking atomicity with the surrounding
 * approve/reject.
 */
@QuarkusTest
class BeschreibungApprovalSperrenServiceIT {

  @Inject
  BeschreibungApprovalSperrenService service;

  @Test
  void releaseApprovalSperre_outsideTransaction_throwsTransactionRequiredException() {
    TransactionalException ex = assertThrows(TransactionalException.class,
        () -> service.releaseApprovalSperre("any-beschreibung-id"));

    assertInstanceOf(TransactionRequiredException.class, ex.getCause(),
        "Expected TxType.MANDATORY to wrap TransactionRequiredException — if this fails the "
            + "annotation on releaseApprovalSperre was likely changed or removed. See the "
            + "class-level Javadoc for why the contract matters.");
  }
}
