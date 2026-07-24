package org.example.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

/**
 * Loads the freelancer's profile (resume/CV/LinkedIn summary) once from a
 * classpath resource, so the user of the app doesn't have to paste it in
 * every time.
 */
public class FreelancerProfileLoader {

    private static final String DEFAULT_RESOURCE = "freelancer-profile.md";

    public String load() {
        return load(DEFAULT_RESOURCE);
    }

    public String load(String resourceName) {
        try (InputStream is = FreelancerProfileLoader.class.getClassLoader().getResourceAsStream(resourceName)) {
            if (is == null) {
                throw new IllegalStateException("Could not find classpath resource: " + resourceName);
            }
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to read freelancer profile resource: " + resourceName, e);
        }
    }
}

