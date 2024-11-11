package org.kgromov.view;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.grid.HeaderRow;
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
import com.vaadin.flow.data.provider.ConfigurableFilterDataProvider;
import com.vaadin.flow.data.provider.hierarchy.*;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.kgromov.model.*;
import org.kgromov.service.BookmarkService;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Stream;

import static org.kgromov.model.NodeSortProperties.*;

@Slf4j
@UIScope
@SpringComponent
@Route("/")
@PageTitle("Bookmarks page")
@CssImport("./styles.css")
public class BookmarksTreeView extends Div {
    private final BookmarkService bookmarkService;
    private TreeGrid<Node> treeGrid;

    @SneakyThrows
    public BookmarksTreeView(BookmarkService bookmarkService) {
        this.bookmarkService = bookmarkService;

        treeGrid = new TreeGrid<>();
        treeGrid.addComponentHierarchyColumn(this::buildNameColumn)
                .setHeader("Title")
                .setKey(NAME.getProperty())
                .setSortProperty(NAME.getProperty());
        treeGrid.addColumn(node -> formattedDate(node.created()))
                .setHeader("Created")
                .setKey(CREATED.getProperty())
                .setSortProperty(CREATED.getProperty())
                .setFlexGrow(0)
                .setWidth("150px");
        treeGrid.addColumn(node -> formattedDate(node.modified()))
                .setHeader("Modified")
                .setKey(MODIFIED.getProperty())
                .setSortProperty(MODIFIED.getProperty())
                .setFlexGrow(0)
                .setWidth("150px");
        treeGrid.addColumn(node -> String.join(", ", node.tags()))
                .setHeader("Tags")
                .setKey(TAGS.getProperty())
                .setFlexGrow(0)
                .setWidth("200px");
        treeGrid.setHeightFull();

        // define hierarchical and filterable data provider
        HierarchicalConfigurableFilterDataProvider<Node, Void, NodeFilter> dataProvider =
                new AbstractBackEndHierarchicalDataProvider<Node, NodeFilter>() {

                    // returns the number of immediate child items based on query filter
                    @Override
                    public int getChildCount(HierarchicalQuery<Node, NodeFilter> query) {
                        return (int) bookmarkService.getChildCount(query.getParent(), query.getLimit(), query.getOffset(), query.getFilter().orElse(null));
                    }

                    // checks if a given item should be expandable
                    @Override
                    public boolean hasChildren(Node item) {
                        return !item.children().isEmpty();
                    }

                    // returns the immediate child items based on offset, limit, filter and sorting
                    @Override
                    protected Stream<Node> fetchChildrenFromBackEnd(HierarchicalQuery<Node, NodeFilter> query) {
                        var sortOrders = query.getSortOrders()
                                .stream()
                                .map(sortOrder -> new NodeSortOrder(NodeSortProperties.valueOf(sortOrder.getSorted()), sortOrder.getDirection()))
                                .toList();
                        return bookmarkService.fetchChildren(query.getParent(), query.getLimit(), query.getOffset(), query.getFilter().orElse(null), sortOrders).stream();
                    }

                }.withConfigurableFilter();
        treeGrid.setDataProvider(dataProvider);
        NodeFilter treeFilter = new NodeFilter(null);
        dataProvider.setFilter(treeFilter);
        this.addHeaderFilter(dataProvider);

        var header = this.createHeader(bookmarkService.fetchBookmarksTree());
        add(header, treeGrid);
        setSizeFull();
    }

    private HorizontalLayout createHeader(List<Node> rootItems) {
        H3 caption = new H3("Bookmarks");
        Button expand = new Button("Expand All");
        expand.addClickListener(event -> treeGrid.expandRecursively(rootItems, 32));
        Button collapse = new Button("Collapse All");
        collapse.addClickListener(event -> treeGrid.collapse(rootItems));

        var header = new HorizontalLayout(caption, expand, collapse);
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.setHeight("var(--lumo-space-xl)");
        return header;
    }

    private void addHeaderFilter(ConfigurableFilterDataProvider<Node, Void, NodeFilter> dataProvider) {
        HeaderRow filterRow = treeGrid.prependHeaderRow();
        TextField searchTermFilter = new TextField();
        searchTermFilter.setPlaceholder("Search by tag or name");
        searchTermFilter.setClearButtonVisible(true);
        searchTermFilter.setPrefixComponent(VaadinIcon.SEARCH.create());
//        tagsSearch.setValueChangeMode(ValueChangeMode.EAGER);
        searchTermFilter.addValueChangeListener(e -> {
            log.info("Filter on name value changes: {}", e.getValue());
            var filter = new NodeFilter(e.getValue());
            dataProvider.setFilter(filter);
            dataProvider.refreshAll();
        });
        filterRow.getCell(treeGrid.getColumnByKey(NAME.getProperty())).setComponent(searchTermFilter);
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
