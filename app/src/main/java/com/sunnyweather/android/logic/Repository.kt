package com.sunnyweather.android.logic

import android.util.Log
import androidx.lifecycle.liveData
import com.sunnyweather.android.logic.dao.PlaceDao
import com.sunnyweather.android.logic.model.Place
import com.sunnyweather.android.logic.model.Weather
import com.sunnyweather.android.logic.network.SunnyWeatherNetwork
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlin.coroutines.CoroutineContext


/**
 * 仓库层：是判断调用方请求的数据应该是从本地数据源中获取还是从网络数据源中获取，并将获得的数据返回给调用方。
 */
object Repository {
    /**
     * 一般在仓库层中定义的方法，为了能将异步获取的数据以响应式编程的方式通知给上一层，通常会返回一个LiveData 对象。
     * 调用SunnyWeatherNetwork的searchPlaces()函数来搜索城市数据，
     * 然后判断如果服务器响应的状态是ok，那么就使用Kotlin 内置的Result.success()方法来包装获取的城市数据列表，
     * 否则使用Result.failure()方法来包装一个异常信息。
     * 最后使用一个emit()方法将包装的结果发射出去，这个emit()方法其实类似于调用LiveData的setValue()方法来通知数据变化，
     * 只不过这里我们无法直接取得返回的LiveData 对象，所以lifecycle-livedata-ktx 库提供了这样一个替代方法。
     */
    fun searchPlaces(query: String) = fire(Dispatchers.IO){
        val placeResponse = SunnyWeatherNetwork.searchPlaces(query)
        if (placeResponse.status == "ok") {
            val places = placeResponse.places
            Result.success(places)
        } else {
            Result.failure(RuntimeException("response status is ${placeResponse.status}"))
        }
    }

    /**
     * refreshWeather()方法用来刷新天气信息。因为对于调用方而言，需要调用两次
    请求才能获得其想要的所有天气数据明显是比较烦琐的行为，因此最好的做法就是在仓库层再
    进行一次统一的封装。
     * 获取实时天气信息和获取未来天气信息这两个请求是没有先后顺序的，因此让它们并发
    执行可以提升程序的运行效率
     */
    fun refreshWeather(lng: String, lat: String) =  fire(Dispatchers.IO) {
        Log.e("refreshWeather","Repository refreshWeather")
        coroutineScope {
            val deferredRealtime = async {
                SunnyWeatherNetwork.getRealtimeWeather(lng, lat)
            }
            val deferredDaily = async {
                SunnyWeatherNetwork.getDailyWeather(lng, lat)
            }
            val realtimeResponse = deferredRealtime.await()
            val dailyResponse = deferredDaily.await()
            if (realtimeResponse.status == "ok" && dailyResponse.status == "ok") {
                val weather = Weather(realtimeResponse.result.realtime,
                    dailyResponse.result.daily)
                Result.success(weather)
            } else {
                Result.failure(
                    RuntimeException(
                        "realtime response status is ${realtimeResponse.status}" +
                                "daily response status is ${dailyResponse.status}"
                    )
                )
            }
        }
    }

    /**
     * fire()函数，这是一个按照liveData()函数的参数
    接收标准定义的一个高阶函数。在fire()函数的内部会先调用一下liveData()函数，然后在
    liveData()函数的代码块中统一进行了try catch 处理，并在try语句中调用传入的Lambda
    表达式中的代码，最终获取Lambda 表达式的执行结果并调用emit()方法发射出去。
     */
    private fun <T> fire(context: CoroutineContext, block: suspend () -> Result<T>) =
        liveData<Result<T>>(context) {
            val result = try {
                block()
            } catch (e: Exception) {
                Result.failure<T>(e)
            }
            emit(result)
        }
    fun savePlace(place: Place) = PlaceDao.savePlace(place)
    fun getSavedPlace() = PlaceDao.getSavedPlace()
    fun isPlaceSaved() = PlaceDao.isPlaceSaved()
}