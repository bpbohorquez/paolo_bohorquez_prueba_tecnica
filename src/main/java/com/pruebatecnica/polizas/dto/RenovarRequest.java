package com.pruebatecnica.polizas.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/** Cuerpo opcional para /polizas/{id}/renovar; si no se envia, se usa el IPC configurado por defecto. */
@Getter
@Setter
public class RenovarRequest {

    private BigDecimal ipcPorcentaje;
}
