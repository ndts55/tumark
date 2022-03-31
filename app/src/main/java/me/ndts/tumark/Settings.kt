package me.ndts.tumark

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

suspend fun Context.readTuId(): String = this.readStringByKey(this.tuIdKey())

suspend fun Context.readPassword(): String = this.readStringByKey(this.passwordKey())

suspend fun Context.readStringByKey(key: Preferences.Key<String>): String =
    this.dataStore.data.map { it[key] ?: "" }.first()

suspend fun Context.writeTuId(tuId: String) {
    this.dataStore.edit {
        it[this.tuIdKey()] = tuId
    }
}

suspend fun Context.writePassword(password: String) {
    this.dataStore.edit {
        it[this.passwordKey()] = password
    }
}

fun Context.tuIdKey(): Preferences.Key<String> =
    this.getString(R.string.tu_id_key).asPreferencesKey()

fun Context.passwordKey(): Preferences.Key<String> =
    this.getString(R.string.password_key).asPreferencesKey()

fun String.asPreferencesKey(): Preferences.Key<String> = stringPreferencesKey(this)
