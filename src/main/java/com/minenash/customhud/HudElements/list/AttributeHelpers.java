package com.minenash.customhud.HudElements.list;

import com.minenash.customhud.complex.ComplexData;
import com.minenash.customhud.mixin.accessors.AttributeContainerAccessor;
import com.minenash.customhud.mixin.accessors.BlockPredicatesComponentAccessor;
import com.minenash.customhud.mixin.accessors.BossHealthOverlayAccessor;
import com.minenash.customhud.mixin.accessors.DefaultAttributeContainerAccessor;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Pattern;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.commands.arguments.SlotArgument;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.AdventureModePredicate;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.Scoreboard;

import static com.minenash.customhud.CustomHud.CLIENT;

public class AttributeHelpers {

    public static final Function<String, Attribute> ENTITY_ATTR_READER = (src) -> BuiltInRegistries.ATTRIBUTE.getValue(Identifier.tryParse(src));
    public static final Function<String, Pack> DATA_PACK_READER = (src) ->
        CLIENT.getSingleplayerServer() == null ? null : CLIENT.getSingleplayerServer().getPackRepository().getPack(src);

    private static final Pattern TIMING_PERIOD_TO_SPECIAL = Pattern.compile("(?<!\\\\)\\.");
    public static final Function<String, String> PROFILER_TIMING_READER = (src) -> {
        src = TIMING_PERIOD_TO_SPECIAL.matcher(src).replaceAll("\u001e");
        if (!src.startsWith("root\u001e"))
            src = "root\u001e" + src;
        return src;
    };

    public static final Function<String, Integer> SLOT_READER = (src) -> {
        if (src.isBlank())
            return null;
        try {
            return SlotArgument.slot().parse(new StringReader(switch (src) {
                case "head", "chest", "legs", "feet" -> "armor." + src;
                case "mainhand", "offhand" -> "weapon." + src;
                case "main", "off" -> "weapon." + src + "hand";
                default -> {
                    if (src.length() < 2) yield src;
                    if (src.charAt(0) == 'h' && src.charAt(1) != 'o') yield "hotbar." + src.substring(1);
                    if (src.charAt(0) == 'i' && src.charAt(1) != 'n') yield "inventory." + src.substring(1);
                    yield src;
                }
            }));
        } catch (CommandSyntaxException e) {
            return null;
        }
    };

    public record ReceivedPower(Direction direction, int power, int strongPower) {}

    public static PlayerInfo getPlayer(String src) {
        PlayerInfo p = CLIENT.getConnection().getPlayerInfo(src);
        if (p != null)
            return p;
        try {
            return CLIENT.getConnection().getPlayerInfo(UUID.fromString(src));
        }
        catch (Exception ignored) {}
        return null;
    }

    public static Entity getFullEntity(Entity entity) {
        return CLIENT.getSingleplayerServer() == null || entity == null? entity :
                CLIENT.getSingleplayerServer().getLevel(entity.level().dimension()).getEntity(entity.getUUID());
    }
    public static AttributeInstance getEntityAttr(Entity entity, Attribute attribute) {
        Entity e = getFullEntity(entity);
        if (!(e instanceof LivingEntity le)) return null;
        return le.getAttribute(BuiltInRegistries.ATTRIBUTE.wrapAsHolder(attribute));
    }

    public static List<?> getEntityAttributes(Entity entity) {
        entity = getFullEntity(entity);
        if (!(entity instanceof LivingEntity le) ) return Collections.EMPTY_LIST;
        AttributeContainerAccessor container = (AttributeContainerAccessor) le.getAttributes();
        Map<Attribute, AttributeInstance> instances = new HashMap<>(((DefaultAttributeContainerAccessor)container.getSupplier()).getInstances());
        instances.putAll(container.getAttributes());
        return Arrays.asList( (entity.level().isClientSide() ?
                instances.values().stream().filter(a -> a.getAttribute().value().isClientSyncable()) : instances.values().stream())
                .sorted(Comparator.comparing(a -> I18n.get(a.getAttribute().value().getDescriptionId()))).toArray() );
    }

