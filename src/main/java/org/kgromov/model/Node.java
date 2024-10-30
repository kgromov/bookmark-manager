package org.kgromov.model;

import java.time.Instant;
import java.util.List;

public interface Node {
    String name();

    Instant created();

    Instant modified();

    String getPath();

    List<Node> children();
}
