package com.minenash.customhud.HudElements.list;

import com.google.common.collect.Lists;
import com.minenash.customhud.HudElements.list.AttributeHelpers.ReceivedPower;
import com.minenash.customhud.HudElements.list.ListProvider.EventListProvider;
import com.minenash.customhud.complex.ComplexData;
import com.minenash.customhud.complex.MusicAndRecordTracker;
import com.minenash.customhud.complex.SubtitleTracker;
import com.minenash.customhud.mixin.accessors.ChatComponentAccessor;
import com.terraformersmc.modmenu.ModMenu;
import com.terraformersmc.modmenu.util.mod.Mod;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import net.minecraft.Optionull;
import net.minecraft.client.multiplayer.chat.GuiMessage;
import net.minecraft.client.gui.components.SubtitleOverlay;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.server.bossevents.CustomBossEvent;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.tags.TagKey;
import com.minenash.customhud.util.Tuple;
import net.minecraft.world.BossEvent;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.GameType;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.PlayerScoreEntry;
import net.minecraft.world.scores.PlayerTeam;

import static com.minenash.customhud.CustomHud.CLIENT;
import static com.minenash.customhud.HudElements.list.AttributeHelpers.*;
import static net.minecraft.world.entity.player.Inventory.SLOT_OFFHAND;

@SuppressWarnings("DataFlowIssue")
public class ListSuppliers {
    
    private static final int HEAD_SLOT = EquipmentSlot.HEAD.getIndex(Inventory.INVENTORY_SIZE);
    private static final int CHEST_SLOT = EquipmentSlot.CHEST.getIndex(Inventory.INVENTORY_SIZE);
    private static final int LEGS_SLOT = EquipmentSlot.LEGS.getIndex(Inventory.INVENTORY_SIZE);
    private static final int FEET_SLOT = EquipmentSlot.FEET.getIndex(Inventory.INVENTORY_SIZE);
    
    public static final Direction[] DIRS = new Direction[]{Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST, Direction.UP, Direction.DOWN};
    public static final Comparator<PlayerInfo> ENTRY_ORDERING =
            Comparator.comparingInt((PlayerInfo entry) -> entry.getGameMode() == GameType.SPECTATOR ? 1 : 0)
                    .thenComparing((entry) -> Optionull.mapOrDefault(entry.getTeam(), PlayerTeam::getName, ""))
                    .thenComparing((entry) -> entry.getProfile().name(), String::compareToIgnoreCase);
    public static final Comparator<PlayerScoreEntry> SCORE_DISPLAY_ORDER =
            Comparator.comparing(PlayerScoreEntry::value).reversed().thenComparing(PlayerScoreEntry::owner, String.CASE_INSENSITIVE_ORDER);


    public static final List<String> IGNORE_MODS = List.of("minecraft", "fabricloader", "java");
    public static final Comparator<?> MOD_ORDERING = Comparator.comparing(mod -> ((Mod) mod).getTranslatedName().toLowerCase(Locale.ROOT));
    public static final Predicate<?> MOD_PREDICATE = (mod) -> !(((Mod) mod).isHidden() || ((Mod) mod).getBadges().contains(Mod.Badge.LIBRARY) || ((Mod) mod).getBadges().contains(Mod.Badge.MINECRAFT));
    public static final Predicate<?> ALL_ROOT_MODS_PREDICATE = (mod) -> !(((Mod) mod).isHidden() || IGNORE_MODS.contains(((Mod) mod).getId()));
    public static final Comparator<MobEffectInstance> EFFECT_ORDERING = Comparator.comparingInt(e -> e.getDuration() == -1 ? Integer.MAX_VALUE : e.getDuration());
//    public static final Comparator<StatusEffectInstance> ALL_EFFECT_ORDERING = Comparator.comparingLong(e -> (3000000000L * e.getEffectType().getCategory().ordinal()) + (e.getDuration() == -1 ? Integer.MAX_VALUE : e.getDuration()));

    public static final ListProvider
    STATUS_EFFECTS = () -> Arrays.asList(CLIENT.player.getActiveEffects().stream().sorted(EFFECT_ORDERING).toArray()),
    STATUS_EFFECTS_POSITIVE = () -> Arrays.asList(CLIENT.player.getActiveEffects().stream().filter(e -> e.getEffect().value().getCategory() == MobEffectCategory.BENEFICIAL).sorted(EFFECT_ORDERING).toArray()),
    STATUS_EFFECTS_NEGATIVE = () -> Arrays.asList(CLIENT.player.getActiveEffects().stream().filter(e -> e.getEffect().value().getCategory() == MobEffectCategory.HARMFUL).sorted(EFFECT_ORDERING).toArray()),
    STATUS_EFFECTS_NEUTRAL = () -> Arrays.asList(CLIENT.player.getActiveEffects().stream().filter(e -> e.getEffect().value().getCategory() == MobEffectCategory.NEUTRAL).sorted(EFFECT_ORDERING).toArray()),

