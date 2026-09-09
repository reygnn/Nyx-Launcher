package com.github.reygnn.nyx_launcher.data.home

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.github.reygnn.nyx_launcher.home.model.GridSpec
import com.github.reygnn.nyx_launcher.home.model.HomeLayout
import com.github.reygnn.nyx_launcher.home.repository.HomeLayoutRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject

/**
 * DataStore-backed [HomeLayoutRepository]. The whole layout is one JSON blob
 * under [KEY] (rule 5 + rule 22). [layout] is a cold flow over `dataStore.data`.
 *
 * A missing key yields [DEFAULT]; a corrupt/undecodable blob also falls back to
 * [DEFAULT] rather than crashing the read path (DATASTORE_READ_SPEC posture —
 * a bad read must not take down the home screen). The reconcile pass and the
 * next successful [save] restore a healthy blob.
 */
class HomeLayoutRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) : HomeLayoutRepository {

    override fun layout(): Flow<HomeLayout> = dataStore.data.map { prefs ->
        val raw = prefs[KEY] ?: return@map DEFAULT
        runCatching { JSON.decodeFromString<HomeLayoutDto>(raw).toDomain() }.getOrDefault(DEFAULT)
    }

    override suspend fun save(layout: HomeLayout) {
        val raw = JSON.encodeToString(layout.toDto())
        dataStore.edit { it[KEY] = raw }
    }

    private companion object {
        val KEY = stringPreferencesKey("home_layout_v1")
        val JSON = Json { ignoreUnknownKeys = true }

        // TODO grid default is a product decision (ICON_HOME_MODEL_SPEC §10).
        val DEFAULT = HomeLayout(
            grid = GridSpec(columns = 4, rows = 6),
            pages = 1,
            items = emptyList(),
            dock = emptyList(),
        )
    }
}
