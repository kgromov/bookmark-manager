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
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.data.provider.hierarchy.TreeData;
import com.vaadin.flow.data.provider.hierarchy.TreeDataProvider;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.function.SerializablePredicate;
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
import org.kgromov.service.BookmarkService;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@UIScope
@SpringComponent
@Route("/")
@PageTitle("Bookmarks page")
@CssImport("./styles.css")
public class BookmarksTreeView extends Div {
    private final BookmarkService bookmarkService;
    private TreeGrid<Node> treeGrid;
    private TreeDataProvider<Node> dataProvider;

    @SneakyThrows
    public BookmarksTreeView(BookmarkService bookmarkService) {
        this.bookmarkService = bookmarkService;
        dataProvider = new TreeDataProvider<>(
                new TreeData<Node>()
                .addItems(bookmarkService.fetchBookmarksTree(), Node::children)
        );
        treeGrid = new TreeGrid<>(dataProvider);

        treeGrid.addComponentHierarchyColumn(this::buildNameColumn)
                .setHeader("Title");
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
                .setKey("tags")
                .setFlexGrow(0)
                .setWidth("200px");
        treeGrid.setHeightFull();

        var header = this.createHeader(dataProvider.getTreeData().getRootItems());

        add(header, treeGrid);

        setSizeFull();
    }

    private HorizontalLayout createHeader(List<Node> rootItems) {
        H3 caption = new H3("Bookmarks");
        Button expand = new Button("Expand All");
        expand.addClickListener(event -> treeGrid.expandRecursively(rootItems, 32));
        Button collapse = new Button("Collapse All");
        collapse.addClickListener(event -> treeGrid.collapse(rootItems));

        var header = new HorizontalLayout(caption, addTagsFilter(), expand, collapse);
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.setHeight("var(--lumo-space-xl)");
        return header;
    }

    private TextField addTagsFilter() {
        TextField tagsSearch = new TextField();
        tagsSearch.setPlaceholder("Search by tag or name");
        tagsSearch.setClearButtonVisible(true);
        tagsSearch.setPrefixComponent(VaadinIcon.SEARCH.create());
        tagsSearch.setValueChangeMode(ValueChangeMode.EAGER);
        tagsSearch.addValueChangeListener(e -> {
            log.info("Filter on name value changes: {}", e.getValue());
            if (e.getValue() == null) {
                dataProvider.setFilter(null);
            } else {
                SerializablePredicate<Node> byTag = node ->
                        node.tags().stream().anyMatch(tag -> tag.toLowerCase().contains(e.getValue().toLowerCase()));
                SerializablePredicate<Node> byName = node -> node.name().toLowerCase().contains(e.getValue().toLowerCase());
                dataProvider.setFilter(byTag.or(byName));
            }
            treeGrid.expandRecursively(dataProvider.getTreeData().getRootItems(), 32);
        });
        return tagsSearch;
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
