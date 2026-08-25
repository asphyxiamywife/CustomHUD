package com.minenash.customhud.HudElements.list;

import com.minenash.customhud.HudElements.FuncElements.Num;
import com.minenash.customhud.HudElements.FuncElements.Num.NumEntry;
import com.minenash.customhud.HudElements.FuncElements.Special.Entry;
import com.minenash.customhud.HudElements.FuncElements.SpecialText.TextEntry;
import com.minenash.customhud.HudElements.list.AttributeHelpers.ItemAttribute;
import com.minenash.customhud.complex.ComplexData;
import com.minenash.customhud.complex.MusicAndRecordTracker;
import com.minenash.customhud.ducks.ResourcePackProfileMetadataDuck;
import com.minenash.customhud.ducks.SubtitleEntryDuck;
import com.minenash.customhud.util.Tuple;
import com.terraformersmc.modmenu.util.mod.Mod;
import net.minecraft.ChatFormatting;
import net.minecraft.client.multiplayer.chat.GuiMessage;
import net.minecraft.client.multiplayer.chat.GuiMessageTag;
import net.minecraft.client.gui.components.SubtitleOverlay;
import net.minecraft.client.gui.components.SubtitleOverlay.Subtitle;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.scores.*;
import net.minecraft.server.bossevents.CustomBossEvent;
import net.minecraft.server.packs.metadata.pack.PackFormat;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.tags.TagKey;
import net.minecraft.util.*;
import net.minecraft.world.BossEvent;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.DisplaySlot;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.PlayerScoreEntry;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Score;
import net.minecraft.world.scores.ScoreHolder;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.Team;
import org.apache.commons.lang3.text.WordUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;

import static com.minenash.customhud.CustomHud.CLIENT;
import static com.minenash.customhud.HudElements.list.AttributeHelpers.getItemItems;
import static com.minenash.customhud.data.StatFormatters.*;

@SuppressWarnings("ALL")
public class AttributeFunctions {

    public static final Function<?,?> DIRECT = (str) -> str;

    private static String formatName(ChatFormatting formatting) {
        return formatting == null ? "reset" : formatting.name().toLowerCase(Locale.ROOT);
    }

    private static int formatColor(ChatFormatting formatting) {
        return switch (formatting) {
            case BLACK -> 0x000000;
            case DARK_BLUE -> 0x0000AA;
            case DARK_GREEN -> 0x00AA00;
            case DARK_AQUA -> 0x00AAAA;
            case DARK_RED -> 0xAA0000;
            case DARK_PURPLE -> 0xAA00AA;
            case GOLD -> 0xFFAA00;
            case GRAY -> 0xAAAAAA;
            case DARK_GRAY -> 0x555555;
            case BLUE -> 0x5555FF;
            case GREEN -> 0x55FF55;
            case AQUA -> 0x55FFFF;
            case RED -> 0xFF5555;
            case LIGHT_PURPLE -> 0xFF55FF;
            case YELLOW -> 0xFFFF55;
            case WHITE -> 0xFFFFFF;
            default -> 0xFFFFFF;
        };
    }


    // STATUS EFFECTS
    public static final Function<MobEffectInstance,String> STATUS_NAME = (status) -> status == null ? null : I18n.get(status.getDescriptionId());
    public static final Function<MobEffectInstance,Identifier> STATUS_ID = (status) -> status == null ? null : BuiltInRegistries.MOB_EFFECT.getKey(status.getEffect().value());
    public static final NumEntry<MobEffectInstance> STATUS_DURATION = Num.of(TICKS_HMS, (status) -> status == null ? null : status.getDuration());
    public static final Function<MobEffectInstance,Boolean> STATUS_INFINITE = (status) -> status == null ? null : status.getDuration() == -1;
    public static final Function<MobEffectInstance,Number> STATUS_AMPLIFICATION = (status) -> status == null ? null : status.getAmplifier();
    public static final Function<MobEffectInstance,Number> STATUS_LEVEL = (status) -> status == null ? null : status.getAmplifier() + (status.getAmplifier() >= 0 ? 1 : 256);
    public static final Function<MobEffectInstance,Boolean> STATUS_AMBIENT = (status) -> status == null ? null : status.isAmbient();
    public static final Function<MobEffectInstance,Boolean> STATUS_SHOW_PARTICLES = (status) -> status == null ? null : status.isVisible();
    public static final Function<MobEffectInstance,Boolean> STATUS_SHOW_ICON = (status) -> status == null ? null : status.showIcon();
    public static final Function<MobEffectInstance,Number> STATUS_COLOR = (status) -> status == null ? null : status.getEffect().value().getColor();
    public static final Entry<MobEffectInstance> STATUS_CATEGORY = new Entry<>(
            (status) -> status == null ? null : WordUtils.capitalize(status.getEffect().value().getCategory().name().toLowerCase()),
            (status) -> status == null ? null : status.getEffect().value().getCategory().ordinal(),
            (status) -> status == null ? null : status.getEffect().value().getCategory().ordinal() != 1);


