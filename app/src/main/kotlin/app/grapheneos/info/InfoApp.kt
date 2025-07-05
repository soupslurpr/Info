package app.grapheneos.info

import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideIn
import androidx.compose.animation.slideOut
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material.icons.outlined.Forum
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.VolunteerActivism
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteItem
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffoldDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDeepLink
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import app.grapheneos.info.preferences.PreferencesViewModel
import app.grapheneos.info.ui.community.CommunityScreen
import app.grapheneos.info.ui.donate.DonateStartScreen
import app.grapheneos.info.ui.donate.GithubSponsorsScreen
import app.grapheneos.info.ui.donate.PaypalScreen
import app.grapheneos.info.ui.donate.banktransfers.BankTransfersScreen
import app.grapheneos.info.ui.donate.cryptocurrency.BitcoinScreen
import app.grapheneos.info.ui.donate.cryptocurrency.CardanoScreen
import app.grapheneos.info.ui.donate.cryptocurrency.DonateCryptoCurrencyStartScreen
import app.grapheneos.info.ui.donate.cryptocurrency.EthereumScreen
import app.grapheneos.info.ui.donate.cryptocurrency.LitecoinScreen
import app.grapheneos.info.ui.donate.cryptocurrency.MoneroScreen
import app.grapheneos.info.ui.donate.cryptocurrency.ZcashScreen
import app.grapheneos.info.ui.releases.ReleasesScreen
import app.grapheneos.info.ui.releases.ReleasesViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class InfoAppScreens(@StringRes val title: Int) {
    Releases(title = R.string.releases),
    Community(title = R.string.community),
    Donate(title = R.string.donate),
    DonateStart(title = R.string.donate),
    DonateGithubSponsors(title = R.string.github_sponsors),
    DonateCryptocurrencies(title = R.string.cryptocurrencies),
    DonateCryptocurrenciesStart(title = R.string.cryptocurrencies),
    DonateCryptocurrenciesBitcoin(title = R.string.bitcoin),
    DonateCryptocurrenciesMonero(title = R.string.monero),
    DonateCryptocurrenciesZcash(title = R.string.zcash),
    DonateCryptocurrenciesEthereum(title = R.string.ethereum),
    DonateCryptocurrenciesCardano(title = R.string.cardano),
    DonateCryptocurrenciesLitecoin(title = R.string.litecoin),
    DonatePaypal(title = R.string.paypal),
    DonateBankTransfers(title = R.string.bank_transfers)
}

