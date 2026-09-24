package com.pruebatecnica.polizas.service;

import com.pruebatecnica.polizas.domain.EstadoPoliza;
import com.pruebatecnica.polizas.domain.EstadoRiesgo;
import com.pruebatecnica.polizas.domain.Poliza;
import com.pruebatecnica.polizas.domain.Riesgo;
import com.pruebatecnica.polizas.domain.TipoPoliza;
import com.pruebatecnica.polizas.dto.PolizaResponse;
import com.pruebatecnica.polizas.exception.NegocioException;
import com.pruebatecnica.polizas.exception.RecursoNoEncontradoException;
import com.pruebatecnica.polizas.repository.PolizaRepository;
import com.pruebatecnica.polizas.service.core.CoreNotificationPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PolizaServiceTest {

    @Mock
    private PolizaRepository polizaRepository;

    @Mock
    private CoreNotificationPort coreNotificationPort;

    private PolizaService polizaService;
    private Poliza polizaIndividualActiva;

    @BeforeEach
    void setUp() {
        polizaService = new PolizaService(polizaRepository, coreNotificationPort, new BigDecimal("5.0"));

        polizaIndividualActiva = Poliza.builder()
                .id(1L)
                .tipo(TipoPoliza.INDIVIDUAL)
                .estado(EstadoPoliza.ACTIVA)
                .tomador("Juan Perez")
                .fechaInicioVigencia(LocalDate.now().minusMonths(1))
                .fechaFinVigencia(LocalDate.now().plusMonths(11))
                .mesesVigencia(12)
                .canonMensual(new BigDecimal("1000000.00"))
                .prima(new BigDecimal("12000000.00"))
                .riesgos(new ArrayList<>())
                .build();
        polizaIndividualActiva.getRiesgos().add(
                Riesgo.builder()
                        .id(10L)
                        .poliza(polizaIndividualActiva)
                        .asegurado("Juan Perez")
                        .beneficiario("Maria Lopez")
                        .inmueble("Apto 101")
                        .estado(EstadoRiesgo.ACTIVO)
                        .build()
        );
    }

    @Test
    void renovar_incrementaCanonYPrimaSegunIpc_yCambiaEstado() {
        when(polizaRepository.findById(1L)).thenReturn(Optional.of(polizaIndividualActiva));
        when(polizaRepository.save(any(Poliza.class))).thenAnswer(inv -> inv.getArgument(0));

        PolizaResponse resultado = polizaService.renovar(1L, null);

        assertThat(resultado.getEstado()).isEqualTo(EstadoPoliza.RENOVADA);
        assertThat(resultado.getCanonMensual()).isEqualByComparingTo("1050000.00");
        assertThat(resultado.getPrima()).isEqualByComparingTo("12600000.00");
        verify(coreNotificationPort).notificar("ACTUALIZACION", 1L);
    }

    @Test
    void renovar_polizaCancelada_lanzaExcepcion() {
        polizaIndividualActiva.setEstado(EstadoPoliza.CANCELADA);
        when(polizaRepository.findById(1L)).thenReturn(Optional.of(polizaIndividualActiva));

        assertThatThrownBy(() -> polizaService.renovar(1L, null))
                .isInstanceOf(NegocioException.class)
                .hasMessageContaining("cancelada");

        verifyNoInteractions(coreNotificationPort);
    }

    @Test
    void cancelar_cancelaPolizaYTodosSusRiesgos() {
        when(polizaRepository.findById(1L)).thenReturn(Optional.of(polizaIndividualActiva));
        when(polizaRepository.save(any(Poliza.class))).thenAnswer(inv -> inv.getArgument(0));

        PolizaResponse resultado = polizaService.cancelar(1L);

        assertThat(resultado.getEstado()).isEqualTo(EstadoPoliza.CANCELADA);
        assertThat(polizaIndividualActiva.getRiesgos())
                .allMatch(r -> r.getEstado() == EstadoRiesgo.CANCELADO);
    }

    @Test
    void cancelar_polizaYaCancelada_lanzaExcepcion() {
        polizaIndividualActiva.setEstado(EstadoPoliza.CANCELADA);
        when(polizaRepository.findById(1L)).thenReturn(Optional.of(polizaIndividualActiva));

        assertThatThrownBy(() -> polizaService.cancelar(1L))
                .isInstanceOf(NegocioException.class)
                .hasMessageContaining("cancelada");
    }

    @Test
    void renovar_polizaInexistente_lanzaRecursoNoEncontrado() {
        when(polizaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> polizaService.renovar(99L, null))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }
}