    public record ItemAttribute(Attribute attribute, AttributeModifier modifier, String slot) {}
    public static List<ItemAttribute> getItemStackAttributes(ItemStack stack) {
        List<ItemAttribute> attributes = new ArrayList<>();

        ItemAttributeModifiers component = stack.get(DataComponents.ATTRIBUTE_MODIFIERS);
        if (component != null)
            for (var entry : component.modifiers())
                attributes.add( new ItemAttribute(entry.attribute().value(), entry.modifier(), entry.slot().getSerializedName()) );
        return attributes;
    }

    public static List<Component> getLore(ItemStack stack) {
        ItemLore component = stack.get(DataComponents.LORE);
        return component != null ? component.lines() : new ArrayList<>();
    }

    public static List<Block> getCanX(ItemStack stack, DataComponentType<AdventureModePredicate> type) {
        AdventureModePredicate component = stack.get(type);
        Set<Block> blocks = new HashSet<>();
        if (component != null) {
            for (var e : ((BlockPredicatesComponentAccessor) component).getPredicates()) {
                if (e.blocks().isPresent())
                    for (var ee : e.blocks().get())
                        blocks.add(ee.value());
            }
        }
        return new ArrayList<>(blocks);
    }

    public static List<ItemStack> compactItems(List<ItemStack> stacks) {
        List<ItemStack> compact = new ArrayList<>();
        outer:
        for (ItemStack stack : stacks) {
            if (stack.isEmpty()) continue;
            for (ItemStack cStack : compact) {
                if (ItemStack.isSameItemSameComponents(stack, cStack)) {
                    cStack.setCount(cStack.getCount() + stack.getCount());
                    continue outer;
                }
            }
            compact.add(stack.copy());
        }
        return compact;
    }

    public static List<ItemStack> getItemItems(ItemStack stack, boolean returnStack) {
        if (stack.isEmpty())
            return returnStack ? Collections.singletonList(stack) : Collections.EMPTY_LIST;

        Iterator<ItemStack> iter = null;
        get_iter:
        {
            var bundle = stack.get(DataComponents.BUNDLE_CONTENTS);
            if (bundle != null) {
                iter = bundle.itemCopies().iterator();
                break get_iter;
            }
            var container = stack.get(DataComponents.CONTAINER);
            if (container != null) {
                iter = container.nonEmptyItemCopyStream().iterator();
                break get_iter;
            }
//            var blockEntity = stack.get(DataComponentTypes.BLOCK_ENTITY_DATA);
//            if (blockEntity != null) {
//                blockEntity.
//            }
        }

        if (iter == null || !iter.hasNext())
            return returnStack ? Collections.singletonList(stack) : Collections.EMPTY_LIST;

        int count = stack.getCount();
        List<ItemStack> items = new ArrayList<>();
        while (iter.hasNext()) {
            ItemStack item = iter.next();
            items.add(item.copyWithCount(item.getCount() * count));
        }
        return items;

//        NbtCompound nbt = stack.getNbt();
//        if (stack.isEmpty() || nbt == null)
//            return returnStack ? Collections.singletonList(stack) : Collections.EMPTY_LIST;
//        if (nbt.contains("Items", NbtElement.LIST_TYPE))
//            return getItemItemsInternal(stack, nbt.getList("Items", NbtElement.COMPOUND_TYPE), returnStack );
//        if (!nbt.contains("BlockEntityTag"))
//            return returnStack ? Collections.singletonList(stack) : Collections.EMPTY_LIST;
//        nbt = nbt.getCompound("BlockEntityTag");
//        if (!nbt.contains("Items", NbtElement.LIST_TYPE))
//            return returnStack ? Collections.singletonList(stack) : Collections.EMPTY_LIST;
//        return getItemItemsInternal(stack, nbt.getList("Items", NbtElement.COMPOUND_TYPE), returnStack );
    }
//    private static List<ItemStack> getItemItemsInternal(ItemStack stack, NbtList list, boolean returnStack) {
//        List<ItemStack> items = new ArrayList<>(list.size());
//
//        for(int i = 0; i < list.size(); ++i) {
//            List<ItemStack> inner = getItemItems(ItemStack.fromNbt(list.getCompound(i)), true);
//            for (ItemStack is : inner)
//                is.setCount(is.getCount() * stack.getCount());
//            items.addAll(inner);
//        }
//
//        if (items.isEmpty() && returnStack)
//            return Collections.singletonList(stack);
//
//        return items;
//    }

