package br.ufg.inf.hemograma.service;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import org.hl7.fhir.r4.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Serviço para parsing de recursos FHIR usando as bibliotecas oficiais HAPI-FHIR.
 * 
 * Referência: https://hapifhir.io/hapi-fhir/docs/model/parsers.html
 */
@Service
public class FhirParserService {

    private static final Logger logger = LoggerFactory.getLogger(FhirParserService.class);
    
    private final FhirContext fhirContext;
    private final IParser jsonParser;

    public FhirParserService() {
        // Cria o contexto FHIR para R4
        this.fhirContext = FhirContext.forR4();
        
        // Cria o parser JSON
        this.jsonParser = fhirContext.newJsonParser();
        
        // Configurações do parser
        this.jsonParser.setPrettyPrint(true);
        
        logger.info("FhirParserService inicializado com HAPI-FHIR {}", fhirContext.getVersion().getVersion());
    }

    /**
     * Faz o parsing de uma string JSON para um recurso FHIR genérico.
     * 
     * @param jsonString String JSON contendo o recurso FHIR
     * @return Recurso FHIR parseado
     */
    public Resource parseResource(String jsonString) {
        try {
            logger.debug("Fazendo parsing de recurso FHIR");
            Resource resource = (Resource) jsonParser.parseResource(jsonString);
            logger.info("Recurso parseado com sucesso: {}", resource.getResourceType());
            return resource;
        } catch (Exception e) {
            logger.error("Erro ao fazer parsing do recurso FHIR: {}", e.getMessage(), e);
            throw new RuntimeException("Erro ao fazer parsing do recurso FHIR", e);
        }
    }

    /**
     * Faz o parsing de uma string JSON para uma Observation.
     * 
     * @param jsonString String JSON contendo a Observation
     * @return Observation parseada
     */
    public Observation parseObservation(String jsonString) {
        try {
            logger.debug("Fazendo parsing de Observation");
            Observation observation = jsonParser.parseResource(Observation.class, jsonString);
            logger.info("Observation parseada com sucesso: ID={}, Status={}", 
                observation.getId(), observation.getStatus());
            return observation;
        } catch (Exception e) {
            logger.error("Erro ao fazer parsing da Observation: {}", e.getMessage(), e);
            throw new RuntimeException("Erro ao fazer parsing da Observation", e);
        }
    }

    /**
     * Faz o parsing de uma string JSON para um Patient.
     * 
     * @param jsonString String JSON contendo o Patient
     * @return Patient parseado
     */
    public Patient parsePatient(String jsonString) {
        try {
            logger.debug("Fazendo parsing de Patient");
            Patient patient = jsonParser.parseResource(Patient.class, jsonString);
            logger.info("Patient parseado com sucesso: ID={}", patient.getId());
            return patient;
        } catch (Exception e) {
            logger.error("Erro ao fazer parsing do Patient: {}", e.getMessage(), e);
            throw new RuntimeException("Erro ao fazer parsing do Patient", e);
        }
    }

    /**
     * Extrai dados de um hemograma de uma Observation usando HAPI-FHIR.
     * 
     * @param observation Observation FHIR
     * @return Map com os dados do hemograma
     */
    public Map<String, Object> extrairDadosHemograma(Observation observation) {
        Map<String, Object> dados = new HashMap<>();
        
        try {
            // ID e Status
            dados.put("id", observation.getIdElement().getIdPart());
            dados.put("status", observation.getStatus().toCode());
            
            // Código da observação
            if (observation.hasCode()) {
                CodeableConcept code = observation.getCode();
                if (code.hasCoding() && !code.getCoding().isEmpty()) {
                    Coding coding = code.getCoding().get(0);
                    dados.put("codigoSistema", coding.getSystem());
                    dados.put("codigo", coding.getCode());
                    dados.put("codigoDisplay", coding.getDisplay());
                }
                if (code.hasText()) {
                    dados.put("codigoTexto", code.getText());
                }
            }
            
            // Categoria
            if (observation.hasCategory()) {
                List<String> categorias = new ArrayList<>();
                for (CodeableConcept category : observation.getCategory()) {
                    if (category.hasCoding() && !category.getCoding().isEmpty()) {
                        categorias.add(category.getCoding().get(0).getCode());
                    }
                }
                dados.put("categorias", categorias);
            }
            
            // Paciente
            if (observation.hasSubject()) {
                Reference subject = observation.getSubject();
                dados.put("pacienteReferencia", subject.getReference());
                if (subject.hasDisplay()) {
                    dados.put("pacienteNome", subject.getDisplay());
                }
            }
            
            // Data/Hora
            if (observation.hasEffectiveDateTimeType()) {
                dados.put("dataHora", observation.getEffectiveDateTimeType().getValueAsString());
            }
            
            // Componentes (valores do hemograma)
            if (observation.hasComponent()) {
                List<Map<String, Object>> componentes = new ArrayList<>();
                
                for (Observation.ObservationComponentComponent component : observation.getComponent()) {
                    Map<String, Object> componenteData = new HashMap<>();
                    
                    // Código do componente
                    if (component.hasCode()) {
                        CodeableConcept code = component.getCode();
                        if (code.hasCoding() && !code.getCoding().isEmpty()) {
                            Coding coding = code.getCoding().get(0);
                            componenteData.put("sistema", coding.getSystem());
                            componenteData.put("codigo", coding.getCode());
                            componenteData.put("display", coding.getDisplay());
                        }
                        if (code.hasText()) {
                            componenteData.put("texto", code.getText());
                        }
                    }
                    
                    // Valor
                    if (component.hasValueQuantity()) {
                        Quantity value = component.getValueQuantity();
                        componenteData.put("valor", value.getValue().doubleValue());
                        componenteData.put("unidade", value.getUnit());
                        componenteData.put("sistema", value.getSystem());
                        componenteData.put("codigoUnidade", value.getCode());
                    }
                    
                    componentes.add(componenteData);
                }


                
                dados.put("componentes", componentes);
                dados.put("totalComponentes", componentes.size());
            }
            
            logger.info("Dados do hemograma extraídos com sucesso: {} componentes", 
                dados.getOrDefault("totalComponentes", 0));
            
        } catch (Exception e) {
            logger.error("Erro ao extrair dados do hemograma: {}", e.getMessage(), e);
            throw new RuntimeException("Erro ao extrair dados do hemograma", e);
        }
        
        return dados;
    }

