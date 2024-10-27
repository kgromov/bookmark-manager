package org.kgromov.view;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
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
@Route("/")
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
        var rootItems = bookmarkParser.parseBookmarksTree(bookmarkPath);
        treeGrid.setItems(rootItems, BookmarkFolderNode::subFolders);
        treeGrid.addHierarchyColumn(BookmarkFolderNode::name).setHeader("Title");
        treeGrid.addColumn(BookmarkFolderNode::created).setHeader("Created");
        treeGrid.addColumn(BookmarkFolderNode::modified).setHeader("Modified");
        treeGrid.setHeightFull();

        H3 caption = new H3("Bookmarks");
        Button expand = new Button("Expand All");
        expand.addClickListener(event -> treeGrid.expandRecursively(rootItems, 10));
        Button collapse = new Button("Collapse All");
        collapse.addClickListener(event -> treeGrid.collapse(rootItems));

        var header = new HorizontalLayout(caption, expand, collapse);
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.setHeight("var(--lumo-space-xl)");
        header.setFlexGrow(1, caption);

        add(header, treeGrid);
        setSizeFull();
    }
}
