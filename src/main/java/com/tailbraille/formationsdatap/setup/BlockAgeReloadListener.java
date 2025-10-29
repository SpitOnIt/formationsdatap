package com.tailbraille.formationsdatap.setup;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import com.tailbraille.formationsdatap.util.BlockAgeUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;
import org.slf4j.Logger;

import java.util.*;

public class BlockAgeReloadListener extends SimpleJsonResourceReloadListener {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Logger LOGGER = LogUtils.getLogger();

    public BlockAgeReloadListener() {
        super(GSON, "map");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> jsonMap,
                         ResourceManager resourceManager,
                         ProfilerFiller profiler) {

        Set<Block> disintegratable = new HashSet<>();
        Map<Block, Block> blockToStair = new HashMap<>();
        Map<Block, Block> stairToSlab = new HashMap<>();
        Map<Block, List<Block>> blockToMossiness = new HashMap<>();

        for (Map.Entry<ResourceLocation, JsonElement> file : jsonMap.entrySet()) {
            ResourceLocation fileId = file.getKey();
            JsonElement root = file.getValue();

            if (!root.isJsonObject()) {
                LOGGER.warn("File {} is not a JSON object; skipping", fileId);
                continue;
            }

            JsonObject obj = root.getAsJsonObject();
            if (!obj.has("block_age")) continue;
            JsonObject age = obj.getAsJsonObject("block_age");

            try {
                if (age.has("disintegratable")) {
                    for (JsonElement el : age.getAsJsonArray("disintegratable")) {
                        String id = el.getAsString();
                        Block b = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(id));
                        if (b != null) {
                            disintegratable.add(b);
                        } else {
                            LOGGER.error("Disintegratable block {} not found (file {})", id, fileId);
                        }
                    }
                }

                if (age.has("block_to_stair")) {
                    for (JsonElement arrEl : age.getAsJsonArray("block_to_stair")) {
                        var arr = arrEl.getAsJsonArray();
                        if (arr.size() != 2) {
                            LOGGER.error("Invalid block_to_stair entry in {}: {}", fileId, arr);
                            continue;
                        }
                        String fromId = arr.get(0).getAsString();
                        String toId = arr.get(1).getAsString();
                        Block from = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(fromId));
                        Block to   = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(toId));
                        if (from != null && to != null) {
                            blockToStair.put(from, to);
                        } else {
                            LOGGER.error("Failed to resolve block_to_stair pair in {}: {} → {}", fileId, fromId, toId);
                        }
                    }
                }

                if (age.has("stair_to_slab")) {
                    for (JsonElement arrEl : age.getAsJsonArray("stair_to_slab")) {
                        var arr = arrEl.getAsJsonArray();
                        if (arr.size() != 2) {
                            LOGGER.error("Invalid stair_to_slab entry in {}: {}", fileId, arr);
                            continue;
                        }
                        String fromId = arr.get(0).getAsString();
                        String toId = arr.get(1).getAsString();
                        Block from = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(fromId));
                        Block to   = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(toId));
                        if (from != null && to != null) {
                            stairToSlab.put(from, to);
                        } else {
                            LOGGER.error("Failed to resolve stair_to_slab pair in {}: {} → {}", fileId, fromId, toId);
                        }
                    }
                }

                if (age.has("block_to_mossiness")) {
                    JsonObject map = age.getAsJsonObject("block_to_mossiness");
                    for (var e : map.entrySet()) {
                        String baseId = e.getKey();
                        Block base = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(baseId));
                        if (base == null) {
                            LOGGER.error("Base block {} not found for mossiness in {}", baseId, fileId);
                            continue;
                        }
                        List<Block> variants = new ArrayList<>();
                        for (JsonElement el : e.getValue().getAsJsonArray()) {
                            String variantId = el.getAsString();
                            Block variant = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(variantId));
                            if (variant != null) {
                                variants.add(variant);
                            } else {
                                LOGGER.error("Mossy variant {} not found for base {} in {}", variantId, baseId, fileId);
                            }
                        }
                        if (!variants.isEmpty()) blockToMossiness.put(base, variants);
                    }
                }

            } catch (Exception ex) {
                LOGGER.error("Failed to parse block_age section in {}", fileId, ex);
            }
        }

        // Single call to inject everything into FormationsBlockAgeProcessor
        BlockAgeUtils.inject(disintegratable, blockToStair, stairToSlab, blockToMossiness);

        if (!ReplacementReloadListener.PRODUCTION_QUIET) {
            LOGGER.info("BlockAgeReloadListener: loaded {} disintegratables, {} stair pairs, {} slab pairs, {} mossy entries",
                    disintegratable.size(), blockToStair.size(), stairToSlab.size(), blockToMossiness.size());
        }
    }
}
