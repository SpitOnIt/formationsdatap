package com.tailbraille.formationsdatap.util;

import com.mojang.logging.LogUtils;
import com.supermartijn642.formations.structure.processors.FormationsBlockAgeProcessor;
import net.minecraft.world.level.block.Block;
import org.slf4j.Logger;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.tailbraille.formationsdatap.setup.ReplacementReloadListener.PRODUCTION_QUIET;

public final class BlockAgeUtils {

    private static final Logger LOGGER = LogUtils.getLogger();

    private BlockAgeUtils() {}

    @SuppressWarnings("unchecked")
    public static void inject(Set<Block> dis,
                              Map<Block, Block> stair,
                              Map<Block, Block> slab,
                              Map<Block, List<Block>> moss) {
        try {
            Field disField = FormationsBlockAgeProcessor.class.getDeclaredField("DISINTEGRATABLE_BLOCKS");
            disField.setAccessible(true);
            Set<Block> disSet = (Set<Block>) disField.get(null);
            disSet.clear();
            disSet.addAll(dis);

            Field stairField = FormationsBlockAgeProcessor.class.getDeclaredField("BLOCK_TO_STAIR");
            stairField.setAccessible(true);
            Map<Block,Block> stairMap = (Map<Block,Block>) stairField.get(null);
            stairMap.clear();
            stairMap.putAll(stair);

            Field slabField = FormationsBlockAgeProcessor.class.getDeclaredField("STAIR_TO_SLAB");
            slabField.setAccessible(true);
            Map<Block,Block> slabMap = (Map<Block,Block>) slabField.get(null);
            slabMap.clear();
            slabMap.putAll(slab);

            Field mossField = FormationsBlockAgeProcessor.class.getDeclaredField("BLOCK_TO_MOSSINESS");
            mossField.setAccessible(true);
            Map<Block,List<Block>> mossMap = (Map<Block,List<Block>>) mossField.get(null);
            mossMap.clear();
            mossMap.putAll(moss);

            if (!PRODUCTION_QUIET) {
                LOGGER.info("Injected block age rules into FormationsBlockAgeProcessor");
            } else {
                LOGGER.debug("Injected block age rules (quiet mode)");
            }

        } catch(Exception e) {
            LOGGER.error("Failed to inject block age rules into FormationsBlockAgeProcessor", e);
        }
    }
}