    // PLAYERS (From PlayerList)
    public static final Function<PlayerInfo,String> PLAYER_ENTRY_NAME = (player) -> player.getProfile().name();
    public static final Function<PlayerInfo,Component> PLAYER_ENTRY_DISPLAY_NAME = (player) -> player.getTabListDisplayName() != null
            ? player.getTabListDisplayName().copy() : PlayerTeam.formatNameForTeam(player.getTeam(), Component.literal(player.getProfile().name()));
    public static final Function<PlayerInfo,String> PLAYER_ENTRY_UUID = (player) -> player.getProfile().id().toString();
    public static final Function<PlayerInfo,String> PLAYER_ENTRY_TEAM = (player) -> player.getTeam().getName();
    public static final Function<PlayerInfo,Number> PLAYER_ENTRY_LATENCY = (player) -> player.getLatency();
    public static final Function<PlayerInfo,Boolean> PLAYER_ENTRY_SURVIVAL = (player) -> player.getGameMode() == GameType.SURVIVAL;
    public static final Function<PlayerInfo,Boolean> PLAYER_ENTRY_CREATIVE = (player) -> player.getGameMode() == GameType.CREATIVE;
    public static final Function<PlayerInfo,Boolean> PLAYER_ENTRY_ADVENTURE = (player) -> player.getGameMode() == GameType.ADVENTURE;
    public static final Function<PlayerInfo,Boolean> PLAYER_ENTRY_SPECTATOR = (player) -> player.getGameMode() == GameType.SPECTATOR;
    public static final Entry<PlayerInfo> PLAYER_ENTRY_GAMEMODE = new Entry<> (
            (player) -> player.getGameMode().getName(),
            (player) -> player.getGameMode().getId(),
            (player) -> true);
    public static final Function<PlayerInfo,Number> PLAYER_ENTRY_LIST_SCORE = (player) -> {
        Scoreboard scoreboard = CLIENT.level.getScoreboard();
        Objective objective = scoreboard.getDisplayObjective(DisplaySlot.LIST);
        return scoreboard.getPlayerScoreInfo(ScoreHolder.fromGameProfile(player.getProfile()), objective).value();
    };


    // SUBTITLES SOUND
    public static final Function<SubtitleOverlay.SoundPlayedAt,Number> SUBTITLE_SOUND_AGE = (sound) -> (Util.getMillis() - sound.time()) / 1000D;
    public static final Function<SubtitleOverlay.SoundPlayedAt,Number> SUBTITLE_SOUND_TIME = (sound) -> (3*CLIENT.options.notificationDisplayTime().get()) - (Util.getMillis() - sound.time()) / 1000D;
    public static final Function<SubtitleOverlay.SoundPlayedAt,Number> SUBTITLE_SOUND_ALPHA = (sound) -> {
        double d = CLIENT.options.notificationDisplayTime().get();
        int p = Mth.floor(Mth.clampedLerp(255.0F, 75.0F, (float)(Util.getMillis() - sound.time()) / (float)(3000.0 * d)));
        return  (p << 24);
    };
    public static final Function<SubtitleOverlay.SoundPlayedAt,Number> SUBTITLE_SOUND_DISTANCE = (sound) -> sound.location().distanceTo(CLIENT.getCameraEntity().getEyePosition());
    public static final Function<SubtitleOverlay.SoundPlayedAt,Number> SUBTITLE_SOUND_X = (sound) -> sound.location().x();
    public static final Function<SubtitleOverlay.SoundPlayedAt,Number> SUBTITLE_SOUND_Y = (sound) -> sound.location().y();
    public static final Function<SubtitleOverlay.SoundPlayedAt,Number> SUBTITLE_SOUND_Z = (sound) -> sound.location().z();
    public static final Function<SubtitleOverlay.SoundPlayedAt,Boolean> SUBTITLE_SOUND_LEFT = (sound) -> subtitle$getDirection(sound) == -1;
    public static final Function<SubtitleOverlay.SoundPlayedAt,Boolean> SUBTITLE_SOUND_RIGHT = (sound) -> subtitle$getDirection(sound) == 1;
    public static final Function<SubtitleOverlay.SoundPlayedAt,String> SUBTITLE_SOUND_DIRECTION = (sound) -> {
        int dir = subtitle$getDirection(sound);
        return dir == 0 ? "=" : dir == 1 ? ">" : "<";
    };
    public static final Function<SubtitleOverlay.SoundPlayedAt,Number> SUBTITLE_SOUND_DIRECTION_YAW = (sound) -> AttributeHelpers.getRelativeYaw(CLIENT.getCameraEntity().position(), sound.location());
    public static final Function<SubtitleOverlay.SoundPlayedAt,Number> SUBTITLE_SOUND_DIRECTION_PITCH = (sound) -> AttributeHelpers.getRelativePitch(CLIENT.getCameraEntity().getEyePosition(), sound.location());


