package cz.ikem.dci.zscanner.persistence

import androidx.room.TypeConverter
import com.google.gson.Gson
import cz.ikem.dci.zscanner.screen_message.CreateMessageMode
import cz.ikem.dci.zscanner.screen_message.ModeDispatcher

class Converters {

    @TypeConverter
    fun listOfStringToJson(lst: List<String>): String {
        return Gson().toJson(lst)
    }

    @TypeConverter
    fun jsonToListOfString(str: String): List<String> {
        return Gson().fromJson(str, Array<String>::class.java).toList()
    }

    @TypeConverter
    fun modeToString(mode: CreateMessageMode): String {
        return ModeDispatcher(mode).modeId
    }

    @TypeConverter
    fun stringToMode(modeId: String): CreateMessageMode {
        return ModeDispatcher(modeId).mode
    }

}