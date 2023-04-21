package com.replaymod.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.apache.commons.lang3.ArrayUtils;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.replaymod.core.events.KeyBindingEventCallback;
import com.replaymod.core.events.KeyEventCallback;
import com.replaymod.core.events.PreRenderCallback;
import com.replaymod.core.versions.MCVer;
import com.replaymod.gui.utils.EventRegistrations;
import com.replaymod.mixin.KeyBindingAccessor;

import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.client.KeyMapping;

public class KeyBindingRegistry extends EventRegistrations {
    private static final String CATEGORY = "replaymod.title";
    
    private final Map<String, Binding> bindings = new HashMap<>();
    private Set<KeyMapping> onlyInReplay = new HashSet<>();
    private Multimap<Integer, Supplier<Boolean>> rawHandlers = ArrayListMultimap.create();

    public Binding registerKeyMapping(String name, int keyCode, Runnable whenPressed, boolean onlyInRepay) {
        Binding binding = registerKeyMapping(name, keyCode, onlyInRepay);
        binding.handlers.add(whenPressed);
        return binding;
    }

    public Binding registerRepeatedKeyMapping(String name, int keyCode, Runnable whenPressed, boolean onlyInRepay) {
        Binding binding = registerKeyMapping(name, keyCode, onlyInRepay);
        binding.repeatedHandlers.add(whenPressed);
        return binding;
    }

    private Binding registerKeyMapping(String name, int keyCode, boolean onlyInRepay) {
        Binding binding = bindings.get(name);
        if (binding == null) {
            KeyMapping keyBinding = new KeyMapping(name, keyCode, CATEGORY);
            MCVer.getMinecraft().options.keyMappings = ArrayUtils.add(MCVer.getMinecraft().options.keyMappings, keyBinding);
            binding = new Binding(name, keyBinding);
            bindings.put(name, binding);
            if (onlyInRepay) {
                this.onlyInReplay.add(keyBinding);
            }
        } else if (!onlyInRepay) {
            this.onlyInReplay.remove(binding.keyBinding);
        }
        return binding;
    }

    public void registerRaw(int keyCode, Supplier<Boolean> whenPressed) {
        rawHandlers.put(keyCode, whenPressed);
    }

    public Map<String, Binding> getBindings() {
        return Collections.unmodifiableMap(bindings);
    }

    public Set<KeyMapping> getOnlyInReplay() {
        return Collections.unmodifiableSet(onlyInReplay);
    }

    {
        on(PreRenderCallback.EVENT, this::handleRepeatedKeyMappings);
    }

    public void handleRepeatedKeyMappings() {
        for (Binding binding : bindings.values()) {
            if (binding.keyBinding.isDown()) {
                invokeKeyMappingHandlers(binding, binding.repeatedHandlers);
            }
        }
    }

    {
        on(KeyBindingEventCallback.EVENT, this::handleKeyMappings);
    }

    private void handleKeyMappings() {
        for (Binding binding : bindings.values()) {
            while (binding.keyBinding.consumeClick()) {
                invokeKeyMappingHandlers(binding, binding.handlers);
                invokeKeyMappingHandlers(binding, binding.repeatedHandlers);
            }
        }
    }

    private void invokeKeyMappingHandlers(Binding binding, Collection<Runnable> handlers) {
        for (final Runnable runnable : handlers) {
            try {
                runnable.run();
            } catch (Throwable cause) {
                CrashReport crashReport = CrashReport.forThrowable(cause, "Handling Key Binding");
                CrashReportCategory category = crashReport.addCategory("Key Binding");
                category.setDetail("Key Binding", () -> binding.name);
                category.setDetail("Handler", runnable::toString);
                throw new ReportedException(crashReport);
            }
        }
    }

    {
        on(KeyEventCallback.EVENT, (keyCode, scanCode, action, modifiers) -> handleRaw(keyCode, action));
    }

    private boolean handleRaw(int keyCode, int action) {
        if (action != KeyEventCallback.ACTION_PRESS) return false;
        for (final Supplier<Boolean> handler : rawHandlers.get(keyCode)) {
            try {
                if (handler.get()) {
                    return true;
                }
            } catch (Throwable cause) {
                CrashReport crashReport = CrashReport.forThrowable(cause, "Handling Raw Key Binding");
                CrashReportCategory category = crashReport.addCategory("Key Binding");
                category.setDetail("Key Code", () -> "" + keyCode);
                category.setDetail("Handler", handler::toString);
                throw new ReportedException(crashReport);
            }
        }
        return false;
    }

    public class Binding {
        public final String name;
        public final KeyMapping keyBinding;
        private final List<Runnable> handlers = new ArrayList<>();
        private final List<Runnable> repeatedHandlers = new ArrayList<>();
        private boolean autoActivation;
        private Consumer<Boolean> autoActivationUpdate;

        public Binding(String name, KeyMapping keyBinding) {
            this.name = name;
            this.keyBinding = keyBinding;
        }

        public String getBoundKey() {
            try {
                return keyBinding.getTranslatedKeyMessage().getString();
            } catch (ArrayIndexOutOfBoundsException e) {
                // Apparently windows likes to press strange keys, see https://www.replaymod.com/forum/thread/55
                return "Unknown";
            }
        }

        public boolean isBound() {
            return !keyBinding.isUnbound();
        }

        public void trigger() {
            KeyBindingAccessor acc = (KeyBindingAccessor) keyBinding;
            acc.setPressTime(acc.getPressTime() + 1);
            handleKeyMappings();
        }

        public void registerAutoActivationSupport(boolean active, Consumer<Boolean> update) {
            this.autoActivation = active;
            this.autoActivationUpdate = update;
        }

        public boolean supportsAutoActivation() {
            return autoActivationUpdate != null;
        }

        public boolean isAutoActivating() {
            return supportsAutoActivation() && autoActivation;
        }

        public void setAutoActivating(boolean active) {
            if (this.autoActivation == active) {
                return;
            }
            this.autoActivation = active;
            this.autoActivationUpdate.accept(active);
        }
    }
}
