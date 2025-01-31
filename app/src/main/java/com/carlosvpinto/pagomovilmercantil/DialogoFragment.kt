package com.carlosvpinto.pagomovilmercantil

import android.os.Bundle
import android.app.AlertDialog
import android.app.Dialog
import androidx.fragment.app.DialogFragment
import com.carlosvpinto.pagomovilmercantil.databinding.FragmentDialogoBinding





class ApprovalDialogFragment : DialogFragment() {

    private lateinit var binding: FragmentDialogoBinding

    // Método estático para crear una instancia del diálogo con argumentos
    companion object {
        private const val ARG_APPROVED = "approved"
        private const val ARG_AMOUNT = "amount"
        private const val ARG_REFERENCE = "reference"
        private const val ARG_IMAGE = "image"

        // Método newInstance para pasar los 4 valores
        fun newInstance(approved: String, amount: String, reference: String, image: Int): ApprovalDialogFragment {
            val fragment = ApprovalDialogFragment()
            val args = Bundle().apply {
                putString(ARG_APPROVED, approved) // Aprobado
                putString(ARG_AMOUNT, amount) // Monto
                putString(ARG_REFERENCE, reference) // Referencia
                putInt(ARG_IMAGE, image) // Imagen (ID del recurso)
            }
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // Inflar el layout personalizado
        binding = FragmentDialogoBinding.inflate(layoutInflater)

        // Obtener los valores de los argumentos
        val approved = arguments?.getString(ARG_APPROVED) ?: "Aprobado"
        val amount = arguments?.getString(ARG_AMOUNT) ?: "0.00"
        val reference = arguments?.getString(ARG_REFERENCE) ?: "N/A"
        val imageResId = arguments?.getInt(ARG_IMAGE) ?: R.drawable.aprovado // Imagen por defecto

        // Configurar los valores en el layout
        binding.tvApproved.text = approved
        binding.tvAmount.text = "Monto: $amount"
        binding.tvReference.text = "Referencia: $reference"
        binding.ivApproval.setImageResource(imageResId)

        // Configurar el botón "Aceptar"
        binding.btnAccept.setOnClickListener {
            dismiss() // Cerrar el diálogo
        }

        // Crear el diálogo con el layout personalizado
        return AlertDialog.Builder(requireContext())
            .setView(binding.root)
            .create()
    }
}