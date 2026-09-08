package com.jyodroid.kunasismoayuda.ui

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.jyodroid.kunasismoayuda.core.domain.model.Country
import com.jyodroid.kunasismoayuda.resources.Res
import com.jyodroid.kunasismoayuda.resources.mod_country_all
import org.jetbrains.compose.resources.stringResource

/**
 * A moderation country filter: **Todos** (null = all countries) + one chip per [Country] (flag + code).
 * It's a view filter, never a permission boundary — "Todos" is always available so nothing goes
 * unmoderated. Shared by the board moderation queue and the SOS responder view.
 */
@Composable
fun CountryFilterRow(selected: String?, onSelect: (String?) -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth().horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        FilterChip(
            selected = selected == null,
            onClick = { onSelect(null) },
            label = { Text(stringResource(Res.string.mod_country_all)) },
            modifier = Modifier.heightIn(min = 40.dp),
        )
        Country.entries.forEach { c ->
            FilterChip(
                selected = selected == c.code,
                onClick = { onSelect(if (selected == c.code) null else c.code) },
                label = { Text("${c.flag} ${c.code}") },
                modifier = Modifier.heightIn(min = 40.dp),
            )
        }
    }
}

/** A compact "🇨🇴 CO" country label for a moderation card, so a moderator sees which country a row is for. */
@Composable
fun CountryBadge(code: String?, modifier: Modifier = Modifier) {
    val country = Country.fromCode(code)
    Text(
        text = "${country.flag} ${country.code}",
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier,
    )
}
