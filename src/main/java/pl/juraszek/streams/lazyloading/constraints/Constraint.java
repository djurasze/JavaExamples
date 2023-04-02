package pl.juraszek.streams.lazyloading.constraints;

import io.vavr.control.Either;
import pl.juraszek.streams.lazyloading.ContractViolation;
import pl.juraszek.streams.lazyloading.Section;
import pl.juraszek.streams.lazyloading.SectionConstraint;

import java.util.stream.Stream;

public interface Constraint {
   boolean isValid(Section section);

   Stream<ContractViolation> check(Section section);
}
