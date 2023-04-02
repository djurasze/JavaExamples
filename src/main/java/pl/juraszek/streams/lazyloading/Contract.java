package pl.juraszek.streams.lazyloading;

import java.util.Set;

public record Contract(Set<SectionConstraint> sectionConstraints) {
}