    ONLINE_PLAYERS = () -> Arrays.asList(CLIENT.getConnection().getOnlinePlayers().stream().sorted(ENTRY_ORDERING).toArray()),
    SUBTITLES = () -> SubtitleTracker.INSTANCE.entries,

    TARGET_BLOCK_STATES = () -> ComplexData.targetBlock == null ? Collections.EMPTY_LIST : ComplexData.targetBlock.getValues().toList(),
    TARGET_BLOCK_TAGS = () -> Collections.EMPTY_LIST,
    TARGET_FLUID_STATES = () -> ComplexData.targetFluid == null ? Collections.EMPTY_LIST : ComplexData.targetFluid.getValues().toList(),
    TARGET_FLUID_TAGS = () -> Collections.EMPTY_LIST,

    TARGET_BLOCK_POWERS = () -> {
        if (ComplexData.targetBlockPos == null) return Collections.EMPTY_LIST;
        List<ReceivedPower> powers = new ArrayList<>(6);
        for (Direction d : DIRS) {
            BlockPos pos = ComplexData.targetBlockPos.relative(d);
            powers.add(new ReceivedPower(d, CLIENT.level.getSignal(pos, d), CLIENT.level.getDirectSignal(pos, d)));
        }
        return powers;
    },

    PLAYER_ATTRIBUTES = () -> getEntityAttributes(CLIENT.player),
    TARGET_ENTITY_ATTRIBUTES = () -> ComplexData.targetEntity == null ? Collections.EMPTY_LIST : getEntityAttributes(ComplexData.targetEntity),
    HOOKED_ENTITY_ATTRIBUTES = () -> hooked() == null ? Collections.EMPTY_LIST : getEntityAttributes(hooked()),
    TEAMS = () -> Arrays.asList(CLIENT.level.getScoreboard().getPlayerTeams().toArray()),
    TARGET_VILLAGER_OFFERS = () -> ComplexData.villagerOffers,

    ITEMS = () -> AttributeHelpers.compactItems(CLIENT.player.getInventory().getNonEquipmentItems()),
    INV_ITEMS = () -> CLIENT.player.getInventory().getNonEquipmentItems().subList(9, CLIENT.player.getInventory().getNonEquipmentItems().size()),
    ARMOR_ITEMS = () -> {
        Inventory inv = CLIENT.player.getInventory();
        return List.of(
                inv.getItem(HEAD_SLOT),
                inv.getItem(CHEST_SLOT),
                inv.getItem(LEGS_SLOT),
                inv.getItem(FEET_SLOT)
        );
    },
    HOTBAR_ITEMS = () -> CLIENT.player.getInventory().getNonEquipmentItems().subList(0, 9),
    ALL_ITEMS = () -> {
        Inventory inv = CLIENT.player.getInventory();
        List<ItemStack> items = new ArrayList<>(inv.getNonEquipmentItems());
        items.add(inv.getItem(HEAD_SLOT));
        items.add(inv.getItem(CHEST_SLOT));
        items.add(inv.getItem(LEGS_SLOT));
        items.add(inv.getItem(FEET_SLOT));
        items.add(inv.getItem(SLOT_OFFHAND));
        return items;
    },
    EQUIPPED_ITEMS = () -> {
        Inventory inv = CLIENT.player.getInventory();
        List<ItemStack> items = new ArrayList<>(5);
        items.add(inv.getItem(HEAD_SLOT));
        items.add(inv.getItem(CHEST_SLOT));
        items.add(inv.getItem(LEGS_SLOT));
        items.add(inv.getItem(FEET_SLOT));
        items.add(inv.getSelectedItem());
        items.add(inv.getItem(SLOT_OFFHAND));
        return items;
    },
    ITEMS_UNPACKED = () -> {
        List<ItemStack> items = new ArrayList<>(36);
        for (ItemStack stack : CLIENT.player.getInventory().getNonEquipmentItems()) {
            List<ItemStack> innerItems = getItemItems(stack, true);
            if (innerItems.isEmpty())
                items.add(stack);
            else
                items.addAll(innerItems);
        }
        return AttributeHelpers.compactItems(items);
    },

