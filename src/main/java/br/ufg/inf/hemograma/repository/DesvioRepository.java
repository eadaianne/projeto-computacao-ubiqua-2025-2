package br.ufg.inf.hemograma.repository;

import br.ufg.inf.hemograma.model.Desvio;
import br.ufg.inf.hemograma.model.Hemograma;
import br.ufg.inf.hemograma.model.enums.SeveridadeDesvio;
import br.ufg.inf.hemograma.model.enums.TipoParametro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DesvioRepository extends JpaRepository<Desvio, Long> {
    
    List<Desvio> findByHemograma(Hemograma hemograma);
    
    List<Desvio> findBySeveridade(SeveridadeDesvio severidade);
    
    List<Desvio> findByTipoParametro(TipoParametro tipoParametro);
    
    @Query("SELECT d FROM Desvio d WHERE d.dataDeteccao >= :dataInicio AND d.dataDeteccao <= :dataFim")
    List<Desvio> findByPeriodo(@Param("dataInicio") LocalDateTime dataInicio,
                                @Param("dataFim") LocalDateTime dataFim);
    
    @Query("SELECT d FROM Desvio d WHERE d.severidade = :severidade AND d.dataDeteccao >= :dataInicio")
    List<Desvio> findBySeveridadeEDataDeteccaoApos(@Param("severidade") SeveridadeDesvio severidade,
                                                     @Param("dataInicio") LocalDateTime dataInicio);
    
    @Query("SELECT COUNT(d) FROM Desvio d WHERE d.severidade = :severidade AND d.dataDeteccao >= :dataInicio")
    Long countBySeveridadeEDataDeteccaoApos(@Param("severidade") SeveridadeDesvio severidade,
                                             @Param("dataInicio") LocalDateTime dataInicio);
    
    List<Desvio> findByNotificacaoEnviada(Boolean notificacaoEnviada);

}

