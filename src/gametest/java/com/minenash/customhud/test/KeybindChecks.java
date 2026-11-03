package com.minenash.customhud.test;

import com.minenash.customhud.ProfileManager;
import com.minenash.customhud.data.Profile;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;

import java.util.Map;

import static com.minenash.customhud.test.ProfileFixtures.check;

final class KeybindChecks {
    private KeybindChecks() {}

    static void run(ClientGameTestContext context, Map<String, Profile> profiles) {
        Profile first = profiles.get("HUD Test 00 - Vanilla");
        Profile second = profiles.get("HUD Test 01 - Normal");
        check(first != null && second != null, "Missing keybind fixtures");
        check(first.keyBinding != null && second.keyBinding != null, "Fixture profiles have no key mapping");

        context.setScreen(() -> null);
        context.runOnClient(client -> {
            ProfileManager.add(first);
            ProfileManager.add(second);
            ProfileManager.enabled = true;
        });
        try {
            bind(context, first, second, InputConstants.Type.KEYBOARD.getOrCreate(InputConstants.KEY_K));
            context.getInput().pressKey(InputConstants.KEY_K);
            context.waitTick();
            context.runOnClient(client -> check(ProfileManager.getActive() == second,
                    "Keyboard-bound profile key did not switch profile"));

            bind(context, first, second, InputConstants.Type.MOUSE.getOrCreate(InputConstants.MOUSE_BUTTON_4));
            context.getInput().pressMouse(InputConstants.MOUSE_BUTTON_4);
            context.waitTick();
            context.runOnClient(client -> check(ProfileManager.getActive() == second,
                    "Mouse-bound profile key did not switch profile"));

            bind(context, first, second, InputConstants.Type.KEYBOARD.getOrCreate(InputConstants.MOUSE_BUTTON_4));
            context.getInput().pressMouse(InputConstants.MOUSE_BUTTON_4);
            context.waitTick();
            context.runOnClient(client -> check(ProfileManager.getActive() == first,
                    "Mouse button 4 triggered a keyboard binding with the same code"));
        } finally {
            context.runOnClient(client -> {
                first.keyBinding.setKey(InputConstants.UNKNOWN);
                second.keyBinding.setKey(InputConstants.UNKNOWN);
                ProfileManager.setActive(null);
                ProfileManager.remove(first, false);
                ProfileManager.remove(second, false);
            });
        }
    }

    private static void bind(ClientGameTestContext context, Profile active, Profile target, InputConstants.Key key) {
        context.runOnClient(client -> {
            ProfileManager.setActive(active);
            target.keyBinding.setKey(key);
            while (target.keyBinding.consumeClick()) {}
        });
    }
}

