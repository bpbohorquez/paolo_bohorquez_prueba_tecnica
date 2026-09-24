package com.pruebatecnica.polizas.service;

import com.pruebatecnica.polizas.domain.EstadoPoliza;
import com.pruebatecnica.polizas.domain.EstadoRiesgo;
import com.pruebatecnica.polizas.domain.Poliza;
import com.pruebatecnica.polizas.domain.Riesgo;
import com.pruebatecnica.polizas.domain.TipoPoliza;
import com.pruebatecnica.polizas.dto.CrearRiesgoRequest;
import com.pruebatecnica.polizas.dto.RiesgoResponse;
import com.pruebatecnica.polizas.exception.NegocioException;
import com.pruebatecnica.polizas.exception.RecursoNoEncontradoException;
import com.pruebatecnica.polizas.repository.PolizaRepository;
import com.pruebatecnica.polizas.repository.RiesgoRepository;
import com.pruebatecnica.polizas.service.core.CoreNotificationPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RiesgoService {

    private static final String EVENTO_ACTUALIZACION = "ACTUALIZACION";

    private final PolizaRepository polizaRepository;
    private final RiesgoRepository riesgoRepository;
    private final CoreNotificationPort coreNotificationPort;

    @Transactional(readOnly = true)
    public List<RiesgoResponse> listarPorPoliza(Long polizaId) {
        obtenerPolizaOrThrow(polizaId);
        return riesgoRepository.findByPolizaId(polizaId).stream()
                .map(RiesgoResponse::from)
                .toList();
    }

    @Transactional
    public RiesgoResponse agregarRiesgo(Long polizaId, CrearRiesgoRequest request) {
        Poliza poliza = obtenerPolizaOrThrow(polizaId);

        if (poliza.getTipo() != TipoPoliza.COLECTIVA) {
            throw new NegocioException("Solo las polizas de tipo COLECTIVA permiten agregar riesgos");
        }
        if (poliza.getEstado() == EstadoPoliza.CANCELADA) {
            throw new NegocioException("No se pueden agregar riesgos a una poliza cancelada");
        }

        Riesgo riesgo = Riesgo.builder()
                .poliza(poliza)
                .asegurado(request.getAsegurado())
                .beneficiario(request.getBeneficiario())
                .inmueble(request.getInmueble())
                .estado(EstadoRiesgo.ACTIVO)
                .build();

        Riesgo guardado = riesgoRepository.save(riesgo);
        coreNotificationPort.notificar(EVENTO_ACTUALIZACION, poliza.getId());
        return RiesgoResponse.from(guardado);
    }

    @Transactional
    public RiesgoResponse cancelarRiesgo(Long riesgoId) {
        Riesgo riesgo = riesgoRepository.findById(riesgoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe un riesgo con id " + riesgoId));

        if (riesgo.getEstado() == EstadoRiesgo.CANCELADO) {
            throw new NegocioException("El riesgo ya se encuentra cancelado");
        }

        riesgo.setEstado(EstadoRiesgo.CANCELADO);
        Riesgo actualizado = riesgoRepository.save(riesgo);
        coreNotificationPort.notificar(EVENTO_ACTUALIZACION, riesgo.getPoliza().getId());
        return RiesgoResponse.from(actualizado);
    }

    private Poliza obtenerPolizaOrThrow(Long polizaId) {
        return polizaRepository.findById(polizaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe una poliza con id " + polizaId));
    }
}
