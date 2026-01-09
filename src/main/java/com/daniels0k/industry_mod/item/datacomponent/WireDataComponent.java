package com.daniels0k.industry_mod.item.datacomponent;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.Optional;

public record WireDataComponent(Optional<BlockPos> pointA, Optional<BlockPos> pointB) {
    public static final Codec<WireDataComponent> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BlockPos.CODEC.optionalFieldOf("pointA").forGetter(WireDataComponent::pointA),
            BlockPos.CODEC.optionalFieldOf("pointB").forGetter(WireDataComponent::pointB)
    ).apply(instance, WireDataComponent::new));

    public static final StreamCodec<FriendlyByteBuf, WireDataComponent> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.optional(BlockPos.STREAM_CODEC), WireDataComponent::pointA,
            ByteBufCodecs.optional(BlockPos.STREAM_CODEC), WireDataComponent::pointB,
            WireDataComponent::new
    );
}
