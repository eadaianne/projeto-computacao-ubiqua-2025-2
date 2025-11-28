package br.ufg.inf.hemograma.service;

import br.ufg.inf.hemograma.model.*;
import br.ufg.inf.hemograma.model.enums.TipoParametro;
import br.ufg.inf.hemograma.repository.DesvioRepository;
import br.ufg.inf.hemograma.repository.HemogramaRepository;
import br.ufg.inf.hemograma.repository.PacienteRepository;
import org.hl7.fhir.r4.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Service
public class HemogramaProcessingService {

    private static final Logger logger = LoggerFactory.getLogger(HemogramaProcessingService.class);

    @Autowired
    private FhirParserService fhirParserService;

    @Autowired
    private PacienteRepository pacienteRepository;

    @Autowired
    private HemogramaRepository hemogramaRepository;

    @Autowired
    private DesvioRepository desvioRepository;

    @Autowired
    private AnalisadorHemogramaService analisadorHemogramaService;

    private final Map<String, Long> processedObservations = new ConcurrentHashMap<>();
    private static final long CACHE_EXPIRATION_MS = TimeUnit.MINUTES.toMillis(5);

    @Async
    public void processarNotificacaoFhirAsync(String payload, Map<String, String> headers) {
        processarNotificacaoFhir(payload, headers);
    }

    public void processarNotificacaoFhir(String payload, Map<String, String> headers) {
        try {
            limparCacheAntigo();

            if (!fhirParserService.isValidFhirResource(payload)) {
                return;
            }

            Resource resource = fhirParserService.parseResource(payload);

            if (resource instanceof Bundle) {
                processarBundle((Bundle) resource);
            } else if (resource instanceof Observation) {
                processarObservation((Observation) resource);
            } else if (resource instanceof Patient) {
                processarPatient((Patient) resource);
            }
        } catch (Exception e) {
            logger.error("Erro ao processar notificação FHIR: {}", e.getMessage());
        }
    }

    private void limparCacheAntigo() {
        long now = System.currentTimeMillis();
        processedObservations.entrySet().removeIf(entry ->
                (now - entry.getValue()) > CACHE_EXPIRATION_MS
        );
    }

    private boolean jaFoiProcessada(String observationId) {
        Long timestamp = processedObservations.get(observationId);
        if (timestamp != null) {
            long age = System.currentTimeMillis() - timestamp;
            return age < CACHE_EXPIRATION_MS;
        }
        return false;
    }

    private void marcarComoProcessada(String observationId) {
        processedObservations.put(observationId, System.currentTimeMillis());
    }

    private void processarBundle(Bundle bundle) {
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

    @Transactional
    protected void processarObservation(Observation observation) {
        try {
            String observationId = observation.getIdElement().getIdPart();

            if (jaFoiProcessada(observationId)) {
                return;
            }

            logger.info("📊 Processando Observation: {}", observationId);

            Map<String, Object> dados = fhirParserService.extrairDadosHemograma(observation);
            String pacienteRef = (String) dados.get("pacienteReferencia");
            Paciente paciente = buscarOuCriarPaciente(pacienteRef);

            Hemograma hemograma = new Hemograma();
            hemograma.setFhirObservationId("Observation/" + observationId);
            hemograma.setPaciente(paciente);
            hemograma.setStatus((String) dados.get("status"));

            if (observation.hasEffectiveDateTimeType()) {
                Date dataColeta = observation.getEffectiveDateTimeType().getValue();
                hemograma.setDataColeta(LocalDateTime.ofInstant(dataColeta.toInstant(), ZoneId.systemDefault()));
            }

            if (dados.containsKey("componentes")) {
                @SuppressWarnings("unchecked")
                java.util.List<Map<String, Object>> componentes =
                        (java.util.List<Map<String, Object>>) dados.get("componentes");

                for (Map<String, Object> componente : componentes) {
                    String codigo = (String) componente.getOrDefault("codigo", "N/A");
                    Object valorObj = componente.get("valor");
                    String unidade = (String) componente.getOrDefault("unidade", "");

                    if (valorObj instanceof Number) {
                        Double valor = ((Number) valorObj).doubleValue();
                        TipoParametro tipo = TipoParametro.porCodigoLOINC(codigo);

                        if (tipo != null) {
                            ParametroHemograma parametro = new ParametroHemograma(tipo, valor, unidade);
                            hemograma.adicionarParametro(parametro);
                        }
                    }
                }
            } else if (observation.hasValueQuantity()) {
                Quantity value = observation.getValueQuantity();
                String codigo = observation.getCode().getCodingFirstRep().getCode();
                TipoParametro tipo = TipoParametro.porCodigoLOINC(codigo);

                if (tipo != null) {
                    ParametroHemograma parametro = new ParametroHemograma(
                            tipo,
                            value.getValue().doubleValue(),
                            value.getUnit()
                    );
                    hemograma.adicionarParametro(parametro);
                }
            }

            hemogramaRepository.save(hemograma);
            logger.info("✅ Hemograma salvo: ID {}", hemograma.getId());

            List<Desvio> desvios = analisadorHemogramaService.analisarHemograma(hemograma, paciente);

            if (!desvios.isEmpty()) {
                desvioRepository.saveAll(desvios);
                for (Desvio desvio : desvios) {
                    logger.warn("🚨 ALERTA: {} - {}",
                            desvio.getTipoParametro().getNome(),
                            desvio.getSeveridade().getDescricao());
                }
            }

            marcarComoProcessada(observationId);

        } catch (Exception e) {
            logger.error("Erro ao processar Observation: {}", e.getMessage());
        }
    }

    private Paciente buscarOuCriarPaciente(String fhirId) {
        return pacienteRepository.findByFhirId(fhirId)
                .orElseGet(() -> pacienteRepository.save(new Paciente(fhirId)));
    }

    private void processarPatient(Patient patient) {
        // Implementação futura se necessário
    }
}
