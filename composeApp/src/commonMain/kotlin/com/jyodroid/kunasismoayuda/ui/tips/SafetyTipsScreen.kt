package com.jyodroid.kunasismoayuda.ui.tips

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import com.jyodroid.kunasismoayuda.resources.Res
import com.jyodroid.kunasismoayuda.resources.tip_calm
import com.jyodroid.kunasismoayuda.resources.tip_car
import com.jyodroid.kunasismoayuda.resources.tip_family
import com.jyodroid.kunasismoayuda.resources.tip_fire
import com.jyodroid.kunasismoayuda.resources.tip_home
import com.jyodroid.kunasismoayuda.resources.tip_kit
import com.jyodroid.kunasismoayuda.resources.tip_medical
import com.jyodroid.kunasismoayuda.resources.tip_message
import com.jyodroid.kunasismoayuda.resources.tip_mind
import com.jyodroid.kunasismoayuda.resources.tip_pet
import com.jyodroid.kunasismoayuda.resources.tip_phone
import com.jyodroid.kunasismoayuda.resources.tip_search
import com.jyodroid.kunasismoayuda.resources.tip_place
import com.jyodroid.kunasismoayuda.resources.tip_shield
import com.jyodroid.kunasismoayuda.resources.tip_warning
import com.jyodroid.kunasismoayuda.resources.phase_after
import com.jyodroid.kunasismoayuda.resources.phase_before
import com.jyodroid.kunasismoayuda.resources.phase_during
import com.jyodroid.kunasismoayuda.resources.phase_mental
import com.jyodroid.kunasismoayuda.resources.phase_animals
import com.jyodroid.kunasismoayuda.resources.tip_after_1_body
import com.jyodroid.kunasismoayuda.resources.tip_after_1_title
import com.jyodroid.kunasismoayuda.resources.tip_after_2_body
import com.jyodroid.kunasismoayuda.resources.tip_after_2_title
import com.jyodroid.kunasismoayuda.resources.tip_after_3_body
import com.jyodroid.kunasismoayuda.resources.tip_after_3_title
import com.jyodroid.kunasismoayuda.resources.tip_before_1_body
import com.jyodroid.kunasismoayuda.resources.tip_before_1_title
import com.jyodroid.kunasismoayuda.resources.tip_before_2_body
import com.jyodroid.kunasismoayuda.resources.tip_before_2_title
import com.jyodroid.kunasismoayuda.resources.tip_before_3_body
import com.jyodroid.kunasismoayuda.resources.tip_before_3_title
import com.jyodroid.kunasismoayuda.resources.tip_during_1_body
import com.jyodroid.kunasismoayuda.resources.tip_during_1_title
import com.jyodroid.kunasismoayuda.resources.tip_during_2_body
import com.jyodroid.kunasismoayuda.resources.tip_during_2_title
import com.jyodroid.kunasismoayuda.resources.tip_during_3_body
import com.jyodroid.kunasismoayuda.resources.tip_during_3_title
import com.jyodroid.kunasismoayuda.resources.tip_mental_line_body
import com.jyodroid.kunasismoayuda.resources.tip_mental_line_title
import com.jyodroid.kunasismoayuda.resources.tip_mental_2_body
import com.jyodroid.kunasismoayuda.resources.tip_mental_2_title
import com.jyodroid.kunasismoayuda.resources.tip_mental_3_body
import com.jyodroid.kunasismoayuda.resources.tip_mental_3_title
import com.jyodroid.kunasismoayuda.resources.tip_mental_4_body
import com.jyodroid.kunasismoayuda.resources.tip_mental_4_title
import com.jyodroid.kunasismoayuda.resources.tip_mental_5_body
import com.jyodroid.kunasismoayuda.resources.tip_mental_5_title
import com.jyodroid.kunasismoayuda.resources.tip_animals_1_body
import com.jyodroid.kunasismoayuda.resources.tip_animals_1_title
import com.jyodroid.kunasismoayuda.resources.tip_animals_2_body
import com.jyodroid.kunasismoayuda.resources.tip_animals_2_title
import com.jyodroid.kunasismoayuda.resources.tip_animals_3_body
import com.jyodroid.kunasismoayuda.resources.tip_animals_3_title
import com.jyodroid.kunasismoayuda.resources.tip_animals_4_body
import com.jyodroid.kunasismoayuda.resources.tip_animals_4_title
import com.jyodroid.kunasismoayuda.resources.tips_source_note
import com.jyodroid.kunasismoayuda.core.domain.model.Country
import com.jyodroid.kunasismoayuda.core.domain.model.CountryEmergency
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

