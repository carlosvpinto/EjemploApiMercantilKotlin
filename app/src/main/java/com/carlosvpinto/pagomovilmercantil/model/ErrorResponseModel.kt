package com.carlosvpinto.pagomovilmercantil.model

data class ErrorResponseModel(
    val processingDate: String,
    val merchantIdentify: MerchantIdentifyModel,
    val errorList: Array<ErrorDetail>
)

data class ErrorDetail(
    val errorCode: String,
    val description: String
)
