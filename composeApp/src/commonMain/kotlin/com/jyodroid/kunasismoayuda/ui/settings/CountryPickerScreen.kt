package com.jyodroid.kunasismoayuda.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.jyodroid.kunasismoayuda.core.domain.model.Country
import com.jyodroid.kunasismoayuda.resources.Res
import com.jyodroid.kunasismoayuda.resources.country_colombia
import com.jyodroid.kunasismoayuda.resources.country_indonesia
import com.jyodroid.kunasismoayuda.resources.country_italy
import com.jyodroid.kunasismoayuda.resources.country_peru
import com.jyodroid.kunasismoayuda.resources.country_picker_subtitle
import com.jyodroid.kunasismoayuda.resources.country_picker_title
import com.jyodroid.kunasismoayuda.resources.country_spain
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

/**
 * First-run country chooser. Shown until a country is stored; also reachable from the Overview
 * switcher. Accessible: the title is a heading and each option is a ≥56dp touch target labelled with
 * its (localized) country name — the flag emoji is decorative, so meaning never rides on colour.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CountryPickerScreen(
    onSelect: (Country) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            // The picker is shown outside the app Scaffold, so it must inset itself past the status
            // bar / Dynamic Island (and the bottom home indicator) or the title collides with the clock.
            .safeDrawingPadding()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = stringResource(Res.string.country_picker_title),
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.semantics { heading() },
        )
        Text(
            text = stringResource(Res.string.country_picker_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(8.dp))
        Country.entries.forEach { country ->
            CountryOption(
                flag = country.flag,
                label = stringResource(country.labelRes()),
                onClick = { onSelect(country) },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CountryOption(
    flag: String,
    label: String,
    onClick: () -> Unit,
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
    ) {
        androidx.compose.foundation.layout.Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text = flag, style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.width(16.dp))
            Text(text = label, style = MaterialTheme.typography.titleMedium)
        }
    }
}

private fun Country.labelRes(): StringResource = when (this) {
    Country.COLOMBIA -> Res.string.country_colombia
    Country.INDONESIA -> Res.string.country_indonesia
    Country.SPAIN -> Res.string.country_spain
    Country.ITALY -> Res.string.country_italy
    Country.PERU -> Res.string.country_peru
}
