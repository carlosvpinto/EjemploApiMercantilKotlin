package com.carlosvpinto.pagomovilmercantil.model

data class BodySolicitudApiModel(
    var clientIdentify: ClientIdentify,
    var merchantIdentify: MerchantIdentify,
    var transferSearchBy: TransferSearchBy
)