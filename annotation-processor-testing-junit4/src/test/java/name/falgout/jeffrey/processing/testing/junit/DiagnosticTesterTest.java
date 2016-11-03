package name.falgout.jeffrey.processing.testing.junit;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.Arrays;

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

    for (String name : Arrays.asList("1", "2", "3", "4")) {
      verify(runNotifier).fireTestStarted(named(name));
      verify(runNotifier).fireTestFinished(named(name));
    }

    verify(runNotifier).fireTestFailure(failedWith("3", AssertionError.class));
    verify(runNotifier).fireTestFailure(failedWith("4", AssertionError.class));

    verifyNoMoreInteractions(runNotifier);
  }

  private static Description named(String name) {
    return argThat(d -> d.getMethodName().equals(name));
  }

  private static Failure failedWith(String name, Class<? extends Throwable> exceptionClass) {
    return argThat(f -> f.getDescription().getMethodName().equals(name)
        && exceptionClass.isInstance(f.getException()));
  }
}
