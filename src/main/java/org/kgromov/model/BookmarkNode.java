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

    public BookmarkNode(Bookmark bookmark) {
        this(
                bookmark.name(),
                bookmark.href(),
                bookmark.tags(),
                bookmark.created(),
                bookmark.modified(),
                bookmark.cssSelector(),
                bookmark.parentPath()
        );
    }
}