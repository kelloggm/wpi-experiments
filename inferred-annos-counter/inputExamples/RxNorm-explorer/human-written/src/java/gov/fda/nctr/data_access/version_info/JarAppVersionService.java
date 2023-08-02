package gov.fda.nctr.data_access.version_info;

import java.io.IOException;
import java.io.InputStream;

import org.checkerframework.checker.nullness.qual.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;

import gov.fda.nctr.models.dto.AppVersion;
import gov.fda.nctr.util.Nullables;


@Service
public class JarAppVersionService implements AppVersionService
{
  private @Nullable AppVersion appVersion;

  private final Logger log = LoggerFactory.getLogger(JarAppVersionService.class);

  public JarAppVersionService()
  {
    ObjectMapper jsonSerializer = Jackson2ObjectMapperBuilder.json().build();
    ObjectReader jsonReader = jsonSerializer.readerFor(AppVersion.class);

    ClassLoader classLoader = Nullables.requireNonNull(JarAppVersionService.class.getClassLoader());

    try (InputStream inputStream = Nullables.asNullable(classLoader.getResourceAsStream("git.properties")))
    {
      if (inputStream == null)
      {
        log.info("Could not retrieve application info (normal if in dev mode).");
        this.appVersion = null;
      }
      else
      {
        AppVersion ver = jsonReader.readValue(inputStream);

        log.info("Application version information: " + ver);

        this.appVersion = ver;
      }
    }
    catch (IOException e)
    {
      log.info("Could not retrieve application info (normal if in dev mode).");
      this.appVersion = null;
    }
  }

  @Override
  public @Nullable AppVersion getAppVersion()
  {
    return appVersion;
  }
}
