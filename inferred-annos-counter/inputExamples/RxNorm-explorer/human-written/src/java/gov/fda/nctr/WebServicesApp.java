package gov.fda.nctr;

import java.util.Collections;

import org.checkerframework.checker.nullness.qual.Nullable;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorViewResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.servlet.ModelAndView;


@SpringBootApplication
@EnableScheduling
public class WebServicesApp
{
  public static void main(String[] args)
  {
    System.getProperties().setProperty("oracle.jdbc.J2EE13Compliant", "true");

    SpringApplication.run(WebServicesApp.class, args);
  }

  // We assume resources that are not found are caused by deep links into the client app making it here to the server,
  // either because the app isn't running yet (from the use of a stashed url for example) or the browser page is being
  // force refreshed. So we send index.html for resources that aren't found, to start the client app and let it try to
  // interpret the link once it's started.
  @Bean
  @SuppressWarnings("nullness") // Null returns are allowed from ErrorViewResolver.resolveErrorView().
  @Nullable ErrorViewResolver serveAppHtmlMissingResources()
  {
    return (request, status, model) -> status == HttpStatus.NOT_FOUND
      ? new ModelAndView("index.html", Collections.emptyMap(), HttpStatus.OK)
      : null;
  }
}
