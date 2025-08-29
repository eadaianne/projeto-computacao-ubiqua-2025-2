# Sistema Ub�quo de An�lise de Hemogramas

Este projeto implementa um sistema para an�lise de hemogramas usando FHIR e computa��o ub�qua, conforme especificado na disciplina de Sistemas Ub�quos.

## Vis�o Geral

O sistema � respons�vel por:
- Receber mensagens FHIR via subscription (Marco 1)
- Realizar an�lise individual de hemogramas com detec��o de desvios (Marco 2)
- Armazenar dados localmente (Marco 3)
- Implementar an�lise coletiva com janelas deslizantes (Marco 4)
- Fornecer API REST para consulta de alertas
- Suportar aplicativo m�vel Android com notifica��es

## Tecnologias Utilizadas

- **Spring Boot 3.2.0** - Framework principal
- **Java 17** - Linguagem de programa��o
- **H2 Database** - Banco de dados em mem�ria (desenvolvimento)
- **HAPI FHIR** - Biblioteca para manipula��o de recursos FHIR
- **SpringDoc OpenAPI** - Documenta��o autom�tica da API
- **JUnit 5** - Testes unit�rios

## Estrutura do Projeto

```
src/
??? main/
?   ??? java/br/ufg/inf/hemograma/
?   ?   ??? controller/          # Controllers REST
?   ?   ??? dto/                 # Data Transfer Objects
?   ?   ??? model/               # Entidades JPA
?   ?   ??? repository/          # Reposit�rios de dados
?   ?   ??? service/             # L�gica de neg�cio
?   ?   ??? config/              # Configura��es
?   ?   ??? exception/           # Exce��es customizadas
?   ??? resources/
?       ??? application.yml      # Configura��es da aplica��o
??? test/                        # Testes unit�rios
```

## API REST - Endpoint de Alertas

### Endpoints Dispon�veis

#### 1. Listar Alertas
```http
GET /api/v1/alertas
```

**Par�metros de consulta:**
- `page` (int): N�mero da p�gina (padr�o: 0)
- `size` (int): Tamanho da p�gina (padr�o: 20)
- `sortBy` (string): Campo para ordena��o (padr�o: dataCriacao)
- `sortDir` (string): Dire��o da ordena��o - ASC/DESC (padr�o: DESC)
- `severidade` (enum): Filtrar por severidade (BAIXA, MEDIA, ALTA, CRITICA)
- `parametro` (enum): Filtrar por par�metro (LEUCOCITOS, HEMOGLOBINA, PLAQUETAS, HEMATOCRITO)
- `patientId` (string): Filtrar por ID do paciente
- `dataInicio` (datetime): Data inicial para filtro
- `dataFim` (datetime): Data final para filtro
- `apenasNaoProcessados` (boolean): Filtrar apenas alertas n�o processados

**Exemplo de resposta:**
```json
{
  "content": [
    {
      "id": 1,
      "hemogramaId": 100,
      "fhirObservationId": "obs-hemograma-001",
      "patientId": "patient-001",
      "parametro": "LEUCOCITOS",
      "nomeParametro": "Leuc�citos",
      "unidadeParametro": "/�L",
      "valorEncontrado": 15000.0,
      "valorMinimoReferencia": 4000.0,
      "valorMaximoReferencia": 11000.0,
      "tipoDesvio": "ACIMA_MAXIMO",
      "severidade": "ALTA",
      "dataCriacao": "2025-08-29T10:30:00",
      "dataColeta": "2025-08-29T08:00:00",
      "processado": false,
      "percentualDesvio": 36.4,
      "descricao": "Leuc�citos acima do valor m�ximo de refer�ncia (15000.00 /�L)"
    }
  ],
  "totalElements": 1,
  "totalPages": 1,
  "size": 20,
  "number": 0
}
```

#### 2. Buscar Alerta por ID
```http
GET /api/v1/alertas/{id}
```

#### 3. Marcar Alerta como Processado
```http
PUT /api/v1/alertas/{id}/processar
```

#### 4. Obter Resumo de Alertas
```http
GET /api/v1/alertas/resumo
```

**Exemplo de resposta:**
```json
{
  "totalAlertas": 150,
  "alertasNaoProcessados": 25,
  "dataInicio": "2025-08-22T10:30:00",
  "dataFim": "2025-08-29T10:30:00",
  "alertasPorSeveridade": {
    "ALTA": 45,
    "MEDIA": 60,
    "BAIXA": 30,
    "CRITICA": 15
  },
  "alertasPorParametro": {
    "LEUCOCITOS": 40,
    "HEMOGLOBINA": 35,
    "PLAQUETAS": 45,
    "HEMATOCRITO": 30
  },
  "alertasPorTipoDesvio": {
    "ACIMA_MAXIMO": 85,
    "ABAIXO_MINIMO": 65
  },
  "totalPacientesComAlertas": 45,
  "mediaAlertasPorPaciente": 3.33,
  "parametroMaisFrequente": "PLAQUETAS",
  "severidadeMaisComum": "MEDIA"
}
```

#### 5. Listar Alertas por Paciente
```http
GET /api/v1/alertas/paciente/{patientId}
```

## Valores de Refer�ncia

Conforme especifica��o do projeto, os seguintes valores s�o utilizados para detec��o de desvios:

| Par�metro    | Unidade | Valor M�nimo | Valor M�ximo |
|--------------|---------|--------------|--------------|
| Leuc�citos   | /�L     | 4.000        | 11.000       |
| Hemoglobina  | g/dL    | 12.0         | 17.5         |
| Plaquetas    | /�L     | 150.000      | 450.000      |
| Hemat�crito  | %       | 36           | 52           |

## Como Executar

### Pr�-requisitos
- Java 17 ou superior
- Maven 3.6 ou superior

### Executando a aplica��o
```bash
# Clonar o reposit�rio
git clone <url-do-repositorio>
cd projeto-computacao-ubiqua-2025-2

# Compilar e executar
mvn spring-boot:run
```

### Acessando a aplica��o
- **API Base URL**: http://localhost:8080/hemograma-api
- **Swagger UI**: http://localhost:8080/hemograma-api/swagger-ui.html
- **Console H2**: http://localhost:8080/hemograma-api/h2-console

### Executando os testes
```bash
mvn test
```

## Documenta��o da API

A documenta��o completa da API est� dispon�vel atrav�s do Swagger UI quando a aplica��o estiver em execu��o.

## Pr�ximos Passos

Para completar os marcos do projeto:

1. **Marco 1**: Implementar receptor FHIR Subscription
2. **Marco 3**: Configurar banco de dados persistente
3. **Marco 4**: Implementar an�lise coletiva com janelas deslizantes
4. **Aplicativo M�vel**: Desenvolver app Android
5. **Seguran�a**: Implementar HTTPS com mTLS

## Contribui��o

Este projeto faz parte da disciplina de Sistemas Ub�quos. Para contribuir:

1. Fa�a um fork do projeto
2. Crie uma branch para sua feature
3. Commit suas mudan�as
4. Push para a branch
5. Abra um Pull Request
