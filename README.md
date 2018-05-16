# annotation-processor-testing

An easy way to verify that your annotation processor is emitting the right
diagnostics.

## Setup
In order to use any of the test runners, you must include your test sources as a
resource:
```
<build>
...
<testResources>
  <testResource>
    <directory>src/test/java/</directory>
  </testResource>
  <testResource>
    <directory>src/test/resources/</directory>
  </testResource>
</testResources>
...
</build>
```

Additionally, you may need to disable annotation processing during test
compilation if you are testing `ERROR` diagnostics.

```
<build>
...
<plugins>
 <groupId>org.apache.maven.plugins</groupId>
 <artifactId>maven-compiler-plugin</artifactId>
 <executions>
   <execution>
     <id>default-testCompile</id>
     <goals>
       <goal>testCompile</goal>
     </goals>
     <configuration>
       <proc>none</proc>
     </configuration>
   </execution>
 </executions>
</plugins
...
</build>
```

## Usage
Annotate your test class with `@UseProcessor`, `@ExpectError`, and
`@ExpectDiagnostic`:

```
@UseProcessor(YourProcessor.class)
public class YourProcessorTest {
  @ExpectError @IncorrectlyUsedAnnotation
  public void method() {}

  @ExpectError("substring of the message") @IncorrectlyUsedAnnotation
  public void method1() {}

  @ExpectError(lineOffset = 1) // Expect a diagnostic on the next line.
  @IncorrectlyUsedAnnotation
  public void method2() {}

  @ExpectError(value = "[abc]+", regex = true) @IncorrectlyUsedAnnotation
  public void method3() {}

  @ExpectError(testName = "descriptive JUnit test name") @IncorrectlyUsedAnnotation
  public void method4() {}

  // No @Expect annotation needed.
  @CorrectlyUsedAnnotation
  public void method5() {}
}
```

### JUnit 4
[![Maven Central][junit4-img]][junit4-link]

Add `@RunWith(DiagnosticTester.class)` to your test class.

### JUnit 5
[![Maven Central][junit5-img]][junit5-link]

Ensure that `annotation-processor-testing-junit5` is included as a dependency,
and the JUnit 5 launcher should automatically discover your tests.

[junit4-img]: https://maven-badges.herokuapp.com/maven-central/name.falgout.jeffrey.testing/annotation-processor-testing-junit4/badge.svg
[junit4-link]: https://maven-badges.herokuapp.com/maven-central/name.falgout.jeffrey.testing/annotation-processor-testing-junit4
[junit5-img]: https://maven-badges.herokuapp.com/maven-central/name.falgout.jeffrey.testing/annotation-processor-testing-junit5/badge.svg
[junit5-link]: https://maven-badges.herokuapp.com/maven-central/name.falgout.jeffrey.testing/annotation-processor-testing-junit5
