package com.tailbraille.formationsdatap.mixin;
import com.supermartijn642.formations.structure.processors.BiomeReplacementProcessor;
import com.mojang.datafixers.util.Pair;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.List;

@Mixin(BiomeReplacementProcessor.class)
public interface BiomeReplacementProcessorInvoker {

    @Invoker("addReplacements")
    static void callAddReplacements(Pair<Block, List<ResourceKey<Biome>>>... entries) {
        throw new AssertionError(); // Mixin will overwrite this
    }
}