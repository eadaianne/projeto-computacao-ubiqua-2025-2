package br.ufg.inf.hemograma.model;

import br.ufg.inf.hemograma.model.enums.TipoParametro;
import jakarta.persistence.*;

/**
 * Entidade que representa um parâmetro individual de um hemograma.
 */
@Entity
@Table(name = "parametros_hemograma")
public class ParametroHemograma {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hemograma_id", nullable = false)
    private Hemograma hemograma;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_parametro", nullable = false)
    private TipoParametro tipoParametro;
    
    @Column(name = "valor", nullable = false)
    private Double valor;
    
    @Column(name = "unidade")
    private String unidade;
    
    @Column(name = "codigo_loinc")
    private String codigoLOINC;
    
    public ParametroHemograma() {
    }
    
    public ParametroHemograma(TipoParametro tipoParametro, Double valor, String unidade) {
        this.tipoParametro = tipoParametro;
        this.valor = valor;
        this.unidade = unidade;
        this.codigoLOINC = tipoParametro != null ? tipoParametro.getCodigoLOINC() : null;
    }
    
    // Getters e Setters
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Hemograma getHemograma() {
        return hemograma;
    }
    
    public void setHemograma(Hemograma hemograma) {
        this.hemograma = hemograma;
    }
    
    public TipoParametro getTipoParametro() {
        return tipoParametro;
    }
    
    public void setTipoParametro(TipoParametro tipoParametro) {
        this.tipoParametro = tipoParametro;
    }
    
    public Double getValor() {
        return valor;
    }
    
    public void setValor(Double valor) {
        this.valor = valor;
    }
    
    public String getUnidade() {
        return unidade;
    }
    
    public void setUnidade(String unidade) {
        this.unidade = unidade;
    }
    
    public String getCodigoLOINC() {
        return codigoLOINC;
    }
    
    public void setCodigoLOINC(String codigoLOINC) {
        this.codigoLOINC = codigoLOINC;
    }
    
    @Override
    public String toString() {
        return "ParametroHemograma{" +
                "tipoParametro=" + tipoParametro +
                ", valor=" + valor +
                ", unidade='" + unidade + '\'' +
                '}';
    }
}

