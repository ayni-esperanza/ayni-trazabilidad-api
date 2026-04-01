package com.trazabilidad.ayni.alerta;

import com.trazabilidad.ayni.alerta.dto.AlertaActividadResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/alertas/actividades")
@RequiredArgsConstructor
public class AlertaActividadController {

    private final AlertaActividadService alertaActividadService;

    @GetMapping
    public ResponseEntity<List<AlertaActividadResponse>> listar() {
        return ResponseEntity.ok(alertaActividadService.listarAlertas());
    }
}
