package gov.fda.nctr.util;

import java.util.HashSet;
import java.util.Set;

import org.checkerframework.checker.nullness.qual.NonNull;


public class CollectionUtils
{
  public static <T> Set<@NonNull T> minus(Set<@NonNull T> set1, Set<@NonNull T> set2)
  {
    Set<@NonNull T> res = new HashSet<@NonNull T>();
    for (T e : set1)
      if (!set2.contains(e))
        res.add(e);
    return res;
  }

  public static <T> Set<@NonNull T> intersection(Set<@NonNull T> set1, Set<@NonNull T> set2)
  {
    Set<@NonNull T> res = new HashSet<@NonNull T>();
    for (T e : set1)
      if (set2.contains(e))
        res.add(e);
    return res;
  }

  private CollectionUtils()
  {
  }
}
