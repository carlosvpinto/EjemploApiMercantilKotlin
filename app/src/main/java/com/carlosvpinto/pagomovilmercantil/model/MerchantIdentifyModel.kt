package com.carlosvpinto.pagomovilmercantil.model

data class MerchantIdentifyModel(
    val integratorId: Int,
    val merchantId: Int,
    val terminalId: String
)
