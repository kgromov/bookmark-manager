package org.kgromov.model;

import com.vaadin.flow.data.provider.SortDirection;

public record NodeSortOrder(NodeSortProperties property, SortDirection direction) {
}