    // SUBTITLES
    public static final Function<Subtitle,Identifier> SUBTITLE_ID = (subtitle) -> ((SubtitleEntryDuck)subtitle).customhud$getSoundID();
    public static final Function<Subtitle,String> SUBTITLE_NAME = (subtitle) -> subtitle.getText().getString();
    public static final Function<Subtitle,Number> SUBTITLE_AGE = (subtitle) -> SUBTITLE_SOUND_AGE.apply(sound(subtitle));
    public static final Function<Subtitle,Number> SUBTITLE_TIME = (subtitle) -> SUBTITLE_SOUND_AGE.apply(sound(subtitle));
    public static final Function<Subtitle,Number> SUBTITLE_ALPHA = (subtitle) -> SUBTITLE_SOUND_ALPHA.apply(sound(subtitle));
    public static final Function<Subtitle,Number> SUBTITLE_DISTANCE = (subtitle) -> SUBTITLE_SOUND_DISTANCE.apply(sound(subtitle));
    public static final Function<Subtitle,Number> SUBTITLE_X = (subtitle) -> SUBTITLE_SOUND_X.apply(sound(subtitle));
    public static final Function<Subtitle,Number> SUBTITLE_Y = (subtitle) -> SUBTITLE_SOUND_Y.apply(sound(subtitle));
    public static final Function<Subtitle,Number> SUBTITLE_Z = (subtitle) -> SUBTITLE_SOUND_Z.apply(sound(subtitle));
    public static final Function<Subtitle,Boolean> SUBTITLE_LEFT = (subtitle) -> SUBTITLE_SOUND_LEFT.apply(sound(subtitle));
    public static final Function<Subtitle,Boolean> SUBTITLE_RIGHT = (subtitle) -> SUBTITLE_SOUND_RIGHT.apply(sound(subtitle));
    public static final Function<Subtitle,String> SUBTITLE_DIRECTION = (subtitle) -> SUBTITLE_SOUND_DIRECTION.apply(sound(subtitle));
    public static final Function<Subtitle,Number> SUBTITLE_DIRECTION_YAW = (subtitle) -> SUBTITLE_SOUND_DIRECTION_YAW.apply(sound(subtitle));
    public static final Function<Subtitle,Number> SUBTITLE_DIRECTION_PITCH = (subtitle) -> SUBTITLE_SOUND_DIRECTION_PITCH.apply(sound(subtitle));




    // BLOCK STATES
    public static final Function<Map.Entry<Property<?>,Comparable<?>>,String> BLOCK_STATE_NAME = (property) -> property == null ? null : property.getKey().getName();
    public static final Function<Map.Entry<Property<?>,Comparable<?>>,String> BLOCK_STATE_VALUE = (property) -> property == null ? null : property.getValue().toString();
    public static final Function<Map.Entry<Property<?>,Comparable<?>>,String> BLOCK_STATE_FULL_TYPE = (property) -> property == null ? null : property.getKey().getValueClass().getSimpleName();
    public static final Entry<Map.Entry<Property<?>,Comparable<?>> > BLOCK_STATE_TYPE = new Entry<> (
            (property) -> property == null ? null : switch (blockstate$getPropertyType(property.getKey().getValueClass())) {
                case 1 -> "Boolean";
                case 2 -> "Number";
                case 3 -> "Enum";
                default -> "String"; },
            (property) -> property == null ? null : blockstate$getPropertyType(property.getKey().getValueClass()),
            (property) -> property == null ? null : blockstate$getPropertyType(property.getKey().getValueClass()) != 0);


    // BLOCK/ITEM TAGS
    public static final Function<TagKey<?>,Identifier> TAG_ID = (tag) -> tag.location();
    public static final Function<TagKey<?>,String> TAG_NAME = (tag) -> tag.location().getNamespace().equals("minecraft") ?
            tag.location().getPath() : tag.location().toString();

    // RECIEVED POWER
    public static final Function<AttributeHelpers.ReceivedPower,String> REC_DIRECTION = (rec) -> rec.direction().getName();
    public static final Function<AttributeHelpers.ReceivedPower,String> REC_OPOSITE_DIRECTION = (rec) -> rec.direction().getOpposite().getName();
    public static final Function<AttributeHelpers.ReceivedPower,Number> REC_POWER = (rec) -> rec.power();
    public static final Function<AttributeHelpers.ReceivedPower,Number> REC_STRONG_POWER = (rec) -> rec.strongPower();

    // ENCHANTMENTS
    public static final Function<Map.Entry<Holder<Enchantment>,Integer>,String> ENCHANT_NAME = (enchant) -> enchant == null ? null : enchant.getKey().value().description().getString();
    public static final Function<Map.Entry<Holder<Enchantment>,Integer>,Identifier> ENCHANT_ID = (enchant) -> enchant == null ? null : enchant.getKey().unwrapKey().get().identifier();
    public static final Function<Map.Entry<Holder<Enchantment>,Integer>,Number> ENCHANT_NUM = (enchant) -> enchant == null ? null : enchant.getValue();
    public static final Function<Map.Entry<Holder<Enchantment>,Integer>,Number> ENCHANT_MAX_NUM = (enchant) -> enchant == null ? null : enchant.getKey().value().getMaxLevel();
    public static final Function<Map.Entry<Holder<Enchantment>,Integer>,String> ENCHANT_FULL = (enchant) -> enchant == null ? null :
            enchant.getKey().value().getMaxLevel() == 1 ? enchant.getKey().value().description().getString()
                    : enchant.getKey().value().description().getString() + " " + I18n.get("enchantment.level." + enchant.getValue());
    public static final Entry<Map.Entry<Enchantment,Integer>> ENCHANT_LEVEL = new Entry<> (
            (enchant) -> enchant == null ? null : I18n.get("enchantment.level." + enchant.getValue()),
            (enchant) -> enchant == null ? null : enchant.getValue(),
            (enchant) -> enchant != null);
    public static final Entry<Map.Entry<Enchantment,Integer>> ENCHANT_MAX_LEVEL = new Entry<> (
            (enchant) -> enchant == null ? null : I18n.get("enchantment.level." + enchant.getKey().getMaxLevel()),
            (enchant) -> enchant == null ? null : enchant.getKey().getMaxLevel(),
            (enchant) -> enchant != null);


