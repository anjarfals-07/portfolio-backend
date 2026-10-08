package com.anjar.portfolio.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation untuk inject current user ID ke controller parameter.
 *
 * Contoh:
 * <pre>
 *   @GetMapping
 *   public ResponseEntity<?> list(@CurrentUserId Long userId) {
 *       // userId otomatis terisi dari SecurityContext
 *   }
 * </pre>
 *
 * Handler: {@link CurrentUserIdResolver}
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface CurrentUserId {
}