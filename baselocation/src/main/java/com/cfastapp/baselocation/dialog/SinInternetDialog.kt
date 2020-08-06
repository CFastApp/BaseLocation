package com.cfastapp.baselocation.dialog

import com.cfastapp.baselocation.R
import com.cfastapp.baselocation.base.dialogfragment.BaseDialogFragment
import com.cfastapp.baselocation.databinding.DialogSinInternetBinding

class SinInternetDialog : BaseDialogFragment<DialogSinInternetBinding>(false) {
    override fun getLayout(): Int {
        return R.layout.dialog_sin_internet
    }

    override fun initOnClick() {
        binding.reintentar.setOnClickListener { dismiss() }
    }
}