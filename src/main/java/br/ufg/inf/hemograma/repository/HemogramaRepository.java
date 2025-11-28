package br.ufg.inf.hemograma.repository;

import br.ufg.inf.hemograma.model.Hemograma;
import br.ufg.inf.hemograma.model.Paciente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface HemogramaRepository extends JpaRepository<Hemograma, Long> {
    
    Optional<Hemograma> findByFhirObservationId(String fhirObservationId);
    
    boolean existsByFhirObservationId(String fhirObservationId);
    
    List<Hemograma> findByPaciente(Paciente paciente);
    
    List<Hemograma> findByPacienteOrderByDataColetaDesc(Paciente paciente);
    
    @Query("SELECT h FROM Hemograma h WHERE h.dataColeta >= :dataInicio AND h.dataColeta <= :dataFim")
    List<Hemograma> findByPeriodo(@Param("dataInicio") LocalDateTime dataInicio, 
                                   @Param("dataFim") LocalDateTime dataFim);
    
    @Query("SELECT h FROM Hemograma h WHERE h.paciente = :paciente AND h.dataColeta >= :dataInicio")
    List<Hemograma> findByPacienteEDataColetaApos(@Param("paciente") Paciente paciente,
                                                    @Param("dataInicio") LocalDateTime dataInicio);

}

