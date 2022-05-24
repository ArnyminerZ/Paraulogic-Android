package com.arnyminerz.paraulogic.game

import com.arnyminerz.paraulogic.play.games.loadSnapshot
import com.arnyminerz.paraulogic.storage.entity.IntroducedWord
import com.arnyminerz.paraulogic.utils.mapJsonObject
import com.arnyminerz.paraulogic.utils.toJsonArray
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.games.SnapshotsClient
import com.google.android.gms.tasks.RuntimeExecutionException
import org.json.JSONException
import timber.log.Timber

/**
 * Obtains the introduced words from the server.
 * @author Arnau Mora
 * @since 20220404
 * @param context The context running from.
 * @param gameInfo The [GameInfo] with the info for today.
 * @param loadingGameProgressCallback Gets called when the progress starts being loaded.
 */
@Suppress("BlockingMethodInNonBlockingContext")
suspend fun getServerIntroducedWordsList(
    gameInfo: GameInfo,
    snapshotsClient: SnapshotsClient,
    loadingGameProgressCallback: (finished: Boolean) -> Unit,
): List<IntroducedWord> = try {
    loadingGameProgressCallback(false)
    loadSnapshot(snapshotsClient)
        ?.snapshotContents
        ?.readFully()
        ?.let { String(it) }
        ?.toJsonArray()
        ?.mapJsonObject { IntroducedWord(it) }
        ?.filter { it.hash == gameInfo.hash }
        ?.toList()
} catch (e: JSONException) {
    Timber.e(e, "Could not parse JSON")
    null
} catch (e: RuntimeExecutionException) {
    Timber.e(e, "There's no stored snapshot.")
    null
} catch (e: ApiException) {
    Timber.e(e, "Could not read snapshot.")
    null
} finally {
    loadingGameProgressCallback(true)
} ?: emptyList()
