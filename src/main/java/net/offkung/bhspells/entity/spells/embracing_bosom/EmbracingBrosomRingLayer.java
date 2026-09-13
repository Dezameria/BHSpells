package net.offkung.bhspells.entity.spells.embracing_bosom;

import net.minecraft.resources.ResourceLocation;
import net.offkung.bhspells.BHSpells;

public record EmbracingBrosomRingLayer(ResourceLocation texture, float radius, float innerRadius, float rotationSpeed, float startAngleJitter, float alpha, Integer tintOverrideRGB, float yOffset) {
    public EmbracingBrosomRingLayer(ResourceLocation texture, float radius, float innerRadius, float rotationSpeed, float startAngleJitter, float alpha, float yOffset) {
        this(texture, radius, innerRadius, rotationSpeed, startAngleJitter, alpha, null, yOffset);
    }

    public int resolveTint(int defaultTintRGB) {
        return tintOverrideRGB != null ? tintOverrideRGB : defaultTintRGB;
    }
}
