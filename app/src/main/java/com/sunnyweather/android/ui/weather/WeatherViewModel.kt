package com.sunnyweather.android.ui.weather

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.sunnyweather.android.logic.Repository
import com.sunnyweather.android.logic.model.Location

/**
 * 定义了一个refreshWeather()方法来
刷新天气信息，并将传入的经纬度参数封装成一个Location对象后赋值给
locationLiveData对象，然后使用Transfor mations 的switchMap()方法来观察这个对
象，并在switchMap()方法的转换函数中调用仓库层中定义的refreshWeather()方法。这
样，仓库层返回的LiveData 对象就可以转换成一个可供Activity 观察的LiveData 对象了。
 */
class WeatherViewModel: ViewModel() {
    private val locationLiveData = MutableLiveData<Location>()
    var locationLng = ""
    var locationLat = ""
    var placeName = ""
    val weatherLiveData = Transformations.switchMap(locationLiveData) { location ->
        Repository.refreshWeather(location.lng, location.lat)
    }
    fun refreshWeather(lng: String, lat: String) {
        locationLiveData.value = Location(lng, lat)
    }
}