package com.pruebatecnica.polizas.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Poliza de arrendamiento (Individual o Colectiva). Riesgo es una entidad hija:
 * una Individual siempre tiene exactamente 1 riesgo, una Colectiva puede tener 1..N.
 */
@Entity
@Table(name = "polizas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Poliza {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoPoliza tipo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoPoliza estado;

    @Column(nullable = false)
    private String tomador;

    @Column(name = "fecha_inicio_vigencia", nullable = false)
    private LocalDate fechaInicioVigencia;

    @Column(name = "fecha_fin_vigencia", nullable = false)
    private LocalDate fechaFinVigencia;

    @Column(name = "meses_vigencia", nullable = false)
    private Integer mesesVigencia;

    @Column(name = "canon_mensual", nullable = false, precision = 14, scale = 2)
    private BigDecimal canonMensual;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal prima;

    @Builder.Default
    @OneToMany(mappedBy = "poliza", cascade = CascadeType.ALL, orphanRemoval = false)
    private List<Riesgo> riesgos = new ArrayList<>();
}
