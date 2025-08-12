package com.example.dataannotation_phone

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.util.TypedValue
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class SurveyDialogFragment : DialogFragment() {

    private var onConfirm: ((Boolean) -> Unit)? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val activityName = arguments?.getString("activity") ?: "Unknown"
        val builder = MaterialAlertDialogBuilder(requireContext())
            .setTitle("설문")
            .setMessage("$activityName 를 올바르게 수집하셨습니까?")
            .setPositiveButton("예") { _, _ -> onConfirm?.invoke(true) }
            .setNegativeButton("아니오") { _, _ -> onConfirm?.invoke(false) }

        val dialog = builder.create()

        dialog.setOnShowListener {
            dialog.findViewById<TextView>(android.R.id.message)
                ?.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
        }

        return dialog
    }

    companion object {
        fun newInstance(activity: String, onConfirm: (Boolean) -> Unit): SurveyDialogFragment {
            val fragment = SurveyDialogFragment()
            fragment.onConfirm = onConfirm
            val args = Bundle()
            args.putString("activity", activity)
            fragment.arguments = args
            return fragment
        }
    }
}