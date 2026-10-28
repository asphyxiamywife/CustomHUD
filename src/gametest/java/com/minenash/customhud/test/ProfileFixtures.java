package com.minenash.customhud.test;

import com.minenash.customhud.data.Profile;
import com.minenash.customhud.errors.Errors;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.StandardOpenOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

final class ProfileFixtures {
    private ProfileFixtures() {}

    static Map<String, Profile> load() {
        try {
            return loadProfiles();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    static void report(String message) {
        try {
            Files.writeString(FabricLoader.getInstance().getGameDir().resolve("hud-test-results.txt"),
                    message + "\n", StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static Map<String, Profile> loadProfiles() throws IOException {
        Map<String, Profile> profiles = new LinkedHashMap<>();
        Path fixtures = FabricLoader.getInstance().getModContainer("custom_hud_tests")
                .orElseThrow().findPath("profiles").orElseThrow();
        try (var paths = Files.list(fixtures)) {
            for (Path path : paths.filter(p -> p.toString().endsWith(".txt")).sorted().toList()) {
                Profile profile = parse(path);
                profiles.put(profile.name, profile);
            }
        }
        check(profiles.size() == 13, "Expected thirteen profile fixtures, got " + profiles.keySet());

        Path examples = FabricLoader.getInstance().getModContainer("custom_hud")
                .orElseThrow().findPath("assets/custom_hud/profiles").orElseThrow();
        try (var paths = Files.list(examples)) {
            for (Path path : paths.filter(p -> p.toString().endsWith(".txt")).sorted().toList()) {
                Profile example = parse(path);
                profiles.put("Bundled - " + example.name, example);
            }
        }
        return profiles;
    }

    private static Profile parse(Path path) {
        String name = path.getFileName().toString().replaceFirst("\\.txt$", "");
        Profile profile = Profile.parseProfile(path, name);
        check(profile != null && !Errors.hasErrors(name), name + ": " + Errors.getErrors(name));
        check(!profile.sections.isEmpty(), name + " has no sections");
        return profile;
    }

    static void check(boolean condition, String message) {
        if (!condition)
            throw new AssertionError(message);
    }
}
