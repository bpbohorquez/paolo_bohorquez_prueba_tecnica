package com.pruebatecnica.polizas.controller;

import com.pruebatecnica.polizas.domain.EstadoPoliza;
import com.pruebatecnica.polizas.domain.TipoPoliza;
import com.pruebatecnica.polizas.dto.CrearRiesgoRequest;
import com.pruebatecnica.polizas.dto.PolizaResponse;
import com.pruebatecnica.polizas.dto.RenovarRequest;
import com.pruebatecnica.polizas.dto.RiesgoResponse;
import com.pruebatecnica.polizas.service.PolizaService;
import com.pruebatecnica.polizas.service.RiesgoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/polizas")
@RequiredArgsConstructor
public class PolizaController {

    private final PolizaService polizaService;
    private final RiesgoService riesgoService;

    @GetMapping
    public ResponseEntity<List<PolizaResponse>> listar(
            @RequestParam(required = false) TipoPoliza tipo,
            @RequestParam(required = false) EstadoPoliza estado) {
        return ResponseEntity.ok(polizaService.listar(tipo, estado));
    }

    @GetMapping("/{id}/riesgos")
    public ResponseEntity<List<RiesgoResponse>> listarRiesgos(@PathVariable Long id) {
        return ResponseEntity.ok(riesgoService.listarPorPoliza(id));
    }

    @PostMapping("/{id}/renovar")
    public ResponseEntity<PolizaResponse> renovar(@PathVariable Long id,
                                                   @RequestBody(required = false) RenovarRequest request) {
        return ResponseEntity.ok(polizaService.renovar(id, request));
    }

    @PostMapping("/{id}/cancelar")
    public ResponseEntity<PolizaResponse> cancelar(@PathVariable Long id) {
        return ResponseEntity.ok(polizaService.cancelar(id));
    }

    @PostMapping("/{id}/riesgos")
    public ResponseEntity<RiesgoResponse> agregarRiesgo(@PathVariable Long id,
                                                         @Valid @RequestBody CrearRiesgoRequest request) {
        RiesgoResponse creado = riesgoService.agregarRiesgo(id, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }
}
