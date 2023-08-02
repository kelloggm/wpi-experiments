package gov.fda.nctr;

import org.checkerframework.checker.nullness.qual.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;


@Configuration
public class AppConfig
{
  public AppConfig() { }

  protected final Logger log = LoggerFactory.getLogger(AppConfig.class);

  @Value("${app.example-prop}")
  private @Nullable String exampleProp;

  public @Nullable String getExampleProp()
  {
    return exampleProp;
  }
}
