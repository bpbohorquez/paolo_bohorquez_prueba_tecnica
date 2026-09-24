package com.pruebatecnica.polizas.controller;

import com.pruebatecnica.polizas.dto.CoreEventoRequest;
import com.pruebatecnica.polizas.service.core.CoreNotificationPort;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/** Mock del servicio agnostico de edicion expuesto en la capa media WebLogic hacia el CORE legado. */
@RestController
@RequestMapping("/core-mock")
@RequiredArgsConstructor
public class CoreMockController {

    private final CoreNotificationPort coreNotificationPort;

    @PostMapping("/evento")
    public ResponseEntity<Map<String, String>> recibirEvento(@Valid @RequestBody CoreEventoRequest request) {
        coreNotificationPort.notificar(request.getEvento(), request.getPolizaId());
        return ResponseEntity.ok(Map.of("status", "registrado"));
    }
}
