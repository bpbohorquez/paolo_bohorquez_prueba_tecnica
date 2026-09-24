package com.pruebatecnica.polizas.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/** Payload del mock: { "evento": "ACTUALIZACION", "polizaId": 555 }. */
@Getter
@Setter
public class CoreEventoRequest {

    @NotBlank(message = "evento es obligatorio")
    private String evento;

    @NotNull(message = "polizaId es obligatorio")
    private Long polizaId;
}