    // ITEMS
    public static final Function<ItemStack, Identifier> ITEM_ID = (stack) -> BuiltInRegistries.ITEM.getKey(stack.getItem());
    public static final Function<ItemStack, String> ITEM_NAME = (stack) -> stack.getItem().getName(stack).getString();
    public static final Function<ItemStack, Number> ITEM_RAW_ID = (stack) -> Item.getId(stack.getItem());
    public static final Function<ItemStack, Boolean> ITEM_IS_NOT_EMPTY = (stack) -> !stack.isEmpty();
    public static final TextEntry<ItemStack> ITEM_CUSTOM_NAME = new TextEntry<>(
            (stack) -> stack.getHoverName(),
            (stack) -> stack.getHoverName().getString().length(),
            (stack) -> !stack.getHoverName().getString().equals(stack.getItem().getName(stack).getString()));

    public static final Function<ItemStack, Number> ITEM_COUNT = (stack) -> stack.getCount();
    public static final Function<ItemStack, Number> ITEM_MAX_COUNT = (stack) -> stack.getMaxStackSize();
    public static final Function<ItemStack, Number> ITEM_INV_COUNT = (stack) -> CLIENT.player.getInventory().countItem(stack.getItem());
    public static final Function<ItemStack, Boolean> ITEM_IS_STACKABLE = (stack) -> stack.getMaxStackSize() > 1;
    public static final Function<ItemStack, Boolean> ITEM_HAS_MORE_OUT_OF_STACK = (stack) -> CLIENT.player.getInventory().countItem(stack.getItem()) > stack.getCount();

    public static final Function<ItemStack, Number> ITEM_COOLDOWN = (stack) -> {
        ItemCooldowns manger = CLIENT.player.getCooldowns();
        ItemCooldowns.CooldownInstance entry = manger.cooldowns.get(manger.getCooldownGroup(stack));
        return entry == null ? stack.get(DataComponents.USE_COOLDOWN) != null ? 0 : Double.NaN
                                                   : entry.endTime() - CLIENT.player.getCooldowns().tickCount;
    };
    public static final Function<ItemStack, Number> ITEM_MAX_COOLDOWN = (stack) -> {
        var c = stack.get(DataComponents.USE_COOLDOWN);
        return c == null ? null : c.ticks();
    };
    public static final Function<ItemStack, Number> ITEM_COOLDOWN_PER = (stack) -> {
        if (stack.get(DataComponents.USE_COOLDOWN) == null)
            return Double.NaN;
        return 100 * CLIENT.player.getCooldowns().getCooldownPercent(stack, CLIENT.getDeltaTracker().getGameTimeDeltaPartialTick(true));
    };
    public static final Function<ItemStack, Boolean> ITEM_COOLING_DOWN = (stack) -> CLIENT.player.getCooldowns().isOnCooldown(stack);
    public static final Function<ItemStack, Boolean> ITEM_HAS_COOLDOWN = (stack) -> stack.get(DataComponents.USE_COOLDOWN) != null;


    public static final Function<ItemStack, Boolean> ITEM_HAS_DURABILITY = (stack) -> stack.getMaxDamage() - CLIENT.player.getMainHandItem().getDamageValue() > 0;
    public static final Function<ItemStack, Boolean> ITEM_HAS_MAX_DURABILITY = (stack) -> stack.getMaxDamage() > 0;
    public static final Function<ItemStack, Number> ITEM_DURABILITY = (stack) -> stack.getMaxDamage() - stack.getDamageValue();
    public static final Function<ItemStack, Number> ITEM_MAX_DURABILITY = (stack) -> stack.getMaxDamage();
    public static final Function<ItemStack, Number> ITEM_DURABILITY_PERCENT = (stack) -> 100 - stack.getDamageValue() / (float) stack.getMaxDamage() * 100;
    public static final Function<ItemStack, Number> ITEM_DURABILITY_COLOR = (stack) ->  stack.getMaxDamage() > 0 ? stack.getBarColor() : null;
    public static final Function<ItemStack, Boolean> ITEM_UNBREAKABLE = (stack) -> stack.has(DataComponents.UNBREAKABLE);
    public static final Function<ItemStack, Number> ITEM_REPAIR_COST = (stack) -> stack.getOrDefault(DataComponents.REPAIR_COST, Double.NaN);
    public static final Entry<ItemStack> ITEM_RARITY = new Entry<>(
            (stack) -> stack.getOrDefault(DataComponents.RARITY, Rarity.COMMON).name(),
            (stack) -> formatColor(stack.getOrDefault(DataComponents.RARITY, Rarity.COMMON).color()),
            (stack) -> stack.getOrDefault(DataComponents.RARITY, Rarity.COMMON) != Rarity.COMMON
    );
    public static final Entry<ItemStack> ITEM_ARMOR_SLOT = new Entry<>(
            (stack) -> {
                Equippable component = stack.getComponents().get(DataComponents.EQUIPPABLE);
                if (component == null || !component.slot().isArmor())
                    return "None";
                String name = component.slot().getName();
                return name.substring(0,1).toUpperCase() + name.substring(1);
            },
            (stack) -> {
                Equippable c = stack.getComponents().get(DataComponents.EQUIPPABLE);
                return c == null || !c.slot().isArmor() ? 0 : 5 - ((c.slot().getIndex()-1) % 4);
            },
            (stack) -> {
                Equippable c = stack.getComponents().get(DataComponents.EQUIPPABLE);
                return c != null && c.slot().isArmor();
            }
    );


    // CAN X
    public static final Function<Block, Identifier> BLOCK_ID = (block) -> BuiltInRegistries.BLOCK.getKey(block);
    public static final Function<Block, String> BLOCK_NAME = (block) -> I18n.get(block.getDescriptionId());

