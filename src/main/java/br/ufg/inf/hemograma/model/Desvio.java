package br.ufg.inf.hemograma.model;

import br.ufg.inf.hemograma.model.enums.SeveridadeDesvio;
import br.ufg.inf.hemograma.model.enums.TipoParametro;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entidade que representa um desvio detectado em um parâmetro do hemograma.
 * 
 * Um desvio ocorre quando um valor está fora da faixa de referência.
 */
@Entity
@Table(name = "desvios")
public class Desvio {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hemograma_id", nullable = false)
    private Hemograma hemograma;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_parametro", nullable = false)
    private TipoParametro tipoParametro;
    
    @Column(name = "valor_encontrado", nullable = false)
    private Double valorEncontrado;
    
    @Column(name = "valor_referencia_minimo")
    private Double valorReferenciaMinimo;
    
    @Column(name = "valor_referencia_maximo")
    private Double valorReferenciaMaximo;
    
    @Column(name = "percentual_desvio")
    private Double percentualDesvio;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "severidade", nullable = false)
    private SeveridadeDesvio severidade;
    
    @Column(name = "descricao", length = 500)
    private String descricao;
    
    @Column(name = "data_deteccao", nullable = false)
    private LocalDateTime dataDeteccao;
    
    @Column(name = "notificacao_enviada")
    private Boolean notificacaoEnviada = false;
    
    public Desvio() {
        this.dataDeteccao = LocalDateTime.now();
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
    
    public Double getValorEncontrado() {
        return valorEncontrado;
    }
    
    public void setValorEncontrado(Double valorEncontrado) {
        this.valorEncontrado = valorEncontrado;
    }
    
    public Double getValorReferenciaMinimo() {
        return valorReferenciaMinimo;
    }
    
    public void setValorReferenciaMinimo(Double valorReferenciaMinimo) {
        this.valorReferenciaMinimo = valorReferenciaMinimo;
    }
    
    public Double getValorReferenciaMaximo() {
        return valorReferenciaMaximo;
    }
    
    public void setValorReferenciaMaximo(Double valorReferenciaMaximo) {
        this.valorReferenciaMaximo = valorReferenciaMaximo;
    }
    
    public Double getPercentualDesvio() {
        return percentualDesvio;
    }
    
    public void setPercentualDesvio(Double percentualDesvio) {
        this.percentualDesvio = percentualDesvio;
    }
    
    public SeveridadeDesvio getSeveridade() {
        return severidade;
    }
    
    public void setSeveridade(SeveridadeDesvio severidade) {
        this.severidade = severidade;
    }
    
    public String getDescricao() {
        return descricao;
    }
    
    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }
    
    public LocalDateTime getDataDeteccao() {
        return dataDeteccao;
    }
    
    public void setDataDeteccao(LocalDateTime dataDeteccao) {
        this.dataDeteccao = dataDeteccao;
    }
    
    public Boolean getNotificacaoEnviada() {
        return notificacaoEnviada;
    }
    
    public void setNotificacaoEnviada(Boolean notificacaoEnviada) {
        this.notificacaoEnviada = notificacaoEnviada;
    }
}

