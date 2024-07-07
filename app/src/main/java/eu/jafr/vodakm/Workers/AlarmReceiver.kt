package eu.jafr.vodakm.Workers

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat.getSystemService
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import eu.jafr.vodakm.Computers.Computer
import eu.jafr.vodakm.GeoParser.RealmHelper
import eu.jafr.vodakm.MainActivity
import eu.jafr.vodakm.Rivers.Ohre
import eu.jafr.vodakm.Utils.Formatter
import eu.jafr.vodakm.Utils.NotificationHelper
import eu.jafr.vodakm.Utils.PreferencesManager
import eu.jafr.vodakm.ViewModels.MainViewModel
import eu.jafr.vodakm.ViewModels.ViewModelProviderSingleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.text.Normalizer.Form
import java.util.Locale
import java.util.function.Consumer


class AlarmReceiver : BroadcastReceiver() {
    private var locationManager: LocationManager? = null

    //  private val computeDistance = ComputeDistance()
    private val scope = CoroutineScope(Dispatchers.Main)

    lateinit var ctx: Context
    lateinit var viewModel: MainViewModel

    lateinit var preferencesManager: PreferencesManager;
    lateinit var computer: Computer
    lateinit var realmHelper: RealmHelper
    private lateinit var notifHelper: NotificationHelper
    lateinit var formatter: Formatter

    @SuppressLint("MissingPermission")
    override fun onReceive(context: Context?, intent: Intent?) {
        ctx = context!!
        viewModel = ViewModelProviderSingleton.getViewModel(ctx)

        preferencesManager = PreferencesManager.getInstance(context)
        computer = Computer()
        realmHelper = RealmHelper()

        notifHelper = NotificationHelper(ctx)
        notifHelper.createNotificationChannel()
        formatter = Formatter()


        Log.e("MyAlarmReceiver", "Alarm triggered")

        locationManager = (context.getSystemService(Context.LOCATION_SERVICE) as LocationManager)
        locationManager!!.getCurrentLocation(
            LocationManager.NETWORK_PROVIDER,
            null,
            context.mainExecutor,
            locationCallback
        )
    }

    private val locationCallback = Consumer<Location> { location ->
        location.let {
            //val startPoint = Pair(it.latitude, it.longitude)
            val startPoint = Pair(50.1886728, 12.7547258)

            scope.launch(Dispatchers.IO) {
                val coords = realmHelper.getRiver<Ohre>();

                val startNearestPoint = preferencesManager.getNearestPoint(PreferencesManager.START_NEAREST_POINT_ID)
                val endNearestPoint = preferencesManager.getNearestPoint(PreferencesManager.END_NEAREST_POINT_ID)

                val currentPoint = computer.getNearestPoint(coords, startPoint)

                val pointsFromStart = realmHelper.GetPointsBetween(startNearestPoint, currentPoint)
                val lengthFromStart = computer.countDistance(pointsFromStart) / 1000


                val points = realmHelper.GetPointsBetween(currentPoint, endNearestPoint)
                val totalLength = computer.countDistance(points) / 1000

                scope.launch(Dispatchers.Main) {
                    viewModel.updateKmRemain(totalLength)

                    val notMesage = String.format(
                        Locale.ROOT,
                        "Zbývá: %s\n Ujeto: %s",
                        formatter.toKm(totalLength),
                        formatter.toKm(lengthFromStart)

                    );
                    notifHelper.sendNotification("xx", notMesage)
                }
            }
        }
    }
}