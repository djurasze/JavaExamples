package pl.juraszek.streams.lazyloading;

import pl.juraszek.streams.lazyloading.constraints.Constraint;

public record SectionConstraint(String sectionName, Constraint constraint) {

   public boolean matches(Section section) {
      return section.name().equals(this.sectionName());
   }
}