    /**
     * Extrai dados de um Patient usando HAPI-FHIR.
     * 
     * @param patient Patient FHIR
     * @return Map com os dados do paciente
     */
    public Map<String, Object> extrairDadosPaciente(Patient patient) {
        Map<String, Object> dados = new HashMap<>();
        
        try {
            // ID
            dados.put("id", patient.getIdElement().getIdPart());
            
            // Nome
            if (patient.hasName() && !patient.getName().isEmpty()) {
                HumanName name = patient.getName().get(0);
                dados.put("nomeCompleto", name.getNameAsSingleString());
                if (name.hasFamily()) {
                    dados.put("sobrenome", name.getFamily());
                }
                if (name.hasGiven()) {
                    dados.put("primeiroNome", name.getGiven().get(0).getValue());
                }
            }
            
            // Gênero
            if (patient.hasGender()) {
                dados.put("genero", patient.getGender().toCode());
            }
            
            // Data de nascimento
            if (patient.hasBirthDate()) {
                dados.put("dataNascimento", patient.getBirthDateElement().getValueAsString());
            }
            
            // Telefone
            if (patient.hasTelecom()) {
                List<String> telefones = new ArrayList<>();
                for (ContactPoint telecom : patient.getTelecom()) {
                    if (telecom.hasValue()) {
                        telefones.add(telecom.getValue());
                    }
                }
                dados.put("telefones", telefones);
            }
            
            // Endereço
            if (patient.hasAddress() && !patient.getAddress().isEmpty()) {
                Address address = patient.getAddress().get(0);
                Map<String, String> enderecoData = new HashMap<>();
                if (address.hasLine()) {
                    enderecoData.put("linha", address.getLine().get(0).getValue());
                }
                if (address.hasCity()) {
                    enderecoData.put("cidade", address.getCity());
                }
                if (address.hasState()) {
                    enderecoData.put("estado", address.getState());
                }
                if (address.hasPostalCode()) {
                    enderecoData.put("cep", address.getPostalCode());
                }
                dados.put("endereco", enderecoData);
            }
            
            logger.info("Dados do paciente extraídos com sucesso: {}", dados.get("nomeCompleto"));
            
        } catch (Exception e) {
            logger.error("Erro ao extrair dados do paciente: {}", e.getMessage(), e);
            throw new RuntimeException("Erro ao extrair dados do paciente", e);
        }
        
        return dados;
    }

    /**
     * Converte um recurso FHIR para JSON string.
     * 
     * @param resource Recurso FHIR
     * @return String JSON
     */
    public String toJson(Resource resource) {
        try {
            return jsonParser.encodeResourceToString(resource);
        } catch (Exception e) {
            logger.error("Erro ao converter recurso para JSON: {}", e.getMessage(), e);
            throw new RuntimeException("Erro ao converter recurso para JSON", e);
        }
    }

    /**
     * Valida se uma string JSON é um recurso FHIR válido.
     * 
     * @param jsonString String JSON
     * @return true se válido, false caso contrário
     */
    public boolean isValidFhirResource(String jsonString) {
        try {
            jsonParser.parseResource(jsonString);
            return true;
        } catch (Exception e) {
            logger.warn("JSON não é um recurso FHIR válido: {}", e.getMessage());
            return false;
        }
    }
}

