package com.jyodroid.kunasismoayuda.ui.tips

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import com.jyodroid.kunasismoayuda.resources.Res
import com.jyodroid.kunasismoayuda.resources.ic_sound
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
import com.jyodroid.kunasismoayuda.resources.tip_listen
import com.jyodroid.kunasismoayuda.resources.tip_listen_a11y
import com.jyodroid.kunasismoayuda.resources.tip_open_details_a11y
import com.jyodroid.kunasismoayuda.resources.tip_story_heading
import com.jyodroid.kunasismoayuda.resources.story_after_1
import com.jyodroid.kunasismoayuda.resources.story_after_2
import com.jyodroid.kunasismoayuda.resources.story_after_3
import com.jyodroid.kunasismoayuda.resources.story_before_1
import com.jyodroid.kunasismoayuda.resources.story_before_2
import com.jyodroid.kunasismoayuda.resources.story_before_3
import com.jyodroid.kunasismoayuda.resources.story_during_1
import com.jyodroid.kunasismoayuda.resources.story_during_2
import com.jyodroid.kunasismoayuda.resources.story_during_3
import com.jyodroid.kunasismoayuda.resources.story_pets_1
import com.jyodroid.kunasismoayuda.resources.story_pets_2
import com.jyodroid.kunasismoayuda.resources.story_pets_3
import com.jyodroid.kunasismoayuda.resources.story_calm_1
import com.jyodroid.kunasismoayuda.resources.story_calm_2
import com.jyodroid.kunasismoayuda.resources.story_calm_3
import com.jyodroid.kunasismoayuda.resources.story_children_1
import com.jyodroid.kunasismoayuda.resources.story_children_2
import com.jyodroid.kunasismoayuda.resources.story_children_3
import com.jyodroid.kunasismoayuda.core.domain.model.Country
import com.jyodroid.kunasismoayuda.core.domain.model.CountryEmergency
import com.jyodroid.kunasismoayuda.core.domain.util.tipSpeechText
import com.jyodroid.kunasismoayuda.ui.platform.Speaker
import com.jyodroid.kunasismoayuda.ui.platform.rememberSpeaker
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

private enum class Phase(val titleRes: StringResource) {
    BEFORE(Res.string.phase_before),
    DURING(Res.string.phase_during),
    AFTER(Res.string.phase_after),
    MENTAL(Res.string.phase_mental),
    ANIMALS(Res.string.phase_animals),
}

/** One panel of a tip's wordless step-by-step storyboard: a pictogram + a short localized caption. */
private data class StoryStep(val iconRes: DrawableResource, val captionRes: StringResource)

private data class Tip(
    val titleRes: StringResource,
    val bodyRes: StringResource,
    val phase: Phase,
    val iconRes: DrawableResource,
    // Some tips reference Colombia-specific community resources (IG accounts, local foundations); they
    // only make sense for Colombia and are hidden for other countries.
    val coOnly: Boolean = false,
    // Optional pictogram sequence shown when the tip is opened — for readers who can't read the text.
    val steps: List<StoryStep> = emptyList(),
)

// Storyboards for the highest-value life-safety tips: wordless UN-OCHA (public-domain) pictograms with
// short localized captions (see composeResources/drawable/STORYBOARD_ASSETS.md for provenance).
private val duringQuakeSteps = listOf(
    StoryStep(Res.drawable.story_during_1, Res.string.story_during_1),
    StoryStep(Res.drawable.story_during_2, Res.string.story_during_2),
    StoryStep(Res.drawable.story_during_3, Res.string.story_during_3),
)
private val beforeQuakeSteps = listOf(
    StoryStep(Res.drawable.story_before_1, Res.string.story_before_1),
    StoryStep(Res.drawable.story_before_2, Res.string.story_before_2),
    StoryStep(Res.drawable.story_before_3, Res.string.story_before_3),
)
private val afterQuakeSteps = listOf(
    StoryStep(Res.drawable.story_after_1, Res.string.story_after_1),
    StoryStep(Res.drawable.story_after_2, Res.string.story_after_2),
    StoryStep(Res.drawable.story_after_3, Res.string.story_after_3),
)
private val petsQuakeSteps = listOf(
    StoryStep(Res.drawable.story_pets_1, Res.string.story_pets_1),
    StoryStep(Res.drawable.story_pets_2, Res.string.story_pets_2),
    StoryStep(Res.drawable.story_pets_3, Res.string.story_pets_3),
)
private val calmingSteps = listOf(
    StoryStep(Res.drawable.story_calm_1, Res.string.story_calm_1),
    StoryStep(Res.drawable.story_calm_2, Res.string.story_calm_2),
    StoryStep(Res.drawable.story_calm_3, Res.string.story_calm_3),
)
private val supportingChildrenSteps = listOf(
    StoryStep(Res.drawable.story_children_1, Res.string.story_children_1),
    StoryStep(Res.drawable.story_children_2, Res.string.story_children_2),
    StoryStep(Res.drawable.story_children_3, Res.string.story_children_3),
)

