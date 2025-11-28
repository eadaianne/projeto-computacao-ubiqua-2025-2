#!/usr/bin/env python3
# -*- coding: utf-8 -*-

import requests
import random
import json
from datetime import datetime, timedelta
import time

FHIR_SERVER = "http://localhost:8080/fhir"
NUM_PACIENTES = 10
NUM_HEMOGRAMAS_POR_PACIENTE = 5

NOMES = ["Joao", "Maria", "Pedro", "Ana", "Carlos", "Juliana", "Lucas", "Fernanda", "Rafael", "Beatriz"]
SOBRENOMES = ["Silva", "Santos", "Oliveira", "Souza", "Lima", "Costa", "Pereira", "Rodrigues", "Almeida", "Nascimento"]

PARAMETROS_HEMOGRAMA = {
    "718-7": {"nome": "Hemoglobin", "unidade": "g/dL", "min": 8.0, "max": 18.0},
    "6690-2": {"nome": "Leukocytes", "unidade": "/uL", "min": 2000, "max": 15000},
    "777-3": {"nome": "Platelets", "unidade": "/uL", "min": 80000, "max": 500000},
    "4544-3": {"nome": "Hematocrit", "unidade": "%", "min": 30.0, "max": 55.0},
    "789-8": {"nome": "Erythrocytes", "unidade": "milhoes/uL", "min": 3.5, "max": 6.5}
}

def gerar_paciente():
    nome = random.choice(NOMES)
    sobrenome = random.choice(SOBRENOMES)
    genero = random.choice(["male", "female"])

    anos_atras = random.randint(18, 80)
    data_nascimento = (datetime.now() - timedelta(days=anos_atras*365)).strftime("%Y-%m-%d")

    paciente = {
        "resourceType": "Patient",
        "name": [{
            "use": "official",
            "family": sobrenome,
            "given": [nome]
        }],
        "gender": genero,
        "birthDate": data_nascimento
    }

    return paciente, f"{nome} {sobrenome}", genero

def criar_paciente_fhir(paciente):
    try:
        response = requests.post(
            f"{FHIR_SERVER}/Patient",
            headers={"Content-Type": "application/fhir+json"},
            json=paciente
        )
        if response.status_code in [200, 201]:
            patient_id = response.json()["id"]
            return patient_id
        else:
            print(f"Erro ao criar paciente: {response.status_code}")
            return None
    except Exception as e:
        print(f"Erro: {e}")
        return None

def gerar_valor_parametro(codigo, genero, com_desvio=False):
    param = PARAMETROS_HEMOGRAMA[codigo]

    if com_desvio and random.random() < 0.3:
        if random.random() < 0.5:
            if codigo == "718-7":
                if genero == "female":
                    valor = random.uniform(8.0, 11.9)
                else:
                    valor = random.uniform(8.0, 13.4)
            else:
                valor = random.uniform(param["min"] * 0.5, param["min"] * 0.95)
        else:
            valor = random.uniform(param["max"] * 1.05, param["max"] * 1.5)
    else:
        valor = random.uniform(param["min"], param["max"])

    if codigo in ["6690-2", "777-3"]:
        valor = int(valor)
    else:
        valor = round(valor, 1)

    return valor

def gerar_hemograma(patient_id, genero, com_desvio=False):
    dias_atras = random.randint(0, 30)
    data_coleta = (datetime.now() - timedelta(days=dias_atras)).strftime("%Y-%m-%dT%H:%M:%SZ")

    componentes = []
    for codigo, info in PARAMETROS_HEMOGRAMA.items():
        valor = gerar_valor_parametro(codigo, genero, com_desvio)
        componentes.append({
            "code": {
                "coding": [{
                    "system": "http://loinc.org",
                    "code": codigo,
                    "display": info["nome"]
                }]
            },
            "valueQuantity": {
                "value": valor,
                "unit": info["unidade"]
            }
        })

    hemograma = {
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
        "subject": {"reference": f"Patient/{patient_id}"},
        "effectiveDateTime": data_coleta,
        "component": componentes
    }

    return hemograma

def criar_hemograma_fhir(hemograma):
    try:
        response = requests.post(
            f"{FHIR_SERVER}/Observation",
            headers={"Content-Type": "application/fhir+json"},
            json=hemograma
        )
        return response.status_code in [200, 201]
    except Exception as e:
        print(f"Erro: {e}")
        return False

def main():
    print("=" * 60)
    print("GERADOR DE HEMOGRAMAS FICTICIOS")
    print("=" * 60)
    print(f"Servidor FHIR: {FHIR_SERVER}")
    print(f"Pacientes: {NUM_PACIENTES}")
    print(f"Hemogramas por paciente: {NUM_HEMOGRAMAS_POR_PACIENTE}")
    print("=" * 60)

    total_hemogramas = 0
    total_desvios = 0

    for i in range(NUM_PACIENTES):
        paciente, nome, genero = gerar_paciente()
        print(f"\nCriando paciente {i+1}/{NUM_PACIENTES}: {nome} ({genero})")

        patient_id = criar_paciente_fhir(paciente)
        if not patient_id:
            continue

        print(f"   Paciente criado: ID {patient_id}")

        for j in range(NUM_HEMOGRAMAS_POR_PACIENTE):
            com_desvio = random.random() < 0.4
            hemograma = gerar_hemograma(patient_id, genero, com_desvio)

            if criar_hemograma_fhir(hemograma):
                total_hemogramas += 1
                if com_desvio:
                    total_desvios += 1
                    print(f"   Hemograma {j+1}/{NUM_HEMOGRAMAS_POR_PACIENTE} criado (com desvio)")
                else:
                    print(f"   Hemograma {j+1}/{NUM_HEMOGRAMAS_POR_PACIENTE} criado (normal)")

            time.sleep(0.5)

    print("\n" + "=" * 60)
    print("GERACAO CONCLUIDA!")
    print("=" * 60)
    print(f"Total de hemogramas criados: {total_hemogramas}")
    print(f"Hemogramas com desvios: {total_desvios}")
    print(f"Hemogramas normais: {total_hemogramas - total_desvios}")
    print("=" * 60)

if __name__ == "__main__":
    main()


