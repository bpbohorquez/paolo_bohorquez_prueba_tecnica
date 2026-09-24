package com.pruebatecnica.polizas.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Riesgo asegurado dentro de una poliza: arrendatario (asegurado), arrendador (beneficiario) e inmueble. */
@Entity
@Table(name = "riesgos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Riesgo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "poliza_id", nullable = false)
    @JsonIgnore
    private Poliza poliza;

    @Column(nullable = false)
    private String asegurado;

    @Column(nullable = false)
    private String beneficiario;

    @Column(nullable = false)
    private String inmueble;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoRiesgo estado;
}