    public static Scoreboard scoreboard() {
        return CLIENT.getSingleplayerServer() != null ? CLIENT.getSingleplayerServer().getScoreboard() : CLIENT.level.getScoreboard();
    }
    public static boolean entryOnline(String entry) {
        if (null != (CLIENT.getSingleplayerServer() != null ? CLIENT.getSingleplayerServer().getPlayerList().getPlayerByName(entry) : CLIENT.getConnection().getPlayerInfo(entry)))
            return true;
        if (ComplexData.serverWorld == null)
            return false;
        try {
            return ComplexData.serverWorld.entityManager.isLoaded(UUID.fromString(entry));
        }
        catch (Exception ignored) {}
        return false;
    }

    public static List<?> bossbars(boolean all) {
        if (CLIENT.getSingleplayerServer() == null)
            return Arrays.asList(((BossHealthOverlayAccessor) CLIENT.gui.hud.getBossOverlay()).getEvents().entrySet().toArray());

        List<BossEvent> serverBossbars = new ArrayList<>();
        serverBossbars.addAll(CLIENT.getSingleplayerServer().getCustomBossEvents().events.values());
        serverBossbars.addAll(ComplexData.bossbars.values());

        if (all)
            return serverBossbars;

        Set<UUID> client = ((BossHealthOverlayAccessor) CLIENT.gui.hud.getBossOverlay()).getEvents().keySet();
        return Arrays.asList( serverBossbars.stream().filter(bar -> client.contains(bar.getId())).toArray() );
    }

    public static BossEvent getBossBar(String input) {
        boolean client = CLIENT.getSingleplayerServer() == null;
        try {
            UUID uuid = UUID.fromString(input);
            if (client)
                return ((BossHealthOverlayAccessor) CLIENT.gui.hud.getBossOverlay()).getEvents().get(uuid);
            for (BossEvent bar : CLIENT.getSingleplayerServer().getCustomBossEvents().events.values())
                if (bar.getId() == uuid)
                    return bar;
            BossEvent bb = ComplexData.bossbars.get(uuid);
            if (bb != null)
                return bb;
        }
        catch (Exception ignored) {}

        if (client) {
            for (BossEvent bar : ((BossHealthOverlayAccessor) CLIENT.gui.hud.getBossOverlay()).getEvents().values())
                if (bar.getName().getString().equalsIgnoreCase(input))
                    return bar;
        }
        else {
            BossEvent bar = CLIENT.getSingleplayerServer().getCustomBossEvents().get(Identifier.tryParse(input));
            if (bar != null)
                return bar;
            for (BossEvent bar2 : CLIENT.getSingleplayerServer().getCustomBossEvents().events.values())
                if (bar2.getName().getString().equalsIgnoreCase(input))
                    return bar2;
            for (BossEvent bar2 : ComplexData.bossbars.values())
                if (bar2.getName().getString().equalsIgnoreCase(input))
                    return bar2;
        }
        return null;
    }

    public static int getBossBarColor(BossEvent bar) {
        return switch (bar.getColor()) {
            case PINK -> 0xEC00B8;
            case BLUE -> 0x00B7EC;
            case RED -> 0xEC3500;
            case GREEN -> 0x1DEC00;
            case YELLOW -> 0xE9EC00;
            case PURPLE -> 0x7B00EC;
            case WHITE -> 0xECECEC;
        };
    }

    public static double getRelativeYaw(Vec3 player, Vec3 other) {
        return Mth.wrapDegrees(CLIENT.player.getYRot() - Math.toDegrees( Mth.atan2(-(other.x() - player.x()), other.z() - player.z()) ));
    }
    public static double getRelativePitch(Vec3 player, Vec3 other) {
        double xDist = other.x() - player.x();
        double zDist = other.z() - player.z();
        return Mth.wrapDegrees(CLIENT.player.getXRot() + Math.toDegrees( Mth.atan2(other.y() - player.y(), Math.sqrt(xDist*xDist + zDist*zDist ) )));
    }

    public static boolean isFabricRP(Pack pack) {
        ComponentContents content = pack.location().title().getContents();
        return pack.getId().equals("fabric") || content instanceof TranslatableContents ttc && (ttc.getKey().equals("pack.name.fabricMod") || ttc.getKey().equals("pack.name.fabricMods"));
    }

}
