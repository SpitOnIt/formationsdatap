package com.tailbraille.formationsdatap.setup;

import java.util.List;

public class ReplacementConfig {
    public static class Group {
        public String name;
        public List<ResourceEntry> entries;
    }

    public static class ResourceEntry {
        public String block;
        public List<String> biomes;
    }
}
