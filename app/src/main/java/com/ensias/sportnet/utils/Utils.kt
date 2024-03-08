package com.ensias.sportnet.utils

import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object Utils {

    val arrayListOfStringsToken: Type = object : TypeToken<ArrayList<String>>(){}.type
    fun formatSocialMediaNumber(number: Int): String {
        if (number < 1000) {
            return number.toString()
        }

        val suffixes = arrayOf("", "k", "M", "B", "T")
        var value = number.toDouble()
        var suffixIndex = 0

        while (value >= 1000 && suffixIndex < suffixes.size - 1) {
            value /= 1000
            suffixIndex++
        }

        return "%.2f%s".format(value, suffixes[suffixIndex])
    }


    fun formatDate(timestamp: Long): String {
        val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        return dateFormat.format(Date(timestamp))
    }



}
