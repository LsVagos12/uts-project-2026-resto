package com.example.restoprofile //

import android.content.Context
import android.content.SharedPreferences

class PreferencesManager(context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("RestoPrefs", Context.MODE_PRIVATE)

    fun saveProfile(name: String, address: String, desc: String, hours: String) {
        sharedPreferences.edit().apply {
            putString("resto_name", name)
            putString("resto_address", address)
            putString("resto_desc", desc)
            putString("resto_hours", hours)
            apply()
        }
    }

    fun getProfile(): Map<String, String> {
        return mapOf(
            "name" to (sharedPreferences.getString("resto_name", "Resto Vibe Rasa") ?: "Resto Vibe Rasa"),
            "address" to (sharedPreferences.getString("resto_address", "Jl. Coding No. 10, Jakarta") ?: "Jl. Coding No. 10, Jakarta"),
            "desc" to (sharedPreferences.getString("resto_desc", "Tempat nongkrong asyik para programmer masa kini.") ?: "Tempat nongkrong asyik para programmer masa kini."),
            "hours" to (sharedPreferences.getString("resto_hours", "10:00 - 22:00") ?: "10:00 - 22:00")
        )
    }

    fun saveTheme(isDark: Boolean) {
        sharedPreferences.edit().putBoolean("is_dark_theme", isDark).apply()
    }

    fun isDarkTheme(): Boolean {
        return sharedPreferences.getBoolean("is_dark_theme", false)
    }
}