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
        stopWatch.start();
        try {
            Document document = Jsoup.parse(bookmarkPath);
            Elements parentFolders = document.select("body>dl>dt>h3");
            List<BookmarkFolderNode> folderNodes = new ArrayList<>();
            parentFolders.forEach(parentFolder -> {
                var bookmarkFolder = mapper.mapToBookmarkFolder(parentFolder, (BookmarkFolderNode) null);
                var folderBookmarks = this.parseBookmarks(parentFolder);
                Elements subFolders = parentFolder.selectXpath("../dl/dt/h3");
                var folderNode = new BookmarkFolderNode(bookmarkFolder, new ArrayList<>(), folderBookmarks);
                folderNodes.add(folderNode);
                subFolders.forEach(subFolder -> this.buildTree(subFolder, folderNode));
            });
            return folderNodes;
        } finally {
            stopWatch.stop();
            log.info("Time to build bookmarks tree = {} s", stopWatch.lastTaskInfo().getTimeSeconds());
        }
    }

    private void buildTree(Element folder, BookmarkFolderNode parentNode) {
        var bookmarkFolder = mapper.mapToBookmarkFolder(folder, parentNode);
        var folderBookmarks = this.parseBookmarks(folder);
        Elements subFolders = folder.selectXpath("../dl/dt/h3");
        var folderNode = new BookmarkFolderNode(bookmarkFolder, new ArrayList<>(), folderBookmarks);
        parentNode.subFolders().add(folderNode);
        subFolders.forEach(subFolder -> buildTree(subFolder, folderNode));
    }

    private List<BookmarkNode> parseBookmarks(Element folder) {
        Elements bookmarkNodes = folder.selectXpath("../dl/dt/a");
        return bookmarkNodes.stream()
                .map(mapper::mapToBookmark)
                .map(BookmarkNode::new)
                .toList();
    }
}
