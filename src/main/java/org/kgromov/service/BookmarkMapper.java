package org.kgromov.service;

import org.jsoup.nodes.Element;
import org.kgromov.model.Bookmark;
import org.kgromov.model.BookmarkFolder;
import org.kgromov.model.BookmarkFolderNode;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.time.Instant;
import java.util.Set;

import static java.util.Optional.ofNullable;

@Component
public class BookmarkMapper {
    public BookmarkFolder mapToBookmarkFolder(Element folderNode, BookmarkFolder parentFolder) {
        return mapToBookmarkFolder(folderNode,
                ofNullable(parentFolder).map(BookmarkFolder::getPath).orElse("")
        );
    }

    public BookmarkFolder mapToBookmarkFolder(Element folderNode, BookmarkFolderNode parentNode) {
        return mapToBookmarkFolder(folderNode,
                ofNullable(parentNode).map(BookmarkFolderNode::getPath).orElse("")
        );
    }

    private BookmarkFolder mapToBookmarkFolder(Element folderNode, String parentPath) {
        return new BookmarkFolder(
                folderNode.text(),
                Instant.ofEpochSecond(Long.parseLong(folderNode.attr("ADD_DATE"))),
                Instant.ofEpochSecond(Long.parseLong(folderNode.attr("LAST_MODIFIED"))),
                parentPath
        );
    }

    public Bookmark mapToBookmark(Element bookmarkNode, BookmarkFolder parentFolder) {
        return new Bookmark(
                bookmarkNode.text(),
                URI.create(bookmarkNode.attr("HREF").replaceAll("[{,}]", "")),
                Set.of(bookmarkNode.attr("TAGS").split("\\s?,")),
                Instant.ofEpochSecond(Long.parseLong(bookmarkNode.attr("ADD_DATE"))),
                Instant.ofEpochSecond(Long.parseLong(bookmarkNode.attr("LAST_MODIFIED"))),
                bookmarkNode.cssSelector(),
                parentFolder.getPath()
        );
    }

    public Bookmark mapToBookmark(Element bookmarkNode) {
        return new Bookmark(
                bookmarkNode.text(),
                URI.create(bookmarkNode.attr("HREF").replaceAll("[{,}]", "")),
                Set.of(bookmarkNode.attr("TAGS").split("\\s?,")),
                Instant.ofEpochSecond(Long.parseLong(bookmarkNode.attr("ADD_DATE"))),
                Instant.ofEpochSecond(Long.parseLong(bookmarkNode.attr("LAST_MODIFIED"))),
                bookmarkNode.cssSelector(),
                null
        );
    }
}
