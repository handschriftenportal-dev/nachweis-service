package de.staatsbibliothek.berlin.hsp.nachweis;

import de.staatsbibliothek.berlin.javaee.authentication.interceptor.LoginCheck;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Priority;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;

/**
 * Replacement for AuthenticationInterceptorImplementation in tests. Temporary fix until bug in
 * AuthenticationInterceptorImplementation is fixed: The bug is at the end of
 * postConstructInterceptCommon:
 * <code>
 * <p>
 * if (oauth2Config.isLoginCheckEnabled() && !authenticationInstance.isUnsatisfied()) { // ... all
 * the login logic ... if (authentication.isAlreadyLoggedIn()) { return invocationContext.proceed();
 * // <-- only path that calls proceed() } // ... }
 * <p>
 * return null; // <-- BUG: when logincheck is disabled, falls through here
 * </code>
 *
 * <p>The library interceptor has a bug: when login check is disabled
 * ({@code oauth2.logincheck.enabled=false}), it returns null without calling
 * {@code invocationContext.proceed()}, preventing {@code @PostConstruct} methods from executing.
 * This replacement always calls {@code proceed()}.
 */
@Interceptor
@LoginCheck
@Priority(2000)
public class TestAuthenticationInterceptor {

  @PostConstruct
  Object intercept(InvocationContext ctx) throws Exception {
    return ctx.proceed();
  }
}
