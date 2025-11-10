package br.ufg.inf.hemograma.service;

import org.hl7.fhir.r4.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Serviço responsável por processar notificações FHIR recebidas
 * e extrair dados de hemogramas para análise.
 *
 * Usa as bibliotecas oficiais HAPI-FHIR para parsing e manipulação de recursos.
 */
@Service
public class HemogramaProcessingService {

    private static final Logger logger = LoggerFactory.getLogger(HemogramaProcessingService.class);

    @Autowired
    private FhirParserService fhirParserService;

    // Cache para evitar processamento duplicado (ID -> timestamp)
    private final Map<String, Long> processedObservations = new ConcurrentHashMap<>();

    // Tempo de expiração do cache (5 minutos)
    private static final long CACHE_EXPIRATION_MS = TimeUnit.MINUTES.toMillis(5);

    /**
     * Processa uma notificação FHIR de forma ASSÍNCRONA.
     *
     * Este método retorna imediatamente, permitindo que o endpoint HTTP
     * responda rapidamente ao HAPI-FHIR, evitando timeouts e retries.
     *
     * @param payload Payload JSON da notificação
     * @param headers Headers HTTP da requisição
     */
    @Async
    public void processarNotificacaoFhirAsync(String payload, Map<String, String> headers) {
        logger.info("⚡ Processamento assíncrono iniciado");
        processarNotificacaoFhir(payload, headers);
    }

    /**
     * Processa uma notificação FHIR recebida do servidor HAPI-FHIR.
     *
     * Usa HAPI-FHIR para parsing e inclui controle de duplicatas.
     *
     * @param payload Payload JSON da notificação
     * @param headers Headers HTTP da requisição
     */
    public void processarNotificacaoFhir(String payload, Map<String, String> headers) {
        logger.info("Iniciando processamento de notificação FHIR");

        try {
            // Limpa cache de observações antigas
            limparCacheAntigo();

            // Valida se é um recurso FHIR válido
            if (!fhirParserService.isValidFhirResource(payload)) {
                logger.warn("Payload não é um recurso FHIR válido");
                return;
            }

            // Faz o parsing usando HAPI-FHIR
            Resource resource = fhirParserService.parseResource(payload);
            String resourceType = resource.getResourceType().name();

            logger.info("Tipo de recurso recebido: {}", resourceType);

            // Processa de acordo com o tipo de recurso
            if (resource instanceof Bundle) {
                processarBundle((Bundle) resource);
            } else if (resource instanceof Observation) {
                processarObservation((Observation) resource);
            } else if (resource instanceof Patient) {
                processarPatient((Patient) resource);
            } else {
                logger.warn("Tipo de recurso não suportado: {}", resourceType);
            }

        } catch (Exception e) {
            logger.error("Erro ao processar notificação FHIR: {}", e.getMessage(), e);
            throw new RuntimeException("Falha no processamento da notificação FHIR", e);
        }
    }

    /**
     * Limpa observações antigas do cache para evitar crescimento infinito.
     */
    private void limparCacheAntigo() {
        long now = System.currentTimeMillis();
        processedObservations.entrySet().removeIf(entry ->
            (now - entry.getValue()) > CACHE_EXPIRATION_MS
        );
    }

    /**
     * Verifica se uma observação já foi processada recentemente.
     *
     * @param observationId ID da observação
     * @return true se já foi processada, false caso contrário
     */
    private boolean jaFoiProcessada(String observationId) {
        Long timestamp = processedObservations.get(observationId);
        if (timestamp != null) {
            long age = System.currentTimeMillis() - timestamp;
            if (age < CACHE_EXPIRATION_MS) {
                logger.warn("⚠️ Observation {} já foi processada há {} ms. Ignorando duplicata.",
                    observationId, age);
                return true;
            }
        }
        return false;
    }

    /**
     * Marca uma observação como processada.
     *
     * @param observationId ID da observação
     */
    private void marcarComoProcessada(String observationId) {
        processedObservations.put(observationId, System.currentTimeMillis());
        logger.debug("Observation {} marcada como processada", observationId);
    }

    /**
     * Processa um Bundle FHIR que pode conter múltiplas Observations.
     *
     * @param bundle Bundle FHIR
     */
    private void processarBundle(Bundle bundle) {
        logger.info("Processando Bundle FHIR com {} entradas", bundle.getEntry().size());

        for (Bundle.BundleEntryComponent entry : bundle.getEntry()) {
            if (entry.hasResource()) {
                Resource resource = entry.getResource();

                if (resource instanceof Observation) {
                    processarObservation((Observation) resource);
                } else if (resource instanceof Patient) {
                    processarPatient((Patient) resource);
                }
            }
        }
    }

