package com.tailbraille.formationsdatap.mixin;

import com.mojang.logging.LogUtils;
import com.supermartijn642.formations.structure.processors.FormationsBlockAgeProcessor;
import net.minecraft.world.level.block.Block;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.*;

import static com.tailbraille.formationsdatap.setup.ReplacementReloadListener.PRODUCTION_QUIET;

@Mixin(value = FormationsBlockAgeProcessor.class, remap = false)
public abstract class FormationsBlockAgeProcessorMixin {

    @Shadow @Final @Mutable
    private static Set<Block> DISINTEGRATABLE_BLOCKS;

    @Shadow @Final @Mutable
    private static Map<Block, Block> BLOCK_TO_STAIR;

    @Shadow @Final @Mutable
    private static Map<Block, Block> STAIR_TO_SLAB;

    @Shadow @Final @Mutable
    private static Map<Block, List<Block>> BLOCK_TO_MOSSINESS;

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void formationsdatap$replaceImmutableCollections(CallbackInfo ci) {
        if (!PRODUCTION_QUIET) {
            LogUtils.getLogger().info(
                    "Replacing block age processor maps with mutable copies: disintegratable={}, toStair={}, stairToSlab={}, mossiness={}",
                    DISINTEGRATABLE_BLOCKS.size(),
                    BLOCK_TO_STAIR.size(),
                    STAIR_TO_SLAB.size(),
                    BLOCK_TO_MOSSINESS.size()
            );
        }

        DISINTEGRATABLE_BLOCKS = new HashSet<>(DISINTEGRATABLE_BLOCKS);
        BLOCK_TO_STAIR = new HashMap<>(BLOCK_TO_STAIR);
        STAIR_TO_SLAB = new HashMap<>(STAIR_TO_SLAB);
        BLOCK_TO_MOSSINESS = new HashMap<>(BLOCK_TO_MOSSINESS);

        DISINTEGRATABLE_BLOCKS.clear();
        BLOCK_TO_STAIR.clear();
        STAIR_TO_SLAB.clear();
        BLOCK_TO_MOSSINESS.clear();
    }
}
