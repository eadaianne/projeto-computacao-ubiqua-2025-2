# ? Guia de Teste - Marcos 2 e 3

## ? **O QUE FOI IMPLEMENTADO:**

### **Marco 2 - Análise Individual (10%)**
- ? Detecção automática de **ANEMIA** conforme especificação
- ? Valores de referência por **sexo e idade**
- ? Cálculo de **percentual de desvio**
- ? Classificação de **severidade** (Leve, Moderado, Grave, Crítico)
- ? Descrições detalhadas dos desvios

### **Marco 3 - Base Consolidada (10%)**
- ? Persistência de **Pacientes**
- ? Persistência de **Hemogramas**
- ? Persistência de **Parâmetros** (Hemoglobina, Leucócitos, etc.)
- ? Persistência de **Desvios** detectados
- ? Banco H2 com console web

---

## ? PASSO A PASSO DE TESTE

### **PASSO 1: Compilar e Iniciar**

```bash
cd "D:/Software Para Computação Ubíqua/projeto-computacao-ubiqua-2025-2"
mvn clean compile
mvn spring-boot:run
```

**Aguarde até ver:**
```
? Started HemogramaUbiquoApplication
? Tomcat started on port 8081
```

---

### **PASSO 2: Iniciar HAPI-FHIR**

Em outro terminal:

```bash
docker-compose up -d
```

Aguarde ~30 segundos.

---

### **PASSO 3: Criar Subscription**

```bash
curl -X POST "http://localhost:8081/hemograma-api/fhir-management/subscription/criar"
```

---

### **PASSO 4: Criar Paciente MULHER (para testar anemia)**

```bash
curl -X POST "http://localhost:8080/fhir/Patient" \
  -H "Content-Type: application/fhir+json" \
  -d '{
    "resourceType": "Patient",
    "name": [{
      "use": "official",
      "family": "Silva",
      "given": ["Maria"]
    }],
    "gender": "female",
    "birthDate": "1985-03-15"
  }'
```

**Anote o ID** (ex: "1")

---

### **PASSO 5: Criar Hemograma com ANEMIA**

**Teste 1: Hemoglobina BAIXA (Anemia) - Mulher**

```bash
curl -X POST "http://localhost:8080/fhir/Observation" \
  -H "Content-Type: application/fhir+json" \
  -d '{
    "resourceType": "Observation",
    "status": "final",
    "category": [{
      "coding": [{
        "system": "http://terminology.hl7.org/CodeSystem/observation-category",
        "code": "laboratory"
      }]
    }],
    "code": {
      "coding": [{
        "system": "http://loinc.org",
        "code": "718-7",
        "display": "Hemoglobin"
      }]
    },
    "subject": {"reference": "Patient/1"},
    "effectiveDateTime": "2025-11-07T10:30:00Z",
    "valueQuantity": {
      "value": 10.5,
      "unit": "g/dL",
      "system": "http://unitsofmeasure.org",
      "code": "g/dL"
    }
  }'
```

**Valor: 10.5 g/dL**
**Referência para mulher: 12.0 - 16.0 g/dL**
**Resultado esperado: ANEMIA DETECTADA** ?

---

### **PASSO 6: Verificar Logs da Aplicação**

**Logs esperados:**

```
========================================
? Processando Observation FHIR
========================================
Observation ID: 1
Status: final
Paciente: Patient/1
  ? Hemoglobin (718-7) = 10.5 g/dL
? Salvando hemograma no banco de dados...
? Hemograma salvo com ID: 1
? Analisando hemograma...
========================================
? ANALISANDO HEMOGRAMA
========================================
Hemograma ID: Observation/1
Paciente: null (Gênero: female, Idade: 39)
?? DESVIO DETECTADO: ? ANEMIA DETECTADA: Hemoglobina BAIXA (10.5 g/dL). Valor de referência para mulher adulta: 12.0 - 16.0 g/dL. Desvio de 12.5% abaixo do limite mínimo.
?? Total de desvios detectados: 1
========================================
? Salvando 1 desvio(s) no banco de dados...
? Desvios salvos com sucesso
? ALERTA: Hemoglobina - Severidade: Leve
? Observation processada com sucesso
========================================
```

