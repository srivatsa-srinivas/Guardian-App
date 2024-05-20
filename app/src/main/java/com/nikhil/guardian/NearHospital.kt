@file:Suppress("DEPRECATION", "NAME_SHADOWING", "CatchMayIgnoreException", "OVERRIDE_DEPRECATION",
    "RemoveRedundantQualifierName", "NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS",
    "PrivatePropertyName"
)

package com.nikhil.guardian

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.PendingResult
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.nikhil.guardian.utils.PermissionUtils
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

class NearHospital : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private lateinit var map: GoogleMap
    private var googleApiClient: GoogleApiClient? = null
    private val REQUESTLOCATION = 199

    private var currentLocation: LatLng? = null // Declare at the class level

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_near)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    @SuppressLint("VisibleForTests")
    private fun enableLoc() {
        googleApiClient = GoogleApiClient.Builder(this)
            .addApi(LocationServices.API)
            .addConnectionCallbacks(object : GoogleApiClient.ConnectionCallbacks {
                override fun onConnected(bundle: Bundle?) {}
                override fun onConnectionSuspended(i: Int) {
                    googleApiClient?.connect()
                }
            })
            .addOnConnectionFailedListener {
            }.build()
        googleApiClient?.connect()
        val locationRequest = LocationRequest.create()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = 30 * 1000.toLong()
        locationRequest.fastestInterval = 5 * 1000.toLong()
        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
        builder.setAlwaysShow(true)
        val result: PendingResult<LocationSettingsResult> =
            LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build())
        result.setResultCallback { result ->
            val status: Status = result.status
            when (status.statusCode) {
                LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> try {
                    status.startResolutionForResult(
                        this@NearHospital,
                        REQUESTLOCATION
                    )
                } catch (e: IntentSender.SendIntentException) {
                }
            }
        }
        setUpLocationListener()
    }

    @SuppressLint("LogNotTimber")
    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUESTLOCATION -> when (resultCode) {
                Activity.RESULT_OK -> Log.d("abc", "OK")
                Activity.RESULT_CANCELED -> Log.d("abc", "CANCEL")
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        map.uiSettings.isZoomControlsEnabled = true
        map.setOnMarkerClickListener(this)

        when {
            PermissionUtils.isAccessFineLocationGranted(this) -> {
                when {
                    PermissionUtils.isLocationEnabled(this) -> {
                        setUpLocationListener()
                    }
                    else -> {
                        enableLoc()
                    }
                }
            }
            else -> {
                PermissionUtils.requestAccessFineLocationPermission(
                    this,
                    NearHospital.LOCATION_PERMISSION_REQUEST_CODE
                )
            }
        }
    }

    @SuppressLint("MissingPermission", "VisibleForTests")
    private fun setUpLocationListener() {
        val fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        val locationRequest = LocationRequest().setInterval(20000).setFastestInterval(20000)
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)

        fusedLocationProviderClient.requestLocationUpdates(
            locationRequest,
            object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    super.onLocationResult(locationResult)
                    for (location in locationResult.locations) {
                        currentLocation = LatLng(location.latitude, location.longitude)
                        map.clear()
                        map.isMyLocationEnabled = true
                        val currentLatLng = LatLng(location.latitude, location.longitude)
                        placeMarkerOnMap(currentLatLng)
                        map.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f))

                        findNearbyHospitals(currentLatLng)
                    }
                }
            },
            Looper.myLooper()
        )
    }

    override fun onMarkerClick(marker: Marker?): Boolean {
        // Check if the clicked marker has a tag and currentLocation is not null
        val location = marker?.tag as? LatLng
        if (location != null && currentLocation != null) {
            calculateDistanceAndDuration(currentLocation!!, location)
        }

        return false
    }

    private fun calculateDistanceAndDuration(origin: LatLng, destination: LatLng) {
        val client = OkHttpClient()
        val url =
            "https://maps.googleapis.com/maps/api/distancematrix/json?" +
                    "origins=${origin.latitude},${origin.longitude}" +
                    "&destinations=${destination.latitude},${destination.longitude}" +
                    "&mode=driving&key=AIzaSyCWeQENE3deFewK51yClc6xgtb2RYPVPhI"

        val request = Request.Builder().url(url).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) throw IOException("Unexpected code $response")

                    val responseBody = response.body?.string()
                    val jsonObject = JSONObject(responseBody)
                    val rows = jsonObject.getJSONArray("rows")
                    if (rows.length() > 0) {
                        val elements = rows.getJSONObject(0).getJSONArray("elements")
                        if (elements.length() > 0) {
                            val distanceText = elements.getJSONObject(0).getJSONObject("distance").getString("text")
                            val durationText = elements.getJSONObject(0).getJSONObject("duration").getString("text")

                            runOnUiThread {
                                Toast.makeText(
                                    this@NearHospital,
                                    "Distance: $distanceText, Duration: $durationText",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    }
                }
            }
        })
    }

    private fun placeMarkerOnMap(location: LatLng) {
        val markerOptions = MarkerOptions().position(location)
        map.addMarker(markerOptions)
    }

    private fun findNearbyHospitals(location: LatLng) {
        val client = OkHttpClient()
        val url =
            "https://maps.googleapis.com/maps/api/place/nearbysearch/json?" +
                    "location=${location.latitude},${location.longitude}" +
                    "&radius=1500&type=hospital&key=AIzaSyCWeQENE3deFewK51yClc6xgtb2RYPVPhI"

        val request = Request.Builder().url(url).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) throw IOException("Unexpected code $response")

                    val responseBody = response.body?.string()
                    val jsonObject = JSONObject(responseBody)
                    val results = jsonObject.getJSONArray("results")

                    for (i in 0 until results.length()) {
                        val result = results.getJSONObject(i)
                        val geometry = result.getJSONObject("geometry")
                        val locationObj = geometry.getJSONObject("location")
                        val lat = locationObj.getDouble("lat")
                        val lng = locationObj.getDouble("lng")
                        val hospitalLatLng = LatLng(lat, lng)

                        runOnUiThread {
                            val hospitalMarkerOptions =
                                MarkerOptions().position(hospitalLatLng).title(result.getString("name"))
                            val marker = map.addMarker(hospitalMarkerOptions)

                            // Set a tag for the marker to store the location
                            marker.tag = hospitalLatLng
                        }
                    }
                }
            }
        })
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            NearHospital.LOCATION_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    when {
                        PermissionUtils.isLocationEnabled(this) -> {
                            setUpLocationListener()
                        }
                        else -> {
                            enableLoc()
                        }
                    }
                } else {
                    Toast.makeText(
                        this,
                        getString(R.string.location_permission_not_granted),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.map_options, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.normal_map -> {
            map.mapType = GoogleMap.MAP_TYPE_NORMAL
            true
        }
        R.id.hybrid_map -> {
            map.mapType = GoogleMap.MAP_TYPE_HYBRID
            true
        }
        R.id.satellite_map -> {
            map.mapType = GoogleMap.MAP_TYPE_SATELLITE
            true
        }
        R.id.terrain_map -> {
            map.mapType = GoogleMap.MAP_TYPE_TERRAIN
            true
        }
        else -> super.onOptionsItemSelected(item)
    }
}