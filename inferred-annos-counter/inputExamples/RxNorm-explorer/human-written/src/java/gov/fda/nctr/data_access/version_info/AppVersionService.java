package gov.fda.nctr.data_access.version_info;

import org.checkerframework.checker.nullness.qual.Nullable;

import gov.fda.nctr.models.dto.AppVersion;

public interface AppVersionService
{
  @Nullable AppVersion getAppVersion();
}
