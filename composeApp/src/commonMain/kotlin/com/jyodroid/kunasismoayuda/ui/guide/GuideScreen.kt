package com.jyodroid.kunasismoayuda.ui.guide

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import com.jyodroid.kunasismoayuda.core.data.remote.PRIVACY_URL
import com.jyodroid.kunasismoayuda.core.domain.model.Country
import com.jyodroid.kunasismoayuda.resources.Res
import com.jyodroid.kunasismoayuda.resources.guide_channels
import com.jyodroid.kunasismoayuda.resources.guide_privacy
import com.jyodroid.kunasismoayuda.resources.guide_tips
import com.jyodroid.kunasismoayuda.resources.mod_entry
import com.jyodroid.kunasismoayuda.ui.help.HelpDirectoryScreen
import com.jyodroid.kunasismoayuda.ui.tips.SafetyTipsScreen
import org.jetbrains.compose.resources.stringResource

private enum class GuideSection { CHANNELS, TIPS }

@Composable
fun GuideScreen(
    country: Country,
    onModeration: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var section by remember { mutableStateOf(GuideSection.CHANNELS) }
    Column(modifier.fillMaxSize()) {
        SecondaryTabRow(selectedTabIndex = section.ordinal) {
            Tab(
                selected = section == GuideSection.CHANNELS,
                onClick = { section = GuideSection.CHANNELS },
                text = { Text(stringResource(Res.string.guide_channels)) },
                modifier = Modifier.heightIn(min = 48.dp),
            )
            Tab(
                selected = section == GuideSection.TIPS,
                onClick = { section = GuideSection.TIPS },
                text = { Text(stringResource(Res.string.guide_tips)) },
                modifier = Modifier.heightIn(min = 48.dp),
            )
        }
        Box(Modifier.weight(1f).fillMaxSize()) {
            when (section) {
                GuideSection.CHANNELS -> HelpDirectoryScreen(country)
                GuideSection.TIPS -> SafetyTipsScreen(country)
            }
        }
        // Bottom row: the public "Privacy & Terms" link (left) + the discreet moderation entry (right).
        val uriHandler = LocalUriHandler.current
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextButton(
                onClick = { uriHandler.openUri(PRIVACY_URL) },
                modifier = Modifier.heightIn(min = 48.dp),
            ) {
                Text(
                    text = stringResource(Res.string.guide_privacy),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            TextButton(onClick = onModeration, modifier = Modifier.heightIn(min = 48.dp)) {
                Text(
                    text = stringResource(Res.string.mod_entry),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
