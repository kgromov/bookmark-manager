package org.kgromov.service;

import org.jsoup.nodes.Element;
import org.kgromov.model.FolderNode;
import org.kgromov.model.BookmarkNode;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.time.Instant;
import java.util.Set;

import static java.util.Optional.ofNullable;

@Component
public class BookmarkMapper {

    public FolderNode mapToBookmarkFolder(Element folderNode, FolderNode parentNode) {
        return new FolderNode(
                folderNode.text(),
                Instant.ofEpochSecond(Long.parseLong(folderNode.attr("ADD_DATE"))),
                Instant.ofEpochSecond(Long.parseLong(folderNode.attr("LAST_MODIFIED"))),
                ofNullable(parentNode).map(FolderNode::getPath).orElse("")
        );
    }

    public BookmarkNode mapToBookmark(Element bookmarkNode, FolderNode parentFolder) {
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
