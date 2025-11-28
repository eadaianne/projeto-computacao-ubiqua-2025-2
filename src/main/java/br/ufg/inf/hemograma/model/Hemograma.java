package br.ufg.inf.hemograma.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidade que representa um hemograma completo.
 */
@Entity
@Table(name = "hemogramas")
public class Hemograma {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "fhir_observation_id", unique = true, nullable = false)
    private String fhirObservationId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paciente_id", nullable = false)
    private Paciente paciente;
    
    @Column(name = "data_coleta")
    private LocalDateTime dataColeta;
    
    @Column(name = "data_cadastro", nullable = false)
    private LocalDateTime dataCadastro;
    
    @Column(name = "status")
    private String status; // final, preliminary, etc.
    
    @OneToMany(mappedBy = "hemograma", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ParametroHemograma> parametros = new ArrayList<>();
    
    @OneToMany(mappedBy = "hemograma", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Desvio> desvios = new ArrayList<>();
    
    public Hemograma() {
        this.dataCadastro = LocalDateTime.now();
    }
    
    public void adicionarParametro(ParametroHemograma parametro) {
        parametros.add(parametro);
        parametro.setHemograma(this);
    }
    
    public void adicionarDesvio(Desvio desvio) {
        desvios.add(desvio);
        desvio.setHemograma(this);
    }
    
    // Getters e Setters
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getFhirObservationId() {
        return fhirObservationId;
    }
    
    public void setFhirObservationId(String fhirObservationId) {
        this.fhirObservationId = fhirObservationId;
    }
    
    public Paciente getPaciente() {
        return paciente;
    }
    
    public void setPaciente(Paciente paciente) {
        this.paciente = paciente;
    }
    
    public LocalDateTime getDataColeta() {
        return dataColeta;
    }
    
    public void setDataColeta(LocalDateTime dataColeta) {
        this.dataColeta = dataColeta;
    }
    
    public LocalDateTime getDataCadastro() {
        return dataCadastro;
    }
    
    public void setDataCadastro(LocalDateTime dataCadastro) {
        this.dataCadastro = dataCadastro;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public List<ParametroHemograma> getParametros() {
        return parametros;
    }
    
    public void setParametros(List<ParametroHemograma> parametros) {
        this.parametros = parametros;
    }
    
    public List<Desvio> getDesvios() {
        return desvios;
    }
    
    public void setDesvios(List<Desvio> desvios) {
        this.desvios = desvios;
    }
    
    @Override
    public String toString() {
        return "Hemograma{" +
                "id=" + id +
                ", fhirObservationId='" + fhirObservationId + '\'' +
                ", dataColeta=" + dataColeta +
                ", status='" + status + '\'' +
                '}';
    }
}

