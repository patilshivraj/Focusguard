package com.focusguard.data

import android.content.Context
import com.focusguard.data.proto.FocusGuardSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64

class PinRepository(private val context: Context) {
    val pinEnabled: Flow<Boolean> = context.focusGuardDataStore.data.map { it.pinEnabled }

    /** True when a PIN hash/salt pair is stored (toggle can be turned on). */
    val hasPin: Flow<Boolean> = context.focusGuardDataStore.data.map { hasConfiguredPin(it) }

    /** True only when PIN is enabled and a PIN is actually configured — use this for gating. */
    val pinRequired: Flow<Boolean> = context.focusGuardDataStore.data.map {
        it.pinEnabled && hasConfiguredPin(it)
    }

    /**
     * Enable/disable PIN requirement.
     * Returns false if enabling is requested but no PIN has been set yet.
     */
    suspend fun setPinEnabled(enabled: Boolean): Boolean {
        if (enabled) {
            val settings = context.focusGuardDataStore.data.first()
            if (!hasConfiguredPin(settings)) return false
        }
        context.focusGuardDataStore.updateData { it.toBuilder().setPinEnabled(enabled).build() }
        return true
    }

    suspend fun setPin(pin: String): Boolean {
        val trimmed = pin.trim()
        if (trimmed.isEmpty()) return false
        val salt = generateSalt()
        val hash = hashPin(trimmed, salt)
        context.focusGuardDataStore.updateData {
            it.toBuilder()
                .setPinEnabled(true)
                .setPinSalt(salt)
                .setPinHash(hash)
                .build()
        }
        return true
    }

    suspend fun clearPin() {
        context.focusGuardDataStore.updateData {
            it.toBuilder()
                .setPinEnabled(false)
                .setPinSalt("")
                .setPinHash("")
                .build()
        }
    }

    /** Clears orphaned "enabled" flag when no PIN hash exists (recovery from lockout). */
    suspend fun clearOrphanedPinEnabled() {
        context.focusGuardDataStore.updateData {
            if (it.pinEnabled && !hasConfiguredPin(it)) {
                it.toBuilder().setPinEnabled(false).build()
            } else {
                it
            }
        }
    }

    suspend fun verifyPin(pin: String): Boolean {
        val settings = context.focusGuardDataStore.data.first()
        // No effective PIN requirement — allow through (also recovers lockout with empty hash).
        if (!settings.pinEnabled || !hasConfiguredPin(settings)) return true
        return hashPin(pin, settings.pinSalt) == settings.pinHash
    }

    private fun hasConfiguredPin(settings: FocusGuardSettings): Boolean =
        settings.pinHash.isNotBlank() && settings.pinSalt.isNotBlank()

    private fun generateSalt(): String {
        val bytes = ByteArray(16)
        SecureRandom().nextBytes(bytes)
        return Base64.getEncoder().encodeToString(bytes)
    }

    private fun hashPin(pin: String, salt: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val bytes = digest.digest("$salt:$pin".toByteArray())
        return Base64.getEncoder().encodeToString(bytes)
    }
}