val navSuiteItemScreens = arrayOf(
    InfoAppScreens.Releases,
    InfoAppScreens.Community,
    InfoAppScreens.Donate
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InfoApp() {
    val navController = rememberNavController()

    val backStackEntry by navController.currentBackStackEntryAsState()

    val preferencesViewModel: PreferencesViewModel = viewModel()

    val preferencesUiState by preferencesViewModel.uiState.collectAsState()

    /**
     * Gets the initial value from preferencesUiState.initialStartDestination.second
     * but doesn't change when it changes so the user doesn't get a weird animation
     * every time they navigate through the bottom navigation bar.
     */
    val startDestination by rememberSaveable { preferencesUiState.startDestination.second }

    val currentScreen =
        InfoAppScreens.valueOf(backStackEntry?.destination?.route ?: startDestination)

    val navSuiteItemScreenSelected = navSuiteItemScreens.find {
        currentScreen.name.startsWith(it.name)
    }

    val releasesViewModel: ReleasesViewModel = viewModel()

    val releasesUiState = releasesViewModel.uiState.collectAsState()

    val changelogLazyListState = rememberLazyListState()

    val changelogLazyListStateScope = rememberCoroutineScope()

    val snackbarHostState = remember { SnackbarHostState() }

    val snackbarCoroutine = rememberCoroutineScope()

    val topAppBarScrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())

    val localUriHandler = LocalUriHandler.current

    val openUriIllegalArguementExceptionSnackbarError =
        stringResource(R.string.browser_link_illegal_argument_exception_snackbar_error)

    val navigationSuiteType = NavigationSuiteScaffoldDefaults.calculateFromAdaptiveInfo(
        currentWindowAdaptiveInfo()
    )

    val layoutDirection = LocalLayoutDirection.current

    NavigationSuiteScaffold(
        navigationItems = {
            navSuiteItemScreens.forEach { navSuiteItemScreen ->
                val selected = navSuiteItemScreen == navSuiteItemScreenSelected
                NavigationSuiteItem(
                    selected = selected,
                    onClick = {
                        if (navSuiteItemScreenSelected != null) {
                            navController.popBackStack(
                                navSuiteItemScreenSelected.name,
                                inclusive = true,
                                saveState = true
                            )
                        }
                        navController.navigate(
                            route = navSuiteItemScreen.name
                        ) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                        navSuiteItemScreen.let {
                            preferencesViewModel.setPreference(
                                preferencesUiState.startDestination.first,
                                it.name
                            )
                        }
                    },
                    icon = {
                        when (navSuiteItemScreen) {
                            InfoAppScreens.Releases -> Icon(
                                painter = painterResource(id = R.drawable.outline_newsmode),
                                contentDescription = null
                            )

                            InfoAppScreens.Community -> Icon(
                                imageVector = if (selected) {
                                    Icons.Filled.Forum
                                } else {
                                    Icons.Outlined.Forum
                                },
                                contentDescription = null
                            )

                            InfoAppScreens.Donate -> Icon(
                                imageVector = if (selected) {
                                    Icons.Filled.VolunteerActivism
                                } else {
                                    Icons.Outlined.VolunteerActivism
                                },
                                contentDescription = null
                            )

                            else -> {}
                        }
                    },
                    label = { Text(text = stringResource(id = navSuiteItemScreen.title)) }
                )
            }
        },
    ) {
        Scaffold(
            modifier = Modifier
                .nestedScroll(topAppBarScrollBehavior.nestedScrollConnection)
                .animateContentSize(),
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = stringResource(id = currentScreen.title),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    },
                    navigationIcon = {
                        if (navController.previousBackStackEntry != null) {
                            IconButton(onClick = { navController.navigateUp() }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = stringResource(R.string.navigate_up_button_description)
                                )
                            }
                        }
                    },
                    actions = {
                        if (navSuiteItemScreenSelected == InfoAppScreens.Releases) {
                            IconButton(
                                onClick = {
                                    try {
                                        localUriHandler.openUri("https://grapheneos.org/releases#about-the-releases")
                                    } catch (e: IllegalArgumentException) {
                                        snackbarCoroutine.launch {
                                            snackbarHostState.showSnackbar(
                                                openUriIllegalArguementExceptionSnackbarError
                                            )
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Info,
                                    contentDescription = stringResource(R.string.releases_top_bar_info_button_content_description)
                                )
                            }
                        }
                    },
                    scrollBehavior = topAppBarScrollBehavior
                )
            },
            snackbarHost = {
                SnackbarHost(snackbarHostState)
            },
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = startDestination,
            ) {
                composableWithDefaultSlideTransitions(
                    route = InfoAppScreens.Releases,
                    navigationSuiteType = navigationSuiteType,
                ) {
                    ReleasesScreen(
                        modifier = Modifier
                            .padding(top = innerPadding.calculateTopPadding())
                            .consumeWindowInsets(innerPadding),
                        entries =
                            releasesUiState.value.entries.toSortedMap().toList().asReversed(),
                        updateChangelog = { useCaches, onFinishedUpdating ->
                            releasesViewModel.updateChangelog(
                                useCaches = useCaches,
                                showSnackbarError = {
                                    snackbarHostState.showSnackbar(it)
                                },
                                {
                                    changelogLazyListStateScope.launch {
                                        withContext(Dispatchers.Main) {
                                            changelogLazyListState.animateScrollToItem(it)
                                        }
                                    }
                                },
                                onFinishedUpdating = onFinishedUpdating,
                            )
                        },
                        changelogLazyListState = changelogLazyListState,
                        additionalContentPadding = PaddingValues(
                            start = innerPadding.calculateStartPadding(layoutDirection),
                            top = 0.dp,
                            end = innerPadding.calculateEndPadding(layoutDirection),
                            bottom = innerPadding.calculateBottomPadding()
                        )
                    )
                }
                composableWithDefaultSlideTransitions(
                    route = InfoAppScreens.Community,
                    navigationSuiteType = navigationSuiteType,
                ) {
                    CommunityScreen(
                        modifier = Modifier.consumeWindowInsets(innerPadding),
                        showSnackbarError = {
                            snackbarCoroutine.launch {
                                snackbarHostState.showSnackbar(it)
                            }
                        },
                        additionalContentPadding = innerPadding
                    )
                }
                navigationWithDefaultSlideTransitions(
                    route = InfoAppScreens.Donate,
                    startDestination = InfoAppScreens.DonateStart.name,
                    navigationSuiteType = navigationSuiteType,
                ) {
                    composableWithDefaultSlideTransitions(
                        route = InfoAppScreens.DonateStart,
                        navigationSuiteType = navigationSuiteType,
                    ) {
                        DonateStartScreen(
                            modifier = Modifier.consumeWindowInsets(innerPadding),
                            onNavigateToGithubSponsorsScreen = {
                                navController.navigate(InfoAppScreens.DonateGithubSponsors.name)
                            },
                            onNavigateToCryptocurrenciesScreen = {
                                navController.navigate(InfoAppScreens.DonateCryptocurrencies.name)
                            },
                            onNavigateToPayPalScreen = {
                                navController.navigate(InfoAppScreens.DonatePaypal.name)
                            },
                            onNavigateToBankTransfersScreen = {
                                navController.navigate(InfoAppScreens.DonateBankTransfers.name)
                            },
                            additionalContentPadding = innerPadding
                        )
                    }
                    composableWithDefaultSlideTransitions(
                        route = InfoAppScreens.DonateGithubSponsors,
                        navigationSuiteType = navigationSuiteType,
                    ) {
                        GithubSponsorsScreen(
                            modifier = Modifier.consumeWindowInsets(innerPadding),
                            showSnackbarError = {
                                snackbarCoroutine.launch {
                                    snackbarHostState.showSnackbar(it)
                                }
                            },
                            additionalContentPadding = innerPadding
                        )
                    }
                    navigationWithDefaultSlideTransitions(
                        route = InfoAppScreens.DonateCryptocurrencies,
                        startDestination = InfoAppScreens.DonateCryptocurrenciesStart.name,
                        navigationSuiteType = navigationSuiteType,
                    ) {
                        composableWithDefaultSlideTransitions(
                            route = InfoAppScreens.DonateCryptocurrenciesStart,
                            navigationSuiteType = navigationSuiteType,
                        ) {
                            DonateCryptoCurrencyStartScreen(
                                modifier = Modifier.consumeWindowInsets(innerPadding),
                                onNavigateToBitcoinScreen = {
                                    navController.navigate(InfoAppScreens.DonateCryptocurrenciesBitcoin.name)
                                },
                                onNavigateToMoneroScreen = {
                                    navController.navigate(InfoAppScreens.DonateCryptocurrenciesMonero.name)
                                },
                                onNavigateToZcashScreen = {
                                    navController.navigate(InfoAppScreens.DonateCryptocurrenciesZcash.name)
                                },
                                onNavigateToEthereumScreen = {
                                    navController.navigate(InfoAppScreens.DonateCryptocurrenciesEthereum.name)
                                },
                                onNavigateToCardanoScreen = {
                                    navController.navigate(InfoAppScreens.DonateCryptocurrenciesCardano.name)
                                },
                                onNavigateToLitecoinScreen = {
                                    navController.navigate(InfoAppScreens.DonateCryptocurrenciesLitecoin.name)
                                },
                                additionalContentPadding = innerPadding
                            )
                        }
                        composableWithDefaultSlideTransitions(
                            route = InfoAppScreens.DonateCryptocurrenciesBitcoin,
                            navigationSuiteType = navigationSuiteType,
                        ) {
                            BitcoinScreen(
                                modifier = Modifier.consumeWindowInsets(innerPadding),
                                showSnackbarError = {
                                    snackbarCoroutine.launch {
                                        snackbarHostState.showSnackbar(it)
                                    }
                                },
                                additionalContentPadding = innerPadding
                            )
                        }
                        composableWithDefaultSlideTransitions(
                            route = InfoAppScreens.DonateCryptocurrenciesMonero,
                            navigationSuiteType = navigationSuiteType,
                        ) {
                            MoneroScreen(
                                modifier = Modifier.consumeWindowInsets(innerPadding),
                                showSnackbarError = {
                                    snackbarCoroutine.launch {
                                        snackbarHostState.showSnackbar(it)
                                    }
                                },
                                additionalContentPadding = innerPadding
                            )
                        }
                        composableWithDefaultSlideTransitions(
                            route = InfoAppScreens.DonateCryptocurrenciesZcash,
                            navigationSuiteType = navigationSuiteType,
                        ) {
                            ZcashScreen(
                                modifier = Modifier.consumeWindowInsets(innerPadding),
                                showSnackbarError = {
                                    snackbarCoroutine.launch {
                                        snackbarHostState.showSnackbar(it)
                                    }
                                },
                                additionalContentPadding = innerPadding
                            )
                        }
                        composableWithDefaultSlideTransitions(
                            route = InfoAppScreens.DonateCryptocurrenciesEthereum,
                            navigationSuiteType = navigationSuiteType,
                        ) {
                            EthereumScreen(
                                modifier = Modifier.consumeWindowInsets(innerPadding),
                                showSnackbarError = {
                                    snackbarCoroutine.launch {
                                        snackbarHostState.showSnackbar(it)
                                    }
                                },
                                additionalContentPadding = innerPadding
                            )
                        }
                        composableWithDefaultSlideTransitions(
                            route = InfoAppScreens.DonateCryptocurrenciesCardano,
                            navigationSuiteType = navigationSuiteType,
                        ) {
                            CardanoScreen(
                                modifier = Modifier.consumeWindowInsets(innerPadding),
                                showSnackbarError = {
                                    snackbarCoroutine.launch {
                                        snackbarHostState.showSnackbar(it)
                                    }
                                },
                                additionalContentPadding = innerPadding
                            )
                        }
                        composableWithDefaultSlideTransitions(
                            route = InfoAppScreens.DonateCryptocurrenciesLitecoin,
                            navigationSuiteType = navigationSuiteType,
                        ) {
                            LitecoinScreen(
                                modifier = Modifier.consumeWindowInsets(innerPadding),
                                showSnackbarError = {
                                    snackbarCoroutine.launch {
                                        snackbarHostState.showSnackbar(it)
                                    }
                                },
                                additionalContentPadding = innerPadding
                            )
                        }
                    }
                    composableWithDefaultSlideTransitions(
                        route = InfoAppScreens.DonatePaypal,
                        navigationSuiteType = navigationSuiteType,
                    ) {
                        PaypalScreen(
                            modifier = Modifier.consumeWindowInsets(innerPadding),
                            showSnackbarError = {
                                snackbarCoroutine.launch {
                                    snackbarHostState.showSnackbar(it)
                                }
                            },
                            additionalContentPadding = innerPadding
                        )
                    }
                    composableWithDefaultSlideTransitions(
                        route = InfoAppScreens.DonateBankTransfers,
                        navigationSuiteType = navigationSuiteType,
                    ) {
                        BankTransfersScreen(
                            modifier = Modifier.consumeWindowInsets(innerPadding),
                            additionalContentPadding = innerPadding
                        )
                    }
                }
            }
        }
    }
}

