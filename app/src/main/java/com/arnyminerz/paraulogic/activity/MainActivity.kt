package com.arnyminerz.paraulogic.activity

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModelProvider
import com.arnyminerz.paraulogic.R
import com.arnyminerz.paraulogic.activity.model.LanguageActivity
import com.arnyminerz.paraulogic.play.games.tryToAddPoints
import com.arnyminerz.paraulogic.pref.PreferencesModule
import com.arnyminerz.paraulogic.pref.dataStore
import com.arnyminerz.paraulogic.ui.dialog.BuyCoffeeDialog
import com.arnyminerz.paraulogic.ui.elements.MainScreen
import com.arnyminerz.paraulogic.ui.theme.AppTheme
import com.arnyminerz.paraulogic.ui.toast
import com.arnyminerz.paraulogic.ui.viewmodel.MainViewModel
import com.arnyminerz.paraulogic.utils.doAsync
import com.arnyminerz.paraulogic.utils.doOnUi
import com.arnyminerz.paraulogic.utils.uiContext
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.android.gms.games.GamesSignInClient
import com.google.android.gms.games.PlayGames
import com.google.android.gms.games.SnapshotsClient
import kotlinx.coroutines.flow.first
import timber.log.Timber

class MainActivity : LanguageActivity() {

    private lateinit var gamesSignInClient: GamesSignInClient

    private lateinit var snapshotsClient: SnapshotsClient

    private val popupLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { }

    @OptIn(
        ExperimentalPagerApi::class,
        ExperimentalMaterialApi::class,
        ExperimentalMaterial3Api::class,
    )
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Timber.d("Initializing games sign in client...")
        gamesSignInClient = PlayGames.getGamesSignInClient(this)

        snapshotsClient = PlayGames.getSnapshotsClient(this)

        Timber.d("Initializing main view model...")
        val viewModel: MainViewModel = ViewModelProvider(
            this,
            MainViewModel.Factory(application)
        )[MainViewModel::class.java]

        setContent {
            val snackbarHostState = remember { SnackbarHostState() }

            AppTheme {
                MainScreen(snackbarHostState, gamesSignInClient, viewModel, popupLauncher)

                var showingDialog by remember { mutableStateOf(false) }
                BuyCoffeeDialog(showingDialog) { showingDialog = false }

                doAsync {
                    if (dataStore.data.first()[PreferencesModule.ShownDonateDialog] != true)
                        uiContext { showingDialog = true }
                }
            }

            viewModel.loadGameInfo(gamesSignInClient, snapshotsClient) {
                doOnUi {
                    if (it)
                        snackbarHostState.showSnackbar(
                            message = getString(R.string.status_loaded_server),
                        )
                    else
                        toast(R.string.status_loading_server)
                }
            }
            viewModel.loadGameHistory()
        }
    }

    override fun onResume() {
        super.onResume()

        doAsync {
            Timber.i("Trying to add missing points...")
            tryToAddPoints(this@MainActivity)
        }
    }

    override fun onStop() {
        super.onStop()

        doAsync {
            Timber.i("Trying to add missing points...")
            tryToAddPoints(this@MainActivity)
        }
    }
}
