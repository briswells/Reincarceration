package org.kif.reincarceration.modifier.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ModifierRegistry {
    private final Map<String, IModifier> modifiers;

    public ModifierRegistry() {
        this.modifiers = new HashMap<>();
    }

    public void registerModifier(IModifier modifier) {
        modifiers.put(modifier.getId(), modifier);
    }

    public IModifier getModifier(String id) {
        return modifiers.get(id);
    }

    public List<IModifier> getAllModifiers() {
        return new ArrayList<>(modifiers.values());
    }

    public List<IModifier> getAvailableModifiers(List<String> completedModifiers) {
        return modifiers.values().stream()
                .filter(modifier -> !completedModifiers.contains(modifier.getId()))
                .collect(Collectors.toList());
    }
}