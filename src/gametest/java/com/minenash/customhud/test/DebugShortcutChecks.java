package com.minenash.customhud.test;

import com.minenash.customhud.ProfileManager;
import com.minenash.customhud.complex.ComplexData;
import com.minenash.customhud.data.Profile;
import com.minenash.customhud.render.CustomHudRenderer3;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.debug.DebugScreenEntries;
import net.minecraft.client.gui.components.debug.DebugScreenEntryStatus;
import net.minecraft.client.gui.components.debug.DebugScreenProfile;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.debug.DebugOptionsScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.renderer.state.gui.GuiRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import static com.minenash.customhud.test.ProfileFixtures.check;

final class DebugShortcutChecks {
    private DebugShortcutChecks() {}

    static void run(ClientGameTestContext context, TestSingleplayerContext world, Map<String, Profile> profiles) {
        context.runOnClient(client -> {
            client.debugEntries.loadProfile(DebugScreenProfile.DEFAULT);
            client.debugEntries.setOverlayVisible(false);
            client.debugEntries.setStatus(DebugScreenEntries.ENTITY_HITBOXES, DebugScreenEntryStatus.NEVER);
            client.debugEntries.setStatus(DebugScreenEntries.CHUNK_BORDERS, DebugScreenEntryStatus.NEVER);
            client.debugEntries.setStatus(DebugScreenEntries.THREE_DIMENSIONAL_CROSSHAIR, DebugScreenEntryStatus.IN_OVERLAY);
        });
        world.getServer().runCommand("execute at @p run summon minecraft:cow ~ ~ ~4 {NoAI:1b,Silent:1b,Rotation:[90f,0f]}");
        context.waitTick();
        var cowType = context.computeOnClient(client -> BuiltInRegistries.ENTITY_TYPE.getValue(Identifier.parse("minecraft:cow")));
        context.waitFor(client -> {
            for (var entity : client.level.entitiesForRendering())
                if (entity.getType() == cowType) return true;
            return false;
        });
        context.getInput().lookAt(0, 8);
        context.runOnClient(client -> ProfileManager.setActive(profiles.get("HUD Test 10 - Targets")));
        context.waitTicks(3);
        context.runOnClient(client -> {
            ComplexData.update(ProfileManager.getActive());
            check(ValueChecks.value(ProfileManager.getActive(), "target_entity_id").getString().equals("minecraft:cow"),
                    "Cow is not targeted in the hitbox scene");
        });
        context.takeScreenshot("values_target_entity");

        List<Scenario> scenarios = List.of(
                new Scenario("normal", profiles.get("HUD Test 01 - Normal"), true),
                new Scenario("forced_profiler", profiles.get("HUD Test 11 - Profiler"), true),
                new Scenario("metrics_charts", profiles.get("HUD Test 12 - Metrics"), true),
                new Scenario("mod_disabled", profiles.get("HUD Test 01 - Normal"), false));
        for (Scenario scenario : scenarios) {
            context.runOnClient(client -> {
                ProfileManager.setActive(scenario.profile());
                ProfileManager.enabled = scenario.enabled();
            });
            capture(context, scenario.name() + "_baseline");
            assertProfiler(context, scenario);
            context.getInput().pressKey(InputConstants.KEY_F3);
            context.runOnClient(client -> {
                check(client.debugEntries.isOverlayVisible(), scenario.name() + ": F3 did not open the overlay");
                check(!hasCustomText(client), "CustomHUD should yield to the F3 overlay");
            });
            capture(context, scenario.name() + "_f3_on");
            context.getInput().pressKey(InputConstants.KEY_F3);
            context.runOnClient(client -> {
                check(!client.debugEntries.isOverlayVisible(), scenario.name() + ": F3 did not close the overlay");
                check(hasCustomText(client) == scenario.enabled(), "CustomHUD visibility did not restore after F3");
            });
            capture(context, scenario.name() + "_f3_off");
            toggleEntry(context, InputConstants.KEY_B, DebugScreenEntries.ENTITY_HITBOXES, true);
            capture(context, scenario.name() + "_hitboxes_on");
            assertProfiler(context, scenario);
            toggleEntry(context, InputConstants.KEY_B, DebugScreenEntries.ENTITY_HITBOXES, false);
            capture(context, scenario.name() + "_hitboxes_off");
            toggleEntry(context, InputConstants.KEY_G, DebugScreenEntries.CHUNK_BORDERS, true);
            capture(context, scenario.name() + "_chunk_borders_on");
            assertProfiler(context, scenario);
            toggleEntry(context, InputConstants.KEY_G, DebugScreenEntries.CHUNK_BORDERS, false);
            capture(context, scenario.name() + "_chunk_borders_off");
            toggleEntry(context, InputConstants.KEY_B, DebugScreenEntries.ENTITY_HITBOXES, true);
            toggleEntry(context, InputConstants.KEY_G, DebugScreenEntries.CHUNK_BORDERS, true);
            capture(context, scenario.name() + "_hitboxes_and_borders");
            assertProfiler(context, scenario);
            toggleEntry(context, InputConstants.KEY_B, DebugScreenEntries.ENTITY_HITBOXES, false);
            toggleEntry(context, InputConstants.KEY_G, DebugScreenEntries.CHUNK_BORDERS, false);
        }

        context.runOnClient(client -> {
            ProfileManager.enabled = true;
            ProfileManager.setActive(profiles.get("HUD Test 01 - Normal"));
        });
        context.getInput().lookAt(0, -45);
        context.getInput().pressKey(InputConstants.KEY_F3);
        for (String name : List.of("HUD Test 00 - Vanilla", "HUD Test 01 - Normal", "HUD Test 02 - Debug", "HUD Test 03 - None")) {
            context.runOnClient(client -> ProfileManager.setActive(profiles.get(name)));
            capture(context, "crosshair_f3_on_" + name.substring(14));
        }
        context.getInput().pressKey(InputConstants.KEY_F3);
        context.runOnClient(client -> ProfileManager.setActive(profiles.get("HUD Test 01 - Normal")));
        for (Chart chart : List.of(
                new Chart("profiler", InputConstants.KEY_1, client -> client.getDebugOverlay().showProfilerChart()),
                new Chart("fps", InputConstants.KEY_2, client -> client.getDebugOverlay().showFpsCharts()),
                new Chart("network", InputConstants.KEY_3, client -> client.getDebugOverlay().showNetworkCharts()))) {
            chord(context, chart.key());
            context.runOnClient(client -> check(chart.enabled().test(client), chart.name() + " chart did not open"));
            capture(context, chart.name() + "_chart_on");
            chord(context, chart.key());
            context.runOnClient(client -> check(!chart.enabled().test(client), chart.name() + " chart did not close"));
            capture(context, chart.name() + "_chart_off");
        }

        // Vanilla chart shortcuts open F3 and leave it open after the chart is toggled off.
        context.runOnClient(client -> check(client.debugEntries.isOverlayVisible(), "Chart shortcuts did not open F3"));
        context.getInput().pressKey(InputConstants.KEY_F3);
        context.runOnClient(client -> check(!client.debugEntries.isOverlayVisible(), "F3 did not close after chart tests"));
        context.getInput().pressKey(InputConstants.KEY_F1);
        context.runOnClient(client -> check(client.options.hideGui && !hasCustomText(client), "F1 did not hide the HUD"));
        capture(context, "f1_hud_hidden");
        context.getInput().pressKey(InputConstants.KEY_F1);
        context.runOnClient(client -> check(!client.options.hideGui && hasCustomText(client), "F1 did not restore the HUD"));
        capture(context, "f1_hud_restored");
        tooltips(context);

        chord(context, InputConstants.KEY_F6);
        context.waitForScreen(DebugOptionsScreen.class);
        capture(context, "f3_f6_debug_options");
        chord(context, InputConstants.KEY_F6);
        context.waitForScreen(null);
        context.runOnClient(client -> client.getChatListener().handleSystemMessage(Component.literal("debug-clear-sentinel"), false));
        context.waitTick();
        chord(context, InputConstants.KEY_D);
        context.runOnClient(client -> check(client.gui.getChat().allMessages.stream()
                .noneMatch(message -> message.content().getString().contains("debug-clear-sentinel")), "F3+D did not clear chat"));
        capture(context, "f3_d_chat_cleared");
        toggleEntry(context, InputConstants.KEY_B, DebugScreenEntries.ENTITY_HITBOXES, true);
        toggleEntry(context, InputConstants.KEY_G, DebugScreenEntries.CHUNK_BORDERS, true);
        chord(context, InputConstants.KEY_A);
        world.getClientLevel().waitForChunksRender();
        context.runOnClient(client -> check(client.debugEntries.isCurrentlyEnabled(DebugScreenEntries.ENTITY_HITBOXES)
                && client.debugEntries.isCurrentlyEnabled(DebugScreenEntries.CHUNK_BORDERS), "F3+A lost debug toggles"));
        capture(context, "f3_a_chunks_reloaded");
        toggleEntry(context, InputConstants.KEY_B, DebugScreenEntries.ENTITY_HITBOXES, false);
        toggleEntry(context, InputConstants.KEY_G, DebugScreenEntries.CHUNK_BORDERS, false);
        context.runOnClient(client -> ProfileFixtures.report("PASS: real F3/B/G input with normal, forced-profiler, metrics and disabled profiles; charts, F1, tooltips, debug settings, chat clear and chunk reload"));
    }

