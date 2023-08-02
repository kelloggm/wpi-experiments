package gov.fda.nctr.util;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.checkerframework.checker.nullness.qual.Nullable;


public final class Nullables
{
  public static <T, U> @Nullable U applyIfPresent(@Nullable T t, Function<T, U> f)
  {
    return t == null ? null : f.apply(t);
  }

  public static <T, U> @Nullable U applyOrNull(@Nullable T t, Function<T, @Nullable U> f)
  {
    return t == null ? null : f.apply(t);
  }

  public static <T, U> U applyOr(@Nullable T t, Function<T, U> f, U defaultVal)
  {
    return t == null ? defaultVal : f.apply(t);
  }

  public static <T> void ifPresent(@Nullable T t, Consumer<T> f)
  {
    if (t != null)
      f.accept(t);
  }

  public static <T> T valueOr(@Nullable T t, T defaultVal)
  {
    return t != null ? t : defaultVal;
  }

  public static <T> T valueOrGet(@Nullable T t, Supplier<T> defaultValFn)
  {
    return t != null ? t : defaultValFn.get();
  }

  public static <T> T valueOrThrow(@Nullable T t, Supplier<? extends RuntimeException> errFn)
  {
    if (t != null)
      return t;
    else
      throw errFn.get();
  }

  /// Like Objects.requireNonNull, but accepts a nullable type, contrary to how the checker interprets
  /// Objects.requireNonNull. This avoids the unhelpful warnings from the checker that the argument might be null. That
  /// the caller accepts this is a possibility and wants that condition checked on penalty of NPE is the *purpose* of
  /// the call in the first place at least as it is used here, and the additional warnings only add noise.
  public static <T> T requireNonNull(@Nullable T t) throws NullPointerException
  {
    if (t == null)
      throw new NullPointerException();
    return t;
  }

  public static <T> @Nullable T asNullable(T t)
  {
    return t;
  }

  public static <T> T nonNull(@Nullable T t, String message)
  {
    if ( t == null )
      throw new RuntimeException(message);
    return t;
  }

  public static <T> T or(@Nullable T t, T defaultVal)
  {
    if ( t != null ) return t;
    else return defaultVal;
  }

  private Nullables()
  {
  }
}
