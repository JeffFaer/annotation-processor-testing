package name.falgout.jeffrey.testing.processor.junit;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.Predicate;

import org.junit.Test;
import org.junit.runner.Description;
import org.junit.runner.RunWith;
import org.junit.runner.Runner;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.InitializationError;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DiagnosticTesterTest {
  @Mock RunNotifier runNotifier;

  @Test
  public void runTestClass() throws InitializationError {
    Runner runner = new DiagnosticTester(DiagnosticTesterTestClass.class);
    runner.run(runNotifier);

    for (String name : Arrays.asList("sameLine", "nextLine", "wrongMessage", "wrongKind",
        "wrongLine")) {
      verify(runNotifier).fireTestStarted(named(name));
      verify(runNotifier).fireTestFinished(named(name));
    }

    verify(runNotifier)
        .fireTestFailure(failedWith("wrongMessage", t -> t.getMessage().contains("was \"\"")));
    verify(runNotifier).fireTestFailure(
        failedWith("wrongKind", t -> t.getMessage().contains("<ERROR> was <WARNING>")));
    verify(runNotifier)
        .fireTestFailure(failedWith("wrongLine", t -> t.getMessage().contains("<23L> was <24L>")));

    verify(runNotifier)
        .fireTestFailure(failedWith(null, t -> t.getMessage().contains("unmatched diagnostics")));

    verifyNoMoreInteractions(runNotifier);
  }

  @Test
  public void runNestedTestClass() throws InitializationError {
    Runner runner = new DiagnosticTester(NestedDiagnosticTesterTestClass.class);
    runner.run(runNotifier);

    InOrder order = inOrder(runNotifier);
    for (int i = 1; i <= 9; i++) {
      order.verify(runNotifier).fireTestStarted(numbered(i));
      order.verify(runNotifier).fireTestFinished(numbered(i));
    }
    order.verifyNoMoreInteractions();

    Description description = runner.getDescription();
    assertThat(description.getChildren()).hasSize(4);
    assertThat(description.getClassName()).endsWith("NestedDiagnosticTesterTestClass");

    assertThat(description.getChildren().get(0).getMethodName()).contains("#1@");
    assertThat(description.getChildren().get(2).getMethodName()).contains("#6@");

    assertThat(description.getChildren().get(1).getChildren()).hasSize(4);
    assertThat(description.getChildren().get(3).getChildren()).hasSize(3);
  }

  private static Description named(String name) {
    return argThat(d -> d.getMethodName().equals(name));
  }

  private static Description numbered(int i) {
    return argThat(d -> d.getMethodName().contains("#" + i + "@"));
  }

  private static Failure failedWith(String name, Predicate<? super Throwable> throwable) {
    return argThat(f -> Objects.equals(f.getDescription().getMethodName(), name)
        && f.getException() instanceof AssertionError && throwable.test(f.getException()));
  }
}
