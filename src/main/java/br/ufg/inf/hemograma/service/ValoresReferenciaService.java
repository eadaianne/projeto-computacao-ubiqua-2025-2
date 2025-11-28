package br.ufg.inf.hemograma.service;

import br.ufg.inf.hemograma.model.Paciente;
import br.ufg.inf.hemograma.model.enums.TipoParametro;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Period;

/**
 * Serviço responsável por fornecer valores de referência para parâmetros de hemograma.
 * 
 * Implementa a lógica de valores de referência baseados em:
 * - Sexo do paciente
 * - Idade do paciente
 * - Tipo de parâmetro
 */
@Service
public class ValoresReferenciaService {
    
    private static final Logger logger = LoggerFactory.getLogger(ValoresReferenciaService.class);
    
    /**
     * Classe interna para representar uma faixa de valores de referência.
     */
    public static class FaixaReferencia {
        private final Double minimo;
        private final Double maximo;
        private final String unidade;
        
        public FaixaReferencia(Double minimo, Double maximo, String unidade) {
            this.minimo = minimo;
            this.maximo = maximo;
            this.unidade = unidade;
        }
        
        public Double getMinimo() { return minimo; }
        public Double getMaximo() { return maximo; }
        public String getUnidade() { return unidade; }
    }
    
    /**
     * Obtém a faixa de referência para um parâmetro específico, considerando sexo e idade.
     * 
     * LÓGICA DE IDENTIFICAÇÃO DE ANEMIA (conforme especificação):
     * - Hemoglobina para mulheres adultas: >= 12.0 g/dL
     * - Hemoglobina para homens adultos: >= 13.5 g/dL
     * - Valores abaixo indicam "Baixa (Anemia)"
     */
    public FaixaReferencia obterFaixaReferencia(TipoParametro tipoParametro, Paciente paciente) {
        String genero = paciente != null ? paciente.getGenero() : null;
        Integer idade = calcularIdade(paciente);
        
        logger.debug("Obtendo faixa de referência para {} - Gênero: {}, Idade: {}", 
                     tipoParametro, genero, idade);
        
        return obterFaixaReferencia(tipoParametro, genero, idade);
    }
    
    /**
     * Obtém a faixa de referência baseada em tipo, gênero e idade.
     */
    public FaixaReferencia obterFaixaReferencia(TipoParametro tipoParametro, String genero, Integer idade) {
        switch (tipoParametro) {
            case HEMOGLOBINA:
                return obterFaixaHemoglobina(genero, idade);
            case LEUCOCITOS:
                return new FaixaReferencia(4000.0, 11000.0, "/μL");
            case PLAQUETAS:
                return new FaixaReferencia(150000.0, 450000.0, "/μL");
            case HEMATOCRITO:
                return obterFaixaHematocrito(genero, idade);
            case ERITROCITOS:
                return obterFaixaEritrocitos(genero);
            case NEUTROFILOS:
                return new FaixaReferencia(1500.0, 7500.0, "/μL");
            case LINFOCITOS:
                return new FaixaReferencia(1000.0, 4000.0, "/μL");
            case MONOCITOS:
                return new FaixaReferencia(200.0, 800.0, "/μL");
            case EOSINOFILOS:
                return new FaixaReferencia(50.0, 500.0, "/μL");
            case BASOFILOS:
                return new FaixaReferencia(0.0, 100.0, "/μL");
            default:
                logger.warn("Tipo de parâmetro sem faixa de referência definida: {}", tipoParametro);
                return new FaixaReferencia(0.0, Double.MAX_VALUE, "");
        }
    }
    
    /**
     * Obtém faixa de referência para Hemoglobina.
     * 
     * ESPECIFICAÇÃO DE ANEMIA:
     * - Mulheres adultas: >= 12.0 g/dL
     * - Homens adultos: >= 13.5 g/dL
     * - Crianças (6-12 anos): >= 11.5 g/dL
     * - Adolescentes (12-18 anos): >= 12.0 g/dL
     */
    private FaixaReferencia obterFaixaHemoglobina(String genero, Integer idade) {
        // Crianças (6-12 anos)
        if (idade != null && idade >= 6 && idade < 12) {
            return new FaixaReferencia(11.5, 15.5, "g/dL");
        }
        
        // Adolescentes (12-18 anos)
        if (idade != null && idade >= 12 && idade < 18) {
            if ("male".equalsIgnoreCase(genero)) {
                return new FaixaReferencia(13.0, 16.0, "g/dL");
            } else {
                return new FaixaReferencia(12.0, 16.0, "g/dL");
            }
        }
        
        // Adultos (>= 18 anos) - CONFORME ESPECIFICAÇÃO
        if ("male".equalsIgnoreCase(genero)) {
            return new FaixaReferencia(13.5, 17.5, "g/dL"); // Homens adultos
        } else {
            return new FaixaReferencia(12.0, 16.0, "g/dL"); // Mulheres adultas
        }
    }
    
    private FaixaReferencia obterFaixaHematocrito(String genero, Integer idade) {
        if ("male".equalsIgnoreCase(genero)) {
            return new FaixaReferencia(40.0, 52.0, "%");
        } else {
            return new FaixaReferencia(36.0, 48.0, "%");
        }
    }
    
    private FaixaReferencia obterFaixaEritrocitos(String genero) {
        if ("male".equalsIgnoreCase(genero)) {
            return new FaixaReferencia(4.5, 6.0, "milhões/μL");
        } else {
            return new FaixaReferencia(4.0, 5.5, "milhões/μL");
        }
    }
    
    private Integer calcularIdade(Paciente paciente) {
        if (paciente == null || paciente.getDataNascimento() == null) {
            return null;
        }
        
        LocalDate dataNascimento = paciente.getDataNascimento();
        LocalDate hoje = LocalDate.now();
        
        return Period.between(dataNascimento, hoje).getYears();
    }
}