fun NavGraphBuilder.navigationWithDefaultSlideTransitions(
    startDestination: String,
    route: InfoAppScreens,
    navigationSuiteType: NavigationSuiteType,
    arguments: List<NamedNavArgument> = emptyList(),
    deepLinks: List<NavDeepLink> = emptyList(),
    enterTransition: (@JvmSuppressWildcards AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition?)? = null,
    exitTransition: (@JvmSuppressWildcards AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition?)? = null,
    popEnterTransition: (@JvmSuppressWildcards AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition?)? = enterTransition,
    popExitTransition: (@JvmSuppressWildcards AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition?)? = exitTransition,
    sizeTransform: (@JvmSuppressWildcards AnimatedContentTransitionScope<NavBackStackEntry>.() -> SizeTransform?)? = null,
    builder: NavGraphBuilder.() -> Unit,
) {
    navigation(startDestination, route.name, arguments, deepLinks, if (enterTransition == null) {
        {
            getEnterTransition(initialState, targetState, navigationSuiteType)
        }
    } else {
        enterTransition
    }, if (exitTransition == null) {
        {
            getExitTransition(initialState, targetState, navigationSuiteType)
        }
    } else {
        exitTransition
    }, if (popEnterTransition == null) {
        {
            getEnterTransition(initialState, targetState, navigationSuiteType)
        }
    } else {
        popEnterTransition
    }, if (popExitTransition == null) {
        {
            getExitTransition(initialState, targetState, navigationSuiteType)
        }
    } else {
        popExitTransition
    }, sizeTransform, builder)
}

