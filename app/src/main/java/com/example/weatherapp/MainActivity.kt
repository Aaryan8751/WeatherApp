package com.example.weatherapp

import android.content.Context
import android.content.Intent
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.karumi.dexter.Dexter
import android.Manifest
import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.location.Location
import android.location.LocationRequest
import android.net.Uri
import android.os.Looper
import android.util.Log
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener

class MainActivity : AppCompatActivity() {

    private lateinit var mFusedLocationProviderClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        if(!isLocationEnabled()){
            Toast.makeText(this, 
                "Your Location Provider is turned off. Please turn it on", 
                Toast.LENGTH_SHORT
            ).show()
            showLocationAlertDialogBox()
        }else{

            Dexter.withActivity(this).withPermissions(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ).withListener(object : MultiplePermissionsListener{
                override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                    if(report!!.areAllPermissionsGranted()){
                        requestLocationData()
                    }

                    if(report!!.isAnyPermissionPermanentlyDenied){
                        Toast.makeText(this@MainActivity,
                            "You have denied location permissions. Please enable the as it is mandatory for app to work", Toast.LENGTH_SHORT).show()
                    }

                }

                override fun onPermissionRationaleShouldBeShown(
                    permissions: MutableList<PermissionRequest>?,
                    token: PermissionToken?
                ) {
                    showRationalDialogForPermissions()
                }

            }).onSameThread().check()

        }
        
    }

    @SuppressLint("MissingPermission")
    private fun requestLocationData(){
        val mLocationRequest = com.google.android.gms.location.LocationRequest()
        mLocationRequest.priority = com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY
        mFusedLocationProviderClient.requestLocationUpdates(
            mLocationRequest,mLocationCallback,
            Looper.myLooper()
        )
    }

    private val mLocationCallback = object :LocationCallback(){
        override fun onLocationResult(locationResult: LocationResult?) {
            val mLastLocation : Location = locationResult!!.lastLocation
            val latitude = mLastLocation.latitude
            Log.i("Current Latitude ","$latitude")
            val longitude = mLastLocation.longitude
            Log.i("Current Longitude","$longitude")
            getLocationWeatherDetails()
        }
    }
    
    private fun getLocationWeatherDetails(){
        if(Constants.isNetworkAvailable(this)){
            Toast.makeText(this@MainActivity, 
                "You have connected to internet", 
                Toast.LENGTH_SHORT
            ).show()
        }else{
            Toast.makeText(this@MainActivity, "No internet Connection", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showRationalDialogForPermissions(){
        AlertDialog.Builder(this)
            .setMessage("It looks like you have turned off permissions required to access location")
            .setPositiveButton("Go to settings"){
                _,_->
                try{
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri = Uri.fromParts("package",packageName,null)
                    intent.data = uri
                    startActivity(intent)
                }catch (e:ActivityNotFoundException){
                    e.printStackTrace()
                }
            }
            .setNegativeButton("Cancel"){
                dialog,which -> dialog.dismiss()
            }.show()
    }


    private fun showLocationAlertDialogBox(){
        val alertDialog : AlertDialog.Builder = AlertDialog.Builder(this)
        alertDialog.setTitle("Alert")
        alertDialog.setMessage("Please turn On Location settings")
        alertDialog.setPositiveButton("Settings"){dialog,which->
            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            startActivity(intent)
        }
        alertDialog.setNegativeButton("No"){dialog,which->
            dialog.dismiss()
        }
        alertDialog.show()
    }
    
    private fun isLocationEnabled():Boolean{
        
        val locationManager : LocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || 
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }
    
}