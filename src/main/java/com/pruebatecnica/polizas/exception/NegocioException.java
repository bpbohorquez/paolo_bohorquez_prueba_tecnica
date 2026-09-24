package com.pruebatecnica.polizas.exception;

/** Violacion de una regla de negocio (estado invalido para la operacion solicitada). */
public class NegocioException extends RuntimeException {

    public NegocioException(String message) {
        super(message);
    }
}
