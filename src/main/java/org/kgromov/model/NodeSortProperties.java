package org.kgromov.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum NodeSortProperties {
    NAME("name"),
    CREATED("created"),
    MODIFIED("modified"),
    TAGS("tags");

    private final String property;
}
