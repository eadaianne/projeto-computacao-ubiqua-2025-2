package br.ufg.inf.hemogramaapp.model

data class Alert(
    val id: String,
    val region: String,
    val parameter: String,
    val message: String
)