package org.kgromov.view;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.dom.Style;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.kgromov.model.BookmarkNode;
import org.kgromov.model.FolderNode;
import org.kgromov.model.Node;
import org.kgromov.service.BookmarkParser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;

import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Slf4j
@UIScope
@SpringComponent
@Route("/")
@PageTitle("Bookmarks page")
@CssImport("./styles.css")
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
        TreeGrid<Node> treeGrid = new TreeGrid<>();
        var rootItems = bookmarkParser.parseBookmarksTree(bookmarkPath)
                .stream()
                .map(node -> (Node) node).toList();
        treeGrid.setItems(rootItems, Node::children);
        treeGrid.addComponentHierarchyColumn(this::buildNameColumn)
                .setHeader("Title")
                .getStyle()
                .setWhiteSpace(Style.WhiteSpace.NORMAL)
                .set("word-wrap", "break-word")
                .set("word-break", "break-word");
        treeGrid.addColumn(node -> formattedDate(node.created()))
                .setHeader("Created")
                .setFlexGrow(0)
                .setWidth("150px");
        treeGrid.addColumn(node -> formattedDate(node.modified()))
                .setHeader("Modified")
                .setFlexGrow(0)
                .setWidth("150px");
        treeGrid.addColumn(node -> String.join(", ", node.tags()))
                .setHeader("Tags")
                .setFlexGrow(0)
                .setWidth("200px");
        ;
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

    private Component buildNameColumn(Node node) {
        return (node instanceof FolderNode)
                ? createFolderNameComponent((FolderNode) node)
                : createBookmarkNameComponent((BookmarkNode) node);
    }

    private Component createFolderNameComponent(FolderNode folder) {
        var horizontalLayout = new HorizontalLayout(
                new Icon(VaadinIcon.FOLDER),
                new Span(folder.name())
        );
        horizontalLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        return horizontalLayout;
    }

    private Component createBookmarkNameComponent(BookmarkNode bookmark) {
        Anchor bookmarkLink = new Anchor(bookmark.href().toString());
        bookmarkLink.add(
                new Icon(VaadinIcon.BOOKMARK),
                new Span(bookmark.name())
        );
        bookmarkLink.getStyle()
                .set("align-items", "center")
                .set("display", "flex");
        return bookmarkLink;
    }

    private String formattedDate(Instant instant) {
        return LocalDate.ofInstant(instant, ZoneId.systemDefault())
                .format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
    }
}