    static void reloadResources(ClientGameTestContext context) {
        chord(context, InputConstants.KEY_T);
        context.waitFor(client -> client.getOverlay() != null);
        context.waitFor(client -> client.getOverlay() == null, 1200);
        context.runOnClient(client -> check(!client.debugEntries.isOverlayVisible(), "F3+T toggled the debug overlay"));
    }

    private static void toggleEntry(ClientGameTestContext context, int key, Identifier entry, boolean enabled) {
        chord(context, key);
        context.runOnClient(client -> {
            check(client.debugEntries.isCurrentlyEnabled(entry) == enabled, "Debug shortcut did not toggle " + entry);
            check(!client.debugEntries.isOverlayVisible(), "Debug chord unexpectedly toggled the F3 overlay");
        });
    }

    private static void chord(ClientGameTestContext context, int key) {
        context.getInput().holdKey(InputConstants.KEY_F3);
        try {
            context.getInput().pressKey(key);
        } finally {
            context.getInput().releaseKey(InputConstants.KEY_F3);
        }
        context.waitTick();
    }

    private static void capture(ClientGameTestContext context, String name) {
        context.waitTicks(3);
        context.runOnClient(client -> client.getToastManager().clear());
        context.takeScreenshot("debug_" + name);
    }

    private static boolean hasCustomText(Minecraft client) {
        var state = new GuiRenderState();
        CustomHudRenderer3.extractRenderState(new GuiGraphicsExtractor(client, state, 0, 0), DeltaTracker.ZERO);
        int[] count = {0};
        state.forEachText(text -> count[0]++);
        return count[0] > 0;
    }

