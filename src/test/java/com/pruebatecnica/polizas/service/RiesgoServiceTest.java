package com.pruebatecnica.polizas.service;

import com.pruebatecnica.polizas.domain.EstadoPoliza;
import com.pruebatecnica.polizas.domain.EstadoRiesgo;
import com.pruebatecnica.polizas.domain.Poliza;
import com.pruebatecnica.polizas.domain.Riesgo;
import com.pruebatecnica.polizas.domain.TipoPoliza;
import com.pruebatecnica.polizas.dto.CrearRiesgoRequest;
import com.pruebatecnica.polizas.dto.RiesgoResponse;
import com.pruebatecnica.polizas.exception.NegocioException;
import com.pruebatecnica.polizas.repository.PolizaRepository;
import com.pruebatecnica.polizas.repository.RiesgoRepository;
import com.pruebatecnica.polizas.service.core.CoreNotificationPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RiesgoServiceTest {

    @Mock
    private PolizaRepository polizaRepository;

    @Mock
    private RiesgoRepository riesgoRepository;

    @Mock
    private CoreNotificationPort coreNotificationPort;

    @InjectMocks
    private RiesgoService riesgoService;

    private Poliza polizaIndividual;
    private Poliza polizaColectiva;

    @BeforeEach
    void setUp() {
        polizaIndividual = Poliza.builder()
                .id(1L)
                .tipo(TipoPoliza.INDIVIDUAL)
                .estado(EstadoPoliza.ACTIVA)
                .mesesVigencia(12)
                .fechaInicioVigencia(LocalDate.now())
                .fechaFinVigencia(LocalDate.now().plusMonths(12))
                .canonMensual(new BigDecimal("1000000"))
                .prima(new BigDecimal("12000000"))
                .riesgos(new ArrayList<>())
                .build();

        polizaColectiva = Poliza.builder()
                .id(2L)
                .tipo(TipoPoliza.COLECTIVA)
                .estado(EstadoPoliza.ACTIVA)
                .mesesVigencia(12)
                .fechaInicioVigencia(LocalDate.now())
                .fechaFinVigencia(LocalDate.now().plusMonths(12))
                .canonMensual(new BigDecimal("1000000"))
                .prima(new BigDecimal("12000000"))
                .riesgos(new ArrayList<>())
                .build();
    }

    private CrearRiesgoRequest nuevaSolicitudRiesgo() {
        CrearRiesgoRequest request = new CrearRiesgoRequest();
        request.setAsegurado("Pedro");
        request.setBeneficiario("Ana");
        request.setInmueble("Casa 1");
        return request;
    }

    @Test
    void agregarRiesgo_polizaIndividual_lanzaExcepcion() {
        when(polizaRepository.findById(1L)).thenReturn(Optional.of(polizaIndividual));

        assertThatThrownBy(() -> riesgoService.agregarRiesgo(1L, nuevaSolicitudRiesgo()))
                .isInstanceOf(NegocioException.class)
                .hasMessageContaining("COLECTIVA");
    }

    @Test
    void agregarRiesgo_polizaColectiva_seCreaCorrectamente() {
        when(polizaRepository.findById(2L)).thenReturn(Optional.of(polizaColectiva));
        when(riesgoRepository.save(any(Riesgo.class))).thenAnswer(inv -> {
            Riesgo r = inv.getArgument(0);
            r.setId(100L);
            return r;
        });

        RiesgoResponse resultado = riesgoService.agregarRiesgo(2L, nuevaSolicitudRiesgo());

        assertThat(resultado.getId()).isEqualTo(100L);
        assertThat(resultado.getAsegurado()).isEqualTo("Pedro");
    }

    @Test
    void cancelarRiesgo_yaCancelado_lanzaExcepcion() {
        Riesgo riesgo = Riesgo.builder()
                .id(5L)
                .poliza(polizaColectiva)
                .estado(EstadoRiesgo.CANCELADO)
                .asegurado("Pedro")
                .beneficiario("Ana")
                .inmueble("Casa 1")
                .build();
        when(riesgoRepository.findById(5L)).thenReturn(Optional.of(riesgo));

        assertThatThrownBy(() -> riesgoService.cancelarRiesgo(5L))
                .isInstanceOf(NegocioException.class)
                .hasMessageContaining("cancelado");
    }
}