    SCOREBOARD_OBJECTIVES = () -> Arrays.asList(scoreboard().getObjectives().toArray()),
            PLAYER_SCOREBOARD_SCORES = () -> Arrays.asList(scoreboard().getOrCreatePlayerInfo(CLIENT.getGameProfile().name()).scores.entrySet().toArray()),

    BOSSBARS = () -> bossbars(false),
    ALL_BOSSBARS = () -> bossbars(true),

    RECORDS = () -> MusicAndRecordTracker.records,

    MODS = () -> Arrays.asList(ModMenu.ROOT_MODS.values().stream().filter((Predicate<? super Mod>) MOD_PREDICATE).sorted((Comparator<? super Mod>) MOD_ORDERING).toArray()),
    ALL_ROOT_MODS = () -> Arrays.asList(ModMenu.ROOT_MODS.values().stream().filter((Predicate<? super Mod>) ALL_ROOT_MODS_PREDICATE).sorted((Comparator<? super Mod>) MOD_ORDERING).toArray()),
    ALL_MODS = () -> Arrays.asList(ModMenu.MODS.values().stream().sorted((Comparator<? super Mod>) MOD_ORDERING).toArray()),

    RESOURCE_PACKS = () -> {
        List<Pack> packs = new ArrayList<>(CLIENT.getResourcePackRepository().getSelectedPacks());
        packs.removeIf(pack -> AttributeHelpers.isFabricRP(pack) || pack.getId().equals("vanilla"));
        Collections.reverse(packs);
        return packs;
    },
    DISABLED_RESOURCE_PACKS = () -> {
        List<Pack> profiles = Lists.newArrayList(CLIENT.getResourcePackRepository().getAvailablePacks());
        profiles.removeAll(CLIENT.getResourcePackRepository().getSelectedPacks());
        Collections.reverse(profiles);
        return profiles;
    },
    DATA_PACKS = () -> CLIENT.getSingleplayerServer() == null ? Collections.EMPTY_LIST : Arrays.asList(CLIENT.getSingleplayerServer().getPackRepository().getSelectedPacks().toArray()),
    DISABLED_DATA_PACKS = () -> {
        if (CLIENT.getSingleplayerServer() == null) return Collections.EMPTY_LIST;

        PackRepository manager = CLIENT.getSingleplayerServer().getPackRepository();
        List<Pack> profiles = Lists.newArrayList(manager.getAvailablePacks());
        profiles.removeAll(manager.getSelectedPacks());
        return profiles;
    },

    CHAT_MESSAGES = () -> ((ChatComponentAccessor) CLIENT.gui.hud.getChat()).getAllMessages(),

    PROFILER_TIMINGS = () -> ComplexData.rootEntries;

    public static final Function<SubtitleOverlay.Subtitle, List<?>> SUBTITLE_SOUNDS = (entry) -> new ArrayList<>(entry.playedAt);
    public static final Function<ComplexData.ProfilerTimingWithPath, List<?>> TIMINGS_SUB_ENTRIES = (timing) -> timing == null ? Collections.EMPTY_LIST : timing.entries();

    public static final Function<AttributeInstance, List<?>> ATTRIBUTE_MODIFIERS = (attr) -> new ArrayList<>(attr.getModifiers());
    public static final Function<PlayerTeam, List<?>> TEAM_MEMBERS = (team) -> Arrays.asList(team.getPlayers().toArray());
    public static final Function<PlayerTeam, List<?>> TEAM_PLAYERS = (team) -> CLIENT.getConnection().getOnlinePlayers().stream().filter(p -> p.getTeam() == team).sorted(ENTRY_ORDERING).toList();

    public static final Function<ItemStack, List<?>> ITEM_ATTRIBUTES = AttributeHelpers::getItemStackAttributes;
    public static final Function<ItemStack, List<?>> ITEM_ENCHANTS = (stack) -> new ArrayList<>(stack.getEnchantments().entrySet());
    public static final Function<ItemStack, List<?>> ITEM_LORE_LINES = AttributeHelpers::getLore;
    public static final Function<ItemStack, List<?>> ITEM_CAN_DESTROY = (stack) -> getCanX(stack, DataComponents.CAN_BREAK);
    public static final Function<ItemStack, List<?>> ITEM_CAN_PLAY_ON = (stack) -> getCanX(stack, DataComponents.CAN_PLACE_ON);
    public static final Function<ItemStack, List<?>> ITEM_TAGS = (stack) -> Collections.EMPTY_LIST;
    public static final Function<ItemStack, List<?>> ITEM_ITEMS = (stack) -> getItemItems(stack, false);
    public static final Function<ItemStack, List<?>> ITEM_ITEMS_COMPACT = (stack) -> compactItems(getItemItems(stack, false));