private val tips = listOf(
    Tip(Res.string.tip_before_1_title, Res.string.tip_before_1_body, Phase.BEFORE, Res.drawable.tip_kit, steps = beforeQuakeSteps),
    Tip(Res.string.tip_before_2_title, Res.string.tip_before_2_body, Phase.BEFORE, Res.drawable.tip_home),
    Tip(Res.string.tip_before_3_title, Res.string.tip_before_3_body, Phase.BEFORE, Res.drawable.tip_place),
    Tip(Res.string.tip_during_1_title, Res.string.tip_during_1_body, Phase.DURING, Res.drawable.tip_shield, steps = duringQuakeSteps),
    Tip(Res.string.tip_during_2_title, Res.string.tip_during_2_body, Phase.DURING, Res.drawable.tip_warning),
    Tip(Res.string.tip_during_3_title, Res.string.tip_during_3_body, Phase.DURING, Res.drawable.tip_car),
    Tip(Res.string.tip_after_1_title, Res.string.tip_after_1_body, Phase.AFTER, Res.drawable.tip_medical, steps = afterQuakeSteps),
    Tip(Res.string.tip_after_2_title, Res.string.tip_after_2_body, Phase.AFTER, Res.drawable.tip_fire),
    Tip(Res.string.tip_after_3_title, Res.string.tip_after_3_body, Phase.AFTER, Res.drawable.tip_message),
    // The mental-health line itself is rendered dynamically (per-country number) ahead of these.
    Tip(Res.string.tip_mental_2_title, Res.string.tip_mental_2_body, Phase.MENTAL, Res.drawable.tip_mind),
    Tip(Res.string.tip_mental_3_title, Res.string.tip_mental_3_body, Phase.MENTAL, Res.drawable.tip_calm, steps = calmingSteps),
    Tip(Res.string.tip_mental_4_title, Res.string.tip_mental_4_body, Phase.MENTAL, Res.drawable.tip_family, steps = supportingChildrenSteps),
    Tip(Res.string.tip_mental_5_title, Res.string.tip_mental_5_body, Phase.MENTAL, Res.drawable.tip_message, coOnly = true),
    Tip(Res.string.tip_animals_1_title, Res.string.tip_animals_1_body, Phase.ANIMALS, Res.drawable.tip_pet, steps = petsQuakeSteps),
    Tip(Res.string.tip_animals_2_title, Res.string.tip_animals_2_body, Phase.ANIMALS, Res.drawable.tip_kit, coOnly = true),
    Tip(Res.string.tip_animals_3_title, Res.string.tip_animals_3_body, Phase.ANIMALS, Res.drawable.tip_medical, coOnly = true),
    Tip(Res.string.tip_animals_4_title, Res.string.tip_animals_4_body, Phase.ANIMALS, Res.drawable.tip_search, coOnly = true),
)

/** A tip with its strings already resolved, ready for the detail sheet. */
private data class TipUi(
    val title: String,
    val body: String,
    val iconRes: DrawableResource,
    val steps: List<StepUi>,
)

private data class StepUi(val iconRes: DrawableResource, val caption: String)

@Composable
fun SafetyTipsScreen(country: Country, modifier: Modifier = Modifier) {
    val visibleTips = tips.filter { !it.coOnly || country == Country.COLOMBIA }
    val speaker = rememberSpeaker()
    var selected by remember { mutableStateOf<TipUi?>(null) }

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
                    val title = stringResource(Res.string.tip_mental_line_title, mh.name)
                    val body = stringResource(Res.string.tip_mental_line_body, CountryEmergency.generalNumber(country))
                    TipCard(
                        title = title,
                        body = body,
                        iconRes = Res.drawable.tip_phone,
                        speaker = speaker,
                        onOpen = { selected = TipUi(title, body, Res.drawable.tip_phone, emptyList()) },
                    )
                }
            }
            items(visibleTips.filter { it.phase == phase }) { tip ->
                val title = stringResource(tip.titleRes)
                val body = stringResource(tip.bodyRes)
                val steps = tip.steps.map { StepUi(it.iconRes, stringResource(it.captionRes)) }
                TipCard(
                    title = title,
                    body = body,
                    iconRes = tip.iconRes,
                    speaker = speaker,
                    onOpen = { selected = TipUi(title, body, tip.iconRes, steps) },
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

    selected?.let { tip ->
        TipDetailSheet(tip = tip, speaker = speaker, onDismiss = { selected = null })
    }
}

@Composable
private fun TipCard(
    title: String,
    body: String,
    iconRes: DrawableResource,
    speaker: Speaker,
    onOpen: () -> Unit,
) {
    val openLabel = stringResource(Res.string.tip_open_details_a11y)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClickLabel = openLabel) { onOpen() },
    ) {
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
            Column(modifier = Modifier.weight(1f)) {
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
            if (speaker.isAvailable) {
                IconButton(
                    onClick = { speaker.speak(tipSpeechText(title, body)) },
                    modifier = Modifier.size(48.dp),
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_sound),
                        contentDescription = stringResource(Res.string.tip_listen_a11y),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TipDetailSheet(tip: TipUi, speaker: Speaker, onDismiss: () -> Unit) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = tip.title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.semantics { heading() },
            )
            Text(
                text = tip.body,
                style = MaterialTheme.typography.bodyLarge,
            )

            if (tip.steps.isNotEmpty()) {
                Text(
                    text = stringResource(Res.string.tip_story_heading),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .semantics { heading() },
                )
                tip.steps.forEachIndexed { index, step ->
                    StoryStepRow(number = index + 1, step = step)
                }
            }

            if (speaker.isAvailable) {
                Button(
                    onClick = { speaker.speak(tipSpeechText(tip.title, tip.body)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp)
                        .padding(top = 8.dp),
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_sound),
                        contentDescription = null,
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(Res.string.tip_listen))
                }
            }
        }
    }
}

@Composable
private fun StoryStepRow(number: Int, step: StepUi) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
        ) {
            Text(
                text = number.toString(),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }
        Image(
            // The caption conveys the step, so it is the illustration's accessible description.
            painter = painterResource(step.iconRes),
            contentDescription = step.caption,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .size(88.dp)
                .clip(RoundedCornerShape(12.dp)),
        )
        Text(
            text = step.caption,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f),
        )
    }
}
