package com.jyodroid.kunasismoayuda

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import com.jyodroid.kunasismoayuda.ui.theme.KunaTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.jyodroid.kunasismoayuda.core.domain.model.Country
import com.jyodroid.kunasismoayuda.core.domain.model.CountryEmergency
import com.jyodroid.kunasismoayuda.core.domain.model.Quake
import com.jyodroid.kunasismoayuda.core.domain.util.Geo
import kotlin.time.Clock
import com.jyodroid.kunasismoayuda.core.location.Coordinates
import com.jyodroid.kunasismoayuda.core.location.LocationProvider
import com.jyodroid.kunasismoayuda.core.location.LocationResult
import com.jyodroid.kunasismoayuda.ui.shelters.ShelterCard
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.jyodroid.kunasismoayuda.core.domain.model.AdminAccount
import com.jyodroid.kunasismoayuda.core.domain.model.ShelterType
import com.jyodroid.kunasismoayuda.feature.map.DisasterMap
import com.jyodroid.kunasismoayuda.feature.map.MapMarker
import com.jyodroid.kunasismoayuda.feature.map.MarkerKind
import com.jyodroid.kunasismoayuda.resources.Res
import com.jyodroid.kunasismoayuda.resources.ic_aid
import com.jyodroid.kunasismoayuda.resources.ic_back
import com.jyodroid.kunasismoayuda.resources.ic_guide
import com.jyodroid.kunasismoayuda.resources.ic_map
import com.jyodroid.kunasismoayuda.resources.ic_overview
import com.jyodroid.kunasismoayuda.resources.ic_shelter
import com.jyodroid.kunasismoayuda.resources.board_new_title
import com.jyodroid.kunasismoayuda.resources.board_paste_title
import com.jyodroid.kunasismoayuda.resources.board_title
import com.jyodroid.kunasismoayuda.resources.detail_title
import com.jyodroid.kunasismoayuda.resources.fire_detail_title
import com.jyodroid.kunasismoayuda.resources.fires_title
import com.jyodroid.kunasismoayuda.resources.guide_title
import com.jyodroid.kunasismoayuda.resources.map_locating
import com.jyodroid.kunasismoayuda.resources.map_near_me
import com.jyodroid.kunasismoayuda.resources.map_show_all
import com.jyodroid.kunasismoayuda.resources.admin_title
import com.jyodroid.kunasismoayuda.resources.map_title
import com.jyodroid.kunasismoayuda.resources.mod_login_title
import com.jyodroid.kunasismoayuda.resources.mod_logout
import com.jyodroid.kunasismoayuda.resources.mod_title
import com.jyodroid.kunasismoayuda.resources.search_new_title
import com.jyodroid.kunasismoayuda.resources.search_title
import com.jyodroid.kunasismoayuda.resources.nav_back
import com.jyodroid.kunasismoayuda.resources.shelter_add_title
import com.jyodroid.kunasismoayuda.resources.shelter_edit_title
import com.jyodroid.kunasismoayuda.resources.shelters_title
import com.jyodroid.kunasismoayuda.resources.sos_resp_title
import com.jyodroid.kunasismoayuda.resources.sos_title
import com.jyodroid.kunasismoayuda.resources.sos_top_button
import com.jyodroid.kunasismoayuda.resources.tab_board
import com.jyodroid.kunasismoayuda.resources.tab_guide
import com.jyodroid.kunasismoayuda.resources.tab_map
import com.jyodroid.kunasismoayuda.resources.tab_overview
import com.jyodroid.kunasismoayuda.resources.tab_shelters
import com.jyodroid.kunasismoayuda.ui.admin.AdminManagementScreen
import com.jyodroid.kunasismoayuda.ui.admin.AdminViewModel
import com.jyodroid.kunasismoayuda.ui.auth.AuthViewModel
import com.jyodroid.kunasismoayuda.ui.auth.LoginScreen
import com.jyodroid.kunasismoayuda.ui.board.BoardScreen
import com.jyodroid.kunasismoayuda.ui.board.BoardViewModel
import com.jyodroid.kunasismoayuda.ui.board.ClassifyPostScreen
import com.jyodroid.kunasismoayuda.ui.board.CreatePostScreen
import com.jyodroid.kunasismoayuda.ui.guide.GuideScreen
import com.jyodroid.kunasismoayuda.ui.moderation.ModerationScreen
import com.jyodroid.kunasismoayuda.ui.moderation.ModerationViewModel
import com.jyodroid.kunasismoayuda.core.domain.model.Fire
import com.jyodroid.kunasismoayuda.core.domain.model.Shelter
import com.jyodroid.kunasismoayuda.ui.fires.FireDetailScreen
import com.jyodroid.kunasismoayuda.ui.fires.FiresListScreen
import com.jyodroid.kunasismoayuda.ui.fires.FiresViewModel
import com.jyodroid.kunasismoayuda.ui.overview.OverviewScreen
import com.jyodroid.kunasismoayuda.ui.quakes.QuakeDetailScreen
import com.jyodroid.kunasismoayuda.ui.quakes.QuakesViewModel
import com.jyodroid.kunasismoayuda.ui.search.SearchCreateScreen
import com.jyodroid.kunasismoayuda.ui.search.SearchScreen
import com.jyodroid.kunasismoayuda.ui.search.SearchViewModel
import com.jyodroid.kunasismoayuda.ui.settings.AppSettingsViewModel
import com.jyodroid.kunasismoayuda.ui.settings.CountryPickerScreen
import com.jyodroid.kunasismoayuda.ui.settings.CountryState
import com.jyodroid.kunasismoayuda.ui.shelters.ShelterAdminViewModel
import com.jyodroid.kunasismoayuda.ui.shelters.ShelterCreateScreen
import com.jyodroid.kunasismoayuda.ui.shelters.SheltersScreen
import com.jyodroid.kunasismoayuda.ui.shelters.SheltersViewModel
import com.jyodroid.kunasismoayuda.ui.sos.SosResponderScreen
import com.jyodroid.kunasismoayuda.ui.sos.SosResponderViewModel
import com.jyodroid.kunasismoayuda.ui.sos.SosScreen
import com.jyodroid.kunasismoayuda.ui.sos.SosViewModel
import coil3.ImageLoader
import coil3.compose.setSingletonImageLoaderFactory
import coil3.network.ktor3.KtorNetworkFetcherFactory
import com.jyodroid.kunasismoayuda.resources.rescue_icon
import io.ktor.client.HttpClient
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import kotlinx.coroutines.launch
import org.koin.compose.KoinContext
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

