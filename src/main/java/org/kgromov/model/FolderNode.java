package org.kgromov.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

public record FolderNode(
        String name,
        Instant created,
        Instant modified,
        String parentPath,
        List<FolderNode> subFolders,
        List<BookmarkNode> bookmarks
) implements Node {

    public FolderNode(String name,
                      Instant created,
                      Instant modified,
                      String parentPath) {
        this(name, created, modified, parentPath, new ArrayList<>(), new ArrayList<>());
    }

    public String getPath() {
        return parentPath + '/' + name;
    }

    @Override
    public List<Node> children() {
        return Stream.concat(
                subFolders().stream(),
                bookmarks().stream()
        ).collect(toList());
    }
}
