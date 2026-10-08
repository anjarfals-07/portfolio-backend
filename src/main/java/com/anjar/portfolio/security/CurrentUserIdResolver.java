package com.anjar.portfolio.security;

import com.anjar.portfolio.util.SecurityUtil;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

/**
 * Resolver untuk annotation {@link CurrentUserId}.
 *
 * Ambil user ID dari SecurityContext via SecurityUtil.
 * Support 2 tipe principal:
 * - UserDetailsImpl (dari JwtAuthFilter)
 * - CurrentUser (kalau filter diubah)
 */
@Component
public class CurrentUserIdResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(CurrentUserId.class)
                && parameter.getParameterType().equals(Long.class);
    }

    @Override
    public Object resolveArgument(
            MethodParameter parameter,
            ModelAndViewContainer mavContainer,
            NativeWebRequest webRequest,
            WebDataBinderFactory binderFactory
    ) {
        // ⭐ Throw BadCredentialsException kalau tidak login
        // → akan di-handle jadi 401 oleh GlobalExceptionHandler
        return SecurityUtil.requireCurrentUserId();
    }
}