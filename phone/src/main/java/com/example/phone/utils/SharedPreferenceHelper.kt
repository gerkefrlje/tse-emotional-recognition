package com.example.phone.utils

import android.content.Context

object SharedPreferenceHelper {
    private const val PREF_NAME = "my_shared_prefs"

    fun saveCount(context: Context, count: Int) {
        val sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        sharedPreferences.edit().putInt("count", count).apply()
    }

    fun getCount(context: Context): Int {
        val sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return sharedPreferences.getInt("count", 0)
    }

    fun saveHRListString(context: Context, hrListString: String) {
        val sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        sharedPreferences.edit().putString("hrListString", hrListString).apply()
    }

    fun getHRListString(context: Context) : String {
        val sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return sharedPreferences.getString("hrListString", "") ?: ""
    }

    fun saveSkinTemperatureString(context: Context, skinTemperature: String) {
        val sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        sharedPreferences.edit().putString("skinListString", skinTemperature).apply()
    }

    fun getSkinTemperatureString(context: Context): String {
        val sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return sharedPreferences.getString("skinListString", "") ?: ""

    }
}