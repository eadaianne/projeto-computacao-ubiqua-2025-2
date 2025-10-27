package br.ufg.inf.hemograma.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Serviço responsável por gerenciar subscriptions no servidor HAPI-FHIR.
 */
@Service
public class FhirSubscriptionService {

    private static final Logger logger = LoggerFactory.getLogger(FhirSubscriptionService.class);
    
    @Value("${app.hemograma.fhir.server-url}")
    private String fhirServerUrl;
    
    @Value("${server.port}")
    private String applicationPort;
    
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Cria uma subscription no servidor HAPI-FHIR para monitorar novos hemogramas.
     * 
     * @return ID da subscription criada ou null se houver erro
     */
    public String criarSubscriptionHemograma() {
        logger.info("Criando subscription para hemogramas no servidor FHIR: {}", fhirServerUrl);
        
        try {
            // Monta o endpoint da aplicação
            String endpointUrl = String.format("http://host.docker.internal:%s/hemograma-api/hemogramas/receber", applicationPort);
            
            // Cria o JSON da subscription
            String subscriptionJson = criarJsonSubscription(endpointUrl);
            
            // Configura headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            // Cria a requisição
            HttpEntity<String> request = new HttpEntity<>(subscriptionJson, headers);
            
            // Envia para o servidor FHIR
            String url = fhirServerUrl + "/Subscription";
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
            
            if (response.getStatusCode() == HttpStatus.CREATED) {
                // Extrai o ID da subscription criada
                String subscriptionId = extrairIdDaResposta(response.getBody());
                logger.info("Subscription criada com sucesso. ID: {}", subscriptionId);
                return subscriptionId;
            } else {
                logger.error("Falha ao criar subscription. Status: {}", response.getStatusCode());
                return null;
            }
            
        } catch (Exception e) {
            logger.error("Erro ao criar subscription: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * Verifica o status de uma subscription específica.
     */
    public String verificarStatusSubscription(String subscriptionId) {
        logger.info("Verificando status da subscription: {}", subscriptionId);
        
        try {
            String url = fhirServerUrl + "/Subscription/" + subscriptionId;
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            
            if (response.getStatusCode() == HttpStatus.OK) {
                JsonNode subscriptionNode = objectMapper.readTree(response.getBody());
                String status = subscriptionNode.has("status") ? subscriptionNode.get("status").asText() : "unknown";
                logger.info("Status da subscription {}: {}", subscriptionId, status);
                return status;
            } else {
                logger.error("Falha ao verificar subscription. Status: {}", response.getStatusCode());
                return "error";
            }
            
        } catch (Exception e) {
            logger.error("Erro ao verificar subscription: {}", e.getMessage(), e);
            return "error";
        }
    }

    /**
     * Lista todas as subscriptions ativas no servidor.
     */
    public String listarSubscriptions() {
        logger.info("Listando subscriptions no servidor FHIR");
        
        try {
            String url = fhirServerUrl + "/Subscription";
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            
            if (response.getStatusCode() == HttpStatus.OK) {
                logger.info("Subscriptions listadas com sucesso");
                return response.getBody();
            } else {
                logger.error("Falha ao listar subscriptions. Status: {}", response.getStatusCode());
                return null;
            }
            
        } catch (Exception e) {
            logger.error("Erro ao listar subscriptions: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * Cancela uma subscription específica.
     */
    public boolean cancelarSubscription(String subscriptionId) {
        logger.info("Cancelando subscription: {}", subscriptionId);
        
        try {
            // Primeiro, busca a subscription atual
            String url = fhirServerUrl + "/Subscription/" + subscriptionId;
            ResponseEntity<String> getResponse = restTemplate.getForEntity(url, String.class);
            
            if (getResponse.getStatusCode() != HttpStatus.OK) {
                logger.error("Subscription não encontrada: {}", subscriptionId);
                return false;
            }
            
            // Modifica o status para "off"
            JsonNode subscriptionNode = objectMapper.readTree(getResponse.getBody());
            ((com.fasterxml.jackson.databind.node.ObjectNode) subscriptionNode).put("status", "off");
            
            // Envia a atualização
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<String> request = new HttpEntity<>(subscriptionNode.toString(), headers);
            
            restTemplate.put(url, request);
            
            logger.info("Subscription cancelada com sucesso: {}", subscriptionId);
            return true;
            
        } catch (Exception e) {
            logger.error("Erro ao cancelar subscription: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Cria o JSON da subscription para hemogramas.
     */
    private String criarJsonSubscription(String endpointUrl) {
        return String.format("""
            {
              "resourceType": "Subscription",
              "status": "requested",
              "reason": "Alertas para novos hemogramas",
              "criteria": "Observation?category=laboratory",
              "channel": {
                "type": "rest-hook",
                "endpoint": "%s",
                "payload": "application/fhir+json"
              }
            }
            """, endpointUrl);
    }

    /**
     * Extrai o ID da subscription da resposta do servidor FHIR.
     */
    private String extrairIdDaResposta(String responseBody) {
        try {
            JsonNode responseNode = objectMapper.readTree(responseBody);
            return responseNode.has("id") ? responseNode.get("id").asText() : null;
        } catch (Exception e) {
            logger.error("Erro ao extrair ID da resposta: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Testa a conectividade com o servidor FHIR.
     */
    public boolean testarConectividade() {
        logger.info("Testando conectividade com servidor FHIR: {}", fhirServerUrl);
        
        try {
            String url = fhirServerUrl + "/metadata";
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            
            boolean conectado = response.getStatusCode() == HttpStatus.OK;
            logger.info("Conectividade com FHIR: {}", conectado ? "OK" : "FALHA");
            
            return conectado;
            
        } catch (Exception e) {
            logger.error("Erro ao testar conectividade: {}", e.getMessage());
            return false;
        }
    }
}
