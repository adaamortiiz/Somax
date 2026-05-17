package com.somax.config;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import com.somax.model.*;
import com.somax.repository.ClaseDirigidaRepository;
import com.somax.repository.HorarioRepository;
import com.somax.repository.NotificacionRepository;
import com.somax.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {
        private final UsuarioRepository usuarioRepository;
        private final ClaseDirigidaRepository claseRepository;
        private final HorarioRepository horarioRepository;
        private final NotificacionRepository notificacionRepository;
        private final PasswordEncoder passwordEncoder;

        @Value("${app.demo.clear-notifications:true}")
        private boolean clearNotificationsOnStartup;

        @Override
        @Transactional
        public void run(String... args) {
                if (clearNotificationsOnStartup) {
                        notificacionRepository.deleteAllInBatch();
                }
                ensureUser("Admin Somax", "admin@somax.com", "600000001", Role.ADMIN);
                Usuario staff = ensureUser("Laura Monitor", "staff@somax.com", "600000002",
                                Role.STAFF);
                ensureUser("Carlos Socio", "user@somax.com", "600000003", Role.USER);

                ClaseDirigida zumba = null;
                ClaseDirigida pilates = null;
                ClaseDirigida spinning = null;

                if (claseRepository.count() == 0) {
                        zumba = ClaseDirigida.builder().nombre("Zumba").descripcion(
                                        "Clase intensa de cardio con ritmos latinos y coreografias guiadas.")
                                        .nivel(NivelClase.BASICO).aforoMaximo(16).privada(false)
                                        .imagenUrl("https://images.unsplash.com/photo-1517836357463-d25dfeac3438")
                                        .build();

                        pilates = ClaseDirigida.builder().nombre("Pilates").descripcion(
                                        "Trabajo de fuerza y control postural con enfoque en core y respiracion.")
                                        .nivel(NivelClase.INTERMEDIO).aforoMaximo(12).privada(false)
                                        .imagenUrl("https://images.unsplash.com/photo-1518611012118-696072aa579a")
                                        .build();

                        spinning = ClaseDirigida.builder().nombre("Spinning").descripcion(
                                        "Sesión de bicicleta indoor con intervalos y música enérgica.")
                                        .nivel(NivelClase.AVANZADO).aforoMaximo(20).privada(false)
                                        .imagenUrl("https://images.unsplash.com/photo-1517960413843-0aee8e2b3285")
                                        .build();

                        claseRepository.saveAll(List.of(zumba, pilates, spinning));
                } else {
                        zumba = claseRepository.findByNombreIgnoreCase("Zumba").orElse(null);
                        pilates = claseRepository.findByNombreIgnoreCase("Pilates").orElse(null);
                        spinning = claseRepository.findByNombreIgnoreCase("Spinning").orElse(null);
                }

                if (!horarioRepository.findUpcomingWithClaseAndMonitor(LocalDateTime.now())
                                .isEmpty()) {
                        return;
                }

                if (zumba != null && pilates != null && spinning != null) {
                        LocalDateTime base = LocalDateTime.now().withSecond(0).withNano(0);
                        List<Horario> nuevos = new java.util.ArrayList<>();

                        // Sembrado de ejemplo: 8 semanas (≈2 meses), 3 clases por semana.
                        for (int week = 0; week < 8; week++) {
                                LocalDateTime w = base.plusWeeks(week);

                                nuevos.add(Horario.builder().clase(zumba).monitor(staff)
                                                .fechaHoraInicio(w.plusDays(1).withHour(10)
                                                                .withMinute(0))
                                                .duracion(50).sala("Sala 1")
                                                .aforoMaximo(12).build());
                                nuevos.add(Horario.builder().clase(pilates).monitor(staff)
                                                .fechaHoraInicio(w.plusDays(3).withHour(18)
                                                                .withMinute(0))
                                                .duracion(60).sala("Sala 2")
                                                .aforoMaximo(10).build());
                                nuevos.add(Horario.builder().clase(spinning).monitor(staff)
                                                .fechaHoraInicio(w.plusDays(5).withHour(9)
                                                                .withMinute(30))
                                                .duracion(45).sala("Sala 3")
                                                .aforoMaximo(15).build());
                        }

                        horarioRepository.saveAll(nuevos);
                }
        }

        private Usuario buildUser(String nombre, String email, String telefono, Role role,
                        boolean activo) {
                Usuario usuario = Usuario.builder().nombre(nombre).email(email).telefono(telefono)
                                .password(passwordEncoder.encode("Somax123")).rol(role)
                                .activo(activo).build();

                Preferencias preferencias = Preferencias.builder().idioma("es")
                                .canalNotificacion(CanalNotificacion.EMAIL).avisos(true)
                                .usuario(usuario).build();
                usuario.setPreferencias(preferencias);
                return usuario;
        }

        private Usuario ensureUser(String nombre, String email, String telefono, Role role) {
                Optional<Usuario> existing = usuarioRepository.findByEmail(email);
                if (existing.isPresent()) {
                        Usuario usuario = existing.get();
                        boolean changed = false;
                        if (usuario.getRol() != role) {
                                usuario.setRol(role);
                                changed = true;
                        }
                        if (!usuario.isActivo()) {
                                usuario.setActivo(true);
                                changed = true;
                        }
                        return changed ? usuarioRepository.save(usuario) : usuario;
                }
                Usuario nuevo = buildUser(nombre, email, telefono, role, true);
                return usuarioRepository.save(nuevo);
        }
}
