# ? Guia do Gerador de Dados Fictícios

## ? **O QUE FAZ:**

O gerador cria automaticamente:
- ? **10 pacientes** fictícios (nomes, gênero, idade aleatórios)
- ? **5 hemogramas por paciente** (total: 50 hemogramas)
- ? **40% dos hemogramas com desvios** (incluindo anemia)
- ? **Dados realistas** com valores dentro e fora das faixas normais

---

## ? **COMO USAR:**

### **Pré-requisitos:**
1. ? HAPI-FHIR rodando (`docker-compose up -d`)
2. ? Aplicação Spring Boot rodando (`mvn spring-boot:run`)
3. ? Subscription criada
4. ? Python 3.x instalado

---

### **Opção 1: Script Automático (Windows)**

```bash
cd scripts
gerar-hemogramas.bat
```

O script irá:
1. Verificar se Python está instalado
2. Instalar dependências (requests)
3. Executar o gerador

---

### **Opção 2: Python Direto**

```bash
cd scripts
python gerar-hemogramas-ficticios.py
```

---

## ? **SAÍDA ESPERADA:**

```
============================================================
? GERADOR DE HEMOGRAMAS FICTÍCIOS
============================================================
Servidor FHIR: http://localhost:8080/fhir
Pacientes: 10
Hemogramas por paciente: 5
============================================================

? Criando paciente 1/10: João Silva (male)
   ? Paciente criado: ID 1
   ? Hemograma 1/5 criado ? (normal)
   ? Hemograma 2/5 criado ?? (com desvio)
   ? Hemograma 3/5 criado ? (normal)
   ? Hemograma 4/5 criado ?? (com desvio)
   ? Hemograma 5/5 criado ? (normal)

? Criando paciente 2/10: Maria Santos (female)
   ? Paciente criado: ID 2
   ? Hemograma 1/5 criado ?? (com desvio)
   ...

============================================================
? GERAÇÃO CONCLUÍDA!
============================================================
Total de hemogramas criados: 50
Hemogramas com desvios: 20
Hemogramas normais: 30
============================================================
```

---

## ? **VERIFICAR RESULTADOS:**

### **1. Logs da Aplicação Spring Boot:**

Você verá logs como:
```
? Processando Observation: 1
? Hemograma salvo: ID 1
? ALERTA: Hemoglobina - Leve
```

### **2. Console H2:**

Abra: `http://localhost:8081/hemograma-api/h2-console`

```sql
-- Ver todos os pacientes
SELECT * FROM PACIENTES;

-- Ver todos os hemogramas
SELECT * FROM HEMOGRAMAS;

-- Ver todos os desvios (incluindo anemia)
SELECT * FROM DESVIOS;

-- Contar desvios por tipo
SELECT TIPO_PARAMETRO, COUNT(*) 
FROM DESVIOS 
GROUP BY TIPO_PARAMETRO;

-- Ver casos de anemia
SELECT * FROM DESVIOS 
WHERE TIPO_PARAMETRO = 'HEMOGLOBINA' 
AND VALOR_ENCONTRADO < VALOR_REFERENCIA_MINIMO;
```

---

## ?? **PERSONALIZAR:**

Edite `gerar-hemogramas-ficticios.py`:

```python
# Linha 11-12: Alterar quantidade
NUM_PACIENTES = 20  # Aumentar para 20 pacientes
NUM_HEMOGRAMAS_POR_PACIENTE = 10  # 10 hemogramas cada

# Linha 73: Alterar chance de desvio
if com_desvio and random.random() < 0.5:  # 50% de chance
```

---

## ? **PARÂMETROS GERADOS:**

| Parâmetro | Código LOINC | Unidade | Faixa Normal |
|-----------|--------------|---------|--------------|
| Hemoglobina | 718-7 | g/dL | 12.0-18.0 |
| Leucócitos | 6690-2 | /?L | 4,000-11,000 |
| Plaquetas | 777-3 | /?L | 150,000-450,000 |
| Hematócrito | 4544-3 | % | 36-52 |
| Eritrócitos | 789-8 | milhões/?L | 4.0-6.0 |

---

## ? **DETECÇÃO DE ANEMIA:**

O gerador cria casos de anemia com:
- **Mulheres:** Hemoglobina < 12.0 g/dL
- **Homens:** Hemoglobina < 13.5 g/dL

Valores gerados para anemia:
- Mulheres: 8.0 - 11.9 g/dL
- Homens: 8.0 - 13.4 g/dL

---

## ?? **TROUBLESHOOTING:**

### **Erro: Python não encontrado**
```bash
# Instale Python 3.x
https://www.python.org/downloads/
```

### **Erro: requests não encontrado**
```bash
pip install requests
```

### **Erro: Connection refused**
```bash
# Verifique se HAPI-FHIR está rodando
docker ps

# Se não estiver, inicie:
docker-compose up -d
```

### **Erro: 404 Not Found**
```bash
# Verifique a URL do servidor FHIR
# Deve ser: http://localhost:8080/fhir
```

---

## ? **CASOS DE USO:**

1. **Teste de Carga:** Gerar 100+ hemogramas
2. **Teste de Anemia:** Verificar detecção automática
3. **Teste de Persistência:** Verificar salvamento no banco
4. **Teste de Análise:** Verificar cálculo de severidade
5. **Demonstração:** Mostrar sistema funcionando

---

## ? **EXEMPLO DE HEMOGRAMA GERADO:**

```json
{
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
      "code": "58410-2",
      "display": "Complete blood count panel"
    }]
  },
  "subject": {"reference": "Patient/1"},
  "effectiveDateTime": "2025-01-15T10:30:00Z",
  "component": [
    {
      "code": {"coding": [{"system": "http://loinc.org", "code": "718-7"}]},
      "valueQuantity": {"value": 10.5, "unit": "g/dL"}
    },
    {
      "code": {"coding": [{"system": "http://loinc.org", "code": "6690-2"}]},
      "valueQuantity": {"value": 7500, "unit": "/?L"}
    }
  ]
}
```

---

**Execute o gerador e veja o sistema em ação!** ?