    public static final Function<BossEvent, List<?>> BOSSBAR_PLAYERS = (bar) -> {
        if (CLIENT.getSingleplayerServer() == null || !(bar instanceof CustomBossEvent cboss)) return Collections.EMPTY_LIST;

        List<?> listPlayers = Arrays.asList(CLIENT.getConnection().getOnlinePlayers().toArray());
        List<PlayerInfo> out = new ArrayList<>(cboss.getPlayers().size());

        for (var player : cboss.getPlayers())
            for (var listPlayer : listPlayers)
                if (player.getUUID().equals(((PlayerInfo) listPlayer).getProfile().id()))
                    out.add((PlayerInfo) listPlayer);

        return out;
    };


    public static final Function<Objective, List<?>> SCOREBOARD_OBJECTIVE_SCORES = (obj) -> scoreboard().listPlayerScores(obj).stream().sorted(SCORE_DISPLAY_ORDER).toList();
    public static final Function<Objective, List<?>> SCOREBOARD_OBJECTIVE_SCORES_ONLINE = (obj) -> scoreboard().listPlayerScores(obj).stream()
            .filter(score -> entryOnline(score.owner()))
            .sorted(SCORE_DISPLAY_ORDER).toList();


    public static ListProvider SCORES(String name) {
        return () -> Arrays.asList(scoreboard().getOrCreatePlayerInfo(name).scores.entrySet().toArray());
    }

    public static <T> ListProvider TAG_ENTRIES(Registry<T> registry, String name) {
        if (name.startsWith("#"))
            name = name.substring(1);
        Identifier id = Identifier.tryParse(name);
        if (id == null)
            return null;
        return () -> {
            List<T> values = new ArrayList<>();
            for (Holder<T> entry : registry.getTagOrEmpty(TagKey.create(registry.key(), id)))
                values.add(entry.value());
            return values;
        };
    }


    // Don't change to method references
    public static final Function<?, List<?>> MOD_AUTHORS = (mod) -> ((Mod) mod).getAuthors();
    public static final Function<?, List<?>> MOD_CONTRIBUTORS = (mod) -> {
        List<Tuple<String, String>> contributors = new ArrayList<>();
        for (var e : ((Mod) mod).getContributors().entrySet()) {
            String set = e.getKey();
            for (var contributor : e.getValue()) {
                contributors.add(new Tuple<>(contributor, set));
            }
        }
        return contributors;
    };
    public static final Function<?, List<?>> MOD_CREDITS = (mod) -> {
        List<Tuple<String, String>> credits = new ArrayList<>();
        for (var e : ((Mod) mod).getCredits().entrySet()) {
            String set = e.getKey();
            for (var contributor : e.getValue()) {
                credits.add(new Tuple<>(contributor, set));
            }
        }
        return credits;
    };
    public static final Function<?, List<?>> MOD_BADGES = (mod) -> Arrays.asList(((Mod) mod).getBadges().toArray());
    public static final Function<?, List<?>> MOD_LICENSES = (mod) -> Arrays.asList(((Mod) mod).getLicense().toArray());
    public static final Function<?, List<?>> MOD_PARENTS = (mod) -> {
        Mod parent = ModMenu.MODS.get(((Mod) mod).getParent());
        return parent == null ? Collections.emptyList() : Collections.singletonList(parent);
    };
    public static final Function<?, List<?>> MOD_CHILDREN = (mod) -> ModMenu.PARENT_MAP.get(((Mod) mod));


    public static final Function<MerchantOffer, List<?>> OFFER_FIRST_ADJUSTED = (offer) -> Collections.singletonList(offer.getCostA());
    public static final Function<MerchantOffer, List<?>> OFFER_FIRST_BASE = (offer) -> Collections.singletonList(offer.getBaseCostA());
    public static final Function<MerchantOffer, List<?>> OFFER_SECOND = (offer) -> Collections.singletonList(offer.getCostB());
    public static final Function<MerchantOffer, List<?>> OFFER_RESULT = (offer) -> Collections.singletonList(offer.getResult());


    private static Entity hooked() {
        return CLIENT.player.fishing == null ? null : CLIENT.player.fishing.getHookedIn();
    }


    // EVENTS

    public static EventListProvider<GuiMessage> ON_CHAT_MESSAGE = new EventListProvider<>("chat_message");


}
