package com.pruebatecnica.polizas.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CrearRiesgoRequest {

    @NotBlank(message = "asegurado es obligatorio")
    private String asegurado;

    @NotBlank(message = "beneficiario es obligatorio")
    private String beneficiario;

    @NotBlank(message = "inmueble es obligatorio")
    private String inmueble;
}