private const val ROUTE_OVERVIEW = "overview"
private const val ROUTE_QUAKE_DETAIL = "quake_detail"
private const val ROUTE_FIRES = "fires"
private const val ROUTE_FIRE_DETAIL = "fire_detail"
private const val ROUTE_MAP = "map"
private const val ROUTE_BOARD = "board"
private const val ROUTE_BOARD_CREATE = "board_create"
private const val ROUTE_BOARD_CLASSIFY = "board_classify"
private const val ROUTE_SHELTERS = "shelters"
private const val ROUTE_GUIDE = "guide"
private const val ROUTE_SOS = "sos"
private const val ROUTE_MODERATION = "moderation"
private const val ROUTE_ADMINS = "admins"
private const val ROUTE_SOS_RESPONDER = "sos_responder"
private const val ROUTE_SHELTER_CREATE = "shelter_create"
private const val ROUTE_SEARCH = "search"
private const val ROUTE_SEARCH_CREATE = "search_create"

private data class TopTab(
    val route: String,
    val icon: DrawableResource,
    val labelRes: StringResource,
)

private val topTabs = listOf(
    TopTab(ROUTE_OVERVIEW, Res.drawable.ic_overview, Res.string.tab_overview),
    TopTab(ROUTE_MAP, Res.drawable.ic_map, Res.string.tab_map),
    TopTab(ROUTE_BOARD, Res.drawable.rescue_icon, Res.string.tab_board),
    TopTab(ROUTE_SHELTERS, Res.drawable.ic_shelter, Res.string.tab_shelters),
    TopTab(ROUTE_GUIDE, Res.drawable.ic_guide, Res.string.tab_guide),
)

