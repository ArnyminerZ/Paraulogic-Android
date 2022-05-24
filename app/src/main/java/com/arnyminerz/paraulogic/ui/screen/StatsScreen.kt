package com.arnyminerz.paraulogic.ui.screen

import android.content.Intent
import android.text.format.DateFormat
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.arnyminerz.paraulogic.R
import com.arnyminerz.paraulogic.game.GameHistoryItem
import com.arnyminerz.paraulogic.game.maxDate
import com.arnyminerz.paraulogic.game.minDate
import com.arnyminerz.paraulogic.ui.elements.DatePicker
import com.arnyminerz.paraulogic.ui.viewmodel.MainViewModel
import com.arnyminerz.paraulogic.utils.activity
import com.google.android.gms.games.PlayGames
import timber.log.Timber
import java.util.Calendar

@Composable
@ExperimentalMaterialApi
@ExperimentalMaterial3Api
fun StatsScreen(
    viewModel: MainViewModel,
    popupLauncher: ActivityResultLauncher<Intent>,
    gameHistory: SnapshotStateList<GameHistoryItem>
) {
    val context = LocalContext.current

    val selectDateText = stringResource(R.string.action_select_date)
    var buttonText by remember { mutableStateOf(selectDateText) }
    var showPicker by remember { mutableStateOf(false) }
    var isToday by remember { mutableStateOf(false) }

    var historyItem by remember { mutableStateOf<GameHistoryItem?>(null) }
    var historyItemWords by remember { mutableStateOf<List<String>>(emptyList()) }
    var historyItemTutis by remember { mutableStateOf<List<String>>(emptyList()) }

    val dayFoundWords = viewModel.dayFoundWords
    val dayFoundTutis = viewModel.dayFoundTutis
    val dayWrongWords = viewModel.dayWrongWords

    if (showPicker)
        DatePicker(
            minDate = gameHistory.minDate(),
            maxDate = gameHistory.maxDate(),
            onDateSelected = { date ->
                val today = Calendar.getInstance()
                val selectedDate = Calendar.getInstance()
                selectedDate.time = date

                isToday = selectedDate.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                        selectedDate.get(Calendar.MONTH) == today.get(Calendar.MONTH) &&
                        selectedDate.get(Calendar.DAY_OF_MONTH) == today.get(Calendar.DAY_OF_MONTH)

                buttonText = if (isToday)
                    context.getString(R.string.stats_today)
                else
                    DateFormat.format("dd-MM-yyyy", date).toString()

                for (item in gameHistory) {
                    val itemDate = Calendar.getInstance()
                    itemDate.time = item.date
                    if (itemDate.get(Calendar.YEAR) == selectedDate.get(Calendar.YEAR) &&
                        itemDate.get(Calendar.MONTH) == selectedDate.get(
                            Calendar.MONTH
                        ) &&
                        itemDate.get(Calendar.DAY_OF_MONTH) == selectedDate.get(
                            Calendar.DAY_OF_MONTH
                        )
                    ) {
                        historyItem = item
                        historyItemWords = item.gameInfo.words.keys.toList()
                        historyItemTutis = item.gameInfo.tutis
                        break
                    }
                }

                if (historyItem != null)
                    viewModel.loadWordsForDay(historyItem!!.gameInfo, date)
                else
                    Timber.e("Could not find history item for $date")
            },
            onDismissRequest = {
                showPicker = false
            },
        )

    Row {
        Button(
            onClick = { showPicker = true },
            colors = ButtonDefaults.outlinedButtonColors(),
        ) {
            Text(text = buttonText)
        }
        Button(
            onClick = {
                PlayGames.getLeaderboardsClient(context.activity!!)
                    .getLeaderboardIntent(context.getString(R.string.leaderboard_world_ranking))
                    .addOnSuccessListener { popupLauncher.launch(it) }
            },
            colors = ButtonDefaults.textButtonColors()
        ) {
            Text(text = stringResource(R.string.action_leaderboard))
        }
    }
    if (historyItem != null && dayFoundWords.isNotEmpty())
        Row {
            Column(modifier = Modifier.fillMaxWidth()) {
                Card(
                    modifier = Modifier
                        .padding(start = 16.dp, end = 16.dp, bottom = 8.dp)
                        .fillMaxWidth()
                ) {
                    Text(
                        modifier = Modifier.padding(8.dp),
                        text =
                        if (isToday)
                            if (historyItemTutis.size == 1)
                                if (dayFoundTutis.size == historyItemTutis.size)
                                    stringResource(
                                        R.string.stats_today_tuti,
                                        dayFoundWords.size,
                                        historyItemWords.size
                                    )
                                else
                                    stringResource(
                                        R.string.stats_today_no_tuti,
                                        dayFoundWords.size,
                                        historyItemWords.size
                                    )
                            else if (dayFoundTutis.size == historyItemTutis.size)
                                stringResource(
                                    R.string.stats_today_all_tutis,
                                    dayFoundWords.size,
                                    historyItemWords.size,
                                    historyItemTutis.size
                                )
                            else
                                stringResource(
                                    R.string.stats_today_n_tutis,
                                    dayFoundWords.size,
                                    historyItemWords.size,
                                    dayFoundTutis.size,
                                    historyItemTutis.size
                                )
                        else
                            if (historyItemTutis.size == 1)
                                if (dayFoundTutis.size == historyItemTutis.size)
                                    stringResource(
                                        R.string.stats_day_tuti,
                                        buttonText,
                                        dayFoundWords.size,
                                        historyItemWords.size
                                    )
                                else
                                    stringResource(
                                        R.string.stats_day_no_tuti,
                                        buttonText,
                                        dayFoundWords.size,
                                        historyItemWords.size
                                    )
                            else if (dayFoundTutis.size == historyItemTutis.size)
                                stringResource(
                                    R.string.stats_day_all_tutis,
                                    buttonText,
                                    dayFoundWords.size,
                                    historyItemWords.size,
                                    historyItemTutis.size
                                )
                            else
                                stringResource(
                                    R.string.stats_day_n_tutis,
                                    buttonText,
                                    dayFoundWords.size,
                                    historyItemWords.size,
                                    dayFoundTutis.size,
                                    historyItemTutis.size
                                )
                    )
                }
                Card(
                    modifier = Modifier
                        .padding(start = 16.dp, end = 16.dp, bottom = 8.dp)
                        .fillMaxWidth()
                ) {
                    val maxWordsCount =
                        dayWrongWords.takeIf { it.isNotEmpty() }?.maxOf { it.value } ?: 0
                    Text(
                        modifier = Modifier.padding(8.dp),
                        text = stringResource(
                            if (isToday)
                                R.string.stats_today_invalid_words
                            else
                                R.string.stats_invalid_words,
                            dayWrongWords.size,
                            maxWordsCount,
                            if (isToday)
                                if (maxWordsCount < 5)
                                    stringResource(R.string.stats_today_invalid_words_comment_1)
                                else if (maxWordsCount < 10)
                                    stringResource(R.string.stats_today_invalid_words_comment_2)
                                else
                                    stringResource(R.string.stats_today_invalid_words_comment_3)
                            else
                                if (maxWordsCount < 5)
                                    stringResource(R.string.stats_invalid_words_comment_1)
                                else if (maxWordsCount < 10)
                                    stringResource(R.string.stats_invalid_words_comment_2)
                                else
                                    stringResource(R.string.stats_invalid_words_comment_3)
                        )
                    )
                }
                if (!isToday && historyItem != null)
                    Card(
                        modifier = Modifier
                            .padding(start = 16.dp, end = 16.dp, bottom = 8.dp)
                            .fillMaxWidth(),
                    ) {
                        val historyGameInfo = historyItem!!.gameInfo

                        LazyColumn(
                            modifier = Modifier
                                .padding(8.dp)
                                .fillMaxWidth()
                        ) {
                            item {
                                Text(text = stringResource(R.string.stats_words))
                            }
                            items(
                                historyGameInfo.words
                                    .toList()
                                    .sortedBy { it.first }
                            ) { (key, word) ->
                                Row {
                                    Column(
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            if (dayFoundWords.find { it.word.lowercase() == key.lowercase() } != null)
                                                Icons.Rounded.Check
                                            else
                                                Icons.Rounded.Close,
                                            "Was word found?" // TODO: Localize
                                        )
                                    }
                                    Column(
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(text = word)
                                    }
                                }
                            }
                        }
                    }
            }
        }
}
