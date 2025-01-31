package com.carlosvpinto.pagomovilmercantil.model

data class SuccessResponseModel(
    val processingDate: String,
    val merchantIdentify: MerchantIdentifyModel,
    val transferSearchList: List<TransferDetail>,
)

data class TransferDetail(
    val trxTime: String,
    val currency: String,
    val trxStatus: String,
    val issuerCustomerId: String,
    val channel: String,
    val trxValueDate: String,
    val previouslySearched: String,
    val issuerCustomerName: String,
    val paymentReference: String
)
