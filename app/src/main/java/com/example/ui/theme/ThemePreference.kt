package com.example.ui.theme

import android.content.Context

/**
 * Menyimpan pilihan arah desain milik user.
 *
 * Sengaja memakai SharedPreferences, bukan DataStore: DataStore belum menjadi dependency
 * project ini (`libs.androidx.datastore.preferences` masih dikomentari di
 * `app/build.gradle.kts`) dan menambah dependency hanya untuk satu nilai string tidak
 * sepadan. Bila DataStore diaktifkan nanti, hanya berkas ini yang perlu diubah.
 */
object ThemePreference {

    private const val PREFS_NAME = "pencatat_keuangan_prefs"
    private const val KEY_DIRECTION = "theme_direction"

    private fun prefs(context: Context) =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun read(context: Context): ThemeDirection =
        ThemeDirection.fromId(prefs(context).getString(KEY_DIRECTION, null))

    fun write(context: Context, direction: ThemeDirection) {
        prefs(context).edit().putString(KEY_DIRECTION, direction.id).apply()
    }
}
