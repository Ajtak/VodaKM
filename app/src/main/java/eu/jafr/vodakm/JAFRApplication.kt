package eu.jafr.vodakm

import android.app.Application
import android.content.Context
import eu.jafr.vodakm.Rivers.Ohre
import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration
import org.geotools.data.DataStore
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.util.prefs.Preferences

class JAFRApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        copyRealmDatabaseFromAssets(this, "database.realm", "database.realm")
    }

    fun copyRealmDatabaseFromAssets(
        context: Context,
        assetFileName: String,
        outputFileName: String
    ) {
        val outputFile = File(context.getFilesDir(), outputFileName)
        if (!outputFile.exists()) {
            try {
                val inputStream: InputStream = context.assets.open(assetFileName)
                val outputStream = FileOutputStream(outputFile)
                val buffer = ByteArray(1024)
                var length: Int

                while (inputStream.read(buffer).also { length = it } > 0) {
                    outputStream.write(buffer, 0, length)
                }

                outputStream.flush()
                outputStream.close()
                inputStream.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }
}