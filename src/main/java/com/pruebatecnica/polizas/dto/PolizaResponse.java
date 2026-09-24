package com.pruebatecnica.polizas.dto;

import com.pruebatecnica.polizas.domain.EstadoPoliza;
import com.pruebatecnica.polizas.domain.Poliza;
import com.pruebatecnica.polizas.domain.TipoPoliza;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Builder
public class PolizaResponse {

    private Long id;
    private TipoPoliza tipo;
    private EstadoPoliza estado;
    private String tomador;
    private LocalDate fechaInicioVigencia;
    private LocalDate fechaFinVigencia;
    private Integer mesesVigencia;
    private BigDecimal canonMensual;
    private BigDecimal prima;
    private int totalRiesgos;

    public static PolizaResponse from(Poliza poliza) {
        return PolizaResponse.builder()
                .id(poliza.getId())
                .tipo(poliza.getTipo())
                .estado(poliza.getEstado())
                .tomador(poliza.getTomador())
                .fechaInicioVigencia(poliza.getFechaInicioVigencia())
                .fechaFinVigencia(poliza.getFechaFinVigencia())
                .mesesVigencia(poliza.getMesesVigencia())
                .canonMensual(poliza.getCanonMensual())
                .prima(poliza.getPrima())
                .totalRiesgos(poliza.getRiesgos().size())
                .build();
    }
}
