package com.carlosvpinto.pagomovilmercantil.model

data class MerchantIdentify(
    var integratorId: Int,
    var merchantId: Int,
    var terminalId: String
)