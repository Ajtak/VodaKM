package eu.jafr.vodakm.Workers

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import eu.jafr.vodakm.MainActivity
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class PositionWorker(context: Context, workerParams: WorkerParameters) :
    Worker(context, workerParams) {
    @DelicateCoroutinesApi
    override fun doWork(): Result {
       // val computeDistance = ComputeDistance()

        val startPoint = Pair(50.1935914, 12.7863739)
        val endPoint = Pair(50.2383572, 12.9291231)

        GlobalScope.launch {
            //val dist = computeDistance.compute(applicationContext, startPoint, endPoint)
           // android.util.Log.e("TEMNOTAW", "$dist")
        }
        return Result.success()
    }
}