fun NavGraphBuilder.composableWithDefaultSlideTransitions(
    route: InfoAppScreens,
    navigationSuiteType: NavigationSuiteType,
    arguments: List<NamedNavArgument> = emptyList(),
    deepLinks: List<NavDeepLink> = emptyList(),
    enterTransition: @JvmSuppressWildcards() (AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition?)? = null,
    exitTransition: @JvmSuppressWildcards() (AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition?)? = null,
    popEnterTransition: @JvmSuppressWildcards() (AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition?)? = enterTransition,
    popExitTransition: @JvmSuppressWildcards() (AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition?)? = exitTransition,
    sizeTransform: @JvmSuppressWildcards() (AnimatedContentTransitionScope<NavBackStackEntry>.() -> SizeTransform?)? = null,
    content: @Composable() (AnimatedContentScope.(NavBackStackEntry) -> Unit),
) {
    composable(route.name, arguments, deepLinks, if (enterTransition == null) {
        {
            getEnterTransition(initialState, targetState, navigationSuiteType)
        }
    } else {
        enterTransition
    }, if (exitTransition == null) {
        {
            getExitTransition(initialState, targetState, navigationSuiteType)
        }
    } else {
        exitTransition
    }, if (popEnterTransition == null) {
        {
            getEnterTransition(initialState, targetState, navigationSuiteType)
        }
    } else {
        popEnterTransition
    }, if (popExitTransition == null) {
        {
            getExitTransition(initialState, targetState, navigationSuiteType)
        }
    } else {
        popExitTransition
    }, sizeTransform, content)
}

