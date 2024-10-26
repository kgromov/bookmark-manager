package org.kgromov.service;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.kgromov.model.BookmarkFolderNode;
import org.kgromov.model.BookmarkNode;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class BookmarkParser {
    private final BookmarkMapper mapper;

    @SneakyThrows
    public List<BookmarkFolderNode> parseBookmarksTree(Path bookmarkPath) {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start("parseBookmarksTree");
        try {
            Document document = Jsoup.parse(bookmarkPath);
            Elements parentFolders = document.select("body>dl>dt>h3");
            List<BookmarkFolderNode> folderNodes = new ArrayList<>();
            parentFolders.forEach(parentFolder -> {
                var bookmarkFolder = mapper.mapToBookmarkFolder(parentFolder, null);
                var folderBookmarks = this.findBookmarks(parentFolder, bookmarkFolder);
                bookmarkFolder.bookmarks().addAll(folderBookmarks);
                Elements subFolders = parentFolder.selectXpath("../dl/dt/h3");
                folderNodes.add(bookmarkFolder);
                subFolders.forEach(subFolder -> this.buildTree(subFolder, bookmarkFolder));
            });
            return folderNodes;
        } finally {
            stopWatch.stop();
            log.info("Time to build bookmarks tree = {} s", stopWatch.lastTaskInfo().getTimeSeconds());
        }
    }

    private void buildTree(Element folder, BookmarkFolderNode parentNode) {
        var bookmarkFolder = mapper.mapToBookmarkFolder(folder, parentNode);
        var folderBookmarks = this.findBookmarks(folder, bookmarkFolder);
        bookmarkFolder.bookmarks().addAll(folderBookmarks);
        Elements subFolders = folder.selectXpath("../dl/dt/h3");
        parentNode.subFolders().add(bookmarkFolder);
        subFolders.forEach(subFolder -> buildTree(subFolder, bookmarkFolder));
    }

    private List<BookmarkNode> findBookmarks(Element folder, BookmarkFolderNode parentNode) {
        Elements bookmarkNodes = folder.selectXpath("../dl/dt/a");
        return bookmarkNodes.stream()
                .map(node -> mapper.mapToBookmark(node, parentNode))
                .toList();
    }
}
