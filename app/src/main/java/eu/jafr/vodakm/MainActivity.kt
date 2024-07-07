package eu.jafr.vodakm

import android.app.Activity
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import eu.jafr.vodakm.DataClass.MapData
import eu.jafr.vodakm.Workers.AlarmReceiver
import eu.jafr.vodakm.Workers.PositionWorker
import eu.jafr.vodakm.databinding.ActivityMainBinding
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.Locale
import java.util.concurrent.TimeUnit
import android.Manifest
import android.os.Build
import androidx.annotation.RequiresApi
import eu.jafr.vodakm.Computers.Computer
import eu.jafr.vodakm.GeoParser.RealmHelper
import eu.jafr.vodakm.Rivers.Ohre
import eu.jafr.vodakm.Utils.Formatter
import eu.jafr.vodakm.Utils.NotificationHelper
import eu.jafr.vodakm.Utils.PreferencesManager
import eu.jafr.vodakm.ViewModels.MainViewModel
import eu.jafr.vodakm.ViewModels.ViewModelProviderSingleton
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding;

    private lateinit var startPoint: Pair<Double, Double>
    private lateinit var endPoint: Pair<Double, Double>
    private val PERMISSION_REQUEST_CODE: Int = 1

    private lateinit var viewModel: MainViewModel
    private var formatter = Formatter()
    private var computer = Computer()

    private lateinit var realmHelper: RealmHelper;
    private lateinit var preferencesManager: PreferencesManager

    @Suppress("DEPRECATION")
    val startForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {

                val data: MapData = result.data?.extras?.getParcelable("data")!!

                val textStr = String.format(
                    Locale.ROOT,
                    "%s (%4.6fN, %4.6fE)", data.address, data.lat, data.lon
                )

                when (data.btnType) {
                    "START" -> {
                        binding.btStart.text = textStr
                        this.startPoint = Pair(data.lat, data.lon)
                        enableTrackButton()
                    }

                    "END" -> {
                        binding.btEnd.text = textStr
                        this.endPoint = Pair(data.lat, data.lon)
                        enableTrackButton()
                    }
                }
            }
        }

    private fun enableTrackButton() {
        if (::endPoint.isInitialized && ::startPoint.isInitialized) {
            this.binding.btTracking.isEnabled = true
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.myToolbar)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        realmHelper = RealmHelper()
        preferencesManager = PreferencesManager.getInstance(this@MainActivity)

        requirePermissions()

        binding.btStart.setOnClickListener {
            val intent = Intent(this, MapPlace::class.java)
            intent.putExtra("btnType", "START")
            startForResult.launch(intent)
        }

        binding.btEnd.setOnClickListener {
            val intent = Intent(this, MapPlace::class.java)
            intent.putExtra("btnType", "END")
            startForResult.launch(intent)
        }

        binding.btTracking.setOnClickListener {

            GlobalScope.launch {
                val coords = realmHelper.getRiver<Ohre>();

                val startNearestPoint = computer.getNearestPoint(coords, startPoint)
                val endNearestPoint = computer.getNearestPoint(coords, endPoint)

                preferencesManager.saveNearestPoint(
                    startNearestPoint,
                    PreferencesManager.START_NEAREST_POINT_ID
                )
                preferencesManager.saveNearestPoint(
                    endNearestPoint,
                    PreferencesManager.END_NEAREST_POINT_ID
                )


                val points = realmHelper.GetPointsBetween(startNearestPoint, endNearestPoint)

                val totalLength = computer.countDistance(points) / 1000
                binding.tvTotalLength.text = String.format(Locale.ROOT, "%4.2f km", totalLength)

                scheduleAlarm()

            }

            viewModel = ViewModelProviderSingleton.getViewModel(this)
            viewModel.kmRemain.observe(this) { newData ->
                binding.tvRemain.text = formatter.toKm(newData)
            }

            // Registrace BroadcastReceiveru pro zachycení broadcastů
            //val filter = IntentFilter("UPDATE_UI_ACTION")
            //LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, filter)

            //initUpdater()
        }
    }

    fun requirePermissions() {
        val locationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
        val notificationPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
        } else {
            PackageManager.PERMISSION_GRANTED
        }

        if (locationPermission != PackageManager.PERMISSION_GRANTED || notificationPermission != PackageManager.PERMISSION_GRANTED) {
            val permissionsNeeded = mutableListOf<String>()

            if (locationPermission != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(Manifest.permission.ACCESS_FINE_LOCATION)
            }

            if (notificationPermission != PackageManager.PERMISSION_GRANTED && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                permissionsNeeded.add(Manifest.permission.POST_NOTIFICATIONS)
            }

            ActivityCompat.requestPermissions(this, permissionsNeeded.toTypedArray(), PERMISSION_REQUEST_CODE)
        }
    }


    fun initUpdater() {
        val workRequest = PeriodicWorkRequestBuilder<PositionWorker>(10, TimeUnit.SECONDS).build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "MyUniqueWorkName",
            ExistingPeriodicWorkPolicy.REPLACE,
            workRequest
        )

    }

    private fun scheduleAlarm() {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, AlarmReceiver::class.java)
        val pendingIntent =
            PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_MUTABLE)

        // Nastavení opakovaného alarmu každých 10 minut
        //val interval = 10 * 60 * 1000L // 10 minut v milisekundách
        val interval = 60000L // 10 minut v milisekundách
        val triggerAtMillis = System.currentTimeMillis() + interval

        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            triggerAtMillis,
            interval,
            pendingIntent
        )


        pendingIntent.send()
    }

    fun batteryOptimalization() {
        val intent = Intent()
        val packageName = packageName
        val pm = getSystemService(POWER_SERVICE) as PowerManager
        if (!pm.isIgnoringBatteryOptimizations(packageName)) {
            intent.action = Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
            intent.data = Uri.parse("package:$packageName")
            startActivity(intent)
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        // Zrušení registrace BroadcastReceiveru při zničení aktivity
        //LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver)
    }


}