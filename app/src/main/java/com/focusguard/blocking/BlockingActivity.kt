package com.focusguard.blocking

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import com.focusguard.ui.components.FocusGuardLogo
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import com.focusguard.ui.theme.FocusGuardTheme
import com.focusguard.util.PackageLabelResolver
import com.focusguard.util.TimeFormatting
import java.time.Instant

class BlockingActivity : ComponentActivity() {
    private var blockedPackage by mutableStateOf("")
    private var availableAgainAtEpochMillis by mutableStateOf<Long?>(null)
    private var triggerSource by mutableStateOf("")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        }
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        readIntent(intent)

        setContent {
            FocusGuardTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    BlockingScreen(
                        packageName = blockedPackage,
                        availableAgainAtEpochMillis = availableAgainAtEpochMillis,
                        onClose = { finishAndGoHome() },
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        readIntent(intent)
    }

    private fun readIntent(intent: Intent) {
        blockedPackage = intent.getStringExtra(EXTRA_BLOCKED_PACKAGE) ?: ""
        availableAgainAtEpochMillis = intent.getLongExtra(EXTRA_AVAILABLE_AGAIN_AT, -1L).let {
            if (it < 0) null else it
        }
        triggerSource = intent.getStringExtra(EXTRA_TRIGGER_SOURCE) ?: ""
    }

    private fun finishAndGoHome() {
        val home = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(home)
        finish()
    }

    companion object {
        const val EXTRA_BLOCKED_PACKAGE = "blocked_package"
        const val EXTRA_AVAILABLE_AGAIN_AT = "available_again_at"
        const val EXTRA_TRIGGER_SOURCE = "trigger_source"
        const val EXTRA_DETECTION_DELAY_MS = "detection_delay_ms"

        fun intent(
            context: Context,
            blockedPackage: String,
            availableAgainAtEpochMillis: Long?,
            triggerSource: String,
            detectionDelayMs: Long,
        ): Intent =
            Intent(context, BlockingActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP or
                    Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                putExtra(EXTRA_BLOCKED_PACKAGE, blockedPackage)
                if (availableAgainAtEpochMillis != null) {
                    putExtra(EXTRA_AVAILABLE_AGAIN_AT, availableAgainAtEpochMillis)
                }
                putExtra(EXTRA_TRIGGER_SOURCE, triggerSource)
                putExtra(EXTRA_DETECTION_DELAY_MS, detectionDelayMs)
            }
    }
}

@androidx.compose.runtime.Composable
private fun BlockingScreen(
    packageName: String,
    availableAgainAtEpochMillis: Long?,
    onClose: () -> Unit,
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val label = PackageLabelResolver.label(context, packageName)
    val appIconBitmap = try {
        context.packageManager.getApplicationIcon(packageName).toBitmap(96, 96).asImageBitmap()
    } catch (_: Exception) {
        null
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        FocusGuardLogo(size = 64.dp)
        Spacer(Modifier.height(12.dp))
        Text("FocusGuard", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(20.dp))
        if (appIconBitmap != null) {
            Image(
                bitmap = appIconBitmap,
                contentDescription = null,
                modifier = Modifier.size(72.dp),
            )
        }
        Spacer(Modifier.height(16.dp))
        Text("App Blocked", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(8.dp))
        Text(
            "$label is blocked.",
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyLarge,
        )
        if (availableAgainAtEpochMillis != null) {
            Spacer(Modifier.height(16.dp))
            Text("Available again at", style = MaterialTheme.typography.bodyMedium)
            Text(
                TimeFormatting.formatInstant(Instant.ofEpochMilli(availableAgainAtEpochMillis)),
                style = MaterialTheme.typography.titleMedium,
            )
        }
        Spacer(Modifier.height(32.dp))
        Button(onClick = onClose) {
            Text("Close")
        }
    }
}
