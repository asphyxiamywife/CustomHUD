package com.minenash.customhud.test;

import com.minenash.customhud.ProfileManager;
import com.minenash.customhud.VariableParser;
import com.minenash.customhud.HudElements.interfaces.HudElement;
import com.minenash.customhud.HudElements.list.ListProviderSet;
import com.minenash.customhud.complex.ComplexData;
import com.minenash.customhud.data.Profile;
import com.minenash.customhud.errors.Errors;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;

import java.util.Map;

import static com.minenash.customhud.test.ProfileFixtures.check;

final class ValueChecks {
    private ValueChecks() {}

    static void run(ClientGameTestContext context, TestSingleplayerContext world, Map<String, Profile> profiles) {
        Profile player = profiles.get("HUD Test 09 - Player Values");
        context.runOnClient(client -> ProfileManager.setActive(player));
        context.waitTicks(3);
        context.runOnClient(client -> {
            number(player, "health", client.player.getHealth());
            number(player, "max_health", client.player.getMaxHealth());
            number(player, "food", client.player.getFoodData().getFoodLevel());
            number(player, "armor", client.player.getArmorValue());
            number(player, "xp_level", client.player.experienceLevel);
            number(player, "air", 20);
            number(player, "x", client.player.getX());
            number(player, "y", client.player.getY());
            number(player, "z", client.player.getZ());
            check(value(player, "singleplayer").getBoolean(), "Singleplayer value is false");
            check(value(player, "survival").getBoolean(), "Survival value is false");
            check(!value(player, "creative").getBoolean(), "Creative value is true in survival");
            check(value(player, "username").getString().equals(client.getUser().getName()), "Username mismatch");
            check(!value(player, "item:main").getString().isBlank(), "Held item value is empty");
        });
        world.getServer().runOnServer(server -> world.getConnection().getServerPlayer().setHealth(14));
        context.waitTicks(2);
        world.getConnection().waitForClientboundPackets();
        context.waitFor(client -> client.player.getHealth() == 14);
        context.runOnClient(client -> number(player, "health", 14));
        context.takeScreenshot("values_player_health_changed");

        Profile target = profiles.get("HUD Test 10 - Targets");
        context.runOnClient(client -> ProfileManager.setActive(target));
        world.getServer().runCommand("execute at @p run setblock ~2 ~1 ~4 minecraft:oak_log[axis=x]");
        context.waitTick();
        world.getConnection().waitForClientboundPackets();
        context.getInput().lookAt(context.computeOnClient(client -> client.player.blockPosition().offset(2, 1, 4)));
        context.waitTicks(3);
        context.runOnClient(client -> {
            ComplexData.update(target);
            check(value(target, "target_block_id").getString().equals("minecraft:oak_log"), "Target block did not update");
        });
        context.takeScreenshot("values_target_block_properties");
        world.getServer().runCommand("execute at @p run setblock ~2 ~1 ~4 minecraft:water");
        context.waitTick();
        world.getConnection().waitForClientboundPackets();
        context.waitTicks(3);
        context.runOnClient(client -> {
            ComplexData.update(target);
            check(value(target, "target_fluid_id").getString().equals("minecraft:water"), "Target fluid did not update");
        });
        context.takeScreenshot("values_target_fluid");
        world.getServer().runCommand("execute at @p run fill ~-8 ~ ~-4 ~12 ~2 ~12 minecraft:air replace minecraft:water");
        world.getServer().runCommand("execute at @p run setblock ~2 ~1 ~4 minecraft:oak_log[axis=x]");
        context.waitTick();
        world.getConnection().waitForClientboundPackets();
        context.runOnClient(client -> ProfileFixtures.report("PASS: evaluated player/world values, live health update, target block/fluid and property list"));
    }

    static HudElement value(Profile profile, String name) {
        HudElement value = VariableParser.parseElement("{" + name + "}", profile, 0, profile.enabled, new ListProviderSet());
        check(value != null && !Errors.hasErrors(profile.name), "Invalid value " + name + ": " + Errors.getErrors(profile.name));
        return value;
    }

    private static void number(Profile profile, String name, double expected) {
        Number actual = value(profile, name).getNumber();
        check(actual != null && Math.abs(actual.doubleValue() - expected) < 0.001,
                name + ": expected " + expected + ", got " + actual);
    }
}
