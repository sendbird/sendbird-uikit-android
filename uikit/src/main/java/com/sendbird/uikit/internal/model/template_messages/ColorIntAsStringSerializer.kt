package com.sendbird.uikit.internal.model.template_messages

import android.graphics.Color
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

internal object ColorIntAsStringSerializer : KSerializer<Int> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("ColorInt", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): Int {
        val decoded = decoder.decodeString()
        // Logger.i("deserialize hex=$decoded")
        return Color.parseColor(decoded)
    }

    override fun serialize(encoder: Encoder, value: Int) {
        val hex = String.format("#%08X", 0xFFFFFFFF and value.toLong())
        // Logger.i("serialize hex=$hex")
        encoder.encodeString(hex)
    }
}
