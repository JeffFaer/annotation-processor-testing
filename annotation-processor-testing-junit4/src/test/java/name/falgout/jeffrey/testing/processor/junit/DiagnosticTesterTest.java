package name.falgout.jeffrey.testing.processor.junit;

import static org.mockito.ArgumentMatchers.argThat;
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
        .fireTestFailure(failedWith("wrongLine", t -> t.getMessage().contains("<26L> was <27L>")));

    verify(runNotifier)
        .fireTestFailure(failedWith(null, t -> t.getMessage().contains("unmatched diagnostics")));

    verifyNoMoreInteractions(runNotifier);
  }

  private static Description named(String name) {
    return argThat(d -> d.getMethodName().equals(name));
  }

  private static Failure failedWith(String name, Predicate<? super Throwable> throwable) {
    return argThat(f -> Objects.equals(f.getDescription().getMethodName(), name)
        && f.getException() instanceof AssertionError && throwable.test(f.getException()));
  }
}