    /**
     * Processa uma Observation FHIR individual usando HAPI-FHIR.
     *
     * @param observation Observation FHIR
     */
    private void processarObservation(Observation observation) {
        try {
            // Extrai ID
            String observationId = observation.getIdElement().getIdPart();

            // Verifica se já foi processada (evita duplicatas)
            if (jaFoiProcessada(observationId)) {
                return; // Ignora duplicata
            }

            logger.info("========================================");
            logger.info("📊 Processando Observation FHIR");
            logger.info("========================================");

            // Extrai dados usando HAPI-FHIR
            Map<String, Object> dados = fhirParserService.extrairDadosHemograma(observation);

            // Log dos dados extraídos
            logger.info("Observation ID: {}", dados.get("id"));
            logger.info("Status: {}", dados.get("status"));

            if (dados.containsKey("codigo")) {
                logger.info("Código: {} | Sistema: {} | Display: {}",
                    dados.get("codigo"),
                    dados.get("codigoSistema"),
                    dados.get("codigoDisplay"));
            }

            if (dados.containsKey("pacienteReferencia")) {
                logger.info("Paciente: {}", dados.get("pacienteReferencia"));
            }

            // Processa componentes (valores do hemograma)
            if (dados.containsKey("componentes")) {
                @SuppressWarnings("unchecked")
                java.util.List<Map<String, Object>> componentes =
                    (java.util.List<Map<String, Object>>) dados.get("componentes");

                logger.info("Total de componentes: {}", componentes.size());

                for (Map<String, Object> componente : componentes) {
                    String display = (String) componente.getOrDefault("display",
                        componente.getOrDefault("texto", "N/A"));
                    String codigo = (String) componente.getOrDefault("codigo", "N/A");
                    Object valor = componente.get("valor");
                    String unidade = (String) componente.getOrDefault("unidade", "");

                    logger.info("  ➤ {} ({}) = {} {}", display, codigo, valor, unidade);

                    // Aqui você pode adicionar lógica de análise:
                    // - Verificar se valores estão dentro da faixa normal
                    // - Gerar alertas para valores anormais
                    // - Classificar gravidade
                }
            } else if (observation.hasValueQuantity()) {
                // Observation simples com um único valor
                Quantity value = observation.getValueQuantity();
                logger.info("Valor: {} {}", value.getValue(), value.getUnit());
            }

            // Marca como processada
            marcarComoProcessada(observationId);

            // Aqui você pode adicionar lógica para:
            // - Salvar os dados no banco de dados
            // - Realizar análises dos valores
            // - Gerar alertas se necessário
            // - Enviar notificações
            // - Integrar com outros sistemas

            logger.info("✅ Observation processada com sucesso");
            logger.info("========================================");

        } catch (Exception e) {
            logger.error("Erro ao processar Observation: {}", e.getMessage(), e);
            throw new RuntimeException("Falha no processamento da Observation", e);
        }
    }

    /**
     * Processa um Patient FHIR usando HAPI-FHIR.
     *
     * @param patient Patient FHIR
     */
    private void processarPatient(Patient patient) {
        try {
            logger.info("========================================");
            logger.info("👤 Processando Patient FHIR");
            logger.info("========================================");

            // Extrai dados usando HAPI-FHIR
            Map<String, Object> dados = fhirParserService.extrairDadosPaciente(patient);

            // Log dos dados extraídos
            logger.info("Patient ID: {}", dados.get("id"));

            if (dados.containsKey("nomeCompleto")) {
                logger.info("Nome: {}", dados.get("nomeCompleto"));
            }

            if (dados.containsKey("genero")) {
                logger.info("Gênero: {}", dados.get("genero"));
            }

            if (dados.containsKey("dataNascimento")) {
                logger.info("Data de Nascimento: {}", dados.get("dataNascimento"));
            }

            // Aqui você pode adicionar lógica para:
            // - Salvar o paciente no banco de dados
            // - Atualizar informações existentes
            // - Vincular com hemogramas

            logger.info("✅ Patient processado com sucesso");
            logger.info("========================================");

        } catch (Exception e) {
            logger.error("Erro ao processar Patient: {}", e.getMessage(), e);
            throw new RuntimeException("Falha no processamento do Patient", e);
        }
    }

    /**
     * Retorna estatísticas de processamento.
     *
     * @return Map com estatísticas
     */
    public Map<String, Object> getEstatisticas() {
        return Map.of(
            "totalProcessadas", processedObservations.size(),
            "cacheSize", processedObservations.size(),
            "cacheExpirationMinutes", TimeUnit.MILLISECONDS.toMinutes(CACHE_EXPIRATION_MS)
        );
    }
}
