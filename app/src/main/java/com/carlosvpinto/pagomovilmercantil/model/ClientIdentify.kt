package com.carlosvpinto.pagomovilmercantil.model

data class ClientIdentify(
    var browserAgent: String,
    var ipAddress: String,
    var mobile: Mobile
)