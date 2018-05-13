package name.falgout.jeffrey.testing.processor.junit5;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.MoreCollectors.onlyElement;
import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth8.assertThat;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Streams;
import java.util.stream.Stream;
import name.falgout.jeffrey.testing.junit.testing.ExtensionTester;
import name.falgout.jeffrey.testing.junit.testing.TestPlanExecutionReport;
import name.falgout.jeffrey.testing.junit.testing.TestPlanExecutionReport.DisplayName;
import name.falgout.jeffrey.testing.processor.tests.DiagnosticTestClass;
import name.falgout.jeffrey.testing.processor.tests.NestedDiagnosticTestClass;
import name.falgout.jeffrey.testing.processor.tests.NestedDiagnosticTestClass.Instance;
import name.falgout.jeffrey.testing.processor.tests.NestedDiagnosticTestClass.Instance.InstanceInstance;
import name.falgout.jeffrey.testing.processor.tests.NestedDiagnosticTestClass.Static;
import name.falgout.jeffrey.testing.processor.tests.NestedDiagnosticTestClass.Static.StaticInstance;
import name.falgout.jeffrey.testing.processor.tests.NestedDiagnosticTestClass.Static.StaticStatic;
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
        getChildren(report.getSuccessful(), DiagnosticTestClass.class);
    assertThat(successful).hasSize(2);
    assertThat(tails(successful)).containsExactly("sameLine", "nextLine").inOrder();

    ImmutableList<DisplayName> failures =
        getChildren(report.getFailures().keySet(), DiagnosticTestClass.class);
    assertThat(failures).hasSize(3);
    assertThat(tails(failures))
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
        getDisplayName(
            report.getFailures().keySet(),
            DiagnosticTestClass.class);
    assertThat(report.getFailure(testClass).get())
        .hasMessageThat()
        .contains("Extra diagnostic");
  }

  @Test
  public void nestedExample() {
    TestPlanExecutionReport report = ExtensionTester.runTests(NestedDiagnosticTestClass.class);

    assertThat(report.getFailures()).isEmpty();

    assertThat(tails(report.getSuccessful()).filter(tail -> tail.startsWith("ERROR")))
        .containsExactly(
            "ERROR#1@8",
            "ERROR#2@13",
            "ERROR#3@17",
            "ERROR#4@23",
            "ERROR#5@27",
            "ERROR#6@31",
            "ERROR#7@35",
            "ERROR#8@39",
            "ERROR#9@43")
        .inOrder();

    assertThat(
        tails(
            getChildren(
                report.getSuccessful(),
                NestedDiagnosticTestClass.class,
                Static.class,
                StaticStatic.class)))
        .containsExactly("ERROR#3@17");

    assertThat(
        tails(
            getChildren(
                report.getSuccessful(),
                NestedDiagnosticTestClass.class,
                Static.class,
                StaticInstance.class)))
        .containsExactly("ERROR#4@23");

    assertThat(
        tails(
            getChildren(
                report.getSuccessful(),
                NestedDiagnosticTestClass.class,
                Instance.class,
                InstanceInstance.class)))
        .containsExactly("ERROR#8@39");
  }

  private Stream<String> tails(Iterable<? extends DisplayName> displayNames) {
    return Streams.stream(displayNames).map(DisplayName::getNames).map(Iterables::getLast);
  }

  private DisplayName getDisplayName(
      Iterable<? extends DisplayName> displayNames, Class<?> clazz) {
    String name = clazz.getName();
    return Streams.stream(displayNames)
        .filter(displayName -> Iterables.getLast(displayName.getNames()).equals(name))
        .collect(onlyElement());
  }

  private ImmutableList<DisplayName> getChildren(
      Iterable<? extends DisplayName> displayNames, Class<?> first, Class<?>... more) {
    ImmutableList<String> subList =
        Lists.asList(first, more)
            .stream()
            .map(Class::getName)
            .collect(toImmutableList());
    return Streams.stream(displayNames)
        .filter(displayName -> {
          int start = displayName.getNames().indexOf(subList.get(0));
          int end = start + subList.size();

          return 0 <= start && end < displayName.getNames().size()
              && displayName.getNames().subList(start, end).equals(subList);
        })
        .collect(toImmutableList());
  }
}
