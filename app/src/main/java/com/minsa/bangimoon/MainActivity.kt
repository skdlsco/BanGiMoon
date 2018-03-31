package com.minsa.bangimoon

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.PermissionChecker
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.minsa.bangimoon.utils.GpsInfo
import com.minsa.bangimoon.utils.NetworkHelper
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*


class MainActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMapLoadedCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var gpsInfo: GpsInfo
    var markers = ArrayList<Marker>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        gpsInfo = GpsInfo(this)
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.main_map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    private fun getData() {
        NetworkHelper.retrofitInstance.getDevices().enqueue(object : Callback<ResponseBody> {
            override fun onFailure(call: Call<ResponseBody>?, t: Throwable?) {

            }

            override fun onResponse(call: Call<ResponseBody>?, response: Response<ResponseBody>?) {
                val str = response?.body()?.string()
                if (!JSONObject(str).getBoolean("result"))
                    return
                val json = JSONObject(str).getJSONArray("data")
                if (json.length() == 0)
                    return
                val newMarkers = ArrayList<Marker>()
                (0 until json.length()).forEach {
                    json.getJSONObject(it).let {
                        val markerLocation = LatLng(it.getDouble("lat"),
                                it.getDouble("lon"))
                        val marker = mMap.addMarker(MarkerOptions()
                                .position(markerLocation)
                                .title("${it.getInt("count")}명의 잠재적 가해자들이 있습니다!")
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)))
                        marker.showInfoWindow()
                        newMarkers.add(marker)
                    }
                }
                markers.forEach { it.remove() }
                markers.clear()
                markers.addAll(newMarkers)
            }
        })
    }

    override fun onMapReady(googleMap: GoogleMap?) {
        mMap = googleMap!!
        mMap.setMinZoomPreference(10f)
        setLocation()
        checkPermission()
        Timer().schedule(object : TimerTask() {
            override fun run() {
                getData()
            }
        }, 0, 5000)
    }

    override fun onMapLoaded() {
    }

    private fun checkPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermission()

        } else {
            setMyLocationEnabled()
        }
    }

    private fun requestPermission() {
        if (PermissionChecker.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PermissionChecker.PERMISSION_GRANTED
                && PermissionChecker.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PermissionChecker.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
                    REQUEST_PERMISSION_CODE)
        } else {
            setMyLocationEnabled()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            REQUEST_PERMISSION_CODE -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED || grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    setMyLocationEnabled()
                } else {
                    Toast.makeText(this, "권한이 없습니다.", Toast.LENGTH_SHORT).show()
                    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION), REQUEST_PERMISSION_CODE)
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun setMyLocationEnabled() {
        mMap.isMyLocationEnabled = true
        setLocation()
    }

    private fun setLocation() {
        gpsInfo.getLocation()
        if (gpsInfo.isGetLocation) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(gpsInfo.latitude, gpsInfo.longitude), 17.0f))

        } else {
            Toast.makeText(this, "자신의 위치 불러오기 실패", Toast.LENGTH_SHORT).show()
        }
        Log.e("lat", "${gpsInfo.latitude}")
        Log.e("lon", "${gpsInfo.longitude}")
    }

    override fun onDestroy() {
        gpsInfo.stopUsingGPS()
        super.onDestroy()
    }

    companion object {
        const val REQUEST_PERMISSION_CODE = 1000
    }
}
