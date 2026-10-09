package com.focusguard.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.focusguard.R

@Composable
fun FocusGuardLogo(
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    contentDescription: String = "FocusGuard",
) {
    Image(
        painter = painterResource(R.drawable.ic_brand_logo),
        contentDescription = contentDescription,
        modifier = modifier.size(size),
    )
}
