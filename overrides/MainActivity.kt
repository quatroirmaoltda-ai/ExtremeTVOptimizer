package br.com.extremetv.optimizer

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.StatFs
import android.provider.Settings
import android.text.format.Formatter
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.content.pm.ResolveInfo
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val Ink = Color(0xFF080B12)
private val Surface = Color(0xFF141B27)
private val Surface2 = Color(0xFF1C2635)
private val Gold = Color(0xFFD9B36A)
private val Soft = Color(0xFF9DAABD)
private val White = Color(0xFFF7F8FA)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { ExtremeLauncher() }
    }
}

private data class TvApp(val name: String, val pkg: String)

@Composable
private fun ExtremeLauncher() {
    val context = LocalContext.current
    var page by remember { mutableStateOf("Início") }
    var apps by remember { mutableStateOf(emptyList<TvApp>()) }
    var ram by remember { mutableStateOf("--") }
    var storage by remember { mutableStateOf("--") }
    var clock by remember { mutableStateOf(currentClock()) }
    var featured by remember { mutableStateOf<TvApp?>(null) }

    fun refresh() {
        val pm = context.packageManager
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER)
        apps = pm.queryIntentActivities(intent, 0)
            .map { it: ResolveInfo -> TvApp(it.loadLabel(pm).toString(), it.activityInfo.packageName) }
            .distinctBy { it.pkg }.sortedBy { it.name.lowercase(Locale.getDefault()) }
        val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memory = ActivityManager.MemoryInfo()
        manager.getMemoryInfo(memory)
        ram = "${Formatter.formatFileSize(context, memory.availMem)} livres"
        val stat = StatFs(context.filesDir.absolutePath)
        storage = "${Formatter.formatFileSize(context, stat.availableBytes)} livres"
        clock = currentClock()
    }

    LaunchedEffect(Unit) { refresh() }
    MaterialTheme(colorScheme = darkColorScheme(background = Ink, surface = Surface, primary = Gold)) {
        Row(Modifier.fillMaxSize().background(Ink).padding(28.dp)) {
            Column(Modifier.width(190.dp).fillMaxHeight(), verticalArrangement = Arrangement.SpaceBetween) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("EXTREME", color = White, fontSize = 27.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
                    Text("TV  /  SMART EXPERIENCE", color = Gold, fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp)
                    Spacer(Modifier.height(26.dp))
                    listOf("Início", "Aplicativos", "Optimizer", "Central").forEach { item ->
                        val selected = page == item
                        Box(
                            Modifier.fillMaxWidth()
                                .background(if (selected) Surface2 else Color.Transparent, RoundedCornerShape(12.dp))
                                .border(if (selected) 1.dp else 0.dp, if (selected) Gold.copy(.7f) else Color.Transparent, RoundedCornerShape(12.dp))
                                .clickable { page = item }.focusable().padding(horizontal = 15.dp, vertical = 14.dp)
                        ) {
                            Text(item, color = if (selected) Gold else White, fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium)
                        }
                    }
                }
                Column {
                    Text(clock, color = White, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
                    Text("ANDROID TV  •  EXTREME", color = Soft, fontSize = 9.sp)
                }
            }
            Spacer(Modifier.width(28.dp))
            Column(Modifier.weight(1f).fillMaxHeight()) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text(when (page) {
                            "Aplicativos" -> "Sua biblioteca"
                            "Optimizer" -> "Desempenho da TV"
                            "Central" -> "Central de controle"
                            else -> "Sua TV. Do seu jeito."
                        }, color = White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                        Text("Entretenimento e controle em uma experiência simples.", color = Soft, fontSize = 12.sp)
                    }
                    Text("EXTREME  ◆", color = Gold, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                }
                Spacer(Modifier.height(22.dp))
                when (page) {
                    "Início" -> {
                        Box(Modifier.fillMaxWidth().height(145.dp).background(Surface, RoundedCornerShape(18.dp)).padding(22.dp)) {
                            Column(Modifier.align(Alignment.CenterStart)) {
                                Text("BEM-VINDO À EXTREME TV", color = Gold, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                                Spacer(Modifier.height(8.dp))
                                Text(featured?.name ?: "Tudo pronto para assistir", color = White, fontSize = 25.sp, fontWeight = FontWeight.Bold)
                                Text("Escolha um aplicativo e aproveite sua programação.", color = Soft, fontSize = 12.sp)
                            }
                            Text("▶", Modifier.align(Alignment.CenterEnd).padding(end = 22.dp), color = Gold, fontSize = 40.sp)
                        }
                        Spacer(Modifier.height(20.dp))
                        Text("SEUS APLICATIVOS", color = Gold, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                        Spacer(Modifier.height(10.dp))
                        AppGrid(apps.take(8), onSelect = { featured = it; launchApp(context, it.pkg) })
                    }
                    "Aplicativos" -> {
                        Text("${apps.size} aplicativos disponíveis para TV", color = Soft)
                        Spacer(Modifier.height(10.dp))
                        AppGrid(apps, onSelect = { featured = it; launchApp(context, it.pkg) })
                    }
                    "Optimizer" -> {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Metric("MEMÓRIA RAM LIVRE", ram, Modifier.weight(1f))
                            Metric("ARMAZENAMENTO LIVRE*", storage, Modifier.weight(1f))
                        }
                        Spacer(Modifier.height(12.dp))
                        Action("Atualizar diagnóstico", "Consultar novamente os indicadores do sistema") { refresh() }
                        Action("Gerenciar aplicativos", "Abrir ferramentas oficiais do Android") { safeOpen(context, Intent(Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS)) }
                        Action("Armazenamento", "Ver uso e opções de limpeza disponíveis") { safeOpen(context, Intent(Settings.ACTION_INTERNAL_STORAGE_SETTINGS)) }
                        Text("*Espaço disponível na área de dados acessível ao aplicativo. O Android não permite limpeza global de RAM por apps comuns.", color = Soft, fontSize = 10.sp)
                    }
                    else -> {
                        Action("Configurações da TV", "Imagem, som, rede e preferências do Android") { safeOpen(context, Intent(Settings.ACTION_SETTINGS)) }
                        Action("Rede e Wi-Fi", "Abrir configurações de conexão") { safeOpen(context, Intent(Settings.ACTION_WIFI_SETTINGS)) }
                        Action("Informações do Extreme TV", "Permissões e detalhes do aplicativo") {
                            safeOpen(context, Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${context.packageName}")))
                        }
                        Action("Atualizar aplicativos", "Recarregar a biblioteca de aplicativos") { refresh() }
                        Text("Os atalhos HDMI dependem dos recursos expostos pelo firmware TCL.", color = Soft, fontSize = 10.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun AppGrid(apps: List<TvApp>, onSelect: (TvApp) -> Unit) {
    if (apps.isEmpty()) {
        Text("Nenhum aplicativo de TV encontrado.", color = Soft)
    } else {
        LazyVerticalGrid(columns = GridCells.Fixed(4), verticalArrangement = Arrangement.spacedBy(10.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            items(apps, key = { it.pkg }) { app ->
                Column(
                    Modifier.height(83.dp).background(Surface, RoundedCornerShape(13.dp))
                        .clickable { onSelect(app) }.focusable().padding(12.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(app.name.take(26), color = White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, maxLines = 2)
                    Spacer(Modifier.height(5.dp))
                    Text("ABRIR  ›", color = Gold, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun Metric(title: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier.height(90.dp).background(Surface, RoundedCornerShape(14.dp)).padding(15.dp), verticalArrangement = Arrangement.Center) {
        Text(title, color = Soft, fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        Spacer(Modifier.height(7.dp))
        Text(value, color = Gold, fontSize = 18.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun Action(title: String, detail: String, action: () -> Unit) {
    Column(
        Modifier.fillMaxWidth().padding(bottom = 9.dp).background(Surface, RoundedCornerShape(12.dp))
            .clickable { action() }.focusable().padding(horizontal = 15.dp, vertical = 12.dp)
    ) {
        Text(title, color = White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        Text(detail, color = Soft, fontSize = 10.sp)
    }
}

private fun launchApp(context: Context, pkg: String) {
    context.packageManager.getLaunchIntentForPackage(pkg)?.let { safeOpen(context, it) }
}
private fun safeOpen(context: Context, intent: Intent) {
    runCatching { context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
}
private fun currentClock(): String = SimpleDateFormat("HH:mm  •  dd/MM", Locale("pt", "BR")).format(Date())