@Composable
fun App() {
    KoinContext {
        // Load Lost & Found photos through the app's shared Ktor client, so image requests carry the
        // X-App-Key header (and any future auth) the same way every other API call does.
        val httpClient = koinInject<HttpClient>()
        setSingletonImageLoaderFactory { context ->
            ImageLoader.Builder(context)
                .components { add(KtorNetworkFetcherFactory(httpClient = httpClient)) }
                .build()
        }
        KunaTheme {
            // Gate the whole app on a chosen country. First launch shows the picker; returning users
            // (a stored choice) skip straight to the content — the Loading state prevents a picker flash.
            val settingsViewModel: AppSettingsViewModel = koinViewModel()
            val countryState by settingsViewModel.state.collectAsStateWithLifecycle()
            when (val cs = countryState) {
                is CountryState.Loading ->
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                is CountryState.NeedsSelection ->
                    CountryPickerScreen(onSelect = settingsViewModel::select)
                is CountryState.Selected ->
                    AppContent(country = cs.country, onCountryChange = settingsViewModel::select)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppContent(
    country: Country,
    onCountryChange: (Country) -> Unit,
) {
    val navController = rememberNavController()
    val viewModel: QuakesViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val firesViewModel: FiresViewModel = koinViewModel()
    val firesState by firesViewModel.state.collectAsStateWithLifecycle()
    val sheltersViewModel: SheltersViewModel = koinViewModel()
    val sheltersState by sheltersViewModel.state.collectAsStateWithLifecycle()
    // Hoisted so both the shelters list (edit/delete) and the create/edit form share one instance.
    val shelterAdminViewModel: ShelterAdminViewModel = koinViewModel()
    val shelterAdminState by shelterAdminViewModel.state.collectAsStateWithLifecycle()
    var editingShelter by remember { mutableStateOf<Shelter?>(null) }
    val boardViewModel: BoardViewModel = koinViewModel()
    val boardState by boardViewModel.state.collectAsStateWithLifecycle()
    val createState by boardViewModel.createState.collectAsStateWithLifecycle()
    val classifyState by boardViewModel.classifyState.collectAsStateWithLifecycle()
    val boardSummary by boardViewModel.summary.collectAsStateWithLifecycle()
    val searchViewModel: SearchViewModel = koinViewModel()
    val searchState by searchViewModel.state.collectAsStateWithLifecycle()
    val searchCreateState by searchViewModel.createState.collectAsStateWithLifecycle()
    val sosViewModel: SosViewModel = koinViewModel()
    val sosState by sosViewModel.state.collectAsStateWithLifecycle()
    val beaconState by sosViewModel.beaconState.collectAsStateWithLifecycle()
    val authViewModel: AuthViewModel = koinViewModel()
    val session by authViewModel.session.collectAsStateWithLifecycle()

    // Whenever the selected country changes, reload every country-scoped feed (quakes + affected
    // regions, shelters, aid board). Keyed on `country`, so it fires exactly once per selection.
    LaunchedEffect(country) {
        viewModel.setCountry(country)
        firesViewModel.setCountry(country)
        sheltersViewModel.setCountry(country)
        boardViewModel.setCountry(country)
        searchViewModel.setCountry(country)
    }

    // The headline quake is the STRONGEST event within the last ~month (not the impact-ranked first):
    // for large, sparsely-seeded countries (e.g. Indonesia) an impact score attenuates a distant major
    // quake below a smaller one near a city, which would wrongly feature an aftershock. Magnitude is
    // the honest "most significant" signal; the month window keeps a stale giant from dominating once
    // it's old, but falls back to the strongest overall if the whole feed is older. Réplicas follow.
    val featuredQuake = remember(state.quakes) {
        pickFeaturedQuake(state.quakes, Clock.System.now().toEpochMilliseconds())
    }
    val featuredAffected = featuredQuake?.let { viewModel.affectedRegions(it) } ?: emptyList()
    val aftershocks = featuredQuake?.let { viewModel.aftershocks(it) } ?: emptyList()

    // Wildfire vertical (second hazard): the worst active fire + the places near any fire, derived the
    // same way quakes are. Keyed on the fire feed so both recompute exactly when it changes.
    val featuredFire = remember(firesState.fires) { firesViewModel.featuredFire }
    val fireAffected = remember(firesState.fires) { firesViewModel.affectedRegions() }
    // FIRMS points have no place name; fall back to the nearest known city for a human label.
    val featuredFireNear = remember(firesState.fires) { featuredFire?.let { firesViewModel.nearestPlace(it) } }
    val rankedFires = remember(firesState.fires) { firesViewModel.rankedFires() }
    // The fire whose detail is open — chosen from the fires list; falls back to the featured fire.
    var selectedFire by remember { mutableStateOf<Fire?>(null) }

    // The map is help-points only — official aid centers coloured by type. The quake itself is
    // never drawn here; all quake/réplica detail lives in the Overview bubble.
    val shelters = sheltersState.shelters

    // Device location for "near me" — requested on demand from the map's button (never on load,
    // so we don't prompt for permission unprompted). When active, the map shows only the closest
    // few points and recenters on the user.
    val locationProvider: LocationProvider = koinInject()
    val locationScope = rememberCoroutineScope()
    var userCoords by remember { mutableStateOf<Coordinates?>(null) }
    var nearMeOnly by remember { mutableStateOf(false) }
    var locating by remember { mutableStateOf(false) }

    val nearestShelters = userCoords?.let { uc ->
        shelters.sortedBy { Geo.distanceKm(uc.latitude, uc.longitude, it.latitude, it.longitude) }
    }
    val visibleShelters = if (nearMeOnly && nearestShelters != null) {
        nearestShelters.take(8)
    } else {
        shelters
    }
    val shelterMarkers = visibleShelters.map { shelter ->
        MapMarker(
            id = "shelter:${shelter.id}",
            latitude = shelter.latitude,
            longitude = shelter.longitude,
            kind = shelter.type.toMarkerKind(),
            label = shelter.name,
        )
    }
    // Active wildfires overlay the help points as orange markers (only when not filtering "near me",
    // so the near-me view stays focused on the closest help centers).
    val fireMarkers = if (nearMeOnly) emptyList() else firesState.fires.map { fire ->
        MapMarker(
            id = "fire:${fire.id}",
            latitude = fire.latitude,
            longitude = fire.longitude,
            kind = MarkerKind.FIRE,
            label = null,
        )
    }
    val markers = shelterMarkers + fireMarkers

    // Focus: "near me" centers tight on the user; otherwise on the help points in the quake's
    // affected places (fallback: all points, then the selected country's center). Affected regions
    // only pick the focus — they are never drawn on the map.
    val affectedNames = featuredAffected.map { it.region.name }.toSet()
    val affectedShelters = shelters.filter { cityOf(it.address) in affectedNames }
    val focusShelters = affectedShelters.ifEmpty { shelters }
    val uc = userCoords
    val mapFocusLat: Double?
    val mapFocusLon: Double?
    val mapFocusZoom: Double?
    if (nearMeOnly && uc != null) {
        mapFocusLat = uc.latitude
        mapFocusLon = uc.longitude
        mapFocusZoom = 12.0
    } else if (focusShelters.isNotEmpty()) {
        mapFocusLat = focusShelters.map { it.latitude }.average()
        mapFocusLon = focusShelters.map { it.longitude }.average()
        mapFocusZoom = 8.0
    } else {
        // No help points to frame (e.g. a country still being seeded) — fall back to its centroid.
        mapFocusLat = country.centerLat
        mapFocusLon = country.centerLon
        mapFocusZoom = country.defaultZoom
    }

    fun requestNearMe() {
        locationScope.launch {
            locating = true
            when (val r = locationProvider.current()) {
                is LocationResult.Granted -> {
                    userCoords = r.coordinates
                    nearMeOnly = true
                }
                // No permission / no fix: keep showing all points (the button stays available).
                else -> Unit
            }
            locating = false
        }
    }

    var selectedShelterId by remember { mutableStateOf<Int?>(null) }
    val selectedShelter = selectedShelterId?.let { id -> shelters.firstOrNull { it.id == id } }
    val selectedDistanceKm = selectedShelter?.let { s ->
        userCoords?.let { u -> Geo.distanceKm(u.latitude, u.longitude, s.latitude, s.longitude) }
    }

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    val titleRes = when (currentRoute) {
        ROUTE_MAP -> Res.string.map_title
        ROUTE_BOARD -> Res.string.board_title
        ROUTE_BOARD_CREATE -> Res.string.board_new_title
        ROUTE_BOARD_CLASSIFY -> Res.string.board_paste_title
        ROUTE_SHELTERS -> Res.string.shelters_title
        ROUTE_GUIDE -> Res.string.guide_title
        ROUTE_SOS -> Res.string.sos_title
        ROUTE_MODERATION -> if (session != null) Res.string.mod_title else Res.string.mod_login_title
        ROUTE_ADMINS -> Res.string.admin_title
        ROUTE_SOS_RESPONDER -> Res.string.sos_resp_title
        ROUTE_SHELTER_CREATE -> if (editingShelter != null) Res.string.shelter_edit_title else Res.string.shelter_add_title
        ROUTE_SEARCH -> Res.string.search_title
        ROUTE_SEARCH_CREATE -> Res.string.search_new_title
        ROUTE_QUAKE_DETAIL -> Res.string.detail_title
        ROUTE_FIRES -> Res.string.fires_title
        ROUTE_FIRE_DETAIL -> Res.string.fire_detail_title
        else -> Res.string.tab_overview
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(titleRes)) },
                navigationIcon = {
                    // Pushed screens (not one of the bottom tabs) get a back arrow — the only
                    // way off them on iOS, which has no system back.
                    val isTab = topTabs.any { it.route == currentRoute }
                    if (currentRoute != null && !isTab) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                painter = painterResource(Res.drawable.ic_back),
                                contentDescription = stringResource(Res.string.nav_back),
                            )
                        }
                    }
                },
                actions = {
                    // On the moderation queue, offer sign-out instead of the SOS shortcut.
                    if (currentRoute == ROUTE_MODERATION && session != null) {
                        TextButton(
                            onClick = { authViewModel.logout() },
                            modifier = Modifier.padding(end = 8.dp),
                        ) {
                            Text(stringResource(Res.string.mod_logout))
                        }
                    } else if (currentRoute != ROUTE_SOS && currentRoute != ROUTE_MODERATION && currentRoute != ROUTE_ADMINS && currentRoute != ROUTE_SOS_RESPONDER && currentRoute != ROUTE_SHELTER_CREATE) {
                        Button(
                            onClick = {
                                sosViewModel.reset()
                                navController.navigate(ROUTE_SOS)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error,
                                contentColor = MaterialTheme.colorScheme.onError,
                            ),
                            modifier = Modifier.padding(end = 8.dp),
                        ) {
                            Text(stringResource(Res.string.sos_top_button))
                        }
                    }
                },
            )
        },
        bottomBar = {
            if (topTabs.any { it.route == currentRoute }) {
                NavigationBar {
                    topTabs.forEach { tab ->
                        NavigationBarItem(
                            selected = currentRoute == tab.route,
                            onClick = { navController.navigateTop(tab.route) },
                            icon = {
                                Icon(
                                    painter = painterResource(tab.icon),
                                    contentDescription = null,
                                )
                            },
                            label = {
                                Text(
                                    text = stringResource(tab.labelRes),
                                    textAlign = TextAlign.Center,
                                    maxLines = 1,
                                )
                            },
                        )
                    }
                }
            }
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = ROUTE_OVERVIEW,
            modifier = Modifier.fillMaxSize().padding(padding),
        ) {
            composable(ROUTE_OVERVIEW) {
                OverviewScreen(
                    state = state,
                    featuredQuake = featuredQuake,
                    affectedRegions = featuredAffected,
                    shelters = shelters,
                    boardSummary = boardSummary,
                    featuredFire = featuredFire,
                    featuredFireNear = featuredFireNear,
                    currentCountry = country,
                    onCountryChange = onCountryChange,
                    onRefresh = {
                        viewModel.load()
                        firesViewModel.load()
                        sheltersViewModel.load()
                        boardViewModel.load()
                        boardViewModel.loadSummary()
                    },
                    onQuakeTap = { navController.navigate(ROUTE_QUAKE_DETAIL) },
                    onFireTap = { navController.navigate(ROUTE_FIRES) },
                    onSheltersTap = { navController.navigateTop(ROUTE_SHELTERS) },
                    onNetworkTap = { navController.navigateTop(ROUTE_BOARD) },
                )
            }
            composable(ROUTE_QUAKE_DETAIL) {
                // The only place quake/réplica detail appears (never on the map).
                featuredQuake?.let {
                    QuakeDetailScreen(
                        quake = it,
                        affectedRegions = featuredAffected,
                        aftershocks = aftershocks,
                    )
                }
            }
            composable(ROUTE_FIRES) {
                FiresListScreen(
                    fires = rankedFires,
                    nearPlaceOf = { firesViewModel.nearestPlace(it) },
                    onFireTap = { fire ->
                        selectedFire = fire
                        navController.navigate(ROUTE_FIRE_DETAIL)
                    },
                )
            }
            composable(ROUTE_FIRE_DETAIL) {
                (selectedFire ?: featuredFire)?.let { fire ->
                    FireDetailScreen(
                        fire = fire,
                        nearPlace = firesViewModel.nearestPlace(fire),
                        affectedRegions = firesViewModel.affectedRegionsFor(fire),
                    )
                }
            }
            composable(ROUTE_MAP) {
                Box(Modifier.fillMaxSize()) {
                    DisasterMap(
                        markers = markers,
                        onMarkerTap = { id ->
                            if (id.startsWith("shelter:")) {
                                selectedShelterId = id.removePrefix("shelter:").toIntOrNull()
                            }
                        },
                        focusLat = mapFocusLat,
                        focusLon = mapFocusLon,
                        focusZoom = mapFocusZoom,
                        userLat = userCoords?.latitude,
                        userLon = userCoords?.longitude,
                        modifier = Modifier.fillMaxSize(),
                    )
                    // Toggle between "closest to me" (device location) and all help points.
                    ExtendedFloatingActionButton(
                        onClick = {
                            if (nearMeOnly) nearMeOnly = false else requestNearMe()
                        },
                        text = {
                            Text(
                                stringResource(
                                    when {
                                        locating -> Res.string.map_locating
                                        nearMeOnly -> Res.string.map_show_all
                                        else -> Res.string.map_near_me
                                    },
                                ),
                            )
                        },
                        icon = {
                            Icon(
                                painter = painterResource(Res.drawable.ic_map),
                                contentDescription = null,
                            )
                        },
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(16.dp),
                    )
                }
            }
            composable(ROUTE_BOARD) {
                BoardScreen(
                    state = boardState,
                    onKindChange = boardViewModel::setKind,
                    onTypeChange = boardViewModel::setType,
                    onRetry = boardViewModel::load,
                    onNewPost = {
                        boardViewModel.resetCreateState()
                        navController.navigate(ROUTE_BOARD_CREATE)
                    },
                    onPastePost = {
                        boardViewModel.resetClassifyState()
                        navController.navigate(ROUTE_BOARD_CLASSIFY)
                    },
                    onSearch = { navController.navigate(ROUTE_SEARCH) },
                    onResolve = boardViewModel::resolve,
                )
            }
            composable(ROUTE_BOARD_CREATE) {
                CreatePostScreen(
                    createState = createState,
                    onSubmit = { newPost ->
                        boardViewModel.submit(newPost) { navController.popBackStack() }
                    },
                )
            }
            composable(ROUTE_BOARD_CLASSIFY) {
                ClassifyPostScreen(
                    state = classifyState,
                    onSubmit = boardViewModel::classify,
                    onConfirm = boardViewModel::confirmClassified,
                    onEdit = boardViewModel::editClassified,
                    onKindChange = boardViewModel::setClassifyKind,
                )
            }
            composable(ROUTE_SEARCH) {
                SearchScreen(
                    state = searchState,
                    onSubjectChange = searchViewModel::setSubject,
                    onStatusChange = searchViewModel::setStatus,
                    onRetry = searchViewModel::load,
                    onNew = {
                        searchViewModel.resetCreateState()
                        navController.navigate(ROUTE_SEARCH_CREATE)
                    },
                )
            }
            composable(ROUTE_SEARCH_CREATE) {
                SearchCreateScreen(
                    createState = searchCreateState,
                    onSubmit = { report, photo, mime ->
                        searchViewModel.submit(report, photo, mime) { navController.popBackStack() }
                    },
                )
            }
            composable(ROUTE_SHELTERS) {
                SheltersScreen(
                    state = sheltersState,
                    onRetry = sheltersViewModel::load,
                    isModerator = session != null,
                    onEditShelter = { shelter ->
                        editingShelter = shelter
                        shelterAdminViewModel.resetState()
                        navController.navigate(ROUTE_SHELTER_CREATE)
                    },
                    onDeleteShelter = { shelter ->
                        shelterAdminViewModel.delete(shelter.id) { sheltersViewModel.load() }
                    },
                )
            }
            composable(ROUTE_GUIDE) {
                GuideScreen(
                    country = country,
                    onModeration = { navController.navigate(ROUTE_MODERATION) },
                )
            }
            composable(ROUTE_MODERATION) {
                // Moderator-only. Show the login form until there's a session, then the queue.
                if (session == null) {
                    val formState by authViewModel.form.collectAsStateWithLifecycle()
                    LoginScreen(state = formState, onSubmit = authViewModel::login)
                } else {
                    val moderationViewModel: ModerationViewModel = koinViewModel()
                    val moderationState by moderationViewModel.state.collectAsStateWithLifecycle()
                    ModerationScreen(
                        state = moderationState,
                        onApprove = moderationViewModel::approve,
                        onReject = moderationViewModel::reject,
                        onLoad = moderationViewModel::load,
                        // The admin-account console is SUPERADMIN-only; plain ADMINs never see the entry.
                        onManageAdmins = if (session?.role == AdminAccount.ROLE_SUPERADMIN) {
                            { navController.navigate(ROUTE_ADMINS) }
                        } else {
                            null
                        },
                        // The SOS responder view is open to any logged-in moderator.
                        onSosResponder = { navController.navigate(ROUTE_SOS_RESPONDER) },
                        // Adding help points is open to any logged-in moderator.
                        onAddShelter = {
                            editingShelter = null
                            shelterAdminViewModel.resetState()
                            navController.navigate(ROUTE_SHELTER_CREATE)
                        },
                    )
                }
            }
            composable(ROUTE_ADMINS) {
                // Super-admin console. Guard against a stale nav entry after sign-out.
                if (session == null) {
                    LaunchedEffect(Unit) { navController.popBackStack() }
                } else {
                    val adminViewModel: AdminViewModel = koinViewModel()
                    val adminState by adminViewModel.state.collectAsStateWithLifecycle()
                    AdminManagementScreen(
                        state = adminState,
                        onLoad = adminViewModel::load,
                        onCreate = adminViewModel::create,
                        onDelete = adminViewModel::delete,
                        onClearCreateError = adminViewModel::clearCreateError,
                    )
                }
            }
            composable(ROUTE_SOS) {
                SosScreen(
                    state = sosState,
                    emergencyNumber = CountryEmergency.generalNumber(country),
                    onSos = sosViewModel::sendSos,
                    onSafe = sosViewModel::sendSafe,
                    beacon = beaconState,
                    canFlash = sosViewModel.canFlash,
                    canSound = sosViewModel.canSound,
                    onStartBeacon = sosViewModel::startBeacon,
                    onStopBeacon = sosViewModel::stopBeacon,
                    onToggleLight = sosViewModel::setBeaconLight,
                    onToggleSound = sosViewModel::setBeaconSound,
                )
            }
            composable(ROUTE_SHELTER_CREATE) {
                // Moderator-only add/edit help-point form with a map-tap location picker.
                if (session == null) {
                    LaunchedEffect(Unit) { navController.popBackStack() }
                } else {
                    ShelterCreateScreen(
                        country = country,
                        state = shelterAdminState,
                        existing = editingShelter,
                        onSubmit = { newShelter ->
                            shelterAdminViewModel.submit(newShelter, editingShelter?.id) {
                                editingShelter = null
                                sheltersViewModel.load() // refresh the list/map with the change
                                navController.popBackStack()
                            }
                        },
                    )
                }
            }
            composable(ROUTE_SOS_RESPONDER) {
                // Moderator-only responder view. Guard against a stale nav entry after sign-out.
                if (session == null) {
                    LaunchedEffect(Unit) { navController.popBackStack() }
                } else {
                    val responderViewModel: SosResponderViewModel = koinViewModel()
                    val responderState by responderViewModel.state.collectAsStateWithLifecycle()
                    SosResponderScreen(
                        state = responderState,
                        onFilterChange = responderViewModel::setFilter,
                        onViewChange = responderViewModel::setShowArchived,
                        onArchive = responderViewModel::archive,
                        onReopen = responderViewModel::reopen,
                        onDelete = responderViewModel::delete,
                        onRetry = responderViewModel::load,
                    )
                }
            }
        }

        // Tapping an aid center on the map opens its details here, in place (no tab jump).
        if (selectedShelter != null) {
            ModalBottomSheet(onDismissRequest = { selectedShelterId = null }) {
                Column(
                    Modifier.padding(start = 16.dp, end = 16.dp, bottom = 32.dp),
                ) {
                    ShelterCard(selectedShelter, distanceKm = selectedDistanceKm)
                }
            }
        }
    }
}

