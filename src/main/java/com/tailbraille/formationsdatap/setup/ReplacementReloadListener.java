package com.tailbraille.formationsdatap.setup;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import com.tailbraille.formationsdatap.mixin.BiomeReplacementProcessorInvoker;
import com.tailbraille.formationsdatap.util.ReplacementUtils;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ReplacementReloadListener extends SimpleJsonResourceReloadListener {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Logger LOGGER = LogUtils.getLogger();

    // Toggle this before building: true = quiet production, false = verbose dev
    public static final boolean PRODUCTION_QUIET = true;

    private static final ResourceLocation DEFAULT_MODDED_BLOCK_ID =
            new ResourceLocation("minecraft", "diamond_ore");

    public ReplacementReloadListener() {
        super(GSON, "map");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> jsonMap,
                         ResourceManager resourceManager,
                         ProfilerFiller profiler) {

        if (!PRODUCTION_QUIET) {
            LOGGER.info("ReplacementReloadListener: begin reload. Found {} JSON files: {}", jsonMap.size(), jsonMap.keySet());
        }

        int totalGroups = 0;
        int totalEntries = 0;

        for (Map.Entry<ResourceLocation, JsonElement> file : jsonMap.entrySet()) {
            ResourceLocation fileId = file.getKey();
            JsonElement root = file.getValue();

            try {
                List<Group> groupsToProcess = new ArrayList<>();

                if (root.isJsonObject() && root.getAsJsonObject().has("groups")) {
                    RootGroup wrapper = GSON.fromJson(root, RootGroup.class);
                    if (wrapper != null && wrapper.groups != null) {
                        groupsToProcess.addAll(wrapper.groups);
                    } else {
                        LOGGER.warn("File {} has 'groups' wrapper but deserialized to null/empty", fileId);
                    }
                } else {
                    Group single = GSON.fromJson(root, Group.class);
                    if (single != null && single.entries != null) {
                        groupsToProcess.add(single);
                    } else {
                        LOGGER.warn("File {} is not a valid single Group; skipping", fileId);
                    }
                }

                if (groupsToProcess.isEmpty()) {
                    LOGGER.warn("File {} produced no groups after parsing; skipping", fileId);
                    continue;
                }

                for (Group group : groupsToProcess) {
                    List<Pair<Block, List<ResourceKey<Biome>>>> resolvedPairs = new ArrayList<>();

                    if (group.entries == null || group.entries.isEmpty()) {
                        LOGGER.warn("Group '{}' in file {} has no entries; skipping", group.name, fileId);
                        continue;
                    }

                    for (ResourceEntry re : group.entries) {
                        totalEntries++;

                        if (re == null || re.block == null || re.biomes == null) {
                            LOGGER.warn("Malformed entry in group '{}' (file {}): {}", group.name, fileId, re);
                            continue;
                        }

                        ResourceLocation blockId = new ResourceLocation(re.block);
                        Block block = ForgeRegistries.BLOCKS.getValue(blockId);

                        if (block == null) {
                            LOGGER.warn("Block {} not found for group '{}' (file {}), forcing to {}",
                                    blockId, group.name, fileId, DEFAULT_MODDED_BLOCK_ID);
                            block = ForgeRegistries.BLOCKS.getValue(DEFAULT_MODDED_BLOCK_ID);
                            if (block == null) {
                                LOGGER.error("Fallback block {} not found, skipping entry in group '{}'", DEFAULT_MODDED_BLOCK_ID, group.name);
                                continue;
                            }
                        }

                        ReplacementUtils.addReplaceable(block);

                        List<ResourceKey<Biome>> biomeKeys = re.biomes.stream()
                                .map(id -> {
                                    ResourceLocation original = new ResourceLocation(id);
                                    ResourceKey<Biome> key = ResourceKey.create(Registries.BIOME, original);

                                    if (!PRODUCTION_QUIET && !"minecraft".equals(original.getNamespace())) {
                                        LOGGER.info("Group '{}': substituting non-vanilla biome {} with {}", group.name, original, original);
                                    }

                                    return key;
                                })
                                .toList();

                        resolvedPairs.add(Pair.of(block, biomeKeys));

                        if (!PRODUCTION_QUIET) {
                            LOGGER.info("Group '{}': loaded entry '{}' in biomes {}", group.name, blockId,
                                    biomeKeys.stream().map(k -> k.location().toString()).toList());
                        }
                    }

                    if (!resolvedPairs.isEmpty()) {
                        BiomeReplacementProcessorInvoker.callAddReplacements(resolvedPairs.toArray(new Pair[0]));
                        totalGroups++;
                        if (!PRODUCTION_QUIET) {
                            LOGGER.info("File {}: loaded group '{}' with {} replacements", fileId, group.name, resolvedPairs.size());
                        }
                    } else {
                        LOGGER.warn("File {}: group '{}' resolved to 0 valid entries; nothing injected", fileId, group.name);
                    }
                }

            } catch (Exception ex) {
                LOGGER.error("Failed to parse file {} in ReplacementReloadListener", fileId, ex);
            }
        }

        if (totalGroups == 0) {
            LOGGER.warn("ReplacementReloadListener: no groups injected. Check folder name in constructor and JSON schema.");
        } else if (!PRODUCTION_QUIET) {
            LOGGER.info("ReplacementReloadListener: finished. Groups injected: {}, total entries: {}", totalGroups, totalEntries);
        }
    }

    static class RootGroup {
        List<Group> groups;
    }

    static class Group {
        String name;
        List<ResourceEntry> entries;
    }

    static class ResourceEntry {
        String block;
        List<String> biomes;
    }
}
