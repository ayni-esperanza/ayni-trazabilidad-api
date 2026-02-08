package com.trazabilidad.ayni.auth;

import com.trazabilidad.ayni.auth.dto.AuthResponse;
import com.trazabilidad.ayni.auth.dto.LoginRequest;
import com.trazabilidad.ayni.auth.dto.RefreshTokenRequest;
import com.trazabilidad.ayni.auth.dto.RegisterRequest;
import com.trazabilidad.ayni.rol.Rol;
import com.trazabilidad.ayni.rol.RolRepository;
import com.trazabilidad.ayni.shared.exception.BadRequestException;
import com.trazabilidad.ayni.shared.exception.DuplicateEntityException;
import com.trazabilidad.ayni.shared.exception.EntityNotFoundException;
import com.trazabilidad.ayni.shared.util.Constants;
import com.trazabilidad.ayni.usuario.Usuario;
import com.trazabilidad.ayni.usuario.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

/**
 * Servicio para gestionar la autenticación y registro de usuarios
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

        private final AuthenticationManager authenticationManager;
        private final UsuarioRepository usuarioRepository;
        private final RolRepository rolRepository;
        private final PasswordEncoder passwordEncoder;
        private final JwtTokenProvider tokenProvider;

        @Value("${jwt.expiration}")
        private long jwtExpirationMs;

        @Transactional
        public AuthResponse login(LoginRequest request) {
                Authentication authentication = authenticationManager.authenticate(
                                new UsernamePasswordAuthenticationToken(
                                                request.getUsernameOrEmail(),
                                                request.getPassword()));

                SecurityContextHolder.getContext().setAuthentication(authentication);

                String accessToken = tokenProvider.generateToken(authentication);
                String refreshToken = tokenProvider.generateRefreshToken(authentication);

                CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
                Usuario usuario = userDetails.getUsuario();

                log.info("Usuario logueado exitosamente: {}", usuario.getUsername());

                return AuthResponse.builder()
                                .accessToken(accessToken)
                                .refreshToken(refreshToken)
                                .tokenType("Bearer")
                                .expiresIn(tokenProvider.getExpirationDateFromToken(accessToken).getTime())
                                .usuario(mapToUsuarioInfo(usuario))
                                .build();
        }

        @Transactional
        public AuthResponse register(RegisterRequest request) {
                if (usuarioRepository.existsByEmail(request.getEmail())) {
                        throw new DuplicateEntityException("El email ya está registrado");
                }

                if (usuarioRepository.existsByUsername(request.getUsername())) {
                        throw new DuplicateEntityException("El username ya está registrado");
                }

                Rol rolAsistente = rolRepository.findByNombreAndActivoTrue(Constants.Roles.ASISTENTE)
                                .orElseThrow(() -> new EntityNotFoundException("Rol ASISTENTE no encontrado"));

                Usuario usuario = Usuario.builder()
                                .nombre(request.getNombre())
                                .apellido(request.getApellido())
                                .email(request.getEmail())
                                .username(request.getUsername())
                                .password(passwordEncoder.encode(request.getPassword()))
                                .telefono(request.getTelefono())
                                .activo(true)
                                .roles(Set.of(rolAsistente))
                                .build();

                usuario = usuarioRepository.save(usuario);
                rolAsistente.getUsuarios().add(usuario);

                Authentication authentication = authenticationManager.authenticate(
                                new UsernamePasswordAuthenticationToken(
                                                request.getUsername(),
                                                request.getPassword()));

                String accessToken = tokenProvider.generateToken(authentication);
                String refreshToken = tokenProvider.generateRefreshToken(authentication);

                log.info("Usuario registrado exitosamente: {}", usuario.getUsername());

                return AuthResponse.builder()
                                .accessToken(accessToken)
                                .refreshToken(refreshToken)
                                .tokenType("Bearer")
                                .expiresIn(tokenProvider.getExpirationDateFromToken(accessToken).getTime())
                                .usuario(mapToUsuarioInfo(usuario))
                                .build();
        }

        @Transactional(readOnly = true)
        public AuthResponse refreshToken(RefreshTokenRequest request) {
                String refreshToken = request.getRefreshToken();

                if (!tokenProvider.validateToken(refreshToken)) {
                        throw new BadRequestException("Refresh token inválido o expirado");
                }

                String username = tokenProvider.getUsernameFromToken(refreshToken);
                Usuario usuario = usuarioRepository.findByUsernameAndActivoTrue(username)
                                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));

                // Validar que el refresh token no esté cerca de expirar (menos de 1 día
                // restante)
                if (tokenProvider.isTokenExpired(refreshToken)) {
                        throw new BadRequestException("El refresh token ha expirado");
                }

                String newAccessToken = tokenProvider.generateTokenFromUsername(username, jwtExpirationMs);

                log.info("Token refrescado para usuario: {}", username);

                return AuthResponse.builder()
                                .accessToken(newAccessToken)
                                .refreshToken(refreshToken)
                                .tokenType("Bearer")
                                .expiresIn(tokenProvider.getExpirationDateFromToken(newAccessToken).getTime())
                                .usuario(mapToUsuarioInfo(usuario))
                                .build();
        }

        private AuthResponse.UsuarioInfo mapToUsuarioInfo(Usuario usuario) {
                return AuthResponse.UsuarioInfo.builder()
                                .id(usuario.getId())
                                .nombre(usuario.getNombre())
                                .apellido(usuario.getApellido())
                                .email(usuario.getEmail())
                                .username(usuario.getUsername())
                                .telefono(usuario.getTelefono())
                                .foto(usuario.getFoto())
                                .roles(usuario.getRoles().stream()
                                                .map(Rol::getNombre)
                                                .toList())
                                .build();
        }
}