/** How recent a quake must be to headline the Overview before we fall back to the all-time strongest. */
private const val FEATURED_WINDOW_DAYS = 30L

/**
 * The headline quake: the strongest (max magnitude) event within the last [FEATURED_WINDOW_DAYS]. If
 * the whole feed is older than that window, falls back to the strongest overall so something always
 * shows. [nowMillis] is injected so this stays a pure, testable function.
 */
private fun pickFeaturedQuake(quakes: List<Quake>, nowMillis: Long): Quake? {
    if (quakes.isEmpty()) return null
    val cutoff = nowMillis - FEATURED_WINDOW_DAYS * 24L * 60L * 60L * 1000L
    val pool = quakes.filter { it.timeMillis >= cutoff }.ifEmpty { quakes }
    return pool.maxByOrNull { it.magnitude ?: 0.0 }
}

/** City parsed from a shelter address tail (after the last comma); "" if none. */
private fun cityOf(address: String): String = address.substringAfterLast(',', "").trim()

private fun ShelterType.toMarkerKind(): MarkerKind = when (this) {
    ShelterType.ACOPIO -> MarkerKind.ACOPIO
    ShelterType.ALBERGUE -> MarkerKind.ALBERGUE
    ShelterType.SALUD -> MarkerKind.SALUD
    ShelterType.AGUA -> MarkerKind.AGUA
    ShelterType.OTRO -> MarkerKind.OTRO
}

private fun androidx.navigation.NavController.navigateTop(route: String) {
    navigate(route) {
        popUpTo(graph.startDestinationId) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