    // ATTRIBUTES
    public static final Function<AttributeInstance,String> ATTRIBUTE_NAME = (attr) -> I18n.get(attr.getAttribute().value().getDescriptionId());
    public static final Function<AttributeInstance,Identifier> ATTRIBUTE_ID = (attr) -> BuiltInRegistries.ATTRIBUTE.getKey(attr.getAttribute().value());
    public static final Function<AttributeInstance,Boolean> ATTRIBUTE_TRACKED = (attr) -> attr.getAttribute().value().isClientSyncable();
    public static final Function<AttributeInstance,Number> ATTRIBUTE_VALUE_DEFAULT = (attr) -> attr.getAttribute().value().getDefaultValue();
    public static final Function<AttributeInstance,Number> ATTRIBUTE_VALUE_BASE = AttributeInstance::getBaseValue;
    public static final Function<AttributeInstance,Number> ATTRIBUTE_VALUE = AttributeInstance::getValue;


    // ATTRIBUTE MODIFIERS
    public static final Function<AttributeModifier,Identifier> ATTRIBUTE_MODIFIER_ID = (modifier) -> modifier.id();
    public static final Function<AttributeModifier,Number> ATTRIBUTE_MODIFIER_VALUE = (modifier) -> modifier.amount();
    public static final Function<AttributeModifier,String> ATTRIBUTE_MODIFIER_OPERATION_NAME = (modifier) -> switch (modifier.operation()) {
        case ADD_VALUE -> "Addition";
        case ADD_MULTIPLIED_BASE -> "Multiplication Base";
        case ADD_MULTIPLIED_TOTAL -> "Multiplication Total"; };
    public static final Function<AttributeModifier,String> ATTRIBUTE_MODIFIER_OPERATION = (modifier) -> switch (modifier.operation()) {
        case ADD_VALUE -> "+";
        case ADD_MULTIPLIED_BASE -> "☒";
        case ADD_MULTIPLIED_TOTAL -> "×"; };


    // ITEM ATTRIBUTE MODIFIERS
    public static final Function<ItemAttribute,String> ITEM_ATTR_SLOT = (attr) -> attr.slot();
    public static final Function<ItemAttribute,String> ITEM_ATTR_NAME = (attr) -> I18n.get(attr.attribute().getDescriptionId());
    public static final Function<ItemAttribute,Identifier> ITEM_ATTR_ID = (attr) -> BuiltInRegistries.ATTRIBUTE.getKey(attr.attribute());
    public static final Function<ItemAttribute,Boolean> ITEM_ATTR_TRACKED = (attr) -> attr.attribute().isClientSyncable();
    public static final Function<ItemAttribute,Number> ITEM_ATTR_VALUE_DEFAULT = (attr) -> attr.attribute().getDefaultValue();
    public static final Function<ItemAttribute,Number> ITEM_ATTR_VALUE_BASE = (attr) -> CLIENT.player.getAttributeBaseValue(BuiltInRegistries.ATTRIBUTE.wrapAsHolder(attr.attribute()));
    public static final Function<ItemAttribute,Number> ITEM_ATTR_VALUE = (attr) -> CLIENT.player.getAttributeValue(BuiltInRegistries.ATTRIBUTE.wrapAsHolder(attr.attribute()));
    public static final Function<ItemAttribute,String> ITEM_ATTR_MODIFIER_NAME = (attr) -> {
        String key = attr.attribute().getDescriptionId();
        return Language.getInstance().has(key) ? I18n.get(key) : attr.modifier().id().toString(); };
    public static final Function<ItemAttribute,Identifier> ITEM_ATTR_MODIFIER_ID = (attr) -> attr.modifier().id();
    public static final Function<ItemAttribute,Number> ITEM_ATTR_MODIFIER_VALUE = (attr) -> attr.modifier().amount();
    public static final Function<ItemAttribute,String> ITEM_ATTR_MODIFIER_OPERATION_NAME = (attr) -> switch (attr.modifier().operation()) {
        case ADD_VALUE -> "Addition";
        case ADD_MULTIPLIED_BASE -> "Multiplication Base";
        case ADD_MULTIPLIED_TOTAL -> "Multiplication Total"; };
    public static final Function<ItemAttribute,String> ITEM_ATTR_MODIFIER_OPERATION = (attr) -> switch (attr.modifier().operation()) {
        case ADD_VALUE -> "+";
        case ADD_MULTIPLIED_BASE -> "☒";
        case ADD_MULTIPLIED_TOTAL -> "×"; };


    // TEAM
    public static final Function<PlayerTeam,Component> TEAM_NAME = (team) -> team.getDisplayName();
    public static final Function<PlayerTeam,String> TEAM_ID = PlayerTeam::getName;
    public static final Function<PlayerTeam,Boolean> TEAM_FRIENDLY_FIRE = PlayerTeam::isAllowFriendlyFire;
    public static final Function<PlayerTeam,Boolean> TEAM_FRIENDLY_INVIS = PlayerTeam::canSeeFriendlyInvisibles;
    public static final Entry<PlayerTeam> TEAM_NAME_TAG_VISIBILITY = new Entry<>(
            (team) -> team.getNameTagVisibility().getDisplayName().getString(),
            (team) -> team.getNameTagVisibility().id,
            (team) -> team$visibleToPlayer(team, team.getNameTagVisibility()));
    public static final Entry<PlayerTeam> TEAM_DEATH_MGS_VISIBILITY = new Entry<>(
            (team) -> team.getNameTagVisibility().getDisplayName().getString(),
            (team) -> team.getNameTagVisibility().id,
            (team) -> team$visibleToPlayer(team, team.getDeathMessageVisibility()));
    public static final Entry<PlayerTeam> TEAM_COLLISION = new Entry<>(
            (team) -> team.getCollisionRule().getDisplayName().getString(),
            (team) -> team.getCollisionRule().id,
            (team) -> team.getCollisionRule() != Team.CollisionRule.NEVER);
    public static final Entry<PlayerTeam> TEAM_COLOR = new Entry<>(
            (team) -> team.getColor().map(TeamColor::getSerializedName).orElse("reset"),
            (team) -> team.getColor().map(TeamColor::rgb).orElse(0xFFFFFF),
            (team) -> team.getColor().isPresent()
    );


