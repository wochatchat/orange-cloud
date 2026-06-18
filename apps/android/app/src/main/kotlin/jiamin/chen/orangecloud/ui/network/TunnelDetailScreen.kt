package jiamin.chen.orangecloud.ui.network

import android.text.format.DateUtils
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Hub
import androidx.compose.material.icons.outlined.SettingsEthernet
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import jiamin.chen.orangecloud.R
import jiamin.chen.orangecloud.core.design.SkyBackground
import jiamin.chen.orangecloud.core.design.SkyEmptyState
import jiamin.chen.orangecloud.core.design.SkyHeader
import jiamin.chen.orangecloud.core.design.onSky
import jiamin.chen.orangecloud.core.design.rememberSkyPhase
import jiamin.chen.orangecloud.core.design.theme.OcOrange
import jiamin.chen.orangecloud.data.model.TunnelConnection
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

/** 隧道详情：信息段 + 活跃连接列表（对齐 iOS TunnelDetailView，只读）。 */
@Composable
fun TunnelDetailScreen(
    onBack: () -> Unit,
    viewModel: TunnelDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val phase = rememberSkyPhase()
    val onSky = phase.onSky

    SkyBackground(phase = phase) {
        Column(Modifier.fillMaxSize().systemBarsPadding()) {
            SkyHeader(
                title = state.tunnel?.name?.ifBlank { viewModel.tunnelName } ?: viewModel.tunnelName.ifBlank { stringResource(R.string.tunnel_title) },
                onSky = onSky,
                isLoading = state.isLoading,
                onRefresh = { viewModel.load() },
                onBack = onBack,
                titleSize = 22,
                backDescription = stringResource(R.string.common_back),
                refreshDescription = stringResource(R.string.common_refresh),
            )
            when {
                state.missingScope ->
                    SkyEmptyState(Icons.Outlined.Hub, stringResource(R.string.scope_missing), onSky, stringResource(R.string.common_refresh)) { viewModel.load() }

                state.tunnel == null && state.isLoading ->
                    Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator(color = onSky) }

                state.tunnel == null ->
                    SkyEmptyState(Icons.Outlined.Hub, stringResource(R.string.error_generic), onSky, stringResource(R.string.common_refresh)) { viewModel.load() }

                else -> {
                    val tunnel = state.tunnel!!
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp)
                            .padding(bottom = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        // 信息段
                        SectionCard(stringResource(R.string.tunnel_section_info)) {
                            StatusRow(tunnel.status)
                            tunnel.tunType?.let { InfoRow(stringResource(R.string.tunnel_field_type), it) }
                            tunnel.remoteConfig?.let {
                                InfoRow(
                                    stringResource(R.string.tunnel_field_config),
                                    stringResource(if (it) R.string.tunnel_config_remote else R.string.tunnel_config_local),
                                )
                            }
                            formatDate(tunnel.createdAt)?.let { InfoRow(stringResource(R.string.tunnel_field_created), it) }
                            InfoRow(stringResource(R.string.tunnel_field_id), tunnel.id, mono = true)
                        }

                        // 活跃连接
                        SectionCard(stringResource(R.string.tunnel_section_connections)) {
                            val connections = tunnel.connections.orEmpty()
                            if (connections.isEmpty()) {
                                Text(
                                    stringResource(R.string.tunnel_no_connections),
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            } else {
                                connections.forEachIndexed { index, conn ->
                                    ConnectionRow(conn)
                                    if (index < connections.lastIndex) {
                                        Spacer(Modifier.size(10.dp))
                                    }
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
private fun SectionCard(title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            title,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 4.dp),
        )
        Surface(
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                content()
            }
        }
    }
}

@Composable
private fun StatusRow(status: String?) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(stringResource(R.string.tunnel_field_status), fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.weight(1f))
        Box(Modifier.size(8.dp).clip(CircleShape).background(tunnelStatusColor(status)))
        Spacer(Modifier.width(6.dp))
        Text(
            stringResource(tunnelStatusLabel(status)),
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun InfoRow(label: String, value: String, mono: Boolean = false) {
    Row(Modifier.fillMaxWidth()) {
        Text(label, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.width(12.dp))
        Spacer(Modifier.weight(1f))
        Text(
            value,
            fontSize = if (mono) 12.sp else 14.sp,
            fontFamily = if (mono) FontFamily.Monospace else FontFamily.Default,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun ConnectionRow(conn: TunnelConnection) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            Icons.Outlined.SettingsEthernet,
            contentDescription = null,
            tint = Color(0xFF2FBF71),
            modifier = Modifier.size(22.dp),
        )
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                conn.coloName ?: stringResource(R.string.tunnel_unknown_colo),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            val sub = listOfNotNull(
                conn.clientVersion?.let { "cloudflared $it" },
                relativeTime(conn.openedAt),
            ).joinToString(" · ")
            if (sub.isNotEmpty()) {
                Text(sub, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

private val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)

private fun formatDate(iso: String?): String? {
    if (iso.isNullOrEmpty()) return null
    return runCatching {
        Instant.parse(iso).atZone(ZoneId.systemDefault()).format(dateFormatter)
    }.getOrNull()
}

private fun relativeTime(iso: String?): String? {
    if (iso.isNullOrEmpty()) return null
    return runCatching {
        val millis = Instant.parse(iso).toEpochMilli()
        DateUtils.getRelativeTimeSpanString(millis, System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS).toString()
    }.getOrNull()
}
