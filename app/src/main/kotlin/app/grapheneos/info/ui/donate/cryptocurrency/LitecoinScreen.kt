package app.grapheneos.info.ui.donate.cryptocurrency

import android.content.res.Configuration
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.Wallpapers
import androidx.compose.ui.unit.dp
import app.grapheneos.info.R
import app.grapheneos.info.ui.reusablecomposables.ScreenLazyColumn

@Composable
fun LitecoinScreen(
    modifier: Modifier = Modifier,
    showSnackbarError: (String) -> Unit = {},
    additionalContentPadding: PaddingValues = PaddingValues(0.dp)
) {
    ScreenLazyColumn(
        modifier = modifier
            .fillMaxSize(),
        additionalContentPadding = additionalContentPadding
    ) {
        item {
            Text(stringResource(R.string.litecoin_info))
        }
        item {
            AddressInfoItem(
                title = "Litecoin",
                qrCodePainterResourceId = R.drawable.donate_litecoin_qr_code,
                qrCodeContentDescription = stringResource(R.string.litecoin_qr_code_description),
                addressUrl = "litecoin:ltc1qzssmqueth6zjzr95rkluy5xdx9q4lk8vyrvea9?label=GrapheneOS%20Foundation&message=Donation%20to%20GrapheneOS%20Foundation",
                address = "ltc1qzssmqueth6zjzr95rkluy5xdx9q4lk8vyrvea9",
                showSnackbarError = showSnackbarError
            )
        }
    }
}

@Preview(
    showBackground = true,
    wallpaper = Wallpapers.RED_DOMINATED_EXAMPLE,
    uiMode = Configuration.UI_MODE_NIGHT_UNDEFINED
)
@Composable
private fun LitecoinScreenPreview() {
    MaterialTheme {
        LitecoinScreen()
    }
}