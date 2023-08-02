package gov.fda.nctr.controllers;

import org.springframework.web.bind.annotation.*;

import gov.fda.nctr.data_access.version_info.AppVersionService;
import gov.fda.nctr.exceptions.ResourceNotFoundException;
import gov.fda.nctr.models.dto.AppVersion;
import gov.fda.nctr.util.Nullables;


@RestController
@RequestMapping("/api/app-version")
public class AppVersionController
{
  private AppVersionService appVersionService;

  public AppVersionController(AppVersionService appVersionService)
  {
    this.appVersionService = appVersionService;
  }

  @GetMapping("/")
  public AppVersion getApplicationVersion()
  {
    AppVersion ver = appVersionService.getAppVersion();

    return Nullables.valueOrThrow(ver, () -> new ResourceNotFoundException("Version information is not available."));
  }
}