    // SCOREBOARD OBJECTIVES
    public static final Function<Objective,Component> OBJECTIVE_NAME = (obj) -> obj.getDisplayName();
    public static final Function<Objective,String> OBJECTIVE_ID = (obj) -> obj.getName();
    public static final Function<Objective,String> OBJECTIVE_CRITIERIA = (obj) -> CLIENT.getSingleplayerServer() == null ? "unknown" : obj.getCriteria().getName();
    public static final Function<Objective,String> OBJECTIVE_DISPLAY_SLOT = (obj) -> {
        Scoreboard scoreboard = AttributeHelpers.scoreboard();
        for (DisplaySlot slot : DisplaySlot.values())
            if (scoreboard.getDisplayObjective(slot) == obj)
                return slot.name().toLowerCase();
        return "none";
    };


    // SCOREBOARD OBJECTIVE SCORE
    public static final Function<PlayerScoreEntry,String> OBJECTIVE_SCORE_HOLDER_OWNER = (score) -> score.owner();
    public static final Function<PlayerScoreEntry,Component> OBJECTIVE_SCORE_HOLDER_DISPLAY = (score) -> score.display();
    public static final Function<PlayerScoreEntry,Number> OBJECTIVE_SCORE_VALUE = (score) -> score.value();


    // SCOREBOARD SCORE
    public static final Function<Map.Entry<Objective, Score>,Component> SCORES_OBJECTIVE_NAME = (entry) -> entry.getKey().getDisplayName();
    public static final Function<Map.Entry<Objective, Score>,String> SCORES_OBJECTIVE_ID = (entry) -> entry.getKey().getName();
    public static final Function<Map.Entry<Objective, Score>,String> SCORES_OBJECTIVE = (entry) -> CLIENT.getSingleplayerServer() == null ? "unknown" : entry.getKey().getCriteria().getName();
    public static final Function<Map.Entry<Objective, Score>,Number> SCORES_VALUE = (entry) -> entry.getValue().value();
    public static final Function<Map.Entry<Objective, Score>,String> SCORES_OBJECTIVE_CRITIERIA = (entry) -> CLIENT.getSingleplayerServer() == null ? "unknown" : entry.getKey().getCriteria().getName();
    public static final Function<Map.Entry<Objective, Score>,String> SCORES_OBJECTIVE_DISPLAY_SLOT = (entry) -> {
        Scoreboard scoreboard = AttributeHelpers.scoreboard();
        for (DisplaySlot slot : DisplaySlot.values())
            if (scoreboard.getDisplayObjective(slot) == entry.getKey())
                return slot.name().toLowerCase();
        return "none";
    };


    // BOSSBARS
    public static final Function<BossEvent,Component> BOSSBAR_NAME = (bar) -> bar.getName();
    public static final Function<BossEvent,String> BOSSBAR_UUID = (bar) -> bar.getId().toString();
    public static final Function<BossEvent,Number> BOSSBAR_PERCENT = (bar) -> bar.getProgress();
    public static final Function<BossEvent,Boolean> BOSSBAR_DARKEN_SKY = (bar) -> bar.shouldDarkenScreen();
    public static final Function<BossEvent,Boolean> BOSSBAR_DRAGON_MUSIC = (bar) -> bar.shouldPlayBossMusic();
    public static final Function<BossEvent,Boolean> BOSSBAR_THICKENS_FOG = (bar) -> bar.shouldCreateWorldFog();
    public static final Entry<BossEvent> BOSSBAR_COLOR = new Entry<>(
            (bar) -> WordUtils.capitalize(bar.getColor().getName().toLowerCase()),
            (bar) -> AttributeHelpers.getBossBarColor(bar),
            (bar) -> bar.getColor() != BossEvent.BossBarColor.WHITE
    );
    public static final Entry<BossEvent> BOSSBAR_TEXT_COLOR = new Entry<>(
            (bar) -> WordUtils.capitalize(formatName(bar.getColor().getFormatting())),
            (bar) -> formatColor(bar.getColor().getFormatting()),
            (bar) -> bar.getColor() != BossEvent.BossBarColor.WHITE
    );
    public static final Entry<BossEvent> BOSSBAR_STYLE = new Entry<>(
            (bar) -> switch (bar.getOverlay()) {
                case PROGRESS -> "Progress";
                case NOTCHED_6 -> "Notched 6";
                case NOTCHED_10 -> "Notched 10";
                case NOTCHED_12 -> "Notched 12";
                case NOTCHED_20 -> "Notched 20";
            },
            (bar) -> bar.getOverlay().ordinal(),
            (bar) -> bar.getOverlay().ordinal() != 0
    );
    public static final Function<BossEvent,Identifier> BOSSBAR_ID = (bar) -> {
        if (CLIENT.getSingleplayerServer() == null) return null;
        for (var entry : CLIENT.getSingleplayerServer().getCustomBossEvents().events.entrySet())
            if (entry.getValue() == bar) return entry.getKey();
        return null;
    };
    public static final Function<BossEvent,Boolean> BOSSBAR_IS_VISIBLE = (bar) -> bar instanceof CustomBossEvent cbb ? cbb.isVisible() : null;


