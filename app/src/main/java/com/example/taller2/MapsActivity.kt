package com.example.taller2

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Geocoder
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.taller2.databinding.ActivityMapsBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import org.json.JSONObject
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private var mMap: GoogleMap? = null
    private var geocoder: Geocoder? = null
    private  var lastKnownLocation: Location? = null
    private lateinit var mSensorManager: SensorManager
    private lateinit var mLigthSensor: Sensor
    private lateinit var mLigthSensorEventListener: SensorEventListener
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var binding: ActivityMapsBinding


    override fun onResume() {
        super.onResume()
        mSensorManager.registerListener(mLigthSensorEventListener, mLigthSensor, SensorManager.SENSOR_DELAY_NORMAL)
    }
    override fun onPause() {
        super.onPause()
        mSensorManager.unregisterListener(mLigthSensorEventListener)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mSensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        mLigthSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)!!

        mLigthSensorEventListener = object : SensorEventListener{
            override fun onSensorChanged(event: SensorEvent?) {
                if (mMap != null) {
                    if (event!!.values[0] < 10000) {

                        mMap!!.setMapStyle(MapStyleOptions.loadRawResourceStyle(baseContext, R.raw.map_style_dark))
                    } else {

                        mMap!!.setMapStyle(MapStyleOptions.loadRawResourceStyle(baseContext, R.raw.map_style_retro))
                    }
                }

            }

            override fun onAccuracyChanged(p0: Sensor?, p1: Int) {}


        }

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        binding.texto.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE) {
                val texto = binding.texto.text.toString()
                if (texto.isNotEmpty()) {
                    geocodeLocation(texto)
                } else {
                    Toast.makeText(this, "La dirección está vacía", Toast.LENGTH_SHORT).show()
                }
                true
            } else {
                false
            }
        }

    }
    private fun geocodeLocation(locationName: String) {
        geocoder = Geocoder(this)
        try {
            val addresses = geocoder?.getFromLocationName(locationName, 1)
            if (addresses != null && addresses.isNotEmpty()) {
                val address = addresses[0]
                val latLng = LatLng(address.latitude, address.longitude)
                mMap?.apply {
                    clear() // Clear existing markers
                    addMarker(MarkerOptions().position(latLng).title(locationName))
                    animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
                }
            } else {
                Toast.makeText(this, "Dirección no encontrada", Toast.LENGTH_SHORT).show()
            }
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(this, "No se pudo geolocalizar la dirección", Toast.LENGTH_SHORT).show()
        }
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */


    private fun updateLocationUI(location: Location) {
        lastKnownLocation = location  // Almacena la última ubicación conocida
        // Actualiza la UI con la ubicación, si es necesario
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        geocoder = Geocoder(baseContext)

        mMap!!.uiSettings.isZoomGesturesEnabled = true
        mMap!!.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style_dark))
        setupMap()

        // Add a marker in Plaza Bolivar and move the camera
        val mk1 = LatLng(4.59580, -74.06578)
        val mk2 = LatLng(4.59580, -74.10590)
        val mk3 = LatLng(4.60580, -74.06590)
        mMap!!.moveCamera(CameraUpdateFactory.zoomTo(15F))
        mMap!!.moveCamera(CameraUpdateFactory.newLatLng(mk1))
        mMap?.setOnMapLongClickListener { latLng ->
            val address = getAddressFromLatLng(latLng)
            if (address != null) {
                mMap?.clear()
                mMap?.addMarker(
                    MarkerOptions()
                        .position(latLng)
                        .title(address)
                )
                mMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))

                // Calcular la distancia
                lastKnownLocation?.let {
                    val results = FloatArray(1)
                    Location.distanceBetween(it.latitude, it.longitude, latLng.latitude, latLng.longitude, results)
                    val distance = results[0]  // Distancia en metros
                    Toast.makeText(this, "Distancia al marcador: ${distance.toInt()} metros", Toast.LENGTH_LONG).show()
                } ?: run {
                    Toast.makeText(this, "Ubicación actual no disponible", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "No se encontró una dirección.", Toast.LENGTH_SHORT).show()
            }
        }




    }
    private fun getAddressFromLatLng(latLng: LatLng): String? {
        val addresses = geocoder?.getFromLocation(latLng.latitude, latLng.longitude, 1)
        return if (addresses != null && addresses.isNotEmpty()) {
            val address = addresses[0]
            address.getAddressLine(0)  // Get the first address line
        } else {
            null
        }
    }
    private fun setupMap() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE)
            return
        }

        // Enable the my location layer on the map
        mMap?.isMyLocationEnabled = true

        val locationRequest = LocationRequest.create().apply {
            interval = 10000
            fastestInterval = 5000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            smallestDisplacement = 30f // Solo recibe actualizaciones después de 30 metros de desplazamiento
        }

        fusedLocationClient.requestLocationUpdates(locationRequest, object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.locations.lastOrNull()?.let { location ->
                    if (lastKnownLocation == null || location.distanceTo(lastKnownLocation!!) > 30) {
                        updateLocationUI(location)
                        recordLocationInJson(location)
                    }
                    lastKnownLocation = location
                }
            }
        }, Looper.getMainLooper())
    }
    private fun recordLocationInJson(location: Location) {
        val jsonObject = JSONObject().apply {
            put("latitude", location.latitude)
            put("longitude", location.longitude)
            put("timestamp", SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date()))
        }

        val file = File(getFilesDir(), "location_data.json")
        FileWriter(file, true).use { fw ->
            fw.append(jsonObject.toString() + "\n")
        }
    }



    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission was granted. Do the contacts-related task you need to do.
                setupMap()
            } else {
                // Permission denied, Disable the functionality that depends on this permission.
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }



}
