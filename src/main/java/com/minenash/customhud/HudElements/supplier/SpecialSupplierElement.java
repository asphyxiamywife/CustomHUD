package com.minenash.customhud.HudElements.supplier;

import com.minenash.customhud.complex.ComplexData;
import com.minenash.customhud.HudElements.interfaces.HudElement;
import com.minenash.customhud.HudElements.SettingsElement;
import com.minenash.customhud.mixin.accessors.GameOptionsAccessor;
import net.minecraft.client.CloudStatus;
import net.minecraft.client.GraphicsPreset;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;
import org.apache.commons.lang3.text.WordUtils;

import java.util.function.Supplier;

import static com.minenash.customhud.ProfileManager.getActive;

public class SpecialSupplierElement implements HudElement {

    private static final Minecraft client = Minecraft.getInstance();
    private static boolean isFacingEastOrSouth() {
        Direction dir = client.getCameraEntity().getDirection();
        return dir == Direction.EAST || dir == Direction.SOUTH;
    }

    public static final Entry DIFFICULTY = of( () -> client.level.getDifficulty().getSerializedName(),
                                               () -> client.level.getDifficulty().getId(),
                                               () -> client.level.getDifficulty().getId() != 0);

    public static final Entry MAX_FPS = of( () -> client.options.framerateLimit().get() == Options.UNLIMITED_FRAMERATE_CUTOFF ? null : client.options.framerateLimit().get().toString(),
                                            () ->  client.options.framerateLimit().get(),
                                            () -> client.options.framerateLimit().get() == Options.UNLIMITED_FRAMERATE_CUTOFF);

    public static final Entry PROFILE_KEYBIND = of( () -> getActive() == null ? "" : getActive().keyBinding.getTranslatedKeyMessage().getString(),
                                                    () -> getActive() == null ? 0 : getActive().keyBinding.key.getValue(),
                                                    () -> getActive() != null && !getActive().keyBinding.isUnbound());

    public static final Entry TIME_HOUR_24 = of( () -> String.format("%02d", ComplexData.timeOfDay / 1000),
                                                 () -> ComplexData.timeOfDay / 1000,
                                                 () -> ComplexData.timeOfDay / 1000 >= 12);

    public static final Entry TIME_MINUTES = of( () -> String.format("%02d",(int)((ComplexData.timeOfDay % 1000) / (1000/60F))),
                                                () -> (int)((ComplexData.timeOfDay % 1000) / (1000/60F)),
                                                () -> (int)((ComplexData.timeOfDay % 1000) / (1000/60F)) != 0);

    public static final Entry TIME_SECONDS = of( () -> String.format("%02d",(int)((ComplexData.timeOfDay % 1000) % (1000/60F) * 3.6F)),
                                                 () -> (int)((ComplexData.timeOfDay % 1000) % (1000/60F) * 3.6F),
                                                 () -> (int)((ComplexData.timeOfDay % 1000) % (1000/60F) * 3.6F) != 0);

    public static final Entry TARGET_BLOCK = of( () -> I18n.get(ComplexData.targetBlock.getBlock().getDescriptionId()),
                                                 () -> Block.getId(ComplexData.targetBlock),
                                                 () -> !ComplexData.targetBlock.isAir());

    public static final Entry TARGET_FLUID = of( () -> WordUtils.capitalize(BuiltInRegistries.FLUID.getKey(ComplexData.targetFluid.getType()).getPath().replace('_',' ')),
                                                 () -> Fluid.FLUID_STATE_REGISTRY.getId(ComplexData.targetFluid),
                                                 () -> !ComplexData.targetFluid.isEmpty());

    public static final Entry ITEM_OLD = of( () -> I18n.get(client.player.getMainHandItem().getItem().getDescriptionId()),
                                         () -> Item.getId(client.player.getMainHandItem().getItem()),
                                         () -> !client.player.getMainHandItem().isEmpty());

    public static final Entry ITEM_NAME = of( () -> client.player.getMainHandItem().getHoverName().getString(),
            () -> client.player.getMainHandItem().getHoverName().getString().length(),
            () -> !client.player.getMainHandItem().isEmpty());

