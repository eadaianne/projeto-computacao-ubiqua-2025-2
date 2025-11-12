package br.ufg.inf.hemograma.model.enums;

/**
 * Enum que representa a severidade de um desvio nos parâmetros do hemograma.
 */
public enum SeveridadeDesvio {
    
    /**
     * Desvio leve: valor fora da faixa de referência, mas próximo aos limites.
     * Percentual de desvio: até 20%
     */
    LEVE("Leve", 1, 0.0, 20.0),
    
    /**
     * Desvio moderado: valor significativamente fora da faixa de referência.
     * Percentual de desvio: 20% a 50%
     */
    MODERADO("Moderado", 2, 20.0, 50.0),
    
    /**
     * Desvio grave: valor muito fora da faixa de referência.
     * Percentual de desvio: 50% a 100%
     */
    GRAVE("Grave", 3, 50.0, 100.0),
    
    /**
     * Desvio crítico: valor extremamente fora da faixa de referência.
     * Percentual de desvio: acima de 100%
     */
    CRITICO("Crítico", 4, 100.0, Double.MAX_VALUE);
    
    private final String descricao;
    private final int nivel;
    private final double percentualMinimo;
    private final double percentualMaximo;
    
    SeveridadeDesvio(String descricao, int nivel, double percentualMinimo, double percentualMaximo) {
        this.descricao = descricao;
        this.nivel = nivel;
        this.percentualMinimo = percentualMinimo;
        this.percentualMaximo = percentualMaximo;
    }
    
    public String getDescricao() {
        return descricao;
    }
    
    public int getNivel() {
        return nivel;
    }
    
    public double getPercentualMinimo() {
        return percentualMinimo;
    }
    
    public double getPercentualMaximo() {
        return percentualMaximo;
    }
    
    /**
     * Determina a severidade com base no percentual de desvio.
     * 
     * @param percentualDesvio Percentual de desvio em relação à faixa de referência
     * @return Severidade correspondente
     */
    public static SeveridadeDesvio porPercentualDesvio(double percentualDesvio) {
        double percentualAbs = Math.abs(percentualDesvio);
        
        for (SeveridadeDesvio severidade : values()) {
            if (percentualAbs >= severidade.percentualMinimo && 
                percentualAbs < severidade.percentualMaximo) {
                return severidade;
            }
        }
        
        return CRITICO; // Default para valores muito altos
    }
    
    /**
     * Verifica se esta severidade é mais grave que outra.
     * 
     * @param outra Outra severidade para comparar
     * @return true se esta severidade é mais grave
     */
    public boolean ehMaisGraveQue(SeveridadeDesvio outra) {
        return this.nivel > outra.nivel;
    }
}

