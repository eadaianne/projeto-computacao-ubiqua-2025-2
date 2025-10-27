package br.ufg.inf.hemograma.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import br.ufg.inf.hemograma.service.FhirSubscriptionService;

import java.util.Map;

/**
 * Controller para gerenciar subscriptions FHIR via API REST.
 */
@RestController
@RequestMapping("/fhir-management")
public class FhirManagementController {

    @Autowired
    private FhirSubscriptionService fhirSubscriptionService;

    /**
     * Cria uma nova subscription para hemogramas.
     */
    @PostMapping("/subscription/criar")
    public ResponseEntity<Map<String, Object>> criarSubscription() {
        String subscriptionId = fhirSubscriptionService.criarSubscriptionHemograma();
        
        if (subscriptionId != null) {
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Subscription criada com sucesso",
                "subscriptionId", subscriptionId
            ));
        } else {
            return ResponseEntity.badRequest().body(Map.of(
                "status", "error",
                "message", "Falha ao criar subscription"
            ));
        }
    }

    /**
     * Verifica o status de uma subscription.
     */
    @GetMapping("/subscription/{id}/status")
    public ResponseEntity<Map<String, Object>> verificarStatus(@PathVariable String id) {
        String status = fhirSubscriptionService.verificarStatusSubscription(id);
        
        return ResponseEntity.ok(Map.of(
            "subscriptionId", id,
            "status", status
        ));
    }

    /**
     * Lista todas as subscriptions.
     */
    @GetMapping("/subscriptions")
    public ResponseEntity<String> listarSubscriptions() {
        String subscriptions = fhirSubscriptionService.listarSubscriptions();
        
        if (subscriptions != null) {
            return ResponseEntity.ok(subscriptions);
        } else {
            return ResponseEntity.badRequest().body("{\"error\": \"Falha ao listar subscriptions\"}");
        }
    }

    /**
     * Cancela uma subscription.
     */
    @DeleteMapping("/subscription/{id}")
    public ResponseEntity<Map<String, Object>> cancelarSubscription(@PathVariable String id) {
        boolean cancelado = fhirSubscriptionService.cancelarSubscription(id);
        
        if (cancelado) {
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Subscription cancelada com sucesso",
                "subscriptionId", id
            ));
        } else {
            return ResponseEntity.badRequest().body(Map.of(
                "status", "error",
                "message", "Falha ao cancelar subscription"
            ));
        }
    }

    /**
     * Testa a conectividade com o servidor FHIR.
     */
    @GetMapping("/conectividade")
    public ResponseEntity<Map<String, Object>> testarConectividade() {
        boolean conectado = fhirSubscriptionService.testarConectividade();
        
        return ResponseEntity.ok(Map.of(
            "conectado", conectado,
            "status", conectado ? "OK" : "FALHA",
            "servidor", "http://localhost:8080/fhir"
        ));
    }
}
