package gov.fda.nctr.models.dto;

import org.checkerframework.checker.nullness.qual.Nullable;
import com.fasterxml.jackson.annotation.JsonAlias;


public class AppVersion
{
  @JsonAlias("git.build.time")
  private @Nullable String buildTimestamp;

  @JsonAlias("git.build.user.email")
  private @Nullable String buildUserEmail;

  @JsonAlias("git.build.user.name")
  private @Nullable String buildUserName;

  @JsonAlias("git.commit.id.abbrev")
  private @Nullable String commitId;

  @JsonAlias("git.commit.time")
  private @Nullable String commitTimestamp;

  public AppVersion()
  {
  }

  public @Nullable String getBuildTimestamp()
  {
    return buildTimestamp;
  }

  public @Nullable String getBuildUserEmail()
  {
    return buildUserEmail;
  }

  public @Nullable String getBuildUserName()
  {
    return buildUserName;
  }

  public @Nullable String getCommitId()
  {
    return commitId;
  }

  public @Nullable String getCommitTimestamp()
  {
    return commitTimestamp;
  }

  @Override
  public String toString()
  {
    return "{" +
      "buildTimestamp=" + buildTimestamp +
      ", buildUserEmail='" + buildUserEmail + '\'' +
      ", buildUserName='" + buildUserName + '\'' +
      ", commitId='" + commitId + '\'' +
      ", commitTimestamp=" + commitTimestamp +
      '}';
  }
}
