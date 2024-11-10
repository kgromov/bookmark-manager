package org.kgromov.service;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.kgromov.model.Node;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookmarkService {
    private final BookmarkParser parser;
    private final BookmarkMapper mapper;

    @Value("${bookmark.resource.path}")
    private Resource bookmarkFile;

    @SneakyThrows
    public List<Node> fetchBookmarksTree() {
        Path bookmarkPath = Paths.get(bookmarkFile.getURI());
        return parser.parseBookmarksTree(bookmarkPath)
                .stream()
                .map(node -> (Node) node)
                .toList();
    }
}
