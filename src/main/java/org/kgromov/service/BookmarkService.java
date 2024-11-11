package org.kgromov.service;

import com.vaadin.flow.data.provider.SortDirection;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.kgromov.model.Node;
import org.kgromov.model.NodeFilter;
import org.kgromov.model.NodeSortOrder;
import org.kgromov.model.NodeSortProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.util.Objects.isNull;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;

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

    public long getChildCount(Node parent, int limit, int offset, NodeFilter nodeFilter) {
        return this.filterChildren(parent, nodeFilter)
                .skip(offset)
                .limit(limit)
                .count();
    }

    public List<Node> fetchChildren(Node parent, int limit, int offset, NodeFilter nodeFilter, List<NodeSortOrder> sortOrders) {
        var comparator = sortOrders.stream()
                .map(sortOrder -> {
                    Comparator<Node> comparator_ = switch (sortOrder.property()) {
                        case NodeSortProperties.NAME -> Comparator.comparing(Node::name);
                        case NodeSortProperties.CREATED -> Comparator.comparing(Node::created);
                        case NodeSortProperties.MODIFIED -> Comparator.comparing(Node::modified);
                        default -> throw new IllegalStateException(STR."Unexpected value: \{sortOrder.property()}");
                    };
                    return sortOrder.direction() == SortDirection.DESCENDING ? comparator_.reversed() : comparator_;
                }).reduce((_, _) -> 0, Comparator::thenComparing);
        return this.filterChildren(parent, nodeFilter)
                .sorted(comparator)
                .skip(offset)
                .limit(limit)
                .collect(toList());
    }

    private Stream<Node> filterChildren(Node parent, NodeFilter nodeFilter) {
        var children = this.getChildren(parent, new ArrayList<>());
        var searchTerm = ofNullable(nodeFilter).map(NodeFilter::searchTerm);
        if (searchTerm.isEmpty()) {
            return children.stream();
        }
        Predicate<Node> byTag = node -> node.tags().stream().anyMatch(tag -> tag.toLowerCase().contains(searchTerm.get().toLowerCase()));
        Predicate<Node> byName = node -> node.name().toLowerCase().contains(searchTerm.get().toLowerCase());
        return children.stream().filter(byTag.or(byName));
    }

    private List<Node> getChildren(Node parent, List<Node> children) {
        if (isNull(parent)) {
            return this.fetchBookmarksTree();
        }
        children.addAll(parent.children());
        parent.children().forEach(child -> this.getChildren(child, children));
        return children;
    }
}
