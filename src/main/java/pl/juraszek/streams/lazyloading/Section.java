package pl.juraszek.streams.lazyloading;

import io.vavr.control.Either;
import pl.juraszek.streams.lazyloading.constraints.Constraint;

import java.util.stream.Stream;

public record Section(String name, String specification) {
   public boolean isSpecValid(Constraint constraint) {
      return constraint.isValid(this);
   }

   public Stream<ContractViolation> validateSpec(Constraint constraint) {
      return constraint.check(this);
   }
}
