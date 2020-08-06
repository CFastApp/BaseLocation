package com.cfastapp.baselocation.base.dialogfragment

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.DialogFragment
import com.cfastapp.baselocation.dialog.ProgressDialog
import com.google.android.material.snackbar.Snackbar

abstract class BaseDialogFragment<B : ViewDataBinding>(val sizeFullParent: Boolean = true) :
    DialogFragment() {
    lateinit var binding: B
    abstract fun getLayout(): Int
    abstract fun initOnClick()
    private var progressDialog: ProgressDialog = ProgressDialog()
    private var progressIniciado = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, getLayout(), container, false)

        if (dialog != null && dialog!!.window != null) {
            dialog!!.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog!!.window!!.requestFeature(Window.FEATURE_NO_TITLE)
        }
        initOnClick()
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        if (dialog != null && dialog!!.window != null && sizeFullParent) {
            dialog!!.window!!.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
            )
        }
    }

    fun showProgressDialog() {
        if (!progressIniciado) {
            progressIniciado = true
            progressDialog.isCancelable = false
            progressDialog.show(requireFragmentManager(), "")
        }
    }

    fun hideProgressDialog() {
        if (progressIniciado) {
            progressIniciado = false
            progressDialog.dismiss()
        }
    }

    fun showMessageSnack(message: String) {
        Snackbar.make(
            requireActivity().findViewById(android.R.id.content), message, Snackbar.LENGTH_SHORT
        ).show()
    }
}