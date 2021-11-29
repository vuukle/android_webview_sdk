package com.vuukle.webview.manager.storage

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import litparty.app.manager.storage.StorageManager


class StorageImpl(activity: AppCompatActivity) : StorageManager {

    private val sharedPreferences by lazy {
        // Create the EncryptedSharedPreferences
        activity.getSharedPreferences("secret_shared_prefs", Context.MODE_PRIVATE)
    }

    override fun putData(key: String, value: String) {

        val sharedPrefsEditor = sharedPreferences.edit()
        sharedPrefsEditor?.putString(key, value)
        sharedPrefsEditor?.apply()
    }

    override fun putData(key: String, value: Int) {

        val sharedPrefsEditor = sharedPreferences.edit()
        sharedPrefsEditor?.putInt(key, value)
        sharedPrefsEditor?.apply()
    }

    override fun putData(key: String, value: Boolean) {

        val sharedPrefsEditor = sharedPreferences.edit()
        sharedPrefsEditor?.putBoolean(key, value)
        sharedPrefsEditor?.apply()
    }

    override fun putData(key: String, value: Float) {

        val sharedPrefsEditor = sharedPreferences.edit()
        sharedPrefsEditor?.putFloat(key, value)
        sharedPrefsEditor?.apply()
    }

    override fun getBooleanData(key: String): Boolean = sharedPreferences.getBoolean(key, false)

    override fun getFloatData(key: String): Float = sharedPreferences.getFloat(key, 0f)

    override fun getIntData(key: String): Int = sharedPreferences.getInt(key, 0)

    override fun getStringData(key: String): String? = sharedPreferences.getString(key, "")
}