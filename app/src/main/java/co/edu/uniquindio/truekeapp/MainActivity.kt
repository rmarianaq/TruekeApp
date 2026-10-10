package co.edu.uniquindio.truekeapp

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import co.edu.uniquindio.truekeapp.core.theme.TruekeAppTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TruekeAppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    ThemeShowcase(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

// Pantalla TEMPORAL para revisar el tema. Se elimina en el Paso 6.
@Composable
fun ThemeShowcase(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = "Trueke",
            style = MaterialTheme.typography.displayMedium,
            color = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = "Intercambia lo que ya no usas por lo que necesitas.",
            style = MaterialTheme.typography.bodyLarge,
        )
        Button(onClick = {}, modifier = Modifier.fillMaxWidth()) {
            Text("Empezar")
        }
        OutlinedButton(onClick = {}, modifier = Modifier.fillMaxWidth()) {
            Text("Ya tengo cuenta")
        }
        Card(shape = MaterialTheme.shapes.large, modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Así se ve una tarjeta del feed",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(20.dp),
            )
        }
    }
}

@Preview(name = "Claro", showBackground = true)
@Preview(name = "Oscuro", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun ThemeShowcasePreview() {
    TruekeAppTheme {
        ThemeShowcase()
    }
}