    private static void assertProfiler(ClientGameTestContext context, Scenario scenario) {
        if (!scenario.name().equals("forced_profiler")) return;
        context.runOnClient(client -> {
            check(FrameProbe.profilerCharts == 1,
                    "Expected one profiler chart in the full frame, got " + FrameProbe.profilerCharts);
        });
    }

    private static void tooltips(ClientGameTestContext context) {
        boolean original = context.computeOnClient(client -> client.options.advancedItemTooltips);
        chord(context, InputConstants.KEY_H);
        context.runOnClient(client -> {
            check(client.options.advancedItemTooltips != original, "F3+H did not toggle advanced tooltips");
            check(Screen.getTooltipFromItem(client, client.player.getMainHandItem()).stream()
                    .anyMatch(line -> line.getString().contains("minecraft:diamond_sword")), "Advanced tooltip has no item ID");
        });
        context.setScreen(() -> new ItemTooltipScreen(Minecraft.getInstance().player));
        double[] mouse = context.computeOnClient(client -> ((ItemTooltipScreen) client.screen).itemPosition());
        context.getInput().setCursorPos(mouse[0], mouse[1]);
        capture(context, "f3_h_advanced_tooltip");
        context.setScreen(() -> null);
        chord(context, InputConstants.KEY_H);
        context.runOnClient(client -> check(client.options.advancedItemTooltips == original, "F3+H did not restore tooltips"));
        capture(context, "f3_h_tooltip_restored");
    }

    private static final class ItemTooltipScreen extends InventoryScreen {
        ItemTooltipScreen(Player player) { super(player); }
        double[] itemPosition() {
            var slot = menu.slots.stream().filter(value -> value.container == minecraft.player.getInventory()
                    && value.getContainerSlot() == 0).findFirst().orElseThrow();
            var window = minecraft.getWindow();
            return new double[]{(leftPos + slot.x + 8) * (double) window.getScreenWidth() / window.getGuiScaledWidth(),
                    (topPos + slot.y + 8) * (double) window.getScreenHeight() / window.getGuiScaledHeight()};
        }
    }

    private record Scenario(String name, Profile profile, boolean enabled) {}
    private record Chart(String name, int key, Predicate<Minecraft> enabled) {}
}
