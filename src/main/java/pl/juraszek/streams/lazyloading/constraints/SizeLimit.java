package pl.juraszek.streams.lazyloading.constraints;

import io.vavr.control.Either;
import pl.juraszek.streams.lazyloading.ContractViolation;
import pl.juraszek.streams.lazyloading.Section;
import pl.juraszek.streams.lazyloading.SectionConstraint;

import java.util.stream.Stream;

public record SizeLimit(int max) implements Constraint {
   @Override
   public boolean isValid(Section section) {
      return section.specification().split(" ").length < max;
   }

   @Override
   public Stream<ContractViolation> check(Section section) {
      return isValid(section) ? Stream.empty() : Stream.of(new ContractViolation("SECTION_CONSTRAINT_VIOLATION",
            String.format("Specification %s not valid according to section constraint %s", section,
                  this)));
   }
}
