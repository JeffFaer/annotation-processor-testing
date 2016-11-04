import name.falgout.jeffrey.testing.processor.ExpectError;

public class Expectations {
  @ExpectError(value = "foo", lineOffset = 5)
  public void foo() {}
}