fun getEnterTransition(
    initialState: NavBackStackEntry,
    targetState: NavBackStackEntry,
    navigationSuiteType: NavigationSuiteType,
): EnterTransition {
    val initialNavRoute = getStateNavRoute(initialState)
    val targetNavRoute = getStateNavRoute(targetState)
    val isNavAnimationVertical = isNavAnimationVertical(
        initialNavRoute,
        targetNavRoute,
        navigationSuiteType
    )

    return if ((initialNavRoute != null) && (targetNavRoute != null)) {
        val isInitialNavRouteGreaterThanTarget =
            initialNavRoute.ordinal > targetNavRoute.ordinal

        slideIn {
            if (isNavAnimationVertical) {
                IntOffset(
                    0,
                    if (isInitialNavRouteGreaterThanTarget) {
                        -it.height
                    } else {
                        it.height
                    }
                )
            } else {
                IntOffset(
                    if (isInitialNavRouteGreaterThanTarget) {
                        -it.width
                    } else {
                        it.width
                    },
                    0
                )
            }
        } + fadeIn()
    } else {
        EnterTransition.None
    }
}

fun getExitTransition(
    initialState: NavBackStackEntry,
    targetState: NavBackStackEntry,
    navigationSuiteType: NavigationSuiteType,
): ExitTransition {
    val initialNavRoute = getStateNavRoute(initialState)
    val targetNavRoute = getStateNavRoute(targetState)
    val isNavAnimationVertical = isNavAnimationVertical(
        initialNavRoute,
        targetNavRoute,
        navigationSuiteType
    )

    return if ((initialNavRoute != null) && (targetNavRoute != null)) {
        val isInitialNavRouteGreaterThanTarget =
            initialNavRoute.ordinal > targetNavRoute.ordinal

        slideOut {
            if (isNavAnimationVertical) {
                IntOffset(
                    0,
                    if (isInitialNavRouteGreaterThanTarget) {
                        it.height
                    } else {
                        -it.height
                    }
                )
            } else {
                IntOffset(
                    if (isInitialNavRouteGreaterThanTarget) {
                        it.width
                    } else {
                        -it.width
                    },
                    0
                )
            }
        } + fadeOut()
    } else {
        ExitTransition.None
    }
}

fun getStateNavRoute(state: NavBackStackEntry): InfoAppScreens? {
    state.destination.route?.let { return InfoAppScreens.valueOf(it) }
    return null
}

private fun isNavAnimationVertical(
    initialNavRoute: InfoAppScreens?,
    targetNavRoute: InfoAppScreens?,
    navigationSuiteType: NavigationSuiteType
): Boolean {
    val isNavigatingToADifferentNavSuiteItem =
        navSuiteItemScreens.find {
            initialNavRoute?.name?.startsWith(it.name) == true
        } != navSuiteItemScreens.find {
            targetNavRoute?.name?.startsWith(it.name) == true
        }

    val isNavAnimationVerticalBecauseOfNavSuiteType = when (navigationSuiteType) {
        NavigationSuiteType.NavigationBar -> false
        NavigationSuiteType.NavigationRail -> true
        NavigationSuiteType.NavigationDrawer -> true
        NavigationSuiteType.None -> false
        else -> false
    }

    return isNavigatingToADifferentNavSuiteItem && (isNavAnimationVerticalBecauseOfNavSuiteType)
}