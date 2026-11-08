package seedpackets.solutions

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import seedpackets.model.Family
import seedpackets.resources.Res
import seedpackets.resources.family_greens
import seedpackets.resources.family_legumes
import seedpackets.resources.family_nightshades
import seedpackets.resources.family_squashes

/** Exercise 5.1: a new family breaks this exhaustive `when` at compile time. */
val Family.label: StringResource
    get() = when (this) {
        Family.LEGUMES -> Res.string.family_legumes
        Family.NIGHTSHADES -> Res.string.family_nightshades
        Family.SQUASHES -> Res.string.family_squashes
        Family.GREENS -> Res.string.family_greens
    }

@Composable
fun FamilyTag(family: Family, modifier: Modifier = Modifier) {
    Text(
        text = stringResource(family.label),
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.tertiary,
        modifier = modifier,
    )
}
