package com.arnyminerz.paraulogic.play.games

import android.app.Activity
import androidx.annotation.WorkerThread
import com.arnyminerz.paraulogic.R
import com.arnyminerz.paraulogic.game.GameHistoryItem
import com.arnyminerz.paraulogic.game.GameInfo
import com.arnyminerz.paraulogic.game.getPoints
import com.arnyminerz.paraulogic.singleton.DatabaseSingleton
import com.arnyminerz.paraulogic.storage.entity.SynchronizedWord
import com.arnyminerz.paraulogic.utils.getMaxDifferenceBetweenDates
import com.google.android.gms.games.PlayGames
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.first
import timber.log.Timber
import java.util.Calendar
import java.util.Date

/**
 * Synchronizes all data from device to Google Play Games
 * @author Arnau Mora
 * @since 20220309
 */
@WorkerThread
suspend fun startSynchronization(
    activity: Activity,
    gameInfo: GameInfo,
    history: List<GameHistoryItem>
) {
    val databaseSingleton = DatabaseSingleton.getInstance(activity)
    val dao = databaseSingleton.db.wordsDao()

    val dbCorrectWords = dao.getAll()
    val dbSynchronizedWords = dao.getAllSynchronized()

    dbCorrectWords.collect { introducedWords ->
        val achievementsClient = PlayGames.getAchievementsClient(activity)

        var toIncrement = 0
        var foundTutis = 0

        val daysPlayedHashes = hashMapOf<String, Date>()
        val hashTutisCount = hashMapOf<String, Int>()
        val hashTutisFound = hashMapOf<String, Int>()

        Timber.i("Iterating introduced words...")
        introducedWords.forEach { introducedWord ->
            if (dbSynchronizedWords.first().map { it.wordId }.contains(introducedWord.uid))
                return@forEach

            Timber.d("Introduced word ${introducedWord.uid} is not synchronized.")

            // Increment one point of score if word was correct.
            if (introducedWord.isCorrect)
                toIncrement += introducedWord.word.getPoints(gameInfo)

            // Increment found tutis by 1 if word is correct and is a tuti
            if (introducedWord.isCorrect && gameInfo.isTuti(introducedWord.word)) {
                foundTutis += 1

                if (hashTutisFound.contains(introducedWord.hash))
                    hashTutisFound[introducedWord.hash] =
                        hashTutisFound.getValue(introducedWord.hash) + 1
                else
                    hashTutisFound[introducedWord.hash] = 1
            }

            // Store at daysPlayedHashes the date for each hash
            if (!daysPlayedHashes.contains(introducedWord.hash))
                daysPlayedHashes[introducedWord.hash] = introducedWord.timestamp.let { timestamp ->
                    val wordDate = Calendar.getInstance().apply { time = Date(timestamp) }
                    Calendar.getInstance()
                        .apply {
                            set(Calendar.YEAR, wordDate.get(Calendar.YEAR))
                            set(Calendar.MONTH, wordDate.get(Calendar.MONTH))
                            set(Calendar.DAY_OF_MONTH, wordDate.get(Calendar.DAY_OF_MONTH))
                        }
                        .time
                }

            // If hashTutisCount doesn't contain the count for the current hash, introduce
            if (!hashTutisCount.contains(introducedWord.hash)) {
                val wordCalendar = Calendar
                    .getInstance()
                    .apply { time = Date(introducedWord.timestamp) }
                for (item in history) {
                    val itemCalendar = Calendar.getInstance().apply { time = item.date }
                    val isCorrectDay =
                        itemCalendar.get(Calendar.YEAR) == wordCalendar.get(Calendar.YEAR) &&
                                itemCalendar.get(Calendar.MONTH) == wordCalendar.get(Calendar.MONTH) &&
                                itemCalendar.get(Calendar.DAY_OF_MONTH) == wordCalendar.get(Calendar.DAY_OF_MONTH)
                    if (!isCorrectDay) continue

                    hashTutisCount[introducedWord.hash] = item.gameInfo.tutisCount
                }
            }

            dao.synchronize(
                SynchronizedWord(0, introducedWord.uid)
            )
        }
        Timber.i("Has to increment $toIncrement points.")

        Firebase.analytics
            .logEvent(FirebaseAnalytics.Event.POST_SCORE) {
                param(FirebaseAnalytics.Param.SCORE, toIncrement.toLong())
            }

        val allTutisDays = arrayListOf<Date>()

        // Analyze daysPlayedHashes, hashTutisFound and hashTutisCount to find out playedForMaxDays
        // and allTutisForMaxDays
        for ((hash, tutisCount) in hashTutisCount)
            try {
                val hashDate = daysPlayedHashes.getValue(hash)
                val tutisFound = hashTutisFound[hash]

                if (tutisFound == tutisCount)
                    allTutisDays.add(hashDate)
            } catch (e: NoSuchElementException) {
                continue
            }

        // Analyze allTutisDays to find out how many days straight
        val allTutisForMaxDays = getMaxDifferenceBetweenDates(allTutisDays)
        val playedForMaxDays = getMaxDifferenceBetweenDates(daysPlayedHashes.values.toList())

        Timber.i("You have played at most for $playedForMaxDays consecutive days.")
        Timber.i("You have found all tutis for at most $allTutisDays consecutive days.")

        // Find your first word
        if (toIncrement > 0)
            achievementsClient.unlock(activity.getString(R.string.achievement_a_la_punta_de_la_llengua))

        if (foundTutis > 0)
            achievementsClient.unlock(activity.getString(R.string.achievement_quin_embarbussament))

        // Find 50 words
        achievementsClient.increment(activity.getString(R.string.achievement_a_ja_va), toIncrement)
        // Find 69 words
        achievementsClient.increment(
            activity.getString(R.string.achievement_tinc_la_figa_calenta),
            toIncrement
        )
        // Find 100 words
        achievementsClient.increment(
            activity.getString(R.string.achievement_a_fer_punyetes),
            toIncrement
        )
        // Find 200 words
        achievementsClient.increment(
            activity.getString(R.string.achievement_com_cagall_per_squia),
            toIncrement
        )
        // Find 300 words
        achievementsClient.increment(
            activity.getString(R.string.achievement_sem_cau_la_cara_de_vergonya),
            toIncrement
        )
        // Find 500 words
        achievementsClient.increment(
            activity.getString(R.string.achievement_mest_fent_la_guitza),
            toIncrement
        )

        // Play for 7 straight days
        if (playedForMaxDays >= 7)
            achievementsClient.unlock(activity.getString(R.string.achievement_fent_i_desfent_sensenya_la_gent))
        // Find all tutis for 7 straight days
        if (allTutisForMaxDays >= 7)
            achievementsClient.unlock(activity.getString(R.string.achievement_quin_embarbussament))
    }
}
