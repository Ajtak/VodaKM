package eu.jafr.vodakm

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import eu.jafr.vodakm.databinding.ActivityMapPlaceXBinding
import org.maplibre.android.MapLibre
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.OnMapReadyCallback
import org.maplibre.android.maps.Style
import org.maplibre.android.storage.FileSource
import org.maplibre.android.style.sources.VectorSource

class MapPlaceX : AppCompatActivity() {
    private lateinit var binding: ActivityMapPlaceXBinding;

    private lateinit var mapView: MapView
    private lateinit var mbtilesPath: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        MapLibre.getInstance(this)

        this.binding = ActivityMapPlaceXBinding.inflate(layoutInflater)

        setContentView(this.binding.root)
        this.mapView = this.binding.mapView


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        mbtilesPath = Environment.getExternalStorageState() + "/czechia.mbtiles"


    }


    private fun initializeMap() {
        this.mapView.getMapAsync { mapboxMap ->

            mapboxMap.setStyle(
                Style.Builder().fromUri("mapbox://styles/mapbox/streets-v11")
            ) { style ->
                val source = VectorSource("offline-source", mbtilesPath)
                style.addSource(source)
                // Přidejte vrstvy, které chcete zobrazit
            }
            mapboxMap.cameraPosition =
                CameraPosition.Builder().target(LatLng(49.7437572, 15.3386383)).zoom(5.2).build()


        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            initializeMap()
        }
    }


    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }
}