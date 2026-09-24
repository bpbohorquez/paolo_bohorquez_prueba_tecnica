package com.pruebatecnica.polizas.dto;

import com.pruebatecnica.polizas.domain.EstadoRiesgo;
import com.pruebatecnica.polizas.domain.Riesgo;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RiesgoResponse {

    private Long id;
    private Long polizaId;
    private String asegurado;
    private String beneficiario;
    private String inmueble;
    private EstadoRiesgo estado;

    public static RiesgoResponse from(Riesgo riesgo) {
        return RiesgoResponse.builder()
                .id(riesgo.getId())
                .polizaId(riesgo.getPoliza().getId())
                .asegurado(riesgo.getAsegurado())
                .beneficiario(riesgo.getBeneficiario())
                .inmueble(riesgo.getInmueble())
                .estado(riesgo.getEstado())
                .build();
    }
}
