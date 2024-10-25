package org.kgromov.view;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.kgromov.model.BookmarkFolderNode;
import org.kgromov.service.BookmarkParser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;

import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
@UIScope
@SpringComponent
@Route(value = "/bookmarks")
@PageTitle("Bookmarks page")
public class BookmarksTreeView extends Div {
    private final BookmarkParser bookmarkParser;

    @Value("classpath:bookmarks/bookmarks.html")
    private Resource bookmarkFile;

    @SneakyThrows
    public BookmarksTreeView(BookmarkParser bookmarkParser) {
        this.bookmarkParser = bookmarkParser;
        URL resource = this.getClass().getClassLoader().getResource("bookmarks/bookmarks.html");
        // TODO: for some reason it's null
//        Path bookmarkPath = Paths.get(bookmarkFile.getURI());
        Path bookmarkPath = Paths.get(resource.toURI());
        var treeGrid = new TreeGrid<BookmarkFolderNode>();
        treeGrid.setItems(bookmarkParser.parseBookmarksTree(bookmarkPath), BookmarkFolderNode::subFolders);
        treeGrid.addHierarchyColumn(BookmarkFolderNode::name).setHeader("Title");
        treeGrid.addColumn(BookmarkFolderNode::created).setHeader("Created");
        treeGrid.addColumn(BookmarkFolderNode::modified).setHeader("Modified");
        add(treeGrid);
    }
}
