package com.cfastapp.baselocation.base.activity

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import com.cfastapp.baselocation.dialog.ProgressDialog
import com.cfastapp.baselocation.dialog.SinInternetDialog
import com.google.android.material.snackbar.Snackbar

abstract class BaseActivity<B : ViewDataBinding> : AppCompatActivity() {
    abstract fun getLayout(): Int
    abstract fun initOnClick()

    protected lateinit var binding: B
    private var progressDialog = ProgressDialog()
    private var progressIniciado = false
    private var sinInternetDialog = SinInternetDialog()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, getLayout())
        binding.lifecycleOwner = this
        initOnClick()
    }

    fun hideKeyBoard(view: View) {
        val inputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }

    fun showSinInternet() {
        if (!sinInternetDialog.isVisible) {
            sinInternetDialog.isCancelable = false
            sinInternetDialog.show(supportFragmentManager, "")
        }
    }

    fun showProgressDialog() {
        if (!progressIniciado) {
            progressIniciado = true
            progressDialog.isCancelable = false
            progressDialog.show(supportFragmentManager, "")
        }
    }

    fun hideProgressDialog() {
        if (progressIniciado) {
            progressIniciado = false
            progressDialog.dismiss()
        }
    }

    fun validarConexionInternet(): Boolean {
        val connectivityManager =
            getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val nw = connectivityManager.activeNetwork ?: return false
            val actNw = connectivityManager.getNetworkCapabilities(nw) ?: return false
            return when {
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                else -> false
            }
        } else {
            val nwInfo = connectivityManager.activeNetworkInfo ?: return false
            return nwInfo.isConnected
        }
    }

    fun showMessageSnack(message: String) {
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_SHORT)
            .show()
    }

    fun showMessageToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    fun <T> goToOtherActivity(cls: Class<T>, finishActual: Boolean = false) {
        val intent = Intent(baseContext, cls)
        if (finishActual) intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
    }
}
