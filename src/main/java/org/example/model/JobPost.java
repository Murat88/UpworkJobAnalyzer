package org.example.model;

import java.util.List;

/**
 * Represents an Upwork job post relevant to the application decision.
 */
public record JobPost(
        String title,
        String description,
        List<String> requiredTechStack,
        ClientInfo client
) {
}

