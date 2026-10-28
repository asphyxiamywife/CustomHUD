package com.minenash.customhud.test;

import com.minenash.customhud.data.Crosshairs;
import com.mojang.blaze3d.platform.NativeImage;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static com.minenash.customhud.test.ProfileFixtures.check;

final class CrosshairChecks {
    private CrosshairChecks() {}

    static void run(Map<Crosshairs, Path> screenshots) {
        try (var input = Files.newInputStream(screenshots.get(Crosshairs.NONE)); var none = NativeImage.read(input)) {
            for (Crosshairs mode : new Crosshairs[]{Crosshairs.NORMAL, Crosshairs.DEBUG, Crosshairs.VANILLA}) {
                try (var imageInput = Files.newInputStream(screenshots.get(mode)); var image = NativeImage.read(imageInput)) {
                    check(image.getWidth() == none.getWidth() && image.getHeight() == none.getHeight(), "Screenshot size changed");
                    int changed = 0;
                    for (int y = image.getHeight() / 2 - 30; y < image.getHeight() / 2 + 30; y++) {
                        for (int x = image.getWidth() / 2 - 30; x < image.getWidth() / 2 + 30; x++) {
                            int a = image.getPixel(x, y), b = none.getPixel(x, y);
                            for (int shift : new int[]{0, 8, 16}) {
                                if (Math.abs(((a >>> shift) & 255) - ((b >>> shift) & 255)) > 10) {
                                    changed++;
                                    break;
                                }
                            }
                        }
                    }
                    check(changed >= 3, mode + " crosshair is missing from the rendered frame");
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
