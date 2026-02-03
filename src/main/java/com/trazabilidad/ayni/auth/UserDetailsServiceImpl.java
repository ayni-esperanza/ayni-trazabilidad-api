package com.trazabilidad.ayni.auth;

import com.trazabilidad.ayni.usuario.Usuario;
import com.trazabilidad.ayni.usuario.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio para cargar usuarios desde la base de datos para Spring Security
 */
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByUsernameAndActivoTrue(usernameOrEmail)
                .or(() -> usuarioRepository.findByEmail(usernameOrEmail))
                .filter(Usuario::getActivo)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Usuario no encontrado con username/email: " + usernameOrEmail));

        return new CustomUserDetails(usuario);
    }
}