private enum class Phase(val titleRes: StringResource) {
    BEFORE(Res.string.phase_before),
    DURING(Res.string.phase_during),
    AFTER(Res.string.phase_after),
    MENTAL(Res.string.phase_mental),
    ANIMALS(Res.string.phase_animals),
}

private data class Tip(
    val titleRes: StringResource,
    val bodyRes: StringResource,
    val phase: Phase,
    val iconRes: DrawableResource,
    // Some tips reference Colombia-specific community resources (IG accounts, local foundations); they
    // only make sense for Colombia and are hidden for other countries.
    val coOnly: Boolean = false,
)

private val tips = listOf(
    Tip(Res.string.tip_before_1_title, Res.string.tip_before_1_body, Phase.BEFORE, Res.drawable.tip_kit),
    Tip(Res.string.tip_before_2_title, Res.string.tip_before_2_body, Phase.BEFORE, Res.drawable.tip_home),
    Tip(Res.string.tip_before_3_title, Res.string.tip_before_3_body, Phase.BEFORE, Res.drawable.tip_place),
    Tip(Res.string.tip_during_1_title, Res.string.tip_during_1_body, Phase.DURING, Res.drawable.tip_shield),
    Tip(Res.string.tip_during_2_title, Res.string.tip_during_2_body, Phase.DURING, Res.drawable.tip_warning),
    Tip(Res.string.tip_during_3_title, Res.string.tip_during_3_body, Phase.DURING, Res.drawable.tip_car),
    Tip(Res.string.tip_after_1_title, Res.string.tip_after_1_body, Phase.AFTER, Res.drawable.tip_medical),
    Tip(Res.string.tip_after_2_title, Res.string.tip_after_2_body, Phase.AFTER, Res.drawable.tip_fire),
    Tip(Res.string.tip_after_3_title, Res.string.tip_after_3_body, Phase.AFTER, Res.drawable.tip_message),
    // The mental-health line itself is rendered dynamically (per-country number) ahead of these.
    Tip(Res.string.tip_mental_2_title, Res.string.tip_mental_2_body, Phase.MENTAL, Res.drawable.tip_mind),
    Tip(Res.string.tip_mental_3_title, Res.string.tip_mental_3_body, Phase.MENTAL, Res.drawable.tip_calm),
    Tip(Res.string.tip_mental_4_title, Res.string.tip_mental_4_body, Phase.MENTAL, Res.drawable.tip_family),
    Tip(Res.string.tip_mental_5_title, Res.string.tip_mental_5_body, Phase.MENTAL, Res.drawable.tip_message, coOnly = true),
    Tip(Res.string.tip_animals_1_title, Res.string.tip_animals_1_body, Phase.ANIMALS, Res.drawable.tip_pet),
    Tip(Res.string.tip_animals_2_title, Res.string.tip_animals_2_body, Phase.ANIMALS, Res.drawable.tip_kit, coOnly = true),
    Tip(Res.string.tip_animals_3_title, Res.string.tip_animals_3_body, Phase.ANIMALS, Res.drawable.tip_medical, coOnly = true),
    Tip(Res.string.tip_animals_4_title, Res.string.tip_animals_4_body, Phase.ANIMALS, Res.drawable.tip_search, coOnly = true),
)

@Composable
fun SafetyTipsScreen(country: Country, modifier: Modifier = Modifier) {
    val visibleTips = tips.filter { !it.coOnly || country == Country.COLOMBIA }
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Phase.entries.forEach { phase ->
            item(key = "header-${phase.name}") {
                Text(
                    text = stringResource(phase.titleRes),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .padding(top = 12.dp, bottom = 4.dp)
                        .semantics { heading() },
                )
            }
            // The mental-health line is per-country (verified number) — render it first in that section.
            if (phase == Phase.MENTAL) {
                item(key = "mental-line") {
                    val mh = CountryEmergency.mentalHealth(country)
                    TipCard(
                        title = stringResource(Res.string.tip_mental_line_title, mh.name),
                        body = stringResource(Res.string.tip_mental_line_body, CountryEmergency.generalNumber(country)),
                        iconRes = Res.drawable.tip_phone,
                    )
                }
            }
            items(visibleTips.filter { it.phase == phase }) { tip ->
                TipCard(
                    title = stringResource(tip.titleRes),
                    body = stringResource(tip.bodyRes),
                    iconRes = tip.iconRes,
                )
            }
        }

        item(key = "source-note") {
            Text(
                text = stringResource(Res.string.tips_source_note),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 16.dp),
            )
        }
    }
}

@Composable
private fun TipCard(title: String, body: String, iconRes: DrawableResource) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Decorative illustration — the title carries the meaning (contentDescription = null).
            Icon(
                painter = painterResource(iconRes),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(40.dp),
            )
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = body,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
