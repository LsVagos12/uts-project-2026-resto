package com.example.restoprofile //
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// ==========================================================================
// 🎨 PALET WARNA CUSTOM (FIX EMULATOR DARK MODE)
// ==========================================================================
val CustomLightScheme = lightColorScheme(
    primary = Color(0xFFE11D48),
    background = Color(0xFFFDFDFD),
    surface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFF1F5F9),
    onBackground = Color(0xFF0F172A),
    onSurface = Color(0xFF0F172A),
    secondary = Color(0xFFEAB308)
)

val CustomDarkScheme = darkColorScheme(
    primary = Color(0xFFFB7185),
    background = Color(0xFF0F0F12),
    surface = Color(0xFF1E1E24),
    surfaceVariant = Color(0xFF272730),
    onBackground = Color(0xFFF8FAFC),
    onSurface = Color(0xFFF8FAFC),
    secondary = Color(0xFFFACC15)
)

data class MenuItem(val id: String, val name: String, val price: String, val description: String, val icon: ImageVector, val tag: String)

val dummyMenu = listOf(
    MenuItem("1", "Nasi Goreng Spesial", "Rp 25.000", "Nasi goreng bumbu rahasia warisan nusantara dengan telur mata sapi.", Icons.Default.DinnerDining, "Best Seller"),
    MenuItem("2", "Mie Ayam Jamur", "Rp 22.000", "Mie kenyal buatan sendiri dengan toping ayam tumis jamur melimpah.", Icons.Default.Fastfood, "Rekomendasi"),
    MenuItem("3", "Es Teh Manis Jumbo", "Rp 5.000", "Seduhan teh melati pilihan segar berukuran jumbo.", Icons.Default.LocalCafe, "Segar"),
    MenuItem("4", "Kopi Susu Aren", "Rp 18.000", "Espresso arabika premium dipadu susu segar dan gula aren murni.", Icons.Default.LocalCafe, "Kopi"),
    MenuItem("5", "Pisang Goreng Keju", "Rp 15.000", "Pisang raja krispi ditaburi keju parut melimpah dan susu manis.", Icons.Default.Icecream, "Camilan")
)

