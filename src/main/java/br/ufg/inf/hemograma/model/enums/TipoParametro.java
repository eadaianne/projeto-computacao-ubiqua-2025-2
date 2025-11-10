package br.ufg.inf.hemograma.model.enums;

/**
 * Enum que representa os tipos de parâmetros de um hemograma.
 * 
 * Baseado nos valores de referência especificados no projeto:
 * - Leucócitos: 4.000 - 11.000 /μL
 * - Hemoglobina: 12.0 - 17.5 g/dL
 * - Plaquetas: 150.000 - 450.000 /μL
 * - Hematócrito: 36 - 52 %
 */
public enum TipoParametro {
    
    LEUCOCITOS("Leucócitos", "/μL", "6690-2"),
    HEMOGLOBINA("Hemoglobina", "g/dL", "718-7"),
    PLAQUETAS("Plaquetas", "/μL", "777-3"),
    HEMATOCRITO("Hematócrito", "%", "4544-3"),
    
    // Outros parâmetros comuns em hemogramas
    ERITROCITOS("Eritrócitos", "milhões/μL", "789-8"),
    VCM("Volume Corpuscular Médio", "fL", "787-2"),
    HCM("Hemoglobina Corpuscular Média", "pg", "785-6"),
    CHCM("Concentração de Hemoglobina Corpuscular Média", "g/dL", "786-4"),
    RDW("Red Cell Distribution Width", "%", "788-0"),
    
    // Leucograma diferencial
    NEUTROFILOS("Neutrófilos", "/μL", "751-8"),
    LINFOCITOS("Linfócitos", "/μL", "731-0"),
    MONOCITOS("Monócitos", "/μL", "742-7"),
    EOSINOFILOS("Eosinófilos", "/μL", "711-2"),
    BASOFILOS("Basófilos", "/μL", "704-7");
    
    private final String nome;
    private final String unidade;
    private final String codigoLOINC;
    
    TipoParametro(String nome, String unidade, String codigoLOINC) {
        this.nome = nome;
        this.unidade = unidade;
        this.codigoLOINC = codigoLOINC;
    }
    
    public String getNome() {
        return nome;
    }
    
    public String getUnidade() {
        return unidade;
    }
    
    public String getCodigoLOINC() {
        return codigoLOINC;
    }
    
    /**
     * Busca um TipoParametro pelo código LOINC.
     * 
     * @param codigoLOINC Código LOINC do parâmetro
     * @return TipoParametro correspondente ou null se não encontrado
     */
    public static TipoParametro porCodigoLOINC(String codigoLOINC) {
        if (codigoLOINC == null) {
            return null;
        }
        
        for (TipoParametro tipo : values()) {
            if (tipo.codigoLOINC.equals(codigoLOINC)) {
                return tipo;
            }
        }
        
        return null;
    }
    
    /**
     * Busca um TipoParametro pelo nome (case-insensitive).
     * 
     * @param nome Nome do parâmetro
     * @return TipoParametro correspondente ou null se não encontrado
     */
    public static TipoParametro porNome(String nome) {
        if (nome == null) {
            return null;
        }
        
        String nomeLower = nome.toLowerCase();
        
        for (TipoParametro tipo : values()) {
            if (tipo.nome.toLowerCase().contains(nomeLower) || 
                nomeLower.contains(tipo.nome.toLowerCase())) {
                return tipo;
            }
        }
        
        return null;
    }
}

