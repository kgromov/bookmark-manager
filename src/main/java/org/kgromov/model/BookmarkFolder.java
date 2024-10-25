package org.kgromov.model;

import java.time.Instant;

public record BookmarkFolder(
        String name,
        Instant created,
        Instant modified,
        String parentPath) {

    public String getPath() {
        return parentPath + '/' + name;
    }
}