    // MODS
    public static final Function<?,String> MOD_NAME = (mod) -> ((Mod)mod).getName();
    public static final Function<?,String> MOD_ID = (mod) -> ((Mod)mod).getId();
    public static final Function<?,String> MOD_SUMMARY = (mod) -> ((Mod)mod).getSummary();
    public static final Function<?,String> MOD_DESCRIPTION = (mod) -> ((Mod)mod).getTranslatedDescription();
    public static final Function<?,String> MOD_VERSION = (mod) -> ((Mod)mod).getVersion();
    public static final Function<?,String> MOD_PREFIXED_VERSION = (mod) -> ((Mod)mod).getPrefixedVersion();
    public static final Function<?,String> MOD_HASH = (mod) -> {
        try { return ((Mod)mod).getSha512Hash(); }
        catch (IOException e) { return null; }
    };
    public static final Function<?,Boolean> MOD_IS_LIBRARY = (mod) -> ((Mod)mod).getBadges().contains(Mod.Badge.LIBRARY);
    public static final Function<?,Boolean> MOD_IS_CLIENT = (mod) -> ((Mod)mod).getBadges().contains(Mod.Badge.CLIENT);
    public static final Function<?,Boolean> MOD_IS_DEPRECATED = (mod) -> ((Mod)mod).getBadges().contains(Mod.Badge.DEPRECATED);
    public static final Function<?,Boolean> MOD_IS_PATCHWORK = (mod) -> ((Mod)mod).getBadges().contains(Mod.Badge.PATCHWORK_FORGE);
    public static final Function<?,Boolean> MOD_IS_FROM_MODPACK = (mod) -> ((Mod)mod).getBadges().contains(Mod.Badge.MODPACK);
    public static final Function<?,Boolean> MOD_IS_MINECRAFT = (mod) -> ((Mod)mod).getBadges().contains(Mod.Badge.MINECRAFT);

    public static final Function<?,String> BADGE_NAME = (badge) -> ((Mod.Badge)badge).getText().getString();
    public static final Function<?,Number> BADGE_OUTLINE_COLOR = (badge) -> ((Mod.Badge)badge).getOutlineColor();
    public static final Function<?,Number> BADGE_FILL_COLOR = (badge) -> ((Mod.Badge)badge).getFillColor();

    public static final Function<?,String> MOD_C_ENTRY_NAME = (entry) -> ((Tuple<String,String>)entry).getA();
    public static final Function<?,String> MOD_C_ENTRY_KEY = (entry) -> ((Tuple<String,String>)entry).getB();


    // PACKS
    public static final Function<Pack,Component> PACK_NAME = (pack) -> pack.getTitle();
    public static final Function<Pack,String> PACK_ID = (pack) -> pack.getId();
    public static final Function<Pack,Component> PACK_DESCRIPTION = (pack) -> pack.getDescription();

    public static final Function<Pack,String> MIN_PACK_VERSION = (pack) -> range(pack).minInclusive().toString();
    public static final Function<Pack,String> MAX_PACK_VERSION = (pack) -> range(pack).maxInclusive().toString();

    public static final Function<Pack,Number> MIN_PACK_VERSION_MAJOR = (pack) -> range(pack).minInclusive().major();
    public static final Function<Pack,Number> MIN_PACK_VERSION_MINOR = (pack) -> range(pack).minInclusive().minor();
    public static final Function<Pack,Number> MAX_PACK_VERSION_MAJOR = (pack) -> range(pack).maxInclusive().major();
    public static final Function<Pack,Number> MAX_PACK_VERSION_MINOR = (pack) -> range(pack).maxInclusive().minor();

    public static final InclusiveRange<PackFormat> range(Pack pack) { return ((ResourcePackProfileMetadataDuck)(Object)pack.metadata).customhud$getPackVersionRange(); }

    public static final Function<Pack,Boolean> PACK_ALWAYS_ENABLED = (pack) -> pack.isRequired();
    public static final Function<Pack,Boolean> PACK_IS_PINNED = (pack) -> pack.isFixedPosition();
    public static final Function<Pack,Boolean> PACK_IS_COMPATIBLE = (pack) -> pack.getCompatibility().isCompatible();


    // RECORDS
    public static final Function<MusicAndRecordTracker.RecordInstance,Component> RECORD_NAME = (rec) -> rec.name;
    public static final Function<MusicAndRecordTracker.RecordInstance, Identifier> RECORD_ID = (rec) -> rec.id;
    public static final NumEntry<MusicAndRecordTracker.RecordInstance> RECORD_LENGTH = Num.of(SEC_HMS, (rec) -> rec.length / 20F);
    public static final NumEntry<MusicAndRecordTracker.RecordInstance> RECORD_ELAPSED = Num.of(SEC_HMS, (rec) -> rec.elapsed / 20F);
    public static final NumEntry<MusicAndRecordTracker.RecordInstance> RECORD_REMAINING = Num.of(SEC_HMS, (rec) -> (rec.length - rec.elapsed) / 20F);
    public static final Function<MusicAndRecordTracker.RecordInstance,Number> RECORD_ELAPSED_PER = (rec) -> 100F * rec.elapsed / rec.length;




