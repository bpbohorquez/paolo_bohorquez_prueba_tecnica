package com.pruebatecnica.polizas.controller;

import com.pruebatecnica.polizas.dto.RiesgoResponse;
import com.pruebatecnica.polizas.service.RiesgoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/riesgos")
@RequiredArgsConstructor
public class RiesgoController {

    private final RiesgoService riesgoService;

    @PostMapping("/{id}/cancelar")
    public ResponseEntity<RiesgoResponse> cancelar(@PathVariable Long id) {
        return ResponseEntity.ok(riesgoService.cancelarRiesgo(id));
    }
}
