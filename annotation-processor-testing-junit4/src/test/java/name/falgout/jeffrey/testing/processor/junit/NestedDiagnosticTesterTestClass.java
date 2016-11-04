package name.falgout.jeffrey.testing.processor.junit;

import org.junit.runner.RunWith;

import name.falgout.jeffrey.testing.processor.ExpectError;
import name.falgout.jeffrey.testing.processor.UseProcessor;

@RunWith(DiagnosticTester.class)
@UseProcessor(FakeProcessor.class)
public class NestedDiagnosticTesterTestClass {
  @ExpectError(lineOffset = 1)
  @FakeProcessor.Error
  void method1() {}

  static class Static {
     @ExpectError("a") @FakeProcessor.Error("a")
     void method1() {}

    static class StaticStatic {
      @ExpectError(value = "b", lineOffset = 1)
      @FakeProcessor.Error("b")
      void method() {}
    }

    class StaticInstance {
      @ExpectError("c") @FakeProcessor.Error("c")
      void method() {}
    }

    @ExpectError("d") @FakeProcessor.Error("d")
    void method2() {}
  }
  
  @ExpectError("e") @FakeProcessor.Error("e")
  void method2() {}

  class Instance {
    @ExpectError("f") @FakeProcessor.Error("f")
    void method1() {}

    class InstanceInstance {
      @ExpectError("g") @FakeProcessor.Error("g")
      void method() {}
    }

    @ExpectError("h") @FakeProcessor.Error("h")
    void method2() {}
  }
}
