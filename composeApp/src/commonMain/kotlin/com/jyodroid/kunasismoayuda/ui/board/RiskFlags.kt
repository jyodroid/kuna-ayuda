package com.jyodroid.kunasismoayuda.ui.board

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.jyodroid.kunasismoayuda.resources.Res
import com.jyodroid.kunasismoayuda.resources.risk_flag_claim
import com.jyodroid.kunasismoayuda.resources.risk_flag_money
import com.jyodroid.kunasismoayuda.resources.risk_flag_source
import com.jyodroid.kunasismoayuda.resources.risk_flags_title
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

/** Maps an AI risk-flag code to a localized label; null for an unknown code (skipped). */
fun riskFlagLabelRes(code: String): StringResource? = when (code.uppercase()) {
    "ASKS_FOR_MONEY" -> Res.string.risk_flag_money
    "UNVERIFIED_CLAIM" -> Res.string.risk_flag_claim
    "NO_SOURCE" -> Res.string.risk_flag_source
    else -> null
}

/**
 * Moderator-facing caution block for the AI-extracted risk flags (asks-for-money / unverified /
 * no-source). A signal only — never a verdict; the meaning is in the heading + labels (not colour alone).
 */
@Composable
fun RiskFlagsBlock(flags: List<String>, modifier: Modifier = Modifier) {
    val labels = flags.mapNotNull { riskFlagLabelRes(it) }
    if (labels.isEmpty()) return
    Surface(
        color = MaterialTheme.colorScheme.errorContainer,
        shape = MaterialTheme.shapes.small,
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(8.dp)) {
            Text(
                text = stringResource(Res.string.risk_flags_title),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onErrorContainer,
            )
            labels.forEach { res ->
                Text(
                    text = "• ${stringResource(res)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                )
            }
        }
    }
}
