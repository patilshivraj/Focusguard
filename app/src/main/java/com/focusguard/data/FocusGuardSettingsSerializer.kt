package com.focusguard.data

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import com.focusguard.data.proto.FocusGuardSettings
import com.google.protobuf.InvalidProtocolBufferException
import java.io.InputStream
import java.io.OutputStream

object FocusGuardSettingsSerializer : Serializer<FocusGuardSettings> {
    override val defaultValue: FocusGuardSettings = FocusGuardSettings.getDefaultInstance()

    override suspend fun readFrom(input: InputStream): FocusGuardSettings {
        try {
            return FocusGuardSettings.parseFrom(input)
        } catch (e: InvalidProtocolBufferException) {
            throw CorruptionException("Cannot read proto.", e)
        }
    }

    override suspend fun writeTo(t: FocusGuardSettings, output: OutputStream) {
        t.writeTo(output)
    }
}
