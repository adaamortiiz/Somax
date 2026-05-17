package com.somax.service;

import java.util.List;
import org.springframework.stereotype.Service;
import com.somax.dto.DashboardResponse;
import com.somax.model.ReservaEstado;
import com.somax.model.Valoracion;
import com.somax.repository.ClaseDirigidaRepository;
import com.somax.repository.HorarioRepository;
import com.somax.repository.ReservaRepository;
import com.somax.repository.ValoracionRepository;
import lombok.RequiredArgsConstructor;
import com.somax.repository.UsuarioRepository;

@Service
@RequiredArgsConstructor
public class DashboardService {
    private final UsuarioRepository usuarioRepository;
    private final ClaseDirigidaRepository claseRepository;
    private final HorarioRepository horarioRepository;
    private final ReservaRepository reservaRepository;
    private final ValoracionRepository valoracionRepository;

    public DashboardResponse buildAdminDashboard() {
        long totalUsuarios = usuarioRepository.count();
        long activos = usuarioRepository.findAll().stream().filter(u -> u.isActivo()).count();
        long totalClases = claseRepository.count();
        long totalHorarios = horarioRepository.count();

        double ocupacionMedia =
                horarioRepository.findAllWithClaseAndMonitor().stream().mapToDouble(horario -> {
            int aforo = horario.getClase().getAforoMaximo();
            if (aforo == 0) {
                return 0.0;
            }
            long ocupadas = reservaRepository.countByHorario_IdAndEstado(horario.getId(),
                    ReservaEstado.CONFIRMADA);
            return (ocupadas * 100.0) / aforo;
        }).average().orElse(0.0);

        List<Valoracion> valoraciones = valoracionRepository.findAll();
        double valoracionMedia =
                valoraciones.stream().mapToInt(Valoracion::getPuntuacion).average().orElse(0.0);

        return DashboardResponse.builder().totalUsuarios(totalUsuarios).usuariosActivos(activos)
                .totalClases(totalClases).totalHorarios(totalHorarios)
                .ocupacionMedia(ocupacionMedia).valoracionMedia(valoracionMedia).build();
    }
}
