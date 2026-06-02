package com.somax.config;

import com.somax.model.CanalNotificacion;
import com.somax.model.ClaseDirigida;
import com.somax.model.Horario;
import com.somax.model.NivelClase;
import com.somax.model.Preferencias;
import com.somax.model.Role;
import com.somax.model.Usuario;
import com.somax.repository.ClaseDirigidaRepository;
import com.somax.repository.HorarioRepository;
import com.somax.repository.NotificacionRepository;
import com.somax.repository.UsuarioRepository;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

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
        Usuario staff = ensureUser("Laura Monitor", "staff@somax.com", "600000002", Role.STAFF);
        ensureUser("Carlos Socio", "user@somax.com", "600000003", Role.USER);

        ClaseDirigida zumba;
        ClaseDirigida pilates;
        ClaseDirigida spinning;

        if (claseRepository.count() == 0) {
            zumba = ClaseDirigida.builder()
                    .nombre("Zumba")
                    .descripcion("Clase intensa de cardio con ritmos latinos y coreografias guiadas.")
                    .nivel(NivelClase.BASICO)
                    .aforoMaximo(16)
                    .privada(false)
                    .imagenUrl("https://images.unsplash.com/photo-1517836357463-d25dfeac3438")
                    .build();

            pilates = ClaseDirigida.builder()
                    .nombre("Pilates")
                    .descripcion("Trabajo de fuerza y control postural con enfoque en core y respiracion.")
                    .nivel(NivelClase.INTERMEDIO)
                    .aforoMaximo(12)
                    .privada(false)
                    .imagenUrl("https://images.unsplash.com/photo-1518611012118-696072aa579a")
                    .build();

            spinning = ClaseDirigida.builder()
                    .nombre("Spinning")
                    .descripcion("Sesion de bicicleta indoor con intervalos y musica energica.")
                    .nivel(NivelClase.AVANZADO)
                    .aforoMaximo(20)
                    .privada(false)
                    .imagenUrl("https://images.unsplash.com/photo-1517960413843-0aee8e2b3285")
                    .build();

            claseRepository.saveAll(List.of(zumba, pilates, spinning));
        } else {
            zumba = claseRepository.findByNombreIgnoreCase("Zumba").orElse(null);
            pilates = claseRepository.findByNombreIgnoreCase("Pilates").orElse(null);
            spinning = claseRepository.findByNombreIgnoreCase("Spinning").orElse(null);
        }

        if (zumba == null || pilates == null || spinning == null) {
            return;
        }

        YearMonth currentMonth = YearMonth.now();
        LocalDate firstDay = currentMonth.atDay(1);
        LocalDate lastDay = currentMonth.atEndOfMonth();

        List<Horario> nuevos = new ArrayList<>();
        for (LocalDate day = firstDay; !day.isAfter(lastDay); day = day.plusDays(1)) {
            if (isClosedDay(day)) {
                continue;
            }

            addBaseDaySchedules(nuevos, day, zumba, pilates, spinning, staff);

            if (isPeakDay(day)) {
                addPeakDaySchedules(nuevos, day, zumba, pilates, spinning, staff);
            }
        }

        if (!nuevos.isEmpty()) {
            horarioRepository.saveAll(nuevos);
        }
    }

    private boolean isClosedDay(LocalDate day) {
        return day.getDayOfWeek() == DayOfWeek.SUNDAY;
    }

    private boolean isPeakDay(LocalDate day) {
        DayOfWeek dow = day.getDayOfWeek();
        return dow == DayOfWeek.MONDAY || dow == DayOfWeek.WEDNESDAY
                || dow == DayOfWeek.FRIDAY;
    }

    private void addBaseDaySchedules(List<Horario> nuevos, LocalDate day, ClaseDirigida zumba,
            ClaseDirigida pilates, ClaseDirigida spinning, Usuario staff) {
        DayOfWeek dow = day.getDayOfWeek();

        if (dow == DayOfWeek.MONDAY || dow == DayOfWeek.WEDNESDAY || dow == DayOfWeek.FRIDAY) {
            addHorarioIfMissing(nuevos, zumba, staff, day.atTime(9, 0), 50, "Sala 1", 12);
            addHorarioIfMissing(nuevos, pilates, staff, day.atTime(18, 0), 60, "Sala 2", 10);
            addHorarioIfMissing(nuevos, spinning, staff, day.atTime(19, 15), 45, "Sala 3", 15);
            return;
        }

        if (dow == DayOfWeek.TUESDAY || dow == DayOfWeek.THURSDAY) {
            addHorarioIfMissing(nuevos, pilates, staff, day.atTime(10, 0), 60, "Sala 2", 10);
            addHorarioIfMissing(nuevos, zumba, staff, day.atTime(17, 30), 50, "Sala 1", 12);
            addHorarioIfMissing(nuevos, spinning, staff, day.atTime(19, 0), 45, "Sala 3", 15);
            return;
        }

        if (dow == DayOfWeek.SATURDAY) {
            addHorarioIfMissing(nuevos, spinning, staff, day.atTime(9, 30), 45, "Sala 3", 15);
            addHorarioIfMissing(nuevos, zumba, staff, day.atTime(11, 0), 50, "Sala 1", 12);
            addHorarioIfMissing(nuevos, pilates, staff, day.atTime(12, 15), 60, "Sala 2", 10);
        }
    }

    private void addPeakDaySchedules(List<Horario> nuevos, LocalDate day, ClaseDirigida zumba,
            ClaseDirigida pilates, ClaseDirigida spinning, Usuario staff) {
        addHorarioIfMissing(nuevos, spinning, staff, day.atTime(7, 30), 45, "Sala 3", 15);
        addHorarioIfMissing(nuevos, zumba, staff, day.atTime(8, 30), 50, "Sala 4", 12);
        addHorarioIfMissing(nuevos, pilates, staff, day.atTime(15, 30), 60, "Sala 2", 10);
        addHorarioIfMissing(nuevos, zumba, staff, day.atTime(16, 45), 50, "Sala 1", 12);
    }

    private void addHorarioIfMissing(List<Horario> nuevos, ClaseDirigida clase, Usuario staff,
            LocalDateTime start, int duracion, String sala, Integer aforoMaximo) {
        if (horarioRepository.existsSameSalaAndStart(sala, start)) {
            return;
        }

        nuevos.add(Horario.builder()
                .clase(clase)
                .monitor(staff)
                .fechaHoraInicio(start)
                .duracion(duracion)
                .sala(sala)
                .aforoMaximo(aforoMaximo)
                .build());
    }

    private Usuario buildUser(String nombre, String email, String telefono, Role role,
            boolean activo) {
        Usuario usuario = Usuario.builder()
                .nombre(nombre)
                .email(email)
                .telefono(telefono)
                .password(passwordEncoder.encode("Somax123"))
                .rol(role)
                .activo(activo)
                .build();

        Preferencias preferencias = Preferencias.builder()
                .idioma("es")
                .canalNotificacion(CanalNotificacion.EMAIL)
                .avisos(true)
                .usuario(usuario)
                .build();
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
