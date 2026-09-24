package com.pruebatecnica.polizas.service;

import com.pruebatecnica.polizas.domain.EstadoPoliza;
import com.pruebatecnica.polizas.domain.EstadoRiesgo;
import com.pruebatecnica.polizas.domain.Poliza;
import com.pruebatecnica.polizas.domain.TipoPoliza;
import com.pruebatecnica.polizas.dto.PolizaResponse;
import com.pruebatecnica.polizas.dto.RenovarRequest;
import com.pruebatecnica.polizas.exception.NegocioException;
import com.pruebatecnica.polizas.exception.RecursoNoEncontradoException;
import com.pruebatecnica.polizas.repository.PolizaRepository;
import com.pruebatecnica.polizas.service.core.CoreNotificationPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class PolizaService {

    private static final String EVENTO_ACTUALIZACION = "ACTUALIZACION";

    private final PolizaRepository polizaRepository;
    private final CoreNotificationPort coreNotificationPort;
    private final BigDecimal ipcPorcentajeDefault;

    public PolizaService(PolizaRepository polizaRepository,
                          CoreNotificationPort coreNotificationPort,
                          @Value("${negocio.ipc-porcentaje}") BigDecimal ipcPorcentajeDefault) {
        this.polizaRepository = polizaRepository;
        this.coreNotificationPort = coreNotificationPort;
        this.ipcPorcentajeDefault = ipcPorcentajeDefault;
    }

    @Transactional(readOnly = true)
    public List<PolizaResponse> listar(TipoPoliza tipo, EstadoPoliza estado) {
        List<Poliza> polizas;
        if (tipo != null && estado != null) {
            polizas = polizaRepository.findByTipoAndEstado(tipo, estado);
        } else if (tipo != null) {
            polizas = polizaRepository.findByTipo(tipo);
        } else if (estado != null) {
            polizas = polizaRepository.findByEstado(estado);
        } else {
            polizas = polizaRepository.findAll();
        }
        return polizas.stream().map(PolizaResponse::from).toList();
    }

    @Transactional
    public PolizaResponse renovar(Long polizaId, RenovarRequest request) {
        Poliza poliza = obtenerPolizaOrThrow(polizaId);

        if (poliza.getEstado() == EstadoPoliza.CANCELADA) {
            throw new NegocioException("No se puede renovar una poliza cancelada");
        }

        BigDecimal ipc = (request != null && request.getIpcPorcentaje() != null)
                ? request.getIpcPorcentaje()
                : ipcPorcentajeDefault;
        BigDecimal factorIncremento = BigDecimal.ONE.add(ipc.divide(BigDecimal.valueOf(100)));

        BigDecimal nuevoCanonPoliza = aplicarIncremento(poliza.getCanonMensual(), factorIncremento);
        poliza.setCanonMensual(nuevoCanonPoliza);
        poliza.setPrima(calcularPrima(nuevoCanonPoliza, poliza.getMesesVigencia()));

        // Se renueva por el mismo periodo de vigencia inicial, a partir del vencimiento actual
        poliza.setFechaInicioVigencia(poliza.getFechaFinVigencia().plusDays(1));
        poliza.setFechaFinVigencia(poliza.getFechaInicioVigencia().plusMonths(poliza.getMesesVigencia()).minusDays(1));
        poliza.setEstado(EstadoPoliza.RENOVADA);

        Poliza actualizada = polizaRepository.save(poliza);
        coreNotificationPort.notificar(EVENTO_ACTUALIZACION, actualizada.getId());
        return PolizaResponse.from(actualizada);
    }

    @Transactional
    public PolizaResponse cancelar(Long polizaId) {
        Poliza poliza = obtenerPolizaOrThrow(polizaId);

        if (poliza.getEstado() == EstadoPoliza.CANCELADA) {
            throw new NegocioException("La poliza ya se encuentra cancelada");
        }

        poliza.setEstado(EstadoPoliza.CANCELADA);
        poliza.getRiesgos().forEach(riesgo -> riesgo.setEstado(EstadoRiesgo.CANCELADO));

        Poliza actualizada = polizaRepository.save(poliza);
        coreNotificationPort.notificar(EVENTO_ACTUALIZACION, actualizada.getId());
        return PolizaResponse.from(actualizada);
    }

    Poliza obtenerPolizaOrThrow(Long polizaId) {
        return polizaRepository.findById(polizaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe una poliza con id " + polizaId));
    }

    private BigDecimal aplicarIncremento(BigDecimal valor, BigDecimal factor) {
        return valor.multiply(factor).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calcularPrima(BigDecimal canonMensual, int mesesVigencia) {
        return canonMensual.multiply(BigDecimal.valueOf(mesesVigencia)).setScale(2, RoundingMode.HALF_UP);
    }
}
