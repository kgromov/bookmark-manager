package org.kgromov.service;

import org.jsoup.nodes.Element;
import org.kgromov.model.BookmarkFolderNode;
import org.kgromov.model.BookmarkNode;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.time.Instant;
import java.util.Set;

import static java.util.Optional.ofNullable;

@Component
public class BookmarkMapper {

    public BookmarkFolderNode mapToBookmarkFolder(Element folderNode, BookmarkFolderNode parentNode) {
        return new BookmarkFolderNode(
                folderNode.text(),
                Instant.ofEpochSecond(Long.parseLong(folderNode.attr("ADD_DATE"))),
                Instant.ofEpochSecond(Long.parseLong(folderNode.attr("LAST_MODIFIED"))),
                ofNullable(parentNode).map(BookmarkFolderNode::getPath).orElse("")
        );
    }

    public BookmarkNode mapToBookmark(Element bookmarkNode, BookmarkFolderNode parentFolder) {
        return new BookmarkNode(
                bookmarkNode.text(),
                URI.create(bookmarkNode.attr("HREF").replaceAll("[{,}]", "")),
                Set.of(bookmarkNode.attr("TAGS").split("\\s?,")),
                Instant.ofEpochSecond(Long.parseLong(bookmarkNode.attr("ADD_DATE"))),
                Instant.ofEpochSecond(Long.parseLong(bookmarkNode.attr("LAST_MODIFIED"))),
                bookmarkNode.cssSelector(),
                parentFolder.getPath()
        );
    }
}
