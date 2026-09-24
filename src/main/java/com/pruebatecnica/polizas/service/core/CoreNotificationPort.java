package com.pruebatecnica.polizas.service.core;

/**
 * Puerto de salida (hexagonal) hacia el servicio agnostico de edicion expuesto en la
 * capa media WebLogic, que mantiene actualizado el CORE transaccional legado.
 */
public interface CoreNotificationPort {

    void notificar(String evento, Long polizaId);
}
