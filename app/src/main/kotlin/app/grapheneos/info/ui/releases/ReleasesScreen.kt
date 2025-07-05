package app.grapheneos.info.ui.releases

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults.LoadingIndicator
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import app.grapheneos.info.R
import app.grapheneos.info.ui.reusablecomposables.ScreenLazyColumn
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ReleasesScreen(
    modifier: Modifier = Modifier,
    entries: List<Pair<String, String>>,
    updateChangelog: (useCaches: Boolean, finishedUpdating: () -> Unit) -> Unit,
    changelogLazyListState: LazyListState,
    additionalContentPadding: PaddingValues = PaddingValues(0.dp)
) {
    val lifecycleOwner = LocalLifecycleOwner.current

    val localUriHandler = LocalUriHandler.current

    val refreshCoroutineScope = rememberCoroutineScope()

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_START) {
                refreshCoroutineScope.launch {
                    updateChangelog(true) {}
                }
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    var isRefreshing by rememberSaveable { mutableStateOf(false) }

    val state = rememberPullToRefreshState()

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = {
            isRefreshing = true
            updateChangelog(false) {
                isRefreshing = false

                refreshCoroutineScope.launch {
                    state.animateToHidden()
                }
            }
        },
        state = state,
        modifier = modifier
            .fillMaxSize(),
        indicator = {
            LoadingIndicator(
                state = state,
                isRefreshing = isRefreshing,
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }
    ) {
        ScreenLazyColumn(
            modifier = Modifier
                .fillMaxSize(),
            state = changelogLazyListState,
            additionalContentPadding = additionalContentPadding,
            verticalArrangement = Arrangement.Top
        ) {
            items(
                items = entries,
                key = { it.first }) {
                Changelog(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    it.second
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    Button(onClick = { localUriHandler.openUri("https://grapheneos.org/releases") }) {
                        Text(text = stringResource(R.string.releases_see_all_button))
                    }
                }
            }
        }
    }
}