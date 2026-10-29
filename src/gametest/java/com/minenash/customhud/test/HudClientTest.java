package com.minenash.customhud.test;

import com.minenash.customhud.ProfileManager;
import com.minenash.customhud.data.Crosshairs;
import com.minenash.customhud.data.Profile;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import org.spongepowered.asm.mixin.MixinEnvironment;

import java.nio.file.Path;
import java.util.EnumMap;
import java.util.Map;

import static com.minenash.customhud.test.ProfileFixtures.check;

@SuppressWarnings("UnstableApiUsage")
public final class HudClientTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        context.runOnClient(client -> MixinEnvironment.getCurrentEnvironment().audit());
        Map<String, Profile> profiles = context.computeOnClient(client -> ProfileFixtures.load());
        context.getInput().resizeWindow(1280, 720);
        try (var world = context.worldBuilder().create()) {
            world.getConnection().waitForChunksRender();
            context.runOnClient(client -> {
                client.options.guiScale().set(3);
                client.resizeGui();
            });
            for (String command : new String[]{
                    "gamemode survival @a",
                    "experience set @a 12 levels",
                    "experience add @a 3 points",
                    "item replace entity @a armor.chest with minecraft:diamond_chestplate",
                    "item replace entity @a weapon.mainhand with minecraft:diamond_sword",
                    "bossbar add customhud:test \"CustomHUD test boss bar\"",
                    "bossbar set customhud:test players @a",
                    "bossbar set customhud:test value 65",
                    "scoreboard objectives add hud_test dummy",
                    "scoreboard objectives setdisplay sidebar hud_test",
                    "scoreboard players set FIXTURE hud_test 42"}) {
                world.getServer().runCommand(command);
            }
            world.getServer().runOnServer(server -> world.getConnection().getServerPlayer().addEffect(
                    new MobEffectInstance(MobEffects.SPEED, Integer.MAX_VALUE, 0, false, false, true)));
            context.waitTicks(2);
            world.getConnection().waitForClientboundPackets();
            context.runOnClient(client -> {
                client.gui.toastManager().clear();
                client.gui.hud.getChat().clearMessages(true);
            });
            context.getInput().lookAt(0, -45);
            Map<Crosshairs, Path> crosshairs = new EnumMap<>(Crosshairs.class);
            for (var entry : profiles.entrySet()) {
                context.runOnClient(client -> {
                    ProfileManager.setActive(entry.getValue());
                    ProfileManager.enabled = true;
                });
                context.waitTicks(3);
                context.runOnClient(client -> client.gui.toastManager().clear());
                Path screenshot = context.takeScreenshot(entry.getKey().replace(' ', '_'));
                if (entry.getKey().matches("HUD Test 0[0-3] - .*"))
                    crosshairs.put(entry.getValue().crosshair, screenshot);
            }

            CrosshairChecks.run(crosshairs);

            ValueChecks.run(context, world, profiles);
            DebugShortcutChecks.run(context, world, profiles);

            Profile scaled = profiles.get("HUD Test 05 - HUD Scale");
            check(scaled != null, "Missing scaling fixture");
            context.runOnClient(client -> HudScaleChecks.run(client, scaled));
            context.runOnClient(client -> ProfileManager.setActive(scaled));
            context.waitTicks(3);
            context.takeScreenshot("scale_normal");
            context.setScreen(() -> new ChatScreen("", false));
            context.waitTicks(3);
            context.takeScreenshot("scale_chat");
            context.setScreen(() -> new PauseScreen(true));
            context.waitTicks(3);
            context.takeScreenshot("scale_pause");
            context.setScreen(() -> null);

            context.runOnClient(client -> {
                ProfileManager.setActive(profiles.get("HUD Test 04 - Hidden"));
                ProfileManager.enabled = false;
            });
            context.waitTicks(3);
            context.takeScreenshot("customhud_disabled");

            Profile rendering = profiles.get("HUD Test 06 - Rendering");
            context.runOnClient(client -> {
                ProfileManager.enabled = true;
                ProfileManager.setActive(rendering);
            });
            DebugShortcutChecks.reloadResources(context);
            context.waitTicks(3);
            context.takeScreenshot("rendering_after_reload");
            context.runOnClient(client -> check(ProfileManager.getActive() != null, "Profile lost after reload"));
        } finally {
            context.runOnClient(client -> ProfileManager.enabled = false);
        }
        context.runOnClient(client -> {
            check(profiles.get("HUD Test 02 - Debug").crosshair == Crosshairs.DEBUG, "Debug mode parsed incorrectly");
            check(profiles.get("HUD Test 03 - None").crosshair == Crosshairs.NONE, "None mode parsed incorrectly");
            ProfileFixtures.report(
                    "PASS: mixin audit; thirteen fixtures and bundled profiles; twelve scale combinations; "
                            + "rendered normal/debug/vanilla crosshairs; anchor/pose restoration; no override; exception cleanup; world/chat/pause screenshots; resource reload");
        });
    }
}
