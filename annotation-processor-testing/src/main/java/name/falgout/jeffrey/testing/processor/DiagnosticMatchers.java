package name.falgout.jeffrey.testing.processor;

import static name.falgout.jeffrey.testing.LambdaMatcher.createMatcher;
import static org.hamcrest.CoreMatchers.containsString;

import java.net.URI;
import java.util.regex.Pattern;

import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

import org.hamcrest.CoreMatchers;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;

public final class DiagnosticMatchers {
  private DiagnosticMatchers() {}

  public static <S> Matcher<? super Diagnostic<? extends S>> is(Diagnostic.Kind kind) {
    return createMatcher(Diagnostic::getKind, CoreMatchers.is(kind));
  }

  public static <S> Matcher<? super Diagnostic<? extends S>> isOnLine(long line) {
    return createMatcher(Diagnostic::getLineNumber, CoreMatchers.is(line));
  }

  public static <S> Matcher<? super Diagnostic<? extends S>> hasMessage(String substring) {
    return hasMessage(containsString(substring));
  }

  public static <S> Matcher<? super Diagnostic<? extends S>> hasMessage(Pattern pattern) {
    return hasMessage(new TypeSafeDiagnosingMatcher<String>(String.class) {
      @Override
      public void describeTo(Description description) {
        description.appendText("a string containing /")
            .appendText(pattern.pattern())
            .appendText("/");
      }

      @Override
      protected boolean matchesSafely(String item, Description mismatchDescription) {
        if (pattern.matcher(item).find()) {
          return true;
        }

        mismatchDescription.appendText("was not contained in ").appendValue(item);
        return false;
      }
    });
  }

  public static <S> Matcher<? super Diagnostic<? extends S>> hasMessage(
      Matcher<? super String> matcher) {
    return createMatcher(d -> d.getMessage(null), "Diagnostic with getMessage()", matcher);
  }

  public static Matcher<? super Diagnostic<? extends JavaFileObject>> hasSource(
      JavaFileObject source) {
    return createMatcher(Diagnostic::getSource, hasUri(source.toUri()));
  }

  private static Matcher<? super JavaFileObject> hasUri(URI uri) {
    return createMatcher(JavaFileObject::toUri, CoreMatchers.is(uri));
  }
}
