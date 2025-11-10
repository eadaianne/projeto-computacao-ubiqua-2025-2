package br.ufg.inf.hemograma.repository;

import br.ufg.inf.hemograma.model.Paciente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositório para a entidade Paciente.
 * 
 * Fornece operações CRUD e queries customizadas para pacientes.
 */
@Repository
public interface PacienteRepository extends JpaRepository<Paciente, Long> {
    
    /**
     * Busca um paciente pelo ID do FHIR.
     * 
     * @param fhirId ID do paciente no servidor FHIR (ex: "Patient/123")
     * @return Optional contendo o paciente se encontrado
     */
    Optional<Paciente> findByFhirId(String fhirId);
    
    /**
     * Verifica se existe um paciente com o ID do FHIR.
     * 
     * @param fhirId ID do paciente no servidor FHIR
     * @return true se existe, false caso contrário
     */
    boolean existsByFhirId(String fhirId);
}