    // OFFERS
    public static final Function<MerchantOffer,Number> OFFER_USES = (offer) -> offer.getUses();
    public static final Function<MerchantOffer,Number> OFFER_MAX_USES = (offer) -> offer.getMaxUses();
    public static final Function<MerchantOffer,Number> OFFER_SPECIAL_PRICE = (offer) -> offer.getSpecialPriceDiff();
    public static final Function<MerchantOffer,Number> OFFER_DEMAND_BONUS = (offer) -> offer.getDemand();
    public static final Function<MerchantOffer,Number> OFFER_PRICE_MULTIPLIER = (offer) -> offer.getPriceMultiplier();
    public static final Function<MerchantOffer,Boolean> OFFER_DISABLED = (offer) -> offer.isOutOfStock();
    public static final Function<MerchantOffer,Boolean> OFFER_CAN_AFFORD = (offer) -> {
        if (offer.isOutOfStock() || CLIENT.player == null) return false;
        ItemStack first = offer.getCostA();
        ItemStack second = offer.getCostB();

        int amountOfFirst = 0;
        int amountOfSecond = 0;

        List<ItemStack> items = new ArrayList<>(37);
        items.add(CLIENT.player.getInventory().player.getOffhandItem());
        for (ItemStack stack : CLIENT.player.getInventory().getNonEquipmentItems())
            items.addAll(getItemItems(stack, true));

        for (ItemStack stack : items) {
            if (ItemStack.isSameItemSameComponents(first, stack))
                amountOfFirst += stack.getCount();
            else if (second != null && ItemStack.isSameItemSameComponents(second, stack))
                amountOfSecond += stack.getCount();

            if (amountOfFirst >= first.getCount() && (second == null || amountOfSecond >= second.getCount()) )
                return true;
        }
        return false;

    };

    public static final Function<ItemLike,Component> TAG_ENTRY_NAME = (convertible) -> convertible.asItem().getName(convertible.asItem().getDefaultInstance());
    public static final Function<ItemLike,Identifier> TAG_ENTRY_ID = (convertible) -> BuiltInRegistries.ITEM.getKey(convertible.asItem());


    //CHAT MESSAGES
    public static final Function<GuiMessage,Component> CHAT_MESSAGE_TEXT = (line) -> line.content();
    public static final NumEntry<GuiMessage> CHAT_MESSAGE_TIME_AGO = Num.of(TICKS_HMS, (line) -> line == null ? null : CLIENT.gui.hud.getGuiTicks() - line.addedTime());
    public static final Function<GuiMessage,String> CHAT_MESSAGE_TYPE = (line) -> {
        if (line == null) return null;
        if (line.tag() == null) return "Normal";
        if (line.tag() == GuiMessageTag.systemSinglePlayer()) return "Singpleplayer";
        return line.tag().logTag();
    };


    // PIE PROFILERS
    public static final Function<ComplexData.ProfilerTimingWithPath,String> TIMING_NAME = (timing) -> timing == null ? null : timing.name();
    public static final Function<ComplexData.ProfilerTimingWithPath,String> TIMING_PATH = (timing) -> timing == null ? null : timing.path().replace('\u001e', '.');
    public static final Function<ComplexData.ProfilerTimingWithPath,Number> TIMING_PER_OF_PARENT = (timing) -> timing == null ? null : timing.parent();
    public static final Function<ComplexData.ProfilerTimingWithPath,Number> TIMING_PER_OF_TOTAL = (timing) -> timing == null ? null : timing.total();
    public static final Function<ComplexData.ProfilerTimingWithPath,Number> TIMING_COLOR = (timing) -> timing == null ? null : timing.color();

    // HELPER METHODS


    public static SubtitleOverlay.SoundPlayedAt sound(Subtitle subtitle) {
        return subtitle.getBestSubtitleCandidate(CLIENT.getSoundManager().getListenerTransform().position());
    }

    public static int subtitle$getDirection(SubtitleOverlay.SoundPlayedAt sound) {
        var camera = CLIENT.getCameraEntity();
        float xRotation = -camera.getXRot() * ((float)Math.PI / 180);
        float yRotation = -camera.getYRot() * ((float)Math.PI / 180);

        Vec3 vec3d2 = new Vec3(0.0, 0.0, -1.0).xRot(xRotation).yRot(yRotation);
        Vec3 vec3d3 = new Vec3(0.0, 1.0, 0.0).xRot(xRotation).yRot(yRotation);
        Vec3 vec3d5 = sound.location().subtract(camera.getEyePosition()).normalize();
        double e = vec3d2.cross(vec3d3).dot(vec3d5);

        return -vec3d2.dot(vec3d5) > 0.5 || e == 0? 0 : e < 0 ? 1 : -1;
    }

    public static int blockstate$getPropertyType(Class<?> type) {
        return type == Boolean.class ? 1 : Number.class.isAssignableFrom(type) ? 2 : type.isEnum() ? 3 : 0;
    }

    public static boolean team$visibleToPlayer(PlayerTeam team, Team.Visibility rule) {
        return rule == Team.Visibility.ALWAYS
                || (rule == Team.Visibility.HIDE_FOR_OTHER_TEAMS && CLIENT.player.isAlliedTo(team))
                || (rule == Team.Visibility.HIDE_FOR_OWN_TEAM && !CLIENT.player.isAlliedTo(team));
    }


}