// ==========================================================================
// MAIN ACTIVITY
// ==========================================================================
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val context = LocalContext.current
            val prefsManager = remember { PreferencesManager(context) }
            val snackbarHostState = remember { SnackbarHostState() }
            val scope = rememberCoroutineScope()

            val profileData = prefsManager.getProfile()
            var restoName by remember { mutableStateOf(profileData["name"] ?: "") }
            var restoAddress by remember { mutableStateOf(profileData["address"] ?: "") }
            var restoDesc by remember { mutableStateOf(profileData["desc"] ?: "") }
            var restoHours by remember { mutableStateOf(profileData["hours"] ?: "") }
            var isDarkTheme by remember { mutableStateOf(prefsManager.isDarkTheme()) }
            var showSplashScreen by remember { mutableStateOf(true) }

            var cartCount by remember { mutableStateOf(0) }

            // 🔥 STATE BARU: Menyimpan ID menu yang di-love oleh user
            var favoriteMenuIds by remember { mutableStateOf(setOf<String>()) }

            LaunchedEffect(Unit) {
                delay(2000)
                showSplashScreen = false
            }

            val currentColorScheme = if (isDarkTheme) CustomDarkScheme else CustomLightScheme

            MaterialTheme(colorScheme = currentColorScheme) {
                Surface(modifier = Modifier.fillMaxSize(), color = currentColorScheme.background) {
                    if (showSplashScreen) {
                        SplashScreenView()
                    } else {
                        val navController = rememberNavController()
                        val navBackStackEntry by navController.currentBackStackEntryAsState()
                        val currentRoute = navBackStackEntry?.destination?.route

                        Scaffold(
                            snackbarHost = { SnackbarHost(snackbarHostState) },
                            bottomBar = {
                                if (currentRoute in listOf("home", "menu", "profile")) {
                                    NavigationBar(containerColor = currentColorScheme.surface, tonalElevation = 8.dp) {
                                        NavigationBarItem(
                                            selected = currentRoute == "home",
                                            onClick = { if(currentRoute != "home") navController.navigate("home") },
                                            icon = { Icon(Icons.Default.Home, null) }, label = { Text("Beranda") }
                                        )
                                        NavigationBarItem(
                                            selected = currentRoute == "menu",
                                            onClick = { if(currentRoute != "menu") navController.navigate("menu") },
                                            icon = {
                                                BadgedBox(badge = { if(cartCount > 0) Badge { Text(cartCount.toString()) } }) {
                                                    Icon(Icons.Default.RestaurantMenu, null)
                                                }
                                            },
                                            label = { Text("Menu") }
                                        )
                                        NavigationBarItem(
                                            selected = currentRoute == "profile",
                                            onClick = { if(currentRoute != "profile") navController.navigate("profile") },
                                            icon = { Icon(Icons.Default.Store, null) }, label = { Text("Profil") }
                                        )
                                    }
                                }
                            }
                        ) { paddingValues ->
                            NavHost(
                                navController = navController, startDestination = "home",
                                modifier = Modifier.padding(paddingValues),
                                enterTransition = { fadeIn() + slideInHorizontally { 100 } },
                                exitTransition = { fadeOut() + slideOutHorizontally { -100 } }
                            ) {
                                composable("home") {
                                    HomeScreen(restoName, isDarkTheme, { isDarkTheme = it; prefsManager.saveTheme(it) }, { navController.navigate("menu") }, { navController.navigate("profile") })
                                }
                                composable("menu") {
                                    MenuScreen(
                                        favoriteIds = favoriteMenuIds,
                                        onToggleFavorite = { id ->
                                            favoriteMenuIds = if (favoriteMenuIds.contains(id)) favoriteMenuIds - id else favoriteMenuIds + id
                                        },
                                        onNavDetail = { navController.navigate("menu/$it") }
                                    )
                                }
                                composable("menu/{menuId}", arguments = listOf(navArgument("menuId") { type = NavType.StringType })) { backStack ->
                                    DetailMenuScreen(
                                        menuId = backStack.arguments?.getString("menuId"),
                                        onBack = { navController.popBackStack() },
                                        onAddToCart = { itemName ->
                                            cartCount++
                                            scope.launch { snackbarHostState.showSnackbar("Berhasil menambahkan $itemName ke keranjang!") }
                                        }
                                    )
                                }
                                composable("profile") {
                                    ProfileScreen(restoName, restoAddress, restoDesc, restoHours, { navController.navigate("edit_profile") })
                                }
                                composable("edit_profile") {
                                    EditProfileScreen(restoName, restoAddress, restoDesc, restoHours, { n, a, d, h ->
                                        prefsManager.saveProfile(n, a, d, h)
                                        restoName = n; restoAddress = a; restoDesc = d; restoHours = h
                                        navController.popBackStack()
                                    }, { navController.popBackStack() })
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SplashScreenView() {
    Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFFE11D48), Color(0xFF4C0519)))), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(modifier = Modifier.size(100.dp).background(Color.White.copy(alpha = 0.2f), CircleShape), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Restaurant, null, modifier = Modifier.size(60.dp), tint = Color.White)
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text("VIBE RESTO", fontSize = 32.sp, fontWeight = FontWeight.Black, color = Color.White, letterSpacing = 4.sp)
            Text("Rasa Berkelas Dunia", fontSize = 14.sp, color = Color.White.copy(alpha = 0.7f))
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(restoName: String, isDarkTheme: Boolean, onThemeToggle: (Boolean) -> Unit, onNavMenu: () -> Unit, onNavProfile: () -> Unit) {
    val banners = listOf("Diskon Gila 50% Weekend Ini!", "Menu Baru: Kopi Susu Aren Autentik", "Gratis Ongkir Pembelian via Aplikasi")
    val pagerState = rememberPagerState(pageCount = { banners.size })

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("Selamat Datang di,", fontSize = 14.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f))
                    Text(restoName, fontSize = 24.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                }
                FilledIconButton(onClick = { onThemeToggle(!isDarkTheme) }, colors = IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                    Icon(if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode, null, tint = MaterialTheme.colorScheme.primary)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            Text("Promo Spesial Hari Ini 🔥", fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.padding(bottom = 8.dp))
            HorizontalPager(state = pagerState, modifier = Modifier.fillMaxWidth().height(110.dp)) { page ->
                Card(modifier = Modifier.fillMaxSize().padding(horizontal = 4.dp), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)) {
                    Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.CenterStart) {
                        Column {
                            Text(banners[page], color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text("Ketuk untuk klaim voucher >", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text("Menu Cepat", fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.padding(bottom = 8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Card(modifier = Modifier.weight(1f).clickable { onNavMenu() }, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.MenuBook, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Lihat Menu", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
                Card(modifier = Modifier.weight(1f).clickable { onNavProfile() }, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Info, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Profil Resto", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }
        }
    }
}

// ==========================================================================
// [2] MENU SCREEN (LIVE SEARCH BAR + FAVORITES TOGGLE FILTER SYSTEM)
// ==========================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuScreen(favoriteIds: Set<String>, onToggleFavorite: (String) -> Unit, onNavDetail: (String) -> Unit) {
    var searchQuery by remember { mutableStateOf("") }
    var showOnlyFavorites by remember { mutableStateOf(false) } // State filter hati

    // Menyaring ganda: berdasarkan teks pencarian DAN tombol love aktif
    val filteredMenu = dummyMenu.filter {
        it.name.contains(searchQuery, ignoreCase = true) && (!showOnlyFavorites || favoriteIds.contains(it.id))
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Eksplor Menu", fontWeight = FontWeight.Black) }) },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Cari hidangan favoritmu...") },
                leadingIcon = { Icon(Icons.Default.Search, null, tint = MaterialTheme.colorScheme.primary) },
                trailingIcon = { if(searchQuery.isNotEmpty()) IconButton(onClick = { searchQuery = "" }) { Icon(Icons.Default.Clear, null) } },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant, unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant)
            )

            // Chips Filter Kategori Cepat + TOMBOL FILTER FAVORIT 🔥
            LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(vertical = 8.dp)) {
                item {
                    FilterChip(
                        selected = showOnlyFavorites,
                        onClick = { showOnlyFavorites = !showOnlyFavorites },
                        label = { Text("Tampilkan Tersuka (❤️)") },
                        leadingIcon = { if (showOnlyFavorites) Icon(Icons.Default.Favorite, null, modifier = Modifier.size(18.dp)) }
                    )
                }
                item { SuggestionChip(onClick = {}, label = { Text("Semua") }) }
                item { SuggestionChip(onClick = {}, label = { Text("Makanan") }) }
            }

            // List Item Padat Modern
            LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (filteredMenu.isEmpty()) {
                    item {
                        Text("Tidak ada menu yang cocok! 🥺", modifier = Modifier.fillMaxWidth().padding(24.dp), textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f))
                    }
                } else {
                    items(filteredMenu) { item ->
                        val isFav = favoriteIds.contains(item.id)
                        Card(modifier = Modifier.fillMaxWidth().clickable { onNavDetail(item.id) }, shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(50.dp).clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) {
                                    Icon(item.icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(item.name, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Box(modifier = Modifier.background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), RoundedCornerShape(4.dp)).padding(horizontal = 4.dp, vertical = 2.dp)) {
                                            Text(item.tag, fontSize = 9.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                    Text(item.description, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                                }

                                // Tombol Love Interaktif 🔥
                                IconButton(onClick = { onToggleFavorite(item.id) }) {
                                    Icon(
                                        imageVector = if (isFav) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                                        contentDescription = null,
                                        tint = if (isFav) Color.Red else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                                    )
                                }
                                Text(item.price, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================================================
// [3] DETAIL MENU SCREEN (INTERACTIVE RATING + NATIVE INTENT SHARE!)
// ==========================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailMenuScreen(menuId: String?, onBack: () -> Unit, onAddToCart: (String) -> Unit) {
    val item = dummyMenu.find { it.id == menuId }
    var rating by remember { mutableStateOf(4) }
    val context = LocalContext.current // Memanggil context untuk Android Intent

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detail Hidangan") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } },
                // 📡 FITUR CANGGIH: TOMBOL NATIVE ANDROID SHARE INTENT SYSTEM
                actions = {
                    item?.let {
                        IconButton(onClick = {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, "Rekomendasi Menu Resto!")
                                putExtra(Intent.EXTRA_TEXT, "Yuk cobain ${it.name} harganya cuma ${it.price}! Deskripsi: ${it.description}")
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Bagikan lewat:"))
                        }) {
                            Icon(Icons.Default.Share, contentDescription = "Bagikan")
                        }
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        item?.let {
            Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Box(modifier = Modifier.fillMaxWidth().height(180.dp).clip(RoundedCornerShape(24.dp)).background(MaterialTheme.colorScheme.surfaceVariant), contentAlignment = Alignment.Center) {
                    Icon(it.icon, null, modifier = Modifier.size(100.dp), tint = MaterialTheme.colorScheme.primary)
                }
                Spacer(modifier = Modifier.height(20.dp))
                Text(it.name, fontSize = 26.sp, fontWeight = FontWeight.Black, textAlign = TextAlign.Center)
                Text(it.price, fontSize = 22.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.ExtraBold)
                Spacer(modifier = Modifier.height(12.dp))
                Text(it.description, fontSize = 14.sp, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f), modifier = Modifier.padding(horizontal = 16.dp))

                Spacer(modifier = Modifier.weight(1f))

                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                    Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Beri Penilaian:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Row {
                            (1..5).forEach { index ->
                                Icon(
                                    imageVector = if (index <= rating) Icons.Default.Star else Icons.Default.StarBorder, null,
                                    tint = if (index <= rating) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                                    modifier = Modifier.size(28.dp).clickable { rating = index }
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))

                Button(onClick = { onAddToCart(it.name) }, modifier = Modifier.fillMaxWidth().height(50.dp), shape = RoundedCornerShape(12.dp)) {
                    Icon(Icons.Default.AddShoppingCart, null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Tambah ke Keranjang", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ==========================================================================
// [4] PROFILE SCREEN
// ==========================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(name: String, address: String, desc: String, hours: String, onNavEdit: () -> Unit) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Profil Restoran", fontWeight = FontWeight.Black) }) },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(8.dp)) {
                    CompactInfoRow(Icons.Default.Storefront, "Nama Resmi", name)
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                    CompactInfoRow(Icons.Default.LocationOn, "Alamat Lengkap", address)
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                    CompactInfoRow(Icons.Default.AccessTime, "Jam Buka", hours)
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                    CompactInfoRow(Icons.Default.Description, "Deskripsi Singkat", desc)
                }
            }
            Spacer(modifier = Modifier.weight(1f))
            Button(onClick = onNavEdit, modifier = Modifier.fillMaxWidth().height(50.dp), shape = RoundedCornerShape(14.dp)) {
                Icon(Icons.Default.Edit, null); Spacer(Modifier.width(8.dp)); Text("Ubah Profil Restoran", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun CompactInfoRow(icon: ImageVector, title: String, content: String) {
    Row(modifier = Modifier.padding(14.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(38.dp).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape), contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.width(14.dp))
        Column {
            Text(title, fontSize = 11.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            Text(content, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

// ==========================================================================
// [5] EDIT PROFILE SCREEN
// ==========================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(initName: String, initAddress: String, initDesc: String, initHours: String, onSave: (String, String, String, String) -> Unit, onCancel: () -> Unit) {
    var name by remember { mutableStateOf(initName) }
    var address by remember { mutableStateOf(initAddress) }
    var desc by remember { mutableStateOf(initDesc) }
    var hours by remember { mutableStateOf(initHours) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Edit Profil", fontWeight = FontWeight.Bold) }, navigationIcon = { IconButton(onClick = onCancel) { Icon(Icons.Default.Close, null) } }) },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nama Restoran") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), singleLine = true)
            OutlinedTextField(value = address, onValueChange = { address = it }, label = { Text("Alamat") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), singleLine = true)
            OutlinedTextField(value = hours, onValueChange = { hours = it }, label = { Text("Jam Operasional") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), singleLine = true)
            OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("Deskripsi Restoran") }, modifier = Modifier.fillMaxWidth().height(100.dp), shape = RoundedCornerShape(12.dp))

            Spacer(modifier = Modifier.weight(1f))
            Button(onClick = { onSave(name, address, desc, hours) }, modifier = Modifier.fillMaxWidth().height(50.dp), shape = RoundedCornerShape(14.dp)) {
                Icon(Icons.Default.Save, null); Spacer(Modifier.width(8.dp)); Text("Simpan Perubahan", fontWeight = FontWeight.Bold)
            }
        }
    }
}