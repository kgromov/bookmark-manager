package org.kgromov.model;


import java.net.URI;
import java.time.Instant;
import java.util.Set;

public record BookmarkNode(
        String name,
        URI href,
        Set<String> tags,
        Instant created,
        Instant modified,
        String cssSelector,
        String parentPath) {
}