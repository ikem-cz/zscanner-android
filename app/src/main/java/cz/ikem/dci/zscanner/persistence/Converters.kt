package cz.ikem.dci.zscanner.persistence

import androidx.room.TypeConverter
import com.google.gson.Gson

class Converters {

    @TypeConverter
    fun listOfStringToJson(lst: List<String>): String {
        return Gson().toJson(lst)
    }

    @TypeConverter
    fun jsonToListOfString(str: String): List<String> {
        return Gson().fromJson(str, Array<String>::class.java).toList()
    }

//    @TypeConverter
//    fun tyoeToString(mode: CreateMessageMode): String {
//        return ModeDispatcher(mode).modeId
//    }
//
//    @TypeConverter
//    fun stringToType(modeId: String): CreateMessageMode {
//        return ModeDispatcher(modeId).mode
//    }

}