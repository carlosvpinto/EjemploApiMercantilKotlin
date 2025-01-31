package com.carlosvpinto.pagomovilmercantil

import android.os.Build
import android.os.Bundle
import android.text.TextUtils.replace
import android.util.Log
import android.widget.Button
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import com.carlosvpinto.pagomovilmercantil.databinding.ActivityMainBinding
import com.carlosvpinto.pagomovilmercantil.model.BodySolicitudApiModel
import com.carlosvpinto.pagomovilmercantil.model.ClientIdentify
import com.carlosvpinto.pagomovilmercantil.model.ErrorResponseModel
import com.carlosvpinto.pagomovilmercantil.model.MerchantIdentify
import com.carlosvpinto.pagomovilmercantil.model.Mobile
import com.carlosvpinto.pagomovilmercantil.model.SuccessResponseModel
import com.carlosvpinto.pagomovilmercantil.model.TransferDataModel
import com.carlosvpinto.pagomovilmercantil.model.TransferSearchBy
import com.google.gson.Gson
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

    private lateinit var binding: ActivityMainBinding

    // Simular datos del API
    private val merchantId = 11103402
    private val integratorId = 31
    private val terminalId = "abcde"
    private val clientId = "17ebe62df9a1ca008b912ddd92f3d486"
    private val secretKey = "0011103402J000000405660872000000000000"
    private val TAG = "MainActivity"


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        val btnBotonApi = findViewById<Button>(R.id.btnBotonApi)

        // Configurar el clic del botón
        binding.btnBotonApi.setOnClickListener {
            // Ejecutar la función para llamar a la API
            callApiFromButton()
            // Mostrar el Fragment con la respuesta
            //showResponseFragment("apiResponse Valor del response")
            // Crear un mensaje personalizado
            // Simular una respuesta de la API
            val approved = "Aprobado"
            val amount = "1400.00"
            val reference = "0025509602566"
            val imageResId = R.drawable.negado // Imagen desde recursos

            // Mostrar el diálogo de aprobación con los 4 valores
            showApprovalDialog(approved, amount, reference, imageResId)
        }
        llenarFormulario()
    }

    private fun llenarFormulario() {
        binding.etAccountNumber.setText("01050054151054540721")
        binding.etCustomerId.setText("V17313258")
        binding.etTransactionDate.setText("2025-01-07")
        binding.etBankId.setText("105")
        binding.etPaymentReference.setText("0025509602566")
        binding.etAmount.setText("1400.00")

    }

    // Método para mostrar el diálogo de aprobación
    private fun showApprovalDialog(
        approved: String,
        amount: String,
        reference: String,
        imageResId: Int
    ) {
        val dialog = ApprovalDialogFragment.newInstance(approved, amount, reference, imageResId)
        dialog.show(supportFragmentManager, "ApprovalDialog")
    }

//    // Método para mostrar el Fragment con la respuesta
//    private fun showResponseFragment(response: String) {
//        val fragment = ResponseFragment().apply {
//            arguments = Bundle().apply {
//                putString("response", response)
//            }
//        }
//
//        // Reemplazar el contenido actual con el Fragment
//        supportFragmentManager.beginTransaction().apply {
//            replace(R.id.fragmentContainer, fragment)
//            addToBackStack(null) // Agregar la transacción a la pila de retroceso
//            commit() // Confirmar la transacción
//        }
//    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun callApiFromButton() {

        val data = TransferDataModel(
            accountNumber = binding.etAccountNumber.text.toString(),
            customerId = binding.etCustomerId.text.toString(),
            transactionDate = binding.etTransactionDate.text.toString(),
            bankId = binding.etBankId.text.toString(),
            paymentReference = binding.etPaymentReference.text.toString(),
            amount = binding.etAmount.text.toString()
        )

        CoroutineScope(Dispatchers.Main).launch {
            try {
                val response =
                    callApi(data, merchantId, integratorId, terminalId, clientId, secretKey)

                Log.d("MainActivity", "API response: $response")
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("MainActivity", "Error en la llamada a la APIII: ${e.message}")
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun callApi(
        data: TransferDataModel,
        merchantId: Int,
        integratorId: Int,
        terminalId: String,
        clientId: String,
        secretKey: String
    ): Any {
        // Crear el cuerpo de la solicitud utilizando el nuevo modelo BodySolicitudApiModel
        val bodySolicitud = BodySolicitudApiModel(
            merchantIdentify = MerchantIdentify(
                integratorId = integratorId,
                merchantId = merchantId,
                terminalId = terminalId
            ),
            clientIdentify = ClientIdentify(
                ipAddress = "10.0.0.1",
                browserAgent = "Chrome 18.1.3",
                mobile = Mobile(manufacturer = "Samsung")
            ),
            transferSearchBy = TransferSearchBy(
                account = encrypt(data.accountNumber, secretKey),
                issuerCustomerId = encrypt(data.customerId, secretKey),
                trxDate = data.transactionDate,
                issuerBankId = data.bankId,
                transactionType = 1,
                paymentReference = data.paymentReference,
                amount = data.amount
            )
        )

        // Convertir el modelo BodySolicitudApiModel a JSON usando Gson
        val jsonBody = Gson().toJson(bodySolicitud)
        Log.d("MainActivity", "Request body: $jsonBody")

        val url =
            URL("https://apimbu.mercantilbanco.com/mercantil-banco/sandbox/v1/payment/transfer-search")
        val connection = withContext(Dispatchers.IO) { url.openConnection() as HttpURLConnection }

        return withContext(Dispatchers.IO) {
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.setRequestProperty("X-IBM-Client-ID", clientId)
            connection.doOutput = true

            // Enviar el cuerpo de la solicitud
            connection.outputStream.use { outputStream ->
                val input = jsonBody.toByteArray(StandardCharsets.UTF_8)
                outputStream.write(input, 0, input.size)
            }

            val responseCode = connection.responseCode
            Log.d(TAG, "callApi: responseCode $responseCode")
            if (responseCode == HttpURLConnection.HTTP_OK) {

                val response = connection.inputStream.use { inputStream ->
                    inputStream.reader().use { it.readText() }
                }

                val jsonResponse = JSONObject(response)

                Log.d(TAG, "callApi: Paso por jsonResponse $jsonResponse")
                if (jsonResponse.has("errorList")) {
                    Log.d(TAG, "callApi: Paso por Errorlist")
                    val errorResponse = Gson().fromJson(response, ErrorResponseModel::class.java)
                    Log.e(
                        "MainActivity",
                        "Error de API: ${errorResponse.errorList[0]?.description}"
                    )
                    errorResponse
                } else {
                    Log.d(TAG, "callApi: paso por success")
                    val successResponse =
                        Gson().fromJson(response, SuccessResponseModel::class.java)
                    successResponse
                }

            } else {
                // Captura la respuesta del servidor en caso de error
                val errorResponse = connection.errorStream?.use { inputStream ->
                    inputStream.reader().use { it.readText() }
                } ?: throw Exception("No se pudo obtener la respuesta del servidor")

                // Convierte la respuesta en un objeto ErrorResponseModel
                val errorResponseModel =
                    Gson().fromJson(errorResponse, ErrorResponseModel::class.java)

                // Obtén el valor de "description" del primer error en la lista
                val errorDescription = errorResponseModel.errorList?.firstOrNull()?.description
                    ?: "Error desconocido"

                // Lanza una excepción con el código de respuesta y la descripción del error
                throw Exception("Error en la solicitud: Código de respuesta $responseCode. Descripción del error: $errorDescription")
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
}
