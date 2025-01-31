package com.carlosvpinto.pagomovilmercantil

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.carlosvpinto.pagomovilmercantil.model.TransferDataModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

class MainActivity : AppCompatActivity() {

    // Simular datos del API
    private val merchantId = 11103402
    private val integratorId = 31
    private val terminalId = "abcde"
    private val clientId = "17ebe62df9a1ca008b912ddd92f3d486"
    private val secretKey = "0011103402J000000405660872000000000000"

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Inicializar el botón
        val btnBotonApi = findViewById<Button>(R.id.btnBotonApi)

        // Acción cuando el botón es presionado
        btnBotonApi.setOnClickListener {
            // Ejecutar la función para llamar a la API
            callApiFromButton()
        }
    }

    // Función que se ejecuta cuando se presiona el botón
    @RequiresApi(Build.VERSION_CODES.O)
    private fun callApiFromButton() {
        // Crear datos para enviar en la solicitud
        val data = TransferDataModel(
            accountNumber = "01050054151054540721",     // Reemplazar con datos reales
            customerId = "V17313258",            // Reemplazar con datos reales
            transactionDate = "2025-01-07",  // Reemplazar con datos reales
            bankId = "105",                  // Reemplazar con datos reales
            paymentReference = "0025509602566",     // Reemplazar con datos reales
            amount = "140.00"                 // Reemplazar con datos reales
        )

        // Ejecutar la solicitud en un hilo separado usando corrutinas
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val response = callApi(data, merchantId, integratorId, terminalId, clientId, secretKey)
                // Manejar la respuesta del API
                Log.d("MainActivity", "API response: $response")

            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("MainActivity", "Error en la llamada a la API: ${e.message}")
            }
        }
    }

    // Implementación de la función callApi (reutilizando el código anterior)
    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun callApi(
        data: TransferDataModel,
        merchantId: Int,
        integratorId: Int,
        terminalId: String,
        clientId: String,
        secretKey: String
    ): JSONObject {
        // Crear el cuerpo del JSON
        val body = JSONObject().apply {
            put("merchantIdentify", JSONObject().apply {
                put("integratorId", integratorId)
                put("merchantId", merchantId)
                put("terminalId", terminalId)
            })
            put("clientIdentify", JSONObject().apply {
                put("ipAddress", "10.0.0.1")
                put("browserAgent", "Chrome 18.1.3")
                put("mobile", JSONObject().apply {
                    put("manufacturer", "Samsung")
                })
            })
            put("transferSearchBy", JSONObject().apply {
                put("account", encrypt(data.accountNumber, secretKey))
                put("issuerCustomerId", encrypt(data.customerId, secretKey))
                put("trxDate", data.transactionDate)
                put("issuerBankId", data.bankId)
                put("transactionType", 1)
                put("paymentReference", data.paymentReference)
                put("amount", data.amount)
            })
        }

        Log.d("MainActivity", "Request body: $body")

        // Realizar la solicitud HTTP POST
        val url = URL("https://apimbu.mercantilbanco.com/mercantil-banco/sandbox/v1/payment/transfer-search")
        val connection = withContext(Dispatchers.IO) { url.openConnection() as HttpURLConnection }

        return withContext(Dispatchers.IO) {
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.setRequestProperty("X-IBM-Client-ID", clientId)
            connection.doOutput = true

            // Escribir el cuerpo de la solicitud
            connection.outputStream.use { outputStream ->
                val input = body.toString().toByteArray(StandardCharsets.UTF_8)
                outputStream.write(input, 0, input.size)
            }

            try {
                // Leer la respuesta
                val responseCode = connection.responseCode
                if (responseCode in 200..299) {
                    // Respuesta exitosa
                    val response = connection.inputStream.use { inputStream ->
                        inputStream.reader().use { it.readText() }
                    }
                    Log.d("MainActivity", "Respuesta exitosa del API: $response")
                    JSONObject(response)
                } else {
                    // Leer el error en caso de respuesta no exitosa
                    val errorResponse = connection.errorStream.use { errorStream ->
                        errorStream.reader().use { it.readText() }
                    }
                    Log.e("MainActivity", "Error en la respuesta del API. Código: $responseCode, Respuesta: $errorResponse")
                    throw Exception("Error en la respuesta del API. Código: $responseCode, Respuesta: $errorResponse")
                }
            } finally {
                connection.disconnect()
            }
        }
    }
}


// Función de encriptación (debe estar definida o importada)
@RequiresApi(Build.VERSION_CODES.O)
fun encrypt(message: String, key: String): String {
    val algorithm = "AES/ECB/PKCS5Padding"

    // Convertir la llave secreta en un hash SHA-256
    val hash = MessageDigest.getInstance("SHA-256").digest(key.toByteArray())

    // Obtener los primeros 16 bytes del hash
    val keyBytes = hash.copyOfRange(0, 16)

    // Crear la clave secreta a partir del hash truncado
    val secretKey = SecretKeySpec(keyBytes, "AES")

    // Encriptar el mensaje usando AES
    val cipher = Cipher.getInstance(algorithm)
    cipher.init(Cipher.ENCRYPT_MODE, secretKey)

    val encryptedBytes = cipher.doFinal(message.toByteArray(Charsets.UTF_8))

    // Convertir el resultado a Base64
    return Base64.getEncoder().encodeToString(encryptedBytes)
}
