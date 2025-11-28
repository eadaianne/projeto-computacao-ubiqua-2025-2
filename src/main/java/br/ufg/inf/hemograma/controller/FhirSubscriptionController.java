package br.ufg.inf.hemograma.controller;

import br.ufg.inf.hemograma.service.HemogramaProcessingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/hemogramas")
public class FhirSubscriptionController {

    private static final Logger logger = LoggerFactory.getLogger(FhirSubscriptionController.class);

    @Autowired
    private HemogramaProcessingService hemogramaProcessingService;

    @PutMapping("/receber/{resourceType}/{id}")
    public ResponseEntity<Map<String, String>> receberNotificacao(
            @PathVariable String resourceType,
            @PathVariable String id,
            @RequestBody(required = false) String payload,
            @RequestHeader Map<String, String> headers) {

        logger.info("📥 Notificação recebida: {}/{}", resourceType, id);
        hemogramaProcessingService.processarNotificacaoFhirAsync(payload, headers);

        return ResponseEntity.ok(Map.of(
            "status", "accepted",
            "resourceType", resourceType,
            "resourceId", id
        ));
    }
}
