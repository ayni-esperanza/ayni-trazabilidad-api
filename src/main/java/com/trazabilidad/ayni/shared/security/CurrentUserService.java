package com.trazabilidad.ayni.shared.security;

import com.trazabilidad.ayni.auth.CustomUserDetails;
import com.trazabilidad.ayni.shared.util.Constants;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

/**
 * Expone informacion del usuario autenticado actual.
 */
@Service
public class CurrentUserService {

    public Long getCurrentUserId() {
        return getCurrentUserDetails().getId();
    }

    public boolean isAdmin() {
        return getCurrentUserDetails().getUsuario().tieneRol(Constants.Roles.ADMINISTRADOR);
    }

    private CustomUserDetails getCurrentUserDetails() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal() == null) {
            throw new AccessDeniedException(Constants.ErrorMessages.UNAUTHORIZED);
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof CustomUserDetails customUserDetails) {
            return customUserDetails;
        }

        throw new AccessDeniedException(Constants.ErrorMessages.UNAUTHORIZED);
    }
}
