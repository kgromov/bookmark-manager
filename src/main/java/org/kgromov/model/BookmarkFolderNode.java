package org.kgromov.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public record BookmarkFolderNode(
        String name,
        Instant created,
        Instant modified,
        String parentPath,
        List<BookmarkFolderNode> subFolders,
        List<BookmarkNode> bookmarks
) {

    public BookmarkFolderNode(String name,
                              Instant created,
                              Instant modified,
                              String parentPath) {
        this(name, created, modified, parentPath, new ArrayList<>(), new ArrayList<>());
    }

    public String getPath() {
        return parentPath + '/' + name;
    }
}
