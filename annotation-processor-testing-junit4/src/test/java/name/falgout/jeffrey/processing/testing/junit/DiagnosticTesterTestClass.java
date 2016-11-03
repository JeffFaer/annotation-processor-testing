package name.falgout.jeffrey.processing.testing.junit;

import name.falgout.jeffrey.processor.testing.ExpectError;
import name.falgout.jeffrey.processor.testing.UseProcessor;

@UseProcessor(FakeProcessor.class)
public class DiagnosticTesterTestClass {
  @ExpectError(value = "foo", testName = "1") @FakeProcessor.Error("foo")
  void method1() {}

  @ExpectError(value = "bar", lineOffset = 1, testName = "2")
  @FakeProcessor.Error("bar")
  void method2() {}

  @ExpectError(value = "something-else", lineOffset = 1, testName = "3")
  @FakeProcessor.Error
  void method3() {}

  @ExpectError(value = "something-else", lineOffset = 1, testName = "4")
  @FakeProcessor.Warning
  void method4() {}
}
