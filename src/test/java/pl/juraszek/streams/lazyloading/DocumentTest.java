package pl.juraszek.streams.lazyloading;

import org.junit.jupiter.api.Named;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import pl.juraszek.streams.lazyloading.constraints.SizeLimit;

import java.util.Set;
import java.util.function.BiFunction;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class DocumentTest {

   private static Stream<Arguments> validationMethods() {
      //@formatter:off
      BiFunction<Document, Contract, Stream<ContractViolation>> faultyIsValidAccordingTo = Document::faultyIsValidAccordingTo;
      BiFunction<Document, Contract, Stream<ContractViolation>> faultyRefactoredIsValidAccordingTo = Document::faultyRefactoredIsValidAccordingTo;
      BiFunction<Document, Contract, Stream<ContractViolation>> faultyProceduralIsValidAccordingTo = Document::faultyProceduralIsValidAccordingTo;
      BiFunction<Document, Contract, Stream<ContractViolation>> proceduralIsValidAccordingTo = Document::proceduralIsValidAccordingTo;
      BiFunction<Document, Contract, Stream<ContractViolation>> refactoredProceduralIsValidAccordingTo = Document::refactoredProceduralIsValidAccordingTo;
      BiFunction<Document, Contract, Stream<ContractViolation>> isValidAccordingTo = Document::isValidAccordingTo;
      return Stream.of(
            Arguments.of(Named.of("Faulty semi-functional, non separated methods",faultyIsValidAccordingTo)),
            Arguments.of(Named.of("Faulty semi-functional, separated methods",faultyRefactoredIsValidAccordingTo)),
            Arguments.of(Named.of("Faulty procedural",faultyProceduralIsValidAccordingTo)),
            Arguments.of(Named.of("Procedural",proceduralIsValidAccordingTo)),
            Arguments.of(Named.of("Procedural without throwing exception",refactoredProceduralIsValidAccordingTo)),
            Arguments.of(Named.of("Functional",isValidAccordingTo))
      );
      //@formatter:on
   }

   @ParameterizedTest
   @MethodSource("validationMethods")
   void shouldFulfillContract(BiFunction<Document, Contract, Stream<ContractViolation>> validationMethod) {
      //      given
      var contract = new Contract(Set.of(new SectionConstraint("Introduction", new SizeLimit(20)),
            new SectionConstraint("Body", new SizeLimit(500)), new SectionConstraint("Conclusion", new SizeLimit(20))));
      var document = new Document(
            Set.of(new Section("Introduction", "Lorem ipsum dolor sit amet, consectetur adipiscing elit."),
                  new Section("Body",
                        "Sollicitudin tempor id eu nisl nunc mi. Ut ornare lectus sit amet est placerat. Viverra maecenas accumsan lacus vel facilisis volutpat est velit egestas."),
                  new Section("Conclusion", "Et magnis dis parturient montes nascetur ridiculus mus mauris. ")));
      //  when
      Stream<ContractViolation> validationResult = validationMethod.apply(document, contract);

      //      then
      assertThat(validationResult).isEmpty();
   }

   @ParameterizedTest
   @MethodSource("validationMethods")
   void shouldRejectWhenSectionDoesNotFulfillConstraint(
         BiFunction<Document, Contract, Stream<ContractViolation>> validationMethod) {
      //      given
      var contract = new Contract(Set.of(new SectionConstraint("Introduction", new SizeLimit(20)),
            new SectionConstraint("Body", new SizeLimit(10)), new SectionConstraint("Conclusion", new SizeLimit(20))));
      var document = new Document(
            Set.of(new Section("Introduction", "Lorem ipsum dolor sit amet, consectetur adipiscing elit."),
                  new Section("Body",
                        "Sollicitudin tempor id eu nisl nunc mi. Ut ornare lectus sit amet est placerat. Viverra maecenas accumsan lacus vel facilisis volutpat est velit egestas."),
                  new Section("Conclusion", "Et magnis dis parturient montes nascetur ridiculus mus mauris. ")));
      //  when
      Stream<ContractViolation> validationResult = validationMethod.apply(document, contract);

      //      then
      assertThat(validationResult).map(ContractViolation::id).containsExactlyInAnyOrder("SECTION_CONSTRAINT_VIOLATION");
   }

   @ParameterizedTest
   @MethodSource("validationMethods")
   void shouldRejectWhenMultipleSectionsDoNotFulfillConstraint(
         BiFunction<Document, Contract, Stream<ContractViolation>> validationMethod) {
      //      given
      var contract = new Contract(Set.of(new SectionConstraint("Introduction", new SizeLimit(4)),
            new SectionConstraint("Body", new SizeLimit(10)), new SectionConstraint("Conclusion", new SizeLimit(20))));
      var document = new Document(
            Set.of(new Section("Introduction", "Lorem ipsum dolor sit amet, consectetur adipiscing elit."),
                  new Section("Body",
                        "Sollicitudin tempor id eu nisl nunc mi. Ut ornare lectus sit amet est placerat. Viverra maecenas accumsan lacus vel facilisis volutpat est velit egestas."),
                  new Section("Conclusion", "Et magnis dis parturient montes nascetur ridiculus mus mauris. ")));
      //  when
      Stream<ContractViolation> validationResult = validationMethod.apply(document, contract);

      //      then
      assertThat(validationResult).map(ContractViolation::id)
            .containsExactlyInAnyOrder("SECTION_CONSTRAINT_VIOLATION", "SECTION_CONSTRAINT_VIOLATION");
   }

   @ParameterizedTest
   @MethodSource("validationMethods")
   void shouldRejectWhenSectionRequiredByContractIsMissing(
         BiFunction<Document, Contract, Stream<ContractViolation>> validationMethod) {
      //      given
      var contract = new Contract(Set.of(new SectionConstraint("Introduction", new SizeLimit(20)),
            new SectionConstraint("Body", new SizeLimit(100)), new SectionConstraint("Conclusion", new SizeLimit(20))));
      var document = new Document(
            Set.of(new Section("Introduction", "Lorem ipsum dolor sit amet, consectetur adipiscing elit."),
                  new Section("Body",
                        "Sollicitudin tempor id eu nisl nunc mi. Ut ornare lectus sit amet est placerat. Viverra maecenas accumsan lacus vel facilisis volutpat est velit egestas.")));
      //  when
      Stream<ContractViolation> validationResult = validationMethod.apply(document, contract);

      //      then
      assertThat(validationResult).map(ContractViolation::id).containsExactlyInAnyOrder("SECTION_MISSING");
   }

   @ParameterizedTest
   @MethodSource("validationMethods")
   void shouldRejectWhenSectionRequiredByContractIsMissingAndSectionDoesNotFulfillConstraint(
         BiFunction<Document, Contract, Stream<ContractViolation>> validationMethod) {
      //      given
      var contract = new Contract(Set.of(new SectionConstraint("Introduction", new SizeLimit(20)),
            new SectionConstraint("Body", new SizeLimit(10)), new SectionConstraint("Conclusion", new SizeLimit(20))));
      var document = new Document(
            Set.of(new Section("Introduction", "Lorem ipsum dolor sit amet, consectetur adipiscing elit."),
                  new Section("Body",
                        "Sollicitudin tempor id eu nisl nunc mi. Ut ornare lectus sit amet est placerat. Viverra maecenas accumsan lacus vel facilisis volutpat est velit egestas.")));
      //  when
      Stream<ContractViolation> validationResult = validationMethod.apply(document, contract);

      //      then
      assertThat(validationResult).map(ContractViolation::id)
            .containsExactlyInAnyOrder("SECTION_MISSING", "SECTION_CONSTRAINT_VIOLATION");
   }
}