package br.com.extremetv.optimizer

import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.pm.ResolveInfo
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.os.StatFs
import android.provider.Settings
import android.text.format.Formatter
import android.widget.ImageView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val Bg = Color(0xFF171820)
private val Panel = Color(0xFF252936)
private val Panel2 = Color(0xFF343A4A)
private val Gold = Color(0xFFE2C27A)
private val White = Color(0xFFF7F7FA)
private val Muted = Color(0xFFB8BDCA)

private data class TvApp(val name: String, val pkg: String, val icon: Drawable)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.decorView.systemUiVisibility =
            (android.view.View.SYSTEM_UI_FLAG_FULLSCREEN or
             android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
             android.view.View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
             android.view.View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
             android.view.View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
             android.view.View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION)
        setContent { MaterialTheme(colorScheme = darkColorScheme(background = Bg, surface = Panel, primary = Gold)) { ExtremeHome(this) } }
    }

    override fun onResume() {
        super.onResume()
        window.decorView.systemUiVisibility =
            (android.view.View.SYSTEM_UI_FLAG_FULLSCREEN or android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or android.view.View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
    }
}

@Composable
private fun ExtremeHome(activity: Activity) {
    val context = activity
    val prefs = remember { context.getSharedPreferences("extreme_home", Context.MODE_PRIVATE) }
    var apps by remember { mutableStateOf(loadApps(context)) }
    var favorites by remember { mutableStateOf(prefs.getStringSet("favorites", emptySet())?.toList() ?: emptyList()) }
    var selected by remember { mutableStateOf<TvApp?>(null) }
    var now by remember { mutableStateOf(clockText()) }
    var page by remember { mutableStateOf("home") }
    var ram by remember { mutableStateOf("--") }
    var storage by remember { mutableStateOf("--") }

    fun refresh() {
        apps = loadApps(context)
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val mi = ActivityManager.MemoryInfo()
        am.getMemoryInfo(mi)
        ram = Formatter.formatFileSize(context, mi.availMem)
        val fs = StatFs(context.filesDir.absolutePath)
        storage = Formatter.formatFileSize(context, fs.availableBytes)
        now = clockText()
    }
    LaunchedEffect(Unit) {
        refresh()
        while (true) { delay(30_000); now = clockText() }
    }
    val featured = selected ?: apps.firstOrNull()

    Column(Modifier.fillMaxSize().background(Bg).padding(horizontal = 42.dp, vertical = 28.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
            Column {
                Text("EXTREME", color = White, fontSize = 25.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
                Text("TV  /  SMART EXPERIENCE", color = Gold, fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.7.sp)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(now.substringBefore(" • "), color = White, fontSize = 36.sp, fontWeight = FontWeight.Light)
                Text(now.substringAfter(" • "), color = Muted, fontSize = 14.sp)
            }
        }
        Spacer(Modifier.height(30.dp))
        when (page) {
            "home" -> {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(featured?.name ?: "Sua TV. Do seu jeito.", color = White, fontSize = 26.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.width(12.dp))
                    Text("ENTRETENIMENTO E CONTROLE", color = Gold, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(16.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(14.dp), contentPadding = PaddingValues(vertical = 8.dp)) {
                    items(apps, key = { it.pkg }) { app ->
                        AppTile(app, selected?.pkg == app.pkg) {
                            selected = app
                            launchApp(context, app.pkg)
                        }
                    }
                }
                Spacer(Modifier.height(22.dp))
                Text("ACESSO RÁPIDO", color = Gold, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                Spacer(Modifier.height(10.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    item { QuickTile("▶", "Play Store") { openPackage(context, "com.android.vending") } }
                    item { QuickTile("⌂", "Central de mídia") { openSettings(context) } }
                    item { QuickTile("⚙", "Configurações") { safeOpen(context, Intent(Settings.ACTION_SETTINGS)) } }
                    item { QuickTile("＋", "Adicionar app") { page = "apps" } }
                    item { QuickTile("◆", "Optimizer") { page = "optimizer" } }
                    item { QuickTile("⇄", "Entradas HDMI") { openInputs(context) } }
                }
                Spacer(Modifier.weight(1f))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("EXTREME  •  SUA EXPERIÊNCIA, DO SEU JEITO", color = Muted, fontSize = 10.sp, letterSpacing = 1.sp)
                    Text("OK: abrir  •  MENU: organizar favoritos", color = Muted, fontSize = 10.sp)
                }
            }
            "apps" -> {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Todos os aplicativos", color = White, fontSize = 25.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.width(18.dp))
                    QuickTile("←", "Voltar") { page = "home" }
                }
                Spacer(Modifier.height(20.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    items(apps, key = { it.pkg }) { app -> AppTile(app, false) { launchApp(context, app.pkg) } }
                }
                Spacer(Modifier.weight(1f))
                Text("Selecione um aplicativo para abrir. Os aplicativos instalados aparecem automaticamente.", color = Muted, fontSize = 11.sp)
            }
            else -> {
                Text("Extreme Optimizer", color = White, fontSize = 27.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(18.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Metric("RAM DISPONÍVEL", ram)
                    Metric("ARMAZENAMENTO LIVRE*", storage)
                }
                Spacer(Modifier.height(18.dp))
                QuickTile("↻", "Atualizar diagnóstico") { refresh() }
                Spacer(Modifier.height(10.dp))
                QuickTile("⚙", "Gerenciar aplicativos") { safeOpen(context, Intent(Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS)) }
                Spacer(Modifier.height(10.dp))
                QuickTile("←", "Voltar à Home") { page = "home" }
                Spacer(Modifier.weight(1f))
                Text("*Medição do espaço privado do aplicativo. O Android não permite limpeza global de RAM por apps comuns.", color = Muted, fontSize = 10.sp)
            }
        }
    }
}

@Composable
private fun AppTile(app: TvApp, isSelected: Boolean, onClick: () -> Unit) {
    var focused by remember { mutableStateOf(false) }
    Column(
        Modifier.width(142.dp).height(126.dp)
            .scale(if (focused) 1.04f else 1f)
            .background(if (focused || isSelected) Color(0xFF3D465B) else Panel, RoundedCornerShape(16.dp))
            .border(if (focused) 2.dp else 1.dp, if (focused) Gold else Color(0xFF424858), RoundedCornerShape(16.dp))
            .onFocusChanged { focused = it.isFocused }
            .clickable(onClick = onClick).focusable().padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        AndroidView(factory = { ImageView(it).apply { scaleType = ImageView.ScaleType.FIT_CENTER; setImageDrawable(app.icon) } },
            modifier = Modifier.size(48.dp))
        Spacer(Modifier.height(8.dp))
        Text(app.name, color = White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, maxLines = 2, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun QuickTile(symbol: String, label: String, onClick: () -> Unit) {
    var focused by remember { mutableStateOf(false) }
    Column(
        Modifier.width(132.dp).height(82.dp)
            .background(if (focused) Panel2 else Color(0xFF222733), RoundedCornerShape(13.dp))
            .border(if (focused) 2.dp else 1.dp, if (focused) Gold else Color(0xFF3B4251), RoundedCornerShape(13.dp))
            .onFocusChanged { focused = it.isFocused }
            .clickable(onClick = onClick).focusable().padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(symbol, color = Gold, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Text(label, color = White, fontSize = 10.sp, fontWeight = FontWeight.Medium, maxLines = 1)
    }
}

@Composable
private fun Metric(title: String, value: String) {
    Column(Modifier.width(260.dp).background(Panel, RoundedCornerShape(14.dp)).padding(18.dp)) {
        Text(title, color = Muted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text(value, color = Gold, fontSize = 23.sp, fontWeight = FontWeight.Bold)
    }
}

private fun loadApps(context: Context): List<TvApp> {
    val pm = context.packageManager
    val leanback = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER)
    val standard = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
    val found = (pm.queryIntentActivities(leanback, 0) + pm.queryIntentActivities(standard, 0))
        .distinctBy { it.activityInfo.packageName }
        .filter { it.activityInfo.packageName != context.packageName }
        .mapNotNull { info ->
            runCatching { TvApp(info.loadLabel(pm).toString(), info.activityInfo.packageName, info.loadIcon(pm)) }.getOrNull()
        }
        .sortedBy { it.name.lowercase(Locale.getDefault()) }
    return found
}

private fun launchApp(context: Context, pkg: String) {
    val intent = context.packageManager.getLaunchIntentForPackage(pkg)
    if (intent != null) safeOpen(context, intent)
}
private fun openPackage(context: Context, pkg: String) = launchApp(context, pkg)
private fun openSettings(context: Context) = safeOpen(context, Intent(Settings.ACTION_SETTINGS))
private fun openInputs(context: Context) {
    val intents = listOf(
        Intent("android.settings.HDMI_SETTINGS"),
        Intent("com.android.tv.settings.HDMI_SETTINGS"),
        Intent(Settings.ACTION_SETTINGS)
    )
    for (intent in intents) {
        try { context.startActivity(intent); return } catch (_: Exception) { }
    }
}
private fun safeOpen(context: Context, intent: Intent) {
    runCatching { context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
}
private fun clockText(): String = SimpleDateFormat("HH:mm  •  EEEE, dd 'de' MMMM", Locale("pt", "BR")).format(Date())
