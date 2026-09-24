package com.pruebatecnica.polizas.service.core;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Adapter mock del servicio agnostico de edicion (CORE via WebLogic). Su unico proposito,
 * segun el enunciado, es dejar registro en logs de que la operacion se intento enviar al CORE.
 */
@Slf4j
@Component
public class CoreMockAdapter implements CoreNotificationPort {

    @Override
    public void notificar(String evento, Long polizaId) {
        log.info("[CORE-MOCK] Operacion enviada al servicio agnostico de edicion -> evento={}, polizaId={}", evento, polizaId);
    }
}
