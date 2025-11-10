package br.ufg.inf.hemograma.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entidade que representa um paciente no sistema.
 * 
 * Armazena informações básicas do paciente extraídas dos recursos FHIR Patient.
 */
@Entity
@Table(name = "pacientes")
public class Paciente {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * ID do paciente no servidor FHIR (ex: "Patient/123")
     */
    @Column(name = "fhir_id", unique = true, nullable = false)
    private String fhirId;
    
    @Column(name = "nome_completo")
    private String nomeCompleto;
    
    @Column(name = "primeiro_nome")
    private String primeiroNome;
    
    @Column(name = "sobrenome")
    private String sobrenome;
    
    @Column(name = "genero")
    private String genero;
    
    @Column(name = "data_nascimento")
    private LocalDate dataNascimento;
    
    @Column(name = "telefone")
    private String telefone;
    
    @Column(name = "endereco")
    private String endereco;
    
    @Column(name = "cidade")
    private String cidade;
    
    @Column(name = "estado")
    private String estado;
    
    @Column(name = "cep")
    private String cep;
    
    @Column(name = "data_cadastro", nullable = false)
    private LocalDateTime dataCadastro;
    
    @Column(name = "data_atualizacao")
    private LocalDateTime dataAtualizacao;

    // Construtores
    
    public Paciente() {
        this.dataCadastro = LocalDateTime.now();
    }
    
    public Paciente(String fhirId) {
        this();
        this.fhirId = fhirId;
    }
    
    // Métodos de conveniência
    
    @PreUpdate
    protected void onUpdate() {
        this.dataAtualizacao = LocalDateTime.now();
    }

    // Getters e Setters
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getFhirId() {
        return fhirId;
    }
    
    public void setFhirId(String fhirId) {
        this.fhirId = fhirId;
    }
    
    public String getNomeCompleto() {
        return nomeCompleto;
    }
    
    public void setNomeCompleto(String nomeCompleto) {
        this.nomeCompleto = nomeCompleto;
    }
    
    public String getPrimeiroNome() {
        return primeiroNome;
    }
    
    public void setPrimeiroNome(String primeiroNome) {
        this.primeiroNome = primeiroNome;
    }
    
    public String getSobrenome() {
        return sobrenome;
    }
    
    public void setSobrenome(String sobrenome) {
        this.sobrenome = sobrenome;
    }
    
    public String getGenero() {
        return genero;
    }
    
    public void setGenero(String genero) {
        this.genero = genero;
    }
    
    public LocalDate getDataNascimento() {
        return dataNascimento;
    }
    
    public void setDataNascimento(LocalDate dataNascimento) {
        this.dataNascimento = dataNascimento;
    }
    
    public String getTelefone() {
        return telefone;
    }
    
    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }
    
    public String getEndereco() {
        return endereco;
    }
    
    public void setEndereco(String endereco) {
        this.endereco = endereco;
    }
    
    public String getCidade() {
        return cidade;
    }
    
    public void setCidade(String cidade) {
        this.cidade = cidade;
    }
    
    public String getEstado() {
        return estado;
    }
    
    public void setEstado(String estado) {
        this.estado = estado;
    }
    
    public String getCep() {
        return cep;
    }
    
    public void setCep(String cep) {
        this.cep = cep;
    }
    
    public LocalDateTime getDataCadastro() {
        return dataCadastro;
    }
    
    public void setDataCadastro(LocalDateTime dataCadastro) {
        this.dataCadastro = dataCadastro;
    }
    
    public LocalDateTime getDataAtualizacao() {
        return dataAtualizacao;
    }
    
    public void setDataAtualizacao(LocalDateTime dataAtualizacao) {
        this.dataAtualizacao = dataAtualizacao;
    }

    @Override
    public String toString() {
        return "Paciente{" +
                "id=" + id +
                ", fhirId='" + fhirId + '\'' +
                ", nomeCompleto='" + nomeCompleto + '\'' +
                ", dataNascimento=" + dataNascimento +
                '}';
    }
}

