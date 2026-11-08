// ch05-compose-basics/shared/src/commonMain/.../ui/DonationForm.kt
package seedpackets.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import seedpackets.resources.Res
import seedpackets.resources.donate
import seedpackets.resources.grams_error
import seedpackets.resources.grams_label

/** The rule, as a plain function: 1 to 500 grams. */
fun isValidGrams(text: String): Boolean = text.toIntOrNull() in 1..500

/** Stateless: shows [value], reports edits, flags invalid input. */
@Composable
fun GramsField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val showError = value.isNotEmpty() && !isValidGrams(value)
    OutlinedTextField(
        value = value,
        onValueChange = { text -> onValueChange(text.filter(Char::isDigit)) },
        label = { Text(stringResource(Res.string.grams_label)) },
        isError = showError,
        supportingText = {
            if (showError) Text(stringResource(Res.string.grams_error))
        },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true,
        modifier = modifier.testTag("grams"),
    )
}

/** Stateful owner: keeps the text, enables Donate only when it is valid. */
@Composable
fun DonationForm(
    onDonate: (grams: Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    var grams by rememberSaveable { mutableStateOf("") }
    Column(modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        GramsField(grams, onValueChange = { grams = it })
        Button(
            onClick = { onDonate(grams.toInt()) },
            enabled = isValidGrams(grams),
        ) { Text(stringResource(Res.string.donate)) }
    }
}
