package com.carlosvpinto.pagomovilmercantil.model

import com.beust.klaxon.Klaxon
private val klaxon = Klaxon()
class TransferDataModel(
    val accountNumber: String,
    val customerId: String,
    val transactionDate: String,
    val bankId: String,
    val paymentReference: String,
    val amount: String
)

