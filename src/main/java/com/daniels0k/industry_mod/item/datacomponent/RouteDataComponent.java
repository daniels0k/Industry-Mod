package com.daniels0k.industry_mod.item.datacomponent;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.List;

public record RouteDataComponent(List<WireDataComponent> connections, String cableType, int distanceMax,
                                 float efficiency, float lossFactor) {
    public static final Codec<RouteDataComponent> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            WireDataComponent.CODEC.listOf().fieldOf("connections").forGetter(RouteDataComponent::connections),
            Codec.STRING.fieldOf("cableType").forGetter(RouteDataComponent::cableType),
            Codec.INT.fieldOf("distanceMax").forGetter(RouteDataComponent::distanceMax),
            Codec.FLOAT.fieldOf("efficiency").forGetter(RouteDataComponent::efficiency),
            Codec.FLOAT.fieldOf("lossFactor").forGetter(RouteDataComponent::lossFactor)
            ).apply(instance, RouteDataComponent::new));

    public static final StreamCodec<FriendlyByteBuf, RouteDataComponent> STREAM_CODEC = StreamCodec.composite(
            WireDataComponent.STREAM_CODEC.apply(ByteBufCodecs.list()), RouteDataComponent::connections,
            ByteBufCodecs.STRING_UTF8, RouteDataComponent::cableType,
            ByteBufCodecs.INT, RouteDataComponent::distanceMax,
            ByteBufCodecs.FLOAT, RouteDataComponent::efficiency,
            ByteBufCodecs.FLOAT, RouteDataComponent::lossFactor,
            RouteDataComponent::new
    );
}
