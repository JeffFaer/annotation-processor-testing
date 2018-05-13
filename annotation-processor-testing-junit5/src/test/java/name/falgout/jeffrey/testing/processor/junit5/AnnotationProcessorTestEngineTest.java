package name.falgout.jeffrey.testing.processor.junit5;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.MoreCollectors.onlyElement;
import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth8.assertThat;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Streams;
import name.falgout.jeffrey.testing.junit.testing.ExtensionTester;
import name.falgout.jeffrey.testing.junit.testing.TestPlanExecutionReport;
import name.falgout.jeffrey.testing.junit.testing.TestPlanExecutionReport.DisplayName;
import name.falgout.jeffrey.testing.processor.tests.DiagnosticTestClass;
import name.falgout.jeffrey.testing.processor.tests.NestedDiagnosticTestClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public final class AnnotationProcessorTestEngineTest {
  @Test
  public void simpleExample() {
    TestPlanExecutionReport report = ExtensionTester.runTests(DiagnosticTestClass.class);

    assertThat(report.getSuccessful()).hasSize(3);
    assertThat(report.getFailures()).hasSize(4);

    ImmutableList<DisplayName> successful =
        getChildren(report.getSuccessful(), DiagnosticTestClass.class.getName());
    assertThat(successful).hasSize(2);
    assertThat(successful.stream().map(DisplayName::getNames).map(Iterables::getLast))
        .containsExactly("sameLine", "nextLine")
        .inOrder();

    ImmutableList<DisplayName> failures =
        getChildren(report.getFailures().keySet(), DiagnosticTestClass.class.getName());
    assertThat(failures).hasSize(3);
    assertThat(failures.stream().map(DisplayName::getNames).map(Iterables::getLast))
        .containsExactly("wrongMessage", "wrongKind", "wrongLine")
        .inOrder();

    assertThat(report.getFailure(failures.get(0)).get())
        .hasMessageThat()
        .contains("\"something-else\" was \"\"");
    assertThat(report.getFailure(failures.get(1)).get())
        .hasMessageThat()
        .contains("<ERROR> was <WARNING>");
    assertThat(report.getFailure(failures.get(2)).get())
        .hasMessageThat()
        .contains("<23L> was <24L>");

    DisplayName testClass =
        getDisplayNameEndingWith(
            report.getFailures().keySet(),
            DiagnosticTestClass.class.getName());
    assertThat(report.getFailure(testClass).get())
        .hasMessageThat()
        .contains("Extra diagnostic");
  }

  @Ignore
  @Test
  public void nestedExample() {
    assertThat(ExtensionTester.runTests(NestedDiagnosticTestClass.class)).isNull();
  }

  private DisplayName getDisplayNameEndingWith(
      Iterable<? extends DisplayName> displayNames, String suffix) {
    return Streams.stream(displayNames)
        .filter(
            displayName -> displayName.getNames().indexOf(suffix)
                == displayName.getNames().size() - 1)
        .collect(onlyElement());
  }

  private ImmutableList<DisplayName> getChildren(
      Iterable<? extends DisplayName> displayNames, String prefixPart) {
    return Streams.stream(displayNames)
        .filter(displayName -> displayName.getNames().contains(prefixPart) &&
            !Iterables.getLast(displayName.getNames()).equals(prefixPart))
        .collect(toImmutableList());
  }
}
