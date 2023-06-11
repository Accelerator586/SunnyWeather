package com.sunnyweather.android.logic

import androidx.lifecycle.liveData
import com.sunnyweather.android.logic.model.Place
import com.sunnyweather.android.logic.network.SunnyWeatherNetwork
import kotlinx.coroutines.Dispatchers


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
    fun searchPlaces(query: String) = liveData(Dispatchers.IO) {
        val result = try {
            val placeResponse = SunnyWeatherNetwork.searchPlaces(query)
            if (placeResponse.status =="ok"){
                val places = placeResponse.places
                Result.success(places)
            }else{
                Result.failure(RuntimeException("response statue is %{placeResponse.status}"))
            }
        }catch (e: Exception){
            Result.failure<List<Place>>(e)
        }
        emit(result)
    }
}