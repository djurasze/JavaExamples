package pl.juraszek.streams.lazyloading;

import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.control.Either;
import io.vavr.control.Try;
import pl.juraszek.streams.lazyloading.constraints.Constraint;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

public record Document(Set<Section> sections) {

   //   Faulty code
   public Stream<ContractViolation> faultyIsValidAccordingTo(Contract contract) {
      return Try.of(() -> contract.sectionConstraints()
                  .stream()
                  .map(sectionConstraint -> sections().stream()
                        .filter(section -> section.name().equals(sectionConstraint.sectionName()))
                        .map(section -> Tuple.of(section, sectionConstraint.constraint()))
                        .findAny()
                        .orElseThrow())
                  .filter(sectionValidation -> !sectionValidation._1.isSpecValid(sectionValidation._2))
                  .map(invalidSectionValidation -> new ContractViolation("SECTION_CONSTRAINT_VIOLATION",
                        String.format("Specification %s not valid according to section constraint %s",
                              invalidSectionValidation._1, invalidSectionValidation._2))))
            .getOrElseGet(throwable -> Stream.of(new ContractViolation("SECTION_MISSING",
                  String.format("Provided document %s does not contain section required by contract %s", this,
                        contract))));

   }

   //   faulty procedural version
   public Stream<ContractViolation> faultyProceduralIsValidAccordingTo(Contract contract) {
      Set<ContractViolation> violations = new HashSet<>();
      try {
         for (var sectionConstraint : contract.sectionConstraints()) {
            var section = findSectionInDocumentBy(sectionConstraint);
            if (filterInvalidSection(section)) {
               violations.add(sectionConstraintViolation(section));
            }
         }
      } catch (Exception throwable) {
         return Stream.of(missingSectionViolation(contract));
      }
      return violations.stream();
   }

   //   procedural version
   public Stream<ContractViolation> proceduralIsValidAccordingTo(Contract contract) {
      Set<ContractViolation> violations = new HashSet<>();

      for (var sectionConstraint : contract.sectionConstraints()) {
         try {
            var section = findSectionInDocumentBy(sectionConstraint);
            if (filterInvalidSection(section)) {
               violations.add(sectionConstraintViolation(section));
            }
         } catch (Exception throwable) {
            violations.add(missingSectionViolation(contract));
         }
      }
      return violations.stream();
   }

   //   procedural without throwing exception
   public Stream<ContractViolation> refactoredProceduralIsValidAccordingTo(Contract contract) {
      Set<ContractViolation> violations = new HashSet<>();

      for (var sectionConstraint : contract.sectionConstraints()) {

         var sectionOpt = findSectionInDocBy(sectionConstraint);

         if (sectionOpt.isEmpty()) {
            violations.add(missingSectionViolation(contract));
         } else {
            var section = sectionOpt.get();

            if (filterInvalidSection(section)) {
               violations.add(sectionConstraintViolation(section));
            }
         }
      }
      return violations.stream();
   }

   //   Faulty code refactoring (separating code)
   public Stream<ContractViolation> faultyRefactoredIsValidAccordingTo(Contract contract) {
      return Try.of(() -> contract.sectionConstraints()
                  .stream()
                  .map(this::findSectionInDocumentBy)
                  .filter(this::filterInvalidSection)
                  .map(this::sectionConstraintViolation))
            .getOrElseGet(throwable -> Stream.of(missingSectionViolation(contract)));

   }

   public Stream<ContractViolation> isValidAccordingTo(Contract contract) {
      //@formatter:off
      return contract.sectionConstraints()
            .stream()
            .flatMap(
                  constraint -> findSectionInDocument(constraint)
                        .map(this::validateSection)
                        .getOrElseGet(Stream::of));
   //@formatter:on
   }

   private ContractViolation missingSectionViolation(Contract contract) {
      return new ContractViolation("SECTION_MISSING",
            String.format("Provided document %s does not contain section required by contract %s", this, contract));
   }

   private ContractViolation missingSectionViolation(SectionConstraint constraint) {
      return new ContractViolation("SECTION_MISSING",
            String.format("Provided document %s does not contain section required by contract constraint %s", this,
                  constraint));
   }

   private ContractViolation sectionConstraintViolation(Tuple2<Section, Constraint> invalidSectionValidation) {
      return new ContractViolation("SECTION_CONSTRAINT_VIOLATION",
            String.format("Specification %s not valid according to section constraint %s", invalidSectionValidation._1,
                  invalidSectionValidation._2));
   }

   private boolean filterInvalidSection(Tuple2<Section, Constraint> sectionValidation) {
      return !sectionValidation._1.isSpecValid(sectionValidation._2);
   }

   private Stream<ContractViolation> validateSection(Tuple2<Section, Constraint> sectionValidation) {
      return sectionValidation._1.validateSpec(sectionValidation._2);
   }

   private Tuple2<Section, Constraint> findSectionInDocumentBy(SectionConstraint sectionConstraint) {
      return sections().stream()
            .filter(sectionConstraint::matches)
            .map(section -> Tuple.of(section, sectionConstraint.constraint()))
            .findAny()
            .orElseThrow();
   }

   private Optional<Tuple2<Section, Constraint>> findSectionInDocBy(SectionConstraint sectionConstraint) {
      return sections().stream()
            .filter(sectionConstraint::matches)
            .map(section -> Tuple.of(section, sectionConstraint.constraint()))
            .findAny();
   }

   private Either<ContractViolation, Tuple2<Section, Constraint>> findSectionInDocument(
         SectionConstraint sectionConstraint) {
      return sections().stream()
            .filter(sectionConstraint::matches)
            .map(section -> Tuple.of(section, sectionConstraint.constraint()))
            .findAny()
            .map(Either::<ContractViolation, Tuple2<Section, Constraint>>right)
            .orElseGet(() -> Either.left(missingSectionViolation(sectionConstraint)));
   }

}
