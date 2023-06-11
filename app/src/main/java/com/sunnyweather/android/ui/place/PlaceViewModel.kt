package com.sunnyweather.android.ui.place

import android.app.DownloadManager.Query
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.sunnyweather.android.logic.Repository
import com.sunnyweather.android.logic.model.Place

/**
 * ViewModel层
 * 先PlaceV iewModel 中也定义了一个searchPlaces()方法，
 但是这里并没有直接调用仓库层中的searchPlaces()方法，而是将传入的搜索参数赋值给了一个searchLiveData对象，
 并使用Transformations的switchMap()方法来观察这个对象，否则仓库层返回的LiveData 对象将无法进行观察。
 */
class PlaceViewModel: ViewModel() {
    private  val searchLiveData = MutableLiveData<String>()
    //用于对界面上显示的城市数 据进行缓存，
    // 因为原则上与界面相关的数据都应该放到ViewModel中，这样可以保证它们在手机屏幕发生旋转的时候不会丢失，稍后我们会在编写UI层代码的时候用到这个集合。
    val placeList = ArrayList<Place>()

    /**
     * 现在每当searchPlaces()函数被调用时，
    switchMap()方法所对应的转换函数就会执行。然后在转换函数中，我们只需要调用仓库层中
    定义的searchPlaces()方法就可以发起网络请求，同时将仓库层返回的LiveData 对象转换成
    一个可供Activity 观察的LiveData 对象。
     */
    val placeLiveData = Transformations.switchMap(searchLiveData){
        query -> Repository.searchPlaces(query)
    }
    fun searchPlaces(query: String){
        searchLiveData.value = query
    }
    fun savePlace(place: Place) = Repository.savePlace(place)
    fun getSavedPlace() = Repository.getSavedPlace()
    fun isPlaceSaved() = Repository.isPlaceSaved()
}