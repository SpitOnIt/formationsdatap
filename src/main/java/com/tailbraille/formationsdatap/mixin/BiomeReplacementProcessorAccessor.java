package com.tailbraille.formationsdatap.mixin;
import com.supermartijn642.formations.structure.processors.BiomeReplacementProcessor;

import net.minecraft.world.level.block.Block;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Set;


@Mixin(BiomeReplacementProcessor.class)
public interface BiomeReplacementProcessorAccessor {
    @Accessor("REPLACEABLE_BLOCKS")
    static Set<Block> getReplaceableBlocks() {
        throw new AssertionError();
    }
}