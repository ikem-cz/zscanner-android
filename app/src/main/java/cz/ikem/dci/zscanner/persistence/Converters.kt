package cz.ikem.dci.zscanner.persistence

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import cz.ikem.dci.zscanner.screen_message.PageActionsQueue
import java.lang.reflect.Type
import java.util.*


class Converters {

    @TypeConverter
    fun listOfStringToJson(lst: List<String>): String {
        return Gson().toJson(lst)
    }

    @TypeConverter
    fun jsonToListOfString(str: String): List<String> {
        return Gson().fromJson(str, Array<String>::class.java).toList()
    }

    private var gson = Gson()

    @TypeConverter
    fun stringToPage(data: String?): List<PageActionsQueue.Page?>? {
        if (data == null) {
            return Collections.emptyList()
        }
        val listType: Type = object : TypeToken<List<PageActionsQueue.Page?>?>() {}.type
        return gson.fromJson<List<PageActionsQueue.Page?>>(data, listType)
    }

    @TypeConverter
    fun pageToString(someObjects: List<PageActionsQueue.Page?>?): String? {
        return gson.toJson(someObjects)
    }

}