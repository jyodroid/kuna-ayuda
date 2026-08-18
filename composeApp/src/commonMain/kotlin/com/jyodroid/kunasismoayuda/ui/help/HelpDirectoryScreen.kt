package com.jyodroid.kunasismoayuda.ui.help

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.heightIn
import com.jyodroid.kunasismoayuda.ui.platform.rememberPhoneCaller
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.jyodroid.kunasismoayuda.core.domain.model.Country
import com.jyodroid.kunasismoayuda.core.domain.model.CountryEmergency
import com.jyodroid.kunasismoayuda.core.domain.model.EmergencyCategory
import com.jyodroid.kunasismoayuda.core.domain.model.EmergencyContact
import com.jyodroid.kunasismoayuda.resources.Res
import com.jyodroid.kunasismoayuda.resources.emc_civil_defense
import com.jyodroid.kunasismoayuda.resources.emc_disaster
import com.jyodroid.kunasismoayuda.resources.emc_fire
import com.jyodroid.kunasismoayuda.resources.emc_general
import com.jyodroid.kunasismoayuda.resources.emc_medical
import com.jyodroid.kunasismoayuda.resources.emc_mental_health
import com.jyodroid.kunasismoayuda.resources.emc_police
import com.jyodroid.kunasismoayuda.resources.emc_red_cross
import com.jyodroid.kunasismoayuda.resources.emc_sar
import com.jyodroid.kunasismoayuda.resources.help_call
import com.jyodroid.kunasismoayuda.resources.help_emergency_banner
import com.jyodroid.kunasismoayuda.resources.help_what_to_say
import com.jyodroid.kunasismoayuda.resources.help_what_to_say_title
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

/**
 * The official emergency directory for the selected [country]. Numbers/agencies come from
 * `CountryEmergency` (verified per country); the category gives a localized label, the agency name is
 * a proper noun, and each row dials its number via `tel:`.
 */
@Composable
fun HelpDirectoryScreen(country: Country, modifier: Modifier = Modifier) {
    val caller = rememberPhoneCaller()
    val contacts = CountryEmergency.contacts(country)
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                ),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = stringResource(Res.string.help_emergency_banner, CountryEmergency.generalNumber(country)),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.padding(16.dp),
                )
            }
        }

        items(contacts) { contact ->
            ChannelCard(
                contact = contact,
                onCall = { caller.call(contact.phone) },
            )
        }

        item {
            Column(Modifier.padding(top = 8.dp)) {
                Text(
                    text = stringResource(Res.string.help_what_to_say_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.semantics { heading() },
                )
                Text(
                    text = stringResource(Res.string.help_what_to_say),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun ChannelCard(contact: EmergencyContact, onCall: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = stringResource(contact.category.labelRes()),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = contact.name,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Button(onClick = onCall, modifier = Modifier.heightIn(min = 48.dp)) {
                Text("${stringResource(Res.string.help_call)}  ${contact.phone}")
            }
        }
    }
}

private fun EmergencyCategory.labelRes(): StringResource = when (this) {
    EmergencyCategory.GENERAL -> Res.string.emc_general
    EmergencyCategory.POLICE -> Res.string.emc_police
    EmergencyCategory.FIRE -> Res.string.emc_fire
    EmergencyCategory.MEDICAL -> Res.string.emc_medical
    EmergencyCategory.RED_CROSS -> Res.string.emc_red_cross
    EmergencyCategory.CIVIL_DEFENSE -> Res.string.emc_civil_defense
    EmergencyCategory.SAR -> Res.string.emc_sar
    EmergencyCategory.DISASTER -> Res.string.emc_disaster
    EmergencyCategory.MENTAL_HEALTH -> Res.string.emc_mental_health
}
