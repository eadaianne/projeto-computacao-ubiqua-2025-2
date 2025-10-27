package br.ufg.inf.hemograma.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;

/**
 * Serviço responsável por processar notificações FHIR recebidas
 * e extrair dados de hemogramas para análise.
 */
@Service
public class HemogramaProcessingService {

    private static final Logger logger = LoggerFactory.getLogger(HemogramaProcessingService.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Processa uma notificação FHIR recebida do servidor HAPI-FHIR.
     * 
     * @param payload Payload JSON da notificação
     * @param headers Headers HTTP da requisição
     */
    public void processarNotificacaoFhir(String payload, Map<String, String> headers) {
        logger.info("Iniciando processamento de notificação FHIR");
        
        try {
            // Parse do JSON recebido
            JsonNode rootNode = objectMapper.readTree(payload);
            
            // Verifica se é uma notificação de subscription
            if (rootNode.has("resourceType")) {
                String resourceType = rootNode.get("resourceType").asText();
                logger.info("Tipo de recurso recebido: {}", resourceType);
                
                if ("Bundle".equals(resourceType)) {
                    processarBundle(rootNode);
                } else if ("Observation".equals(resourceType)) {
                    processarObservation(rootNode);
                } else {
                    logger.warn("Tipo de recurso não suportado: {}", resourceType);
                }
            } else {
                logger.warn("Payload não contém resourceType válido");
            }
            
        } catch (Exception e) {
            logger.error("Erro ao processar notificação FHIR: {}", e.getMessage(), e);
            throw new RuntimeException("Falha no processamento da notificação FHIR", e);
        }
    }

    /**
     * Processa um Bundle FHIR que pode conter múltiplas Observations.
     */
    private void processarBundle(JsonNode bundleNode) {
        logger.info("Processando Bundle FHIR");
        
        if (bundleNode.has("entry")) {
            JsonNode entries = bundleNode.get("entry");
            
            for (JsonNode entry : entries) {
                if (entry.has("resource")) {
                    JsonNode resource = entry.get("resource");
                    
                    if ("Observation".equals(resource.get("resourceType").asText())) {
                        processarObservation(resource);
                    }
                }
            }
        }
    }

    /**
     * Processa uma Observation FHIR individual.
     */
    private void processarObservation(JsonNode observationNode) {
        logger.info("Processando Observation FHIR");
        
        try {
            // Extrai informações básicas
            String id = observationNode.has("id") ? observationNode.get("id").asText() : "N/A";
            String status = observationNode.has("status") ? observationNode.get("status").asText() : "N/A";
            
            logger.info("Observation ID: {}, Status: {}", id, status);
            
            // Extrai código da observação
            if (observationNode.has("code")) {
                JsonNode codeNode = observationNode.get("code");
                processarCodigoObservacao(codeNode);
            }
            
            // Extrai valores da observação
            if (observationNode.has("component")) {
                JsonNode components = observationNode.get("component");
                processarComponentesHemograma(components);
            } else if (observationNode.has("valueQuantity")) {
                JsonNode valueQuantity = observationNode.get("valueQuantity");
                processarValorQuantidade(valueQuantity);
            }
            
            // Extrai informações do paciente
            if (observationNode.has("subject")) {
                JsonNode subject = observationNode.get("subject");
                String patientReference = subject.has("reference") ? subject.get("reference").asText() : "N/A";
                logger.info("Paciente: {}", patientReference);
            }
            
            // Aqui você pode adicionar lógica para:
            // - Salvar os dados no banco de dados
            // - Realizar análises dos valores
            // - Gerar alertas se necessário
            // - Enviar notificações
            
            logger.info("Observation processada com sucesso");
            
        } catch (Exception e) {
            logger.error("Erro ao processar Observation: {}", e.getMessage(), e);
            throw new RuntimeException("Falha no processamento da Observation", e);
        }
    }

    /**
     * Processa o código da observação para identificar o tipo de exame.
     */
    private void processarCodigoObservacao(JsonNode codeNode) {
        if (codeNode.has("coding")) {
            JsonNode codings = codeNode.get("coding");
            
            for (JsonNode coding : codings) {
                String system = coding.has("system") ? coding.get("system").asText() : "N/A";
                String code = coding.has("code") ? coding.get("code").asText() : "N/A";
                String display = coding.has("display") ? coding.get("display").asText() : "N/A";
                
                logger.info("Código: {} | Sistema: {} | Display: {}", code, system, display);
            }
        }
    }

    /**
     * Processa componentes de um hemograma completo.
     */
    private void processarComponentesHemograma(JsonNode components) {
        logger.info("Processando componentes do hemograma");
        
        for (JsonNode component : components) {
            if (component.has("code") && component.has("valueQuantity")) {
                JsonNode codeNode = component.get("code");
                JsonNode valueNode = component.get("valueQuantity");
                
                // Extrai o código do componente
                String componentCode = "N/A";
                String componentDisplay = "N/A";
                
                if (codeNode.has("coding")) {
                    JsonNode coding = codeNode.get("coding").get(0);
                    componentCode = coding.has("code") ? coding.get("code").asText() : "N/A";
                    componentDisplay = coding.has("display") ? coding.get("display").asText() : "N/A";
                }
                
                // Extrai o valor
                double value = valueNode.has("value") ? valueNode.get("value").asDouble() : 0.0;
                String unit = valueNode.has("unit") ? valueNode.get("unit").asText() : "N/A";
                
                logger.info("Componente: {} ({}) = {} {}", componentDisplay, componentCode, value, unit);
                
                // Aqui você pode adicionar lógica específica para cada tipo de componente
                // Por exemplo: leucócitos, hemoglobina, plaquetas, etc.
            }
        }
    }

    /**
     * Processa um valor de quantidade simples.
     */
    private void processarValorQuantidade(JsonNode valueQuantity) {
        double value = valueQuantity.has("value") ? valueQuantity.get("value").asDouble() : 0.0;
        String unit = valueQuantity.has("unit") ? valueQuantity.get("unit").asText() : "N/A";
        String system = valueQuantity.has("system") ? valueQuantity.get("system").asText() : "N/A";
        
        logger.info("Valor: {} {} (Sistema: {})", value, unit, system);
    }
}
