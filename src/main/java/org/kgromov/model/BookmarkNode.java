package org.kgromov.model;


import java.net.URI;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static java.util.Collections.emptyList;

public record BookmarkNode(
        String name,
        URI href,
        Set<String> tags,
        Instant created,
        Instant modified,
        String cssSelector,
        String parentPath) implements Node {
    @Override
    public String getPath() {
        return this.parentPath;
    }

    @Override
    public List<Node> children() {
        return emptyList();
    }
}