package com.minenash.customhud.HudElements;

import com.minenash.customhud.CustomHud;
import com.minenash.customhud.HudElements.interfaces.HudElement;
import com.minenash.customhud.data.Flags;
import com.minenash.customhud.HudElements.supplier.*;
import com.minenash.customhud.errors.ErrorType;
import com.minenash.customhud.mixin.accessors.GameOptionsAccessor;
import com.minenash.customhud.mixin.accessors.KeyBindingAccessor;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.NarratorStatus;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.client.*;
import net.minecraft.server.level.ParticleStatus;
import net.minecraft.sounds.SoundSource;
import com.minenash.customhud.util.Tuple;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.ChatVisiblity;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class SettingsElement {

    private static final Minecraft client = Minecraft.getInstance();
    public static boolean initialized = false;

    //Boolean, Integer, Double
    public static final Map<String, OptionInstance<?>> simpleOptions = new HashMap<>();
    private static final Map<String, Integer> staticIntOptions = new HashMap<>();

    private static void init() {
        ((GameOptionsAccessor)Minecraft.getInstance().options).invokeAccept(new Options.FieldAccess() {
            @Override
            public <T> void process(String key, OptionInstance<T> option) {
                simpleOptions.put(key.toLowerCase(), option);
            }

            @Override
            public int process(String key, int current) {
                staticIntOptions.put(key.toLowerCase(), current); return current;
            }

            @Override
            public boolean process(String key, boolean current) {
                return current;
            }

            @Override
            public String process(String key, String current) {
                return current;
            }

            @Override
            public float process(String key, float current) {
                return current;
            }

            @Override
            public <T> T process(String key, T current, Function<String, T> decoder, Function<T, String> encoder) {
                return current;
            }
        });
    }

    public static Tuple<HudElement,Tuple<ErrorType,String>> create(String setting, Flags flags) {
        if (!initialized)
            init();
        initialized = true;
        setting = setting.toLowerCase();

        if (setting.equals("max_fps"))
            return new Tuple<>(new SpecialSupplierElement(SpecialSupplierElement.MAX_FPS), null);

        if (setting.startsWith("lang")) {
            String code = client.getLanguageManager().getSelected();
            HudElement element = switch (setting.substring(4)) {
                case "" -> new StringSupplierElement(() -> client.getLanguageManager().getLanguage(code).name());
                case "_region" -> new StringSupplierElement(() -> client.getLanguageManager().getLanguage(code).region());
                case "_code" -> new StringSupplierElement(() -> code);
                default -> null;
            };
            return new Tuple<>(element, new Tuple<>(ErrorType.UNKNOWN_SETTING, setting));
        }

        if (setting.startsWith("key."))
            setting = "key_" + setting;
        else if (setting.startsWith("sound_"))
            setting = "soundcategory_" + setting.substring(6);

        Options options = Minecraft.getInstance().options;
        if (setting.startsWith("key_")) {
            String key = setting.substring(4);
            for (KeyMapping binding : options.keyMappings)
                if (binding.getName().equalsIgnoreCase(key))
                    return new Tuple<>(new SpecialSupplierElement(SpecialSupplierElement.of(
                            () -> binding.getTranslatedKeyMessage().getString(),
                            () -> ((KeyBindingAccessor) binding).getKey().getValue(),
                            () -> !binding.isUnbound()
                    )), null);
            return new Tuple<>(null, new Tuple<>(ErrorType.UNKNOWN_KEYBIND, key));
        }

        if (setting.startsWith("soundcategory_")) {
            String cat = setting.substring(14);
            for (SoundSource soundCategory : SoundSource.values())
                if (soundCategory.getName().equalsIgnoreCase(cat))
                    return new Tuple<>(new NumberSupplierElement(NumberSupplierElement.of(
                            () -> ((GameOptionsAccessor)options).getSoundSourceVolumes().get(soundCategory).get() * 100,
                            flags.precision != -1 ? flags.precision : 0), flags), null);
            return new Tuple<>(null,new Tuple<>(ErrorType.UNKNOWN_SOUND_CATEGORY, cat));
        }

        OptionInstance<?> option = simpleOptions.get(setting);
        if (option != null)
            return new Tuple<>(getSimpleOptionElement(option, flags), null);

        if (staticIntOptions.containsKey(setting)) {
            int value = staticIntOptions.get(setting);
            return new Tuple<>(new NumberSupplierElement(() -> value, flags),null);
        }

        return new Tuple<>(null, new Tuple<>(ErrorType.UNKNOWN_SETTING, setting));

    }

    private static HudElement getSimpleOptionElement(OptionInstance<?> option, Flags flags) {
        CustomHud.logInDebugMode("Option: " + option.toString() + " | " + option.get().getClass().getName());
        if (option.get() instanceof Boolean)
            return new BooleanSupplierElement(() -> (Boolean) option.get());
        if (option.get() instanceof Number)
            return new NumberSupplierElement(NumberSupplierElement.of(() -> (Number) option.get(), option.get() instanceof Integer ? 0 : 1), flags);
        if (option.get() instanceof String)
            return new StringSupplierElement(() -> ((String)option.get()).isEmpty() ? "Default" : (String)option.get());
        if (option.get() instanceof ParticleStatus) {
            return new SpecialSupplierElement(SpecialSupplierElement.of(
                    () -> ((ParticleStatus) option.get()).toString().toLowerCase(),
                    () -> ((ParticleStatus) option.get()).ordinal(),
                    () -> ((ParticleStatus) option.get()).ordinal() != 2));
        }
        if (option.get() instanceof ChatVisiblity) {
            return new SpecialSupplierElement(SpecialSupplierElement.of(
                    () -> ((ChatVisiblity) option.get()).toString().toLowerCase(),
                    () -> ((ChatVisiblity) option.get()).ordinal(),
                    () -> ((ChatVisiblity) option.get()).ordinal() != 2));
        }
        if (option.get() instanceof HumanoidArm) {
            return new SpecialSupplierElement(SpecialSupplierElement.of(
                    () -> ((HumanoidArm) option.get()).toString().toLowerCase(),
                    () -> ((HumanoidArm) option.get()).ordinal(),
                    () -> ((HumanoidArm) option.get()).ordinal() != 1));
        }
        if (option.get() instanceof NarratorStatus)
            return new SpecialSupplierElement(SpecialSupplierElement.of(
                    () -> ((NarratorStatus) option.get()).getName().getString(),
                    () -> ((NarratorStatus) option.get()).getId(),
                    () -> ((NarratorStatus) option.get()).getId() != 0
            ));
        return null;
    }

}
