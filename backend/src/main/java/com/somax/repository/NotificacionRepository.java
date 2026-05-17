package com.somax.repository;

import com.somax.model.Notificacion;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificacionRepository extends JpaRepository<Notificacion, Long> {
    List<Notificacion> findByUsuario_IdOrderByFechaEnvioDesc(Long usuarioId);
}
