package com.minenash.customhud.mixin;

import com.minenash.customhud.ProfileManager;
import com.minenash.customhud.data.Profile;
import com.minenash.customhud.data.Toggle;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyMapping.class)
public class KeyBindingMixin {

    @Inject(method = "click", at = @At("TAIL"))
    private static void checkKeybinds(InputConstants.Key key, CallbackInfo ci) {
        for (Profile p : ProfileManager.getProfiles()) {
            if (p.keyBinding.matches(key))
                ++p.keyBinding.clickCount;
        }
        Profile activeProfile = ProfileManager.getActive();
        if (activeProfile != null) {
            for (Toggle t : activeProfile.toggles.values()) {
                if (t.key.matches(key))
                    ++t.key.clickCount;
            }
        }

    }

}
