package org.kgromov.model;

import java.time.Instant;
import java.util.List;

public record BookmarkFolderNode(
        String name,
        Instant created,
        Instant modified,
        String parentPath,
        List<BookmarkFolderNode> subFolders,
        List<BookmarkNode> bookmarks
) {

    public BookmarkFolderNode(BookmarkFolder folder,
                              List<BookmarkFolderNode> subFolders,
                              List<BookmarkNode> bookmarks) {
        this(folder.name(),
                folder.created(),
                folder.modified(),
                folder.parentPath(),
                subFolders,
                bookmarks
        );
    }

    public String getPath() {
        return parentPath + '/' + name;
    }
}
