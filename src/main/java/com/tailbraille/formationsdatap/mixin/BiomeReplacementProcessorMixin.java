package com.tailbraille.formationsdatap.mixin;

import com.mojang.logging.LogUtils;
import com.supermartijn642.formations.structure.processors.BiomeReplacementProcessor;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;
import java.util.Set;

@Mixin(value = BiomeReplacementProcessor.class, remap = false)
public abstract class BiomeReplacementProcessorMixin {
    @Shadow @Final @Mutable
    private static Map<ResourceKey<Biome>, Map<Block, Block>> BIOME_REPLACEMENT_MAP;

    @Shadow @Final @Mutable
    private static Map<ResourceKey<Biome>, Map<Block, Block>> BIOME_SOIL_REPLACEMENT_MAP;

    @Shadow @Final @Mutable
    private static Set<Block> REPLACEABLE_BLOCKS;

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void clearBiomeMaps(CallbackInfo ci) {
        LogUtils.getLogger().info("Clearing biome replacement maps: {} + {} entries",
                BIOME_REPLACEMENT_MAP.size(), BIOME_SOIL_REPLACEMENT_MAP.size());

        BIOME_REPLACEMENT_MAP.clear();
        BIOME_SOIL_REPLACEMENT_MAP.clear();
        REPLACEABLE_BLOCKS.clear();
    }
    /**
    @Inject(method = "<clinit>", at = @At("TAIL"), cancellable = true)
    private static void onDoSomething(CallbackInfo ci) {
        callAddReplacements();
        try {
            for (List<Pair<Block, List<ResourceKey<Biome>>>> group : ConfigReader.loadGroups()) {
                List<Pair<Block, List<ResourceKey<Biome>>>> updatedGroup = group.stream().map(pair -> {
                    Block block = pair.getFirst();
                    List<ResourceKey<Biome>> updatedBiomes = pair.getSecond().stream().map(biomeKey -> {
                        ResourceLocation original = biomeKey.location();
                        if (!original.getNamespace().equals("minecraft")) {
                            ResourceLocation moddedLocation = new ResourceLocation(original.getNamespace(), original.getPath());
                            ResourceKey<Biome> moddedKey = ResourceKey.create(Registries.BIOME, moddedLocation);
                            LogUtils.getLogger().info("Substituting biome {} with {}", original, moddedLocation);
                            return moddedKey;
                        }
                        return biomeKey;
                    }).toList();
                    return Pair.of(block, updatedBiomes);
                }).toList();

                BiomeReplacementProcessorInvoker.callAddReplacements(updatedGroup.toArray(new Pair[0]));
                LogUtils.getLogger().info("Processed group with biome substitutions");
            }
        } catch (Exception e) {
            LogUtils.getLogger().info("Failed to Load Config.json");
            System.err.println("[FormationsMixin] Failed to load config.json: " + e.getMessage());
        }

        LogUtils.getLogger().info("Extra Behavior Injected!");
        System.out.println("Extra behavior injected!");
    }
    */
}
