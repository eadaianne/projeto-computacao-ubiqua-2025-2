package br.ufg.inf.hemograma.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import br.ufg.inf.hemograma.service.HemogramaProcessingService;

import java.util.Map;

/**
 * Controller responsável por receber notificações do servidor HAPI-FHIR
 * quando novos hemogramas (Observations) são criados.
 */
@RestController
@RequestMapping("/hemogramas")
public class FhirSubscriptionController {

    private static final Logger logger = LoggerFactory.getLogger(FhirSubscriptionController.class);

    @Autowired
    private HemogramaProcessingService hemogramaProcessingService;

    /**
     * Endpoint que recebe notificações do HAPI-FHIR quando uma nova Observation
     * correspondente aos critérios da subscription é criada.
     * 
     * @param payload Dados da notificação enviados pelo HAPI-FHIR
     * @return ResponseEntity indicando o status do processamento
     */
    @PostMapping("/receber")
    public ResponseEntity<Map<String, String>> receberNotificacaoHemograma(
            @RequestBody String payload,
            @RequestHeader Map<String, String> headers) {
        
        logger.info("Recebida notificação FHIR para novo hemograma");
        logger.debug("Headers recebidos: {}", headers);
        logger.debug("Payload recebido: {}", payload);

        try {
            // Processa a notificação recebida
            hemogramaProcessingService.processarNotificacaoFhir(payload, headers);
            
            logger.info("Notificação FHIR processada com sucesso");
            
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Notificação processada com sucesso"
            ));
            
        } catch (Exception e) {
            logger.error("Erro ao processar notificação FHIR: {}", e.getMessage(), e);
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                    "status", "error",
                    "message", "Erro ao processar notificação: " + e.getMessage()
                ));
        }
    }

    /**
     * Endpoint para verificar se o serviço está funcionando.
     * Útil para testes de conectividade.
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, String>> verificarStatus() {
        return ResponseEntity.ok(Map.of(
            "status", "active",
            "service", "FHIR Subscription Receiver",
            "message", "Serviço ativo e pronto para receber notificações"
        ));
    }

    /**
     * Endpoint para teste manual de processamento.
     * Útil durante desenvolvimento e testes.
     */
    @PostMapping("/teste")
    public ResponseEntity<Map<String, String>> testeProcessamento(
            @RequestBody String payload) {

        logger.info("Executando teste manual de processamento");

        try {
            hemogramaProcessingService.processarNotificacaoFhir(payload, Map.of());

            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Teste executado com sucesso"
            ));

        } catch (Exception e) {
            logger.error("Erro no teste de processamento: {}", e.getMessage(), e);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                    "status", "error",
                    "message", "Erro no teste: " + e.getMessage()
                ));
        }
    }

    /**
     * Endpoint para simular uma notificação FHIR completa.
     * Útil para testar o fluxo completo sem depender do HAPI-FHIR.
     */
    @PostMapping("/simular-notificacao")
    public ResponseEntity<Map<String, Object>> simularNotificacaoFhir() {

        logger.info("Simulando notificação FHIR completa");

        try {
            // Simula headers típicos de uma notificação FHIR
            Map<String, String> headers = Map.of(
                "Content-Type", "application/fhir+json",
                "User-Agent", "HAPI-FHIR-Server",
                "X-Subscription-Id", "test-subscription-123"
            );

            // Payload de exemplo de um hemograma
            String payload = """
                {
                  "resourceType": "Observation",
                  "id": "hemograma-teste-123",
                  "status": "final",
                  "category": [
                    {
                      "coding": [
                        {
                          "system": "http://terminology.hl7.org/CodeSystem/observation-category",
                          "code": "laboratory",
                          "display": "Laboratory"
                        }
                      ]
                    }
                  ],
                  "code": {
                    "coding": [
                      {
                        "system": "http://loinc.org",
                        "code": "58410-2",
                        "display": "Complete blood count (hemogram) panel"
                      }
                    ],
                    "text": "Hemograma Completo"
                  },
                  "subject": {
                    "reference": "Patient/paciente-teste-456",
                    "display": "João Silva (TESTE)"
                  },
                  "effectiveDateTime": "2025-01-26T15:30:00Z",
                  "component": [
                    {
                      "code": {
                        "coding": [
                          {
                            "system": "http://loinc.org",
                            "code": "6690-2",
                            "display": "Leukocytes"
                          }
                        ],
                        "text": "Leucócitos"
                      },
                      "valueQuantity": {
                        "value": 8500,
                        "unit": "/μL"
                      }
                    },
                    {
                      "code": {
                        "coding": [
                          {
                            "system": "http://loinc.org",
                            "code": "718-7",
                            "display": "Hemoglobin"
                          }
                        ],
                        "text": "Hemoglobina"
                      },
                      "valueQuantity": {
                        "value": 13.8,
                        "unit": "g/dL"
                      }
                    }
                  ]
                }
                """;

            // Processa a notificação simulada
            hemogramaProcessingService.processarNotificacaoFhir(payload, headers);

            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Notificação FHIR simulada processada com sucesso",
                "observationId", "hemograma-teste-123",
                "patientId", "paciente-teste-456",
                "timestamp", java.time.Instant.now().toString()
            ));

        } catch (Exception e) {
            logger.error("Erro ao simular notificação FHIR: {}", e.getMessage(), e);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                    "status", "error",
                    "message", "Erro na simulação: " + e.getMessage()
                ));
        }
    }

    /**
     * Endpoint para verificar quantas notificações foram recebidas.
     * Útil para monitorar se as subscriptions estão funcionando.
     */
    @GetMapping("/estatisticas")
    public ResponseEntity<Map<String, Object>> obterEstatisticas() {
        // Aqui você pode implementar um contador de notificações recebidas
        // Por enquanto, retorna informações básicas

        return ResponseEntity.ok(Map.of(
            "status", "active",
            "service", "FHIR Subscription Receiver",
            "uptime", java.time.Instant.now().toString(),
            "endpoints", Map.of(
                "receber", "/hemogramas/receber",
                "teste", "/hemogramas/teste",
                "simular", "/hemogramas/simular-notificacao",
                "status", "/hemogramas/status"
            )
        ));
    }
}
