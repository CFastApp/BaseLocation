package com.cfastapp.baselocation.base.activity

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.databinding.ViewDataBinding
import com.cfastapp.baselocation.util.ResponseEnabledGPS
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*

abstract class BaseActivityLocation<B : ViewDataBinding>(
    val secondsToUpdateGPS: Int = 1,//Tiempo de Intervalo para recepcion de coordenadas de GPS
    var primeraLocalizacion: Int = 0,//Usado para el conteo de la localizacion
    val isRemoveRequestLocation: Boolean = false
) : BaseActivity<B>(), GoogleApiClient.OnConnectionFailedListener {

    protected val REQUEST_CHECK_SETTINGS_GPS = 100
    protected lateinit var mGoogleApiClient: GoogleApiClient
    protected lateinit var locationRequest: LocationRequest
    protected val PERMISO_UBICACION = 1
    private var mFusedLocationClient: FusedLocationProviderClient? = null
    private var counter = 0

    abstract fun gpsActivatedSuccessfull()
    abstract fun permissionLocationDenied()
    abstract fun gpsNotActivate()
    abstract fun coordenadasLocalizacionActual(latitud: Double, longitud: Double)

    fun solicitarLocalizacion() {
        permisoAccesoUbicacion()
    }

    private fun permisoAccesoUbicacion() {
        if (ActivityCompat.checkSelfPermission(
                baseContext, Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            //CUANDO EL PERMISO NO FUE ACEPTADO PREVIAMENTE SE SOLICITARAN LOS PERMISOS
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), PERMISO_UBICACION
            )
        } else {
            enabledGPS()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISO_UBICACION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    enabledGPS()
                } else {
                    permissionLocationDenied()
                }
                return
            }
        }
    }

    fun enabledGPS() {
        val secondsInMilis = secondsToUpdateGPS * 1000
        this.let {
            mGoogleApiClient = GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addOnConnectionFailedListener(this).build()
            mGoogleApiClient.connect()
            locationRequest = LocationRequest.create()
            locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            locationRequest.interval =
                secondsInMilis.toLong() //TODO: Se coloca el tiempo de intervalo de actualización de las coordenadas en milisegundos
            locationRequest.fastestInterval =
                secondsInMilis.toLong() //TODO: Se coloca el tiempo de intervalo de actualización de las coordenadas en milisegundos
            val builder = LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest)
            builder.setAlwaysShow(true)

            val client = LocationServices.getSettingsClient(this)
            val task = client.checkLocationSettings(builder.build())

            task.addOnFailureListener(
                this
            ) { e ->
                if (e is ResolvableApiException) {
                    try {
                        e.startResolutionForResult(this, REQUEST_CHECK_SETTINGS_GPS)
                    } catch (sendEx: IntentSender.SendIntentException) {
                        println("BaseActivityLocation enabledGPS ${sendEx.message}")
                    }
                }
            }
            task.addOnSuccessListener {
                gpsActivatedSuccessfull()
            }
        }
    }

    override fun onConnectionFailed(p0: ConnectionResult) {
        println("BaseActivityLocation onConnectionFailed ")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CHECK_SETTINGS_GPS) {
            when (ResponseEnabledGPS.digitCode(resultCode)) {
                ResponseEnabledGPS.ENABLED -> gpsActivatedSuccessfull()
                ResponseEnabledGPS.DISABLED -> {
                    gpsNotActivate()
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun obtenerMisCoordenadas() {
        if (mFusedLocationClient == null) {
            mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        }
        mFusedLocationClient!!.requestLocationUpdates(locationRequest, locationCallback, null)
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult?) {
            super.onLocationResult(locationResult)
            val location = locationResult?.lastLocation
            if (location != null && location.latitude != 0.0 && location.longitude != 0.0) {
                counter++
                if (counter > primeraLocalizacion) {
                    coordenadasLocalizacionActual(location.latitude, location.longitude)
                    if (isRemoveRequestLocation) removeLocationUpdates()
                }
            }
        }
    }

    fun removeLocationUpdates() {
        if (mFusedLocationClient != null) mFusedLocationClient!!.removeLocationUpdates(
            locationCallback
        )
    }
}