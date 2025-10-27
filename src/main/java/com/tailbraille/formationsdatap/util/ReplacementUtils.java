package com.tailbraille.formationsdatap.util;

import com.tailbraille.formationsdatap.mixin.BiomeReplacementProcessorAccessor;
import net.minecraft.world.level.block.Block;

public class ReplacementUtils {
    public static void addReplaceable(Block block) {
        BiomeReplacementProcessorAccessor.getReplaceableBlocks().add(block);
    }
}
