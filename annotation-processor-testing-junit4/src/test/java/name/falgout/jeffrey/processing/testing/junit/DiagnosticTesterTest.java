package name.falgout.jeffrey.processing.testing.junit;

import org.junit.runner.RunWith;

import name.falgout.jeffrey.processor.testing.ExpectError;
import name.falgout.jeffrey.processor.testing.UseProcessor;

@RunWith(DiagnosticTester.class)
@UseProcessor(FakeProcessor.class)
public class DiagnosticTesterTest {
  @ExpectError("foo") @FakeProcessor.Error("foo")
  void method1() {}

  @ExpectError(value = "bar", lineOffset = 1)
  @FakeProcessor.Error("bar")
  void method2() {}

  @ExpectError(value = "something-else", lineOffset = 1)
  @FakeProcessor.Error
  void method3() {}

  @ExpectError(value = "something-else", lineOffset = 1)
  @FakeProcessor.Warning
  void method4() {}
}
