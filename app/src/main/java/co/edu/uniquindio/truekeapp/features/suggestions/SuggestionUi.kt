package co.edu.uniquindio.truekeapp.features.suggestions

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import co.edu.uniquindio.truekeapp.R
import co.edu.uniquindio.truekeapp.core.component.labelRes
import co.edu.uniquindio.truekeapp.domain.service.Suggestion
import co.edu.uniquindio.truekeapp.domain.service.SuggestionReason

// Texto del motivo de la recomendación: "Por tu interés en Libros", "Cerca de ti"...
@Composable
fun reasonText(suggestion: Suggestion): String = when (suggestion.reason) {
    SuggestionReason.CATEGORY_INTEREST ->
        stringResource(R.string.reason_category, stringResource(suggestion.publication.category.labelRes))
    SuggestionReason.POPULAR -> stringResource(R.string.reason_popular)
    SuggestionReason.NEARBY -> stringResource(R.string.reason_nearby)
    SuggestionReason.NEW -> stringResource(R.string.reason_new)
}
