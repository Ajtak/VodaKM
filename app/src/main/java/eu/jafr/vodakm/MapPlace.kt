package eu.jafr.vodakm

import android.content.Intent
import android.graphics.Rect
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import eu.jafr.vodakm.DataClass.MapData
import eu.jafr.vodakm.databinding.ActivityMapPlaceBinding
import org.osmdroid.api.IMapController
import org.osmdroid.config.Configuration.getInstance
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.events.MapListener
import org.osmdroid.events.ScrollEvent
import org.osmdroid.events.ZoomEvent
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import java.io.IOException
import java.util.Locale


class MapPlace : AppCompatActivity() {
    private lateinit var binding: ActivityMapPlaceBinding;
    lateinit var mMap: MapView
    lateinit var controller: IMapController;
    lateinit var mMyLocationOverlay: MyLocationNewOverlay;
    private val REQUEST_PERMISSIONS_REQUEST_CODE = 1;

    lateinit var outData: MapData
    var btnType: String = "";

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        this.binding = ActivityMapPlaceBinding.inflate(layoutInflater)
        this.btnType = intent.extras?.getString("btnType")!!

        setContentView(this.binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        this.binding.fab.setOnClickListener {
            val data = Intent()
            data.putExtra("data", outData);
            setResult(RESULT_OK, data);
            finish()
        }


        getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));


        mMap = binding.mapView
        mMap.setTileSource(TileSourceFactory.MAPNIK)
        mMap.mapCenter
        mMap.setMultiTouchControls(true)
        mMap.getLocalVisibleRect(Rect())


        mMyLocationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(this), mMap)
        controller = mMap.controller

        mMyLocationOverlay.enableMyLocation()
        mMyLocationOverlay.enableFollowLocation()
        mMyLocationOverlay.isDrawAccuracyEnabled = true
        mMyLocationOverlay.runOnFirstFix {
            runOnUiThread {
                controller.setCenter(mMyLocationOverlay.myLocation);
                controller.animateTo(mMyLocationOverlay.myLocation)
            }
        }


;
        val mapPoint = GeoPoint(49.7437572, 15.3386383)

        controller.setZoom(8.3)
        controller.animateTo(mapPoint)


        mMap.overlays.add(mMyLocationOverlay)

        mMap.getOverlays().add(MapEventsOverlay(object : MapEventsReceiver {
            override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
                return false
            }

            override fun longPressHelper(p: GeoPoint?): Boolean {
                val markerId = "selectedPos"

                mMap.overlays.forEach {
                    if (it is Marker && it.id == markerId) {
                        mMap.overlays.remove(it)
                    }
                }

                val marker = Marker(mMap)
                marker.id = markerId
                marker.setPosition(p)
                val address = getAddressFromLocation(p!!.latitude, p.longitude)
                marker.title = address

                outData =
                    MapData(
                        address = address,
                        lat = p.latitude,
                        lon = p.longitude,
                        btnType = btnType
                    )

                marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                mMap.getOverlays().add(marker)
                mMap.invalidate() // Aktualizace mapy


                return true
            }


        }))
        mMap.addMapListener(object : MapListener {
            override fun onZoom(zoomEvent: ZoomEvent): Boolean {
                return false
            }

            override fun onScroll(scrollEvent: ScrollEvent): Boolean {
                Log.d("aabb", "map onScroll") // THIS IS NOT PRINTED WHEN FOLLOWLOCATION MOVES MAP
                return false
            }
        })


    }


    override fun onResume() {
        super.onResume();
        mMap.onResume(); //needed for compass, my location overlays, v6.0.0 and up
    }

    override fun onPause() {
        super.onPause();
        mMap.onPause();  //needed for compass, my location overlays, v6.0.0 and up
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        val permissionsToRequest = ArrayList<String>();
        var i = 0;
        while (i < grantResults.size) {
            permissionsToRequest.add(permissions[i]);
            i++;
        }
        if (permissionsToRequest.size > 0) {
            ActivityCompat.requestPermissions(
                this, permissionsToRequest.toTypedArray(), REQUEST_PERMISSIONS_REQUEST_CODE
            );
        }
    }

    fun getAddressFromLocation(latitude: Double, longitude: Double): String {
        val geocoder = Geocoder(this, Locale.getDefault())
        var addressText = "Adresa nenalezena"

        try {
            val addresses: List<Address>? = geocoder.getFromLocation(latitude, longitude, 1)
            if (addresses != null && !addresses.isEmpty()) {
                val address: Address = addresses[0]
                // Získání kompletní adresy jako text
                addressText = address.getAddressLine(0)
            }
        } catch (e: IOException) {
            Log.e("Geocoder", "Unable to get address from location", e)
        }

        return addressText
    }


}