    @Deprecated
    public static final Entry OFFHAND_ITEM = of( () -> I18n.get(client.player.getOffhandItem().getItem().getDescriptionId()),
                                                 () -> Item.getId(client.player.getOffhandItem().getItem()),
                                                 () -> !client.player.getOffhandItem().isEmpty());
    @Deprecated
    public static final Entry OFFHAND_ITEM_NAME = of( () -> client.player.getOffhandItem().getHoverName().getString(),
                                                      () -> client.player.getOffhandItem().getHoverName().getString().length(),
                                                      () -> !client.player.getOffhandItem().isEmpty());

    public static final Entry GRAPHICS_MODE = of(() -> {
        if (!SettingsElement.initialized)
            return "fancy";
        var opt = SettingsElement.simpleOptions.get("graphicsmode");
        if (opt == null)
            return "fancy";
        var value = opt.get();
        return value instanceof GraphicsPreset ? value.toString().toLowerCase() : "fancy";
    },
            () -> {
                if (!SettingsElement.initialized)
                    return 1;
                var opt = SettingsElement.simpleOptions.get("graphicsmode");
                if (opt == null)
                    return 1;
                var value = opt.get();
                if (value instanceof GraphicsPreset) {
                    return ((GraphicsPreset) value) == GraphicsPreset.FAST ? 0
                            : (((GraphicsPreset) value) == GraphicsPreset.FANCY ? 1 : 2);
                }
                return 1;
            },
            () -> true);

    public static final Entry CLOUDS = of(
            () -> {
                @SuppressWarnings("unchecked")
                var opt = (OptionInstance<CloudStatus>) ((GameOptionsAccessor) client.options).getCloudRenderMode();
                return opt.get() == CloudStatus.OFF ? "off"
                        : (opt.get() == CloudStatus.FAST ? "fast" : "fancy");
            },
            () -> {
                @SuppressWarnings("unchecked")
                var opt = (OptionInstance<CloudStatus>) ((GameOptionsAccessor) client.options).getCloudRenderMode();
                return opt.get() == CloudStatus.OFF ? 0 : (opt.get() == CloudStatus.FAST ? 1 : 2);
            },
            () -> {
                @SuppressWarnings("unchecked")
                var opt = (OptionInstance<CloudStatus>) ((GameOptionsAccessor) client.options).getCloudRenderMode();
                return opt.get() != CloudStatus.OFF;
            });

    public static final Entry GAMEMODE = of ( () -> client.gameMode.getPlayerMode().getName(),
                                              () -> client.gameMode.getPlayerMode().getId(),
                                              () -> true);

    public static final Entry FACING_TOWARDS_PN_WORD = of( () -> isFacingEastOrSouth() ? "positive" : "negative",
            () -> isFacingEastOrSouth() ? 1 : 0,
            SpecialSupplierElement::isFacingEastOrSouth);

    public static final Entry FACING_TOWARDS_PN_SIGN = of( () -> isFacingEastOrSouth() ? "+" : "-",
            () -> isFacingEastOrSouth() ? 1 : 0,
            SpecialSupplierElement::isFacingEastOrSouth);

    public static final Entry ACTIVE_RENDERER = of( () -> {var r = renderer(); return r != null ? r.getClass().getSimpleName() : "none (vanilla)";},
                                                    () -> {var r = renderer(); return r != null ? r.getClass().getSimpleName().length() : 7;},
                                                    () -> renderer() != null);

    public static Object renderer() { return null; }

    public static final Entry CAMERA_PERSPECTIVE = of (
            () -> switch (client.options.getCameraType()) {
                case FIRST_PERSON -> "First Person";
                case THIRD_PERSON_BACK -> "Third Person (Back)";
                case THIRD_PERSON_FRONT -> "Third Person (Front)";
            },
            () -> client.options.getCameraType().ordinal(),
            () -> client.options.getCameraType().ordinal() != 0
    );


    public record Entry(Supplier<String> stringSupplier, Supplier<Number> numberSupplier, Supplier<Boolean> booleanSupplier) {}
    public static Entry of(Supplier<String> stringSupplier, Supplier<Number> numberSupplier, Supplier<Boolean> booleanSupplier) {
        return new Entry(stringSupplier, numberSupplier, booleanSupplier);
    }

    private final Entry entry;

    public SpecialSupplierElement(Entry entry) {
        this.entry = entry;
    }

    @Override
    public String getString() {
        return sanitize(entry.stringSupplier, "-");
    }

    @Override
    public Number getNumber() {
        return sanitize(entry.numberSupplier, Double.NaN);
    }

    @Override
    public boolean getBoolean() {
        return sanitize(entry.booleanSupplier, false);
    }

}