? **Se você ver "? ANEMIA DETECTADA", funcionou!**

---

### **PASSO 7: Verificar no Banco H2**

Abra no navegador:
```
http://localhost:8081/hemograma-api/h2-console
```

**Login:**
- JDBC URL: `jdbc:h2:mem:hemograma_db`
- User: `sa`
- Password: *(vazio)*

**Queries:**

```sql
-- Ver pacientes
SELECT * FROM PACIENTES;

-- Ver hemogramas
SELECT * FROM HEMOGRAMAS;

-- Ver parâmetros
SELECT * FROM PARAMETROS_HEMOGRAMA;

-- Ver desvios (ANEMIA)
SELECT * FROM DESVIOS;
```

**Resultado esperado em DESVIOS:**
```
ID | TIPO_PARAMETRO | VALOR_ENCONTRADO | VALOR_REF_MIN | VALOR_REF_MAX | SEVERIDADE | DESCRICAO
1  | HEMOGLOBINA    | 10.5             | 12.0          | 16.0          | LEVE       | ? ANEMIA DETECTADA: ...
```

---

## ? TESTES ADICIONAIS

### **Teste 2: Hemograma NORMAL (sem desvios)**

```bash
curl -X POST "http://localhost:8080/fhir/Observation" \
  -H "Content-Type: application/fhir+json" \
  -d '{
    "resourceType": "Observation",
    "status": "final",
    "category": [{"coding": [{"system": "http://terminology.hl7.org/CodeSystem/observation-category","code": "laboratory"}]}],
    "code": {"coding": [{"system": "http://loinc.org","code": "718-7"}]},
    "subject": {"reference": "Patient/1"},
    "valueQuantity": {"value": 14.0, "unit": "g/dL"}
  }'
```

**Logs esperados:**
```
? Nenhum desvio detectado. Hemograma dentro dos valores normais.
```

---

### **Teste 3: Hemograma com MÚLTIPLOS parâmetros**

```bash
curl -X POST "http://localhost:8080/fhir/Observation" \
  -H "Content-Type: application/fhir+json" \
  -d '{
    "resourceType": "Observation",
    "status": "final",
    "category": [{"coding": [{"system": "http://terminology.hl7.org/CodeSystem/observation-category","code": "laboratory"}]}],
    "code": {"coding": [{"system": "http://loinc.org","code": "58410-2"}]},
    "subject": {"reference": "Patient/1"},
    "component": [
      {
        "code": {"coding": [{"system": "http://loinc.org","code": "718-7"}]},
        "valueQuantity": {"value": 9.0, "unit": "g/dL"}
      },
      {
        "code": {"coding": [{"system": "http://loinc.org","code": "6690-2"}]},
        "valueQuantity": {"value": 2500, "unit": "/?L"}
      },
      {
        "code": {"coding": [{"system": "http://loinc.org","code": "777-3"}]},
        "valueQuantity": {"value": 100000, "unit": "/?L"}
      }
    ]
  }'
```

**Resultado esperado:**
- ?? Hemoglobina: 9.0 g/dL ? **ANEMIA GRAVE**
- ?? Leucócitos: 2500 /?L ? **LEUCOPENIA**
- ?? Plaquetas: 100000 /?L ? **PLAQUETOPENIA**

**Total: 3 desvios detectados**

---

## ? CHECKLIST DE VALIDAÇÃO

- [ ] Aplicação iniciou sem erros
- [ ] HAPI-FHIR rodando
- [ ] Subscription criada
- [ ] Paciente criado
- [ ] Hemograma com anemia criado
- [ ] Logs mostram "? ANEMIA DETECTADA"
- [ ] Logs mostram "? Salvando hemograma no banco"
- [ ] Logs mostram "? Desvios salvos com sucesso"
- [ ] Console H2 mostra dados em PACIENTES
- [ ] Console H2 mostra dados em HEMOGRAMAS
- [ ] Console H2 mostra dados em PARAMETROS_HEMOGRAMA
- [ ] Console H2 mostra dados em DESVIOS
- [ ] Descrição do desvio menciona "ANEMIA"

---

**Execute os testes e me diga os resultados!** ?
