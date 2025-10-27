package com.tailbraille.formationsdatap.setup;

import com.mojang.logging.LogUtils;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;

@Mod.EventBusSubscriber(modid = "formationsdatap", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ReplacementLoader {

    private static final Logger LOGGER = LogUtils.getLogger();

    @SubscribeEvent
    public static void onAddReloadListeners(AddReloadListenerEvent event) {
        LOGGER.info("Registering ReplacementReloadListener for formationsdatap datapack JSONs");
        event.addListener(new ReplacementReloadListener());
    }
}
