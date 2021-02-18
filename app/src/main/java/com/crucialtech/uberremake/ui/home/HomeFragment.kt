package com.crucialtech.uberremake.ui.home

import android.Manifest
import android.content.pm.PackageManager
import android.content.res.Resources
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.crucialtech.uberremake.DRIVER_INFO_REFERENCE
import com.crucialtech.uberremake.DRIVER_LOCATION_REFERENCE
import com.crucialtech.uberremake.R
import com.firebase.geofire.GeoFire
import com.firebase.geofire.GeoLocation
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener

class HomeFragment : Fragment(), OnMapReadyCallback {

    private lateinit var homeViewModel: HomeViewModel

    private lateinit var mMap: GoogleMap

    lateinit var mapFragment : SupportMapFragment

    lateinit var locationRequest: LocationRequest
    lateinit var locationCallback: LocationCallback
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient


    //Online System
    lateinit var onlineRef : DatabaseReference
    lateinit var currentUserRef : DatabaseReference
    lateinit var driversLocationRef : DatabaseReference
    lateinit var geoFire: GeoFire


    private val onlineValueEventListener = object : ValueEventListener{
        override fun onDataChange(snapshot: DataSnapshot) {
            if(snapshot.exists()){
                currentUserRef.onDisconnect().removeValue()
            }
        }

        override fun onCancelled(error: DatabaseError) {
            Snackbar.make(mapFragment.requireView(),error.message,Snackbar.LENGTH_LONG).show()
        }

    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_home, container, false)



        mapFragment = childFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        init()

        return root
    }

    private fun init() {
        onlineRef = FirebaseDatabase.getInstance().reference.child(".info/connected")
        driversLocationRef = FirebaseDatabase.getInstance().reference.child(
            DRIVER_LOCATION_REFERENCE)
        currentUserRef = FirebaseDatabase
            .getInstance()
            .reference
            .child(DRIVER_LOCATION_REFERENCE)
            .child(FirebaseAuth.getInstance().currentUser!!.uid)

        geoFire = GeoFire(driversLocationRef)


        locationRequest = LocationRequest()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.fastestInterval = 3000
        locationRequest.interval = 5000
        locationRequest.smallestDisplacement = 10f

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                super.onLocationResult(locationResult)

                val newPos = LatLng(locationResult!!.lastLocation.latitude
                    ,locationResult.lastLocation.longitude)

                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(newPos,18f))

                geoFire.setLocation(FirebaseAuth.getInstance().currentUser!!.uid,
                    GeoLocation(locationResult.lastLocation.latitude,locationResult.lastLocation.longitude)
                ){ key :String?, error:DatabaseError? ->
                    if(error != null){
                        Snackbar.make(mapFragment.requireView(),error.message,Snackbar.LENGTH_LONG).show()
                    }else{
                        Snackbar.make(mapFragment.requireView(),"You are online",Snackbar.LENGTH_LONG).show()

                    }
                }
            }
        }

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireContext())
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        fusedLocationProviderClient.requestLocationUpdates(locationRequest,locationCallback, Looper.myLooper())
    }

    override fun onDestroy() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
        geoFire.removeLocation(FirebaseAuth.getInstance().currentUser!!.uid)
        onlineRef.removeEventListener(onlineValueEventListener)
        super.onDestroy()

    }

    override fun onResume() {
        super.onResume()
        registerOnlineSystem()
    }

    private fun registerOnlineSystem() {
        onlineRef.addValueEventListener(onlineValueEventListener)
    }

    override fun onMapReady(googleMap: GoogleMap?) {
        mMap = googleMap!!

        Dexter.withContext(requireContext())
            .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
            .withListener(object : PermissionListener {
                override fun onPermissionGranted(p0: PermissionGrantedResponse?) {
                    mMap.isMyLocationEnabled = true
                    mMap.uiSettings.isMyLocationButtonEnabled = true
                    mMap.setOnMyLocationClickListener {
                        if (ActivityCompat.checkSelfPermission(
                                requireContext(),
                                Manifest.permission.ACCESS_FINE_LOCATION
                            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                                requireContext(),
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            // TODO: Consider calling
                            //    ActivityCompat#requestPermissions
                            // here to request the missing permissions, and then overriding
                            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                            //                                          int[] grantResults)
                            // to handle the case where the user grants the permission. See the documentation
                            // for ActivityCompat#requestPermissions for more details.
                            return@setOnMyLocationClickListener
                        }
                        fusedLocationProviderClient.lastLocation.addOnFailureListener {
                            Toast.makeText(requireContext(), it.message.toString(), Toast.LENGTH_SHORT).show()
                        }.addOnSuccessListener {
                            val userLatlng = LatLng(it.latitude,it.longitude)
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLatlng,18f))
                        }

                        true
                    }


                }





                override fun onPermissionDenied(p0: PermissionDeniedResponse?) {
                    Toast.makeText(
                        requireContext(),
                        "Permission ${p0?.permissionName} was denied",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                override fun onPermissionRationaleShouldBeShown(
                    p0: PermissionRequest?,
                    p1: PermissionToken?
                ) {
                    TODO("Not yet implemented")
                }

            }).check()


        try{
            val success = googleMap
                .setMapStyle(
                    MapStyleOptions
                    .loadRawResourceStyle(context,R.raw.uber_maps_style))
            if(!success){
                Log.e("ERROR", "Style parsing Error" )
            }
        }catch (e: Resources.NotFoundException){
            Log.e("Error", e.message.toString())
        }


    }
}