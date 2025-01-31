package com.carlosvpinto.pagomovilmercantil.model

data class TransferSearchBy(
    var account: String,
    var amount: String,
    var issuerBankId: String,
    var issuerCustomerId: String,
    var paymentReference: String,
    var transactionType: Int,
    var trxDate: String
)