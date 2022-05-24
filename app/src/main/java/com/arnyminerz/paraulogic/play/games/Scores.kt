package com.arnyminerz.paraulogic.play.games

import android.app.Activity
import androidx.annotation.WorkerThread
import com.arnyminerz.paraulogic.R
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.games.AnnotatedData
import com.google.android.gms.games.LeaderboardsClient
import com.google.android.gms.games.PlayGames
import com.google.android.gms.games.leaderboard.LeaderboardVariant
import timber.log.Timber
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

private var missingAddedPoints = arrayListOf<Long>()

/**
 * Tries to add all the points that have not been able to be added.
 * @author Arnau Mora
 * @since 20220309
 */
@WorkerThread
suspend fun tryToAddPoints(activity: Activity) {
    val points = missingAddedPoints.sum()
    if (points > 0)
        addPlayerPoints(
            activity,
            points,
            { },
            { error ->
                Timber.e(error, "Could still not add points.")
            },
            { missingAddedPoints.clear() },
        )
    else
        Timber.d("No points to get added.")
}

@WorkerThread
private suspend fun LeaderboardsClient.getLeaderboardAnnotatedData(
    leaderboardId: String,
    maxResults: Int = 1
) = suspendCoroutine<AnnotatedData<LeaderboardsClient.LeaderboardScores>> { cont ->
    loadPlayerCenteredScores(
        leaderboardId,
        LeaderboardVariant.TIME_SPAN_ALL_TIME,
        LeaderboardVariant.COLLECTION_PUBLIC,
        maxResults,
    )
        .addOnSuccessListener { cont.resume(it) }
        .addOnFailureListener { cont.resumeWithException(it) }
}

/**
 * Adds [points] to the player's leaderboard.
 * @author Arnau Mora
 * @since 20220309
 */
@WorkerThread
private suspend fun addPlayerPoints(
    activity: Activity,
    points: Long,
    loginRequired: () -> Unit,
    errorListener: (e: ApiException) -> Unit,
    onAdded: () -> Unit
) {
    Timber.d("Getting leaderboard id...")
    val leaderboardId = activity.getString(R.string.leaderboard_world_ranking)

    val leaderboardsClient = PlayGames.getLeaderboardsClient(activity)

    try {
        Timber.v("Getting current player leaderboard...")
        val lsad = leaderboardsClient.getLeaderboardAnnotatedData(leaderboardId, 1)
        val leaderboardScore = lsad.get() ?: run {
            Timber.e("Could not fetch user score. Leaderboard score is null.")
            return
        }

        try {
            val scores = leaderboardScore.scores
            Timber.d("Scores (${scores.count}): $scores")

            val score = if (scores.count <= 0)
                0
            else
                scores
                    .get(0)
                    .rawScore
            Timber.d("Current player score: $score")

            val newScore = score + points
            Timber.v("Submitting new score: $newScore")
            leaderboardsClient.submitScore(leaderboardId, newScore)
            onAdded()
        } catch (e: IllegalStateException) {
            Timber.e(e, "Could not get score.")
        } finally {
            leaderboardScore.release()
        }
    } catch (error: Exception) {
        Timber.e(error, "Could not load leaderboard.")
        (error as? ApiException)?.let { e ->
            if (e.statusCode == 26502) { // CLIENT_RECONNECT_REQUIRED
                missingAddedPoints.add(points)
                loginRequired()
            }
            errorListener(e)
        }
    }
}

/**
 * Adds [points] to the player's leaderboard.
 * @author Arnau Mora
 * @since 20220309
 */
suspend fun addPlayerPoints(activity: Activity, points: Long, loginRequired: () -> Unit) =
    addPlayerPoints(activity, points, loginRequired, { }, { })
