package com.sendbird.uikit.internal.model.serializer

import com.sendbird.uikit.consts.ReplyType
import com.sendbird.uikit.consts.SuggestedRepliesDirection
import com.sendbird.uikit.consts.SuggestedRepliesFor
import com.sendbird.uikit.consts.ThreadReplySelectType
import com.sendbird.uikit.internal.model.notifications.CSVColor
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

internal object CSVColorIntAsStringSerializer : KSerializer<CSVColor> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("CSVColor class", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): CSVColor {
        val decoded = decoder.decodeString()
        // Logger.i("deserialize hex=$decoded")
        return CSVColor(decoded)
    }

    override fun serialize(encoder: Encoder, value: CSVColor) {
        // Logger.i("serialize hex=$hex")
        encoder.encodeString(value.getColorHexString())
    }
}

internal object ReplyTypeAsStringSerializer : KSerializer<ReplyType> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("ReplyType enum class", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): ReplyType {
        val decoded = decoder.decodeString()
        return ReplyType.from(decoded)
    }

    override fun serialize(encoder: Encoder, value: ReplyType) {
        encoder.encodeString(value.value)
    }
}

internal object SuggestedRepliesForAsStringSerializer : KSerializer<SuggestedRepliesFor> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("SuggestedRepliesFor enum class", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): SuggestedRepliesFor {
        val decoded = decoder.decodeString()
        return SuggestedRepliesFor.from(decoded)
    }

    override fun serialize(encoder: Encoder, value: SuggestedRepliesFor) {
        encoder.encodeString(value.value)
    }
}

internal object SuggestedRepliesDirectionAsStringSerializer : KSerializer<SuggestedRepliesDirection> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("SuggestedRepliesDirection enum class", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): SuggestedRepliesDirection {
        val decoded = decoder.decodeString()
        return SuggestedRepliesDirection.from(decoded)
    }

    override fun serialize(encoder: Encoder, value: SuggestedRepliesDirection) {
        encoder.encodeString(value.value)
    }
}

internal object ThreadReplySelectTypeAsStringSerializer : KSerializer<ThreadReplySelectType> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Thread reply select type enum class", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): ThreadReplySelectType {
        val decoded = decoder.decodeString()
        return ThreadReplySelectType.from(decoded)
    }

    override fun serialize(encoder: Encoder, value: ThreadReplySelectType) {
        encoder.encodeString(value.value)
    }
}
