package br.ufg.inf.hemograma.service;

import br.ufg.inf.hemograma.model.Desvio;
import br.ufg.inf.hemograma.model.Hemograma;
import br.ufg.inf.hemograma.model.Paciente;
import br.ufg.inf.hemograma.model.ParametroHemograma;
import br.ufg.inf.hemograma.model.enums.SeveridadeDesvio;
import br.ufg.inf.hemograma.model.enums.TipoParametro;
import br.ufg.inf.hemograma.service.ValoresReferenciaService.FaixaReferencia;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class AnalisadorHemogramaService {

    private static final Logger logger = LoggerFactory.getLogger(AnalisadorHemogramaService.class);

    @Autowired
    private ValoresReferenciaService valoresReferenciaService;

    public List<Desvio> analisarHemograma(Hemograma hemograma, Paciente paciente) {
        List<Desvio> desvios = new ArrayList<>();

        for (ParametroHemograma parametro : hemograma.getParametros()) {
            Desvio desvio = analisarParametro(parametro, paciente);
            if (desvio != null) {
                desvio.setHemograma(hemograma);
                desvios.add(desvio);
                hemograma.adicionarDesvio(desvio);
                logger.warn("⚠️ {}", desvio.getDescricao());
            }
        }

        return desvios;
    }

    /**
     * LÓGICA DE IDENTIFICAÇÃO DE ANEMIA:
     * - Se Hemoglobina < Limite Inferior (VR Mínimo) → "Baixa (Anemia)"
     */
    private Desvio analisarParametro(ParametroHemograma parametro, Paciente paciente) {
        TipoParametro tipo = parametro.getTipoParametro();
        Double valor = parametro.getValor();
        
        FaixaReferencia faixa = valoresReferenciaService.obterFaixaReferencia(tipo, paciente);
        
        // Verifica se está dentro da faixa
        if (valor >= faixa.getMinimo() && valor <= faixa.getMaximo()) {
            return null; // Valor normal
        }
        
        // Detectou desvio - criar objeto Desvio
        Desvio desvio = new Desvio();
        desvio.setTipoParametro(tipo);
        desvio.setValorEncontrado(valor);
        desvio.setValorReferenciaMinimo(faixa.getMinimo());
        desvio.setValorReferenciaMaximo(faixa.getMaximo());
        
        // Calcular percentual de desvio
        Double percentualDesvio = calcularPercentualDesvio(valor, faixa);
        desvio.setPercentualDesvio(percentualDesvio);
        
        // Determinar severidade
        SeveridadeDesvio severidade = SeveridadeDesvio.porPercentualDesvio(percentualDesvio);
        desvio.setSeveridade(severidade);
        
        // Gerar descrição
        String descricao = gerarDescricao(tipo, valor, faixa, percentualDesvio, paciente);
        desvio.setDescricao(descricao);
        
        return desvio;
    }
    
    /**
     * Calcula o percentual de desvio em relação à faixa de referência.
     */
    private Double calcularPercentualDesvio(Double valor, FaixaReferencia faixa) {
        if (valor < faixa.getMinimo()) {
            // Valor abaixo do mínimo
            return ((faixa.getMinimo() - valor) / faixa.getMinimo()) * 100.0;
        } else {
            // Valor acima do máximo
            return ((valor - faixa.getMaximo()) / faixa.getMaximo()) * 100.0;
        }
    }
    
    /**
     * Gera descrição detalhada do desvio.
     * 
     * ESPECIFICAÇÃO DE ANEMIA:
     * - Hemoglobina abaixo do limite → "Baixa (Anemia)"
     */
    private String gerarDescricao(TipoParametro tipo, Double valor, FaixaReferencia faixa, 
                                   Double percentualDesvio, Paciente paciente) {
        StringBuilder desc = new StringBuilder();
        
        // Identificar se é ANEMIA (conforme especificação)
        if (tipo == TipoParametro.HEMOGLOBINA && valor < faixa.getMinimo()) {
            desc.append("🩸 ANEMIA DETECTADA: ");
            desc.append(String.format("Hemoglobina BAIXA (%.1f g/dL). ", valor));
            desc.append(String.format("Valor de referência para %s: %.1f - %.1f g/dL. ",
                                      obterDescricaoGenero(paciente),
                                      faixa.getMinimo(),
                                      faixa.getMaximo()));
            desc.append(String.format("Desvio de %.1f%% abaixo do limite mínimo.", percentualDesvio));
            return desc.toString();
        }
        
        // Outros desvios
        desc.append(tipo.getNome()).append(": ");
        
        if (valor < faixa.getMinimo()) {
            desc.append(String.format("BAIXO (%.2f %s). ", valor, faixa.getUnidade()));
        } else {
            desc.append(String.format("ALTO (%.2f %s). ", valor, faixa.getUnidade()));
        }
        
        desc.append(String.format("Faixa de referência: %.2f - %.2f %s. ",
                                  faixa.getMinimo(),
                                  faixa.getMaximo(),
                                  faixa.getUnidade()));
        
        desc.append(String.format("Desvio de %.1f%%.", percentualDesvio));
        
        return desc.toString();
    }
    
    private String obterDescricaoGenero(Paciente paciente) {
        if (paciente == null || paciente.getGenero() == null) {
            return "adulto";
        }
        
        return "male".equalsIgnoreCase(paciente.getGenero()) ? "homem adulto" : "mulher adulta";
    }
    
    private Integer calcularIdade(Paciente paciente) {
        if (paciente == null || paciente.getDataNascimento() == null) {
            return null;
        }
        
        return java.time.Period.between(paciente.getDataNascimento(), java.time.LocalDate.now()).getYears();
    }
}

