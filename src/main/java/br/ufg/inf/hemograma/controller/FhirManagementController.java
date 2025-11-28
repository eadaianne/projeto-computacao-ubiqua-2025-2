package br.ufg.inf.hemograma.controller;

import br.ufg.inf.hemograma.service.FhirSubscriptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/fhir-management")
public class FhirManagementController {

    @Autowired
    private FhirSubscriptionService fhirSubscriptionService;

    @PostMapping("/subscription/criar")
    public ResponseEntity<Map<String, Object>> criarSubscription() {
        String subscriptionId = fhirSubscriptionService.criarSubscriptionHemograma();

        if (subscriptionId != null) {
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "Subscription criada com sucesso",
                    "subscriptionId", subscriptionId
            ));
        }
        return ResponseEntity.badRequest().body(Map.of(
                "status", "error",
                "message", "Falha ao criar subscription"
        ));
    }

    @GetMapping("/subscription/{id}/status")
    public ResponseEntity<Map<String, Object>> verificarStatus(@PathVariable String id) {
        String status = fhirSubscriptionService.verificarStatusSubscription(id);
        return ResponseEntity.ok(Map.of(
                "subscriptionId", id,
                "status", status
        ));
    }

    @GetMapping("/subscriptions")
    public ResponseEntity<String> listarSubscriptions() {
        String subscriptions = fhirSubscriptionService.listarSubscriptions();

        if (subscriptions != null) {
            return ResponseEntity.ok(subscriptions);
        }
        return ResponseEntity.badRequest().body("{\"error\": \"Falha ao listar subscriptions\"}");
    }
}
