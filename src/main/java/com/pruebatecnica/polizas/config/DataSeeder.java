package com.pruebatecnica.polizas.config;

import com.pruebatecnica.polizas.domain.EstadoPoliza;
import com.pruebatecnica.polizas.domain.EstadoRiesgo;
import com.pruebatecnica.polizas.domain.Poliza;
import com.pruebatecnica.polizas.domain.Riesgo;
import com.pruebatecnica.polizas.domain.TipoPoliza;
import com.pruebatecnica.polizas.repository.PolizaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** Datos de ejemplo para poder probar los endpoints sin necesitar un endpoint de creacion. */
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final PolizaRepository polizaRepository;

    @Override
    public void run(String... args) {
        polizaRepository.save(polizaIndividualActiva());
        polizaRepository.save(polizaColectivaActiva());
        polizaRepository.save(polizaIndividualCancelada());
    }

    private Poliza polizaIndividualActiva() {
        Poliza poliza = Poliza.builder()
                .tipo(TipoPoliza.INDIVIDUAL)
                .estado(EstadoPoliza.ACTIVA)
                .tomador("Juan Perez")
                .fechaInicioVigencia(LocalDate.now().minusMonths(2))
                .fechaFinVigencia(LocalDate.now().plusMonths(10))
                .mesesVigencia(12)
                .canonMensual(new BigDecimal("1200000.00"))
                .prima(new BigDecimal("14400000.00"))
                .build();
        poliza.setRiesgos(List.of(
                Riesgo.builder()
                        .poliza(poliza)
                        .asegurado("Juan Perez (arrendatario)")
                        .beneficiario("Maria Lopez (arrendadora)")
                        .inmueble("Apto 502, Calle 10 # 5-20, Bogota")
                        .estado(EstadoRiesgo.ACTIVO)
                        .build()
        ));
        return poliza;
    }

    private Poliza polizaColectivaActiva() {
        Poliza poliza = Poliza.builder()
                .tipo(TipoPoliza.COLECTIVA)
                .estado(EstadoPoliza.ACTIVA)
                .tomador("Inmobiliaria Los Andes S.A.S.")
                .fechaInicioVigencia(LocalDate.now().minusMonths(1))
                .fechaFinVigencia(LocalDate.now().plusMonths(11))
                .mesesVigencia(12)
                .canonMensual(new BigDecimal("900000.00"))
                .prima(new BigDecimal("10800000.00"))
                .build();
        poliza.setRiesgos(List.of(
                Riesgo.builder()
                        .poliza(poliza)
                        .asegurado("Carlos Ruiz (arrendatario)")
                        .beneficiario("Ana Torres (arrendadora)")
                        .inmueble("Casa 12, Barrio El Prado, Medellin")
                        .estado(EstadoRiesgo.ACTIVO)
                        .build(),
                Riesgo.builder()
                        .poliza(poliza)
                        .asegurado("Diana Gomez (arrendataria)")
                        .beneficiario("Pedro Salazar (arrendador)")
                        .inmueble("Local 3, Centro Comercial Plaza, Cali")
                        .estado(EstadoRiesgo.ACTIVO)
                        .build()
        ));
        return poliza;
    }

    private Poliza polizaIndividualCancelada() {
        Poliza poliza = Poliza.builder()
                .tipo(TipoPoliza.INDIVIDUAL)
                .estado(EstadoPoliza.CANCELADA)
                .tomador("Laura Martinez")
                .fechaInicioVigencia(LocalDate.now().minusMonths(6))
                .fechaFinVigencia(LocalDate.now().plusMonths(6))
                .mesesVigencia(12)
                .canonMensual(new BigDecimal("800000.00"))
                .prima(new BigDecimal("9600000.00"))
                .build();
        poliza.setRiesgos(List.of(
                Riesgo.builder()
                        .poliza(poliza)
                        .asegurado("Laura Martinez (arrendataria)")
                        .beneficiario("Jorge Ramirez (arrendador)")
                        .inmueble("Apto 201, Carrera 45 # 12-30, Bogota")
                        .estado(EstadoRiesgo.CANCELADO)
                        .build()
        ));
        return poliza;
    }
}
