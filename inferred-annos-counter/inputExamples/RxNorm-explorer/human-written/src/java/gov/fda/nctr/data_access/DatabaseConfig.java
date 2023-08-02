package gov.fda.nctr.data_access;

import javax.sql.DataSource;
import javax.validation.constraints.NotEmpty;

import org.checkerframework.checker.nullness.qual.Nullable;

import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;


@Configuration
public class DatabaseConfig
{
  protected final Logger log = LoggerFactory.getLogger(DatabaseConfig.class);

  @Value("${jdbc.driverClassName}")
  private @NotEmpty String driverClassName;

  @Value("${jdbc.url}")
  private @NotEmpty String url;

  @Value("${jdbc.username}")
  private @NotEmpty String username;

  @Value("${jdbc.password}")
  private @NotEmpty String password;

  @SuppressWarnings("initialization.fields.uninitialized")
  // Spring Framework should enforce the field initialization constraints.
  DatabaseConfig() {}

  public enum DatabaseUnquoatedIdentifierStorage
  {
    UPPERCASE,
    LOWERCASE,
    MIXED
  }

  public enum DatabaseType { POSTGRESQL, ORACLE }

  @Bean
  public DataSource getDataSource()
  {
    return
      DataSourceBuilder.create()
      .driverClassName(driverClassName)
      .url(url)
      .username(username)
      .password(password)
      .build();
  }

  public DatabaseType getDatabaseType()
  {
    if (driverClassName.startsWith("org.postgresql."))
      return DatabaseType.POSTGRESQL;
    else if (driverClassName.startsWith("oracle."))
      return DatabaseType.ORACLE;
    else
      throw new RuntimeException("Unrecognized database type.");
  }

  public String normalizeId(String ident)
  {
    switch (getUnquotedIdentifierStorage())
    {
      case LOWERCASE:
        return ident.toLowerCase();
      case UPPERCASE:
        return ident.toUpperCase();
      default:
        return ident;
    }
  }

  public @Nullable Object optionalJsonParamValue(@Nullable String maybeContent)
  {
    return makeJsonParamValueImpl(maybeContent);
  }

  public Object jsonParamValue(String content)
  {
    @Nullable Object paramVal = makeJsonParamValueImpl(content);
    if (paramVal == null)
      throw new RuntimeException("Expected content, got null.");
    else
      return paramVal;
  }

  public Object jsonParamValue(ObjectNode content)
  {
    @Nullable Object paramVal = makeJsonParamValueImpl(content.toString());
    if (paramVal == null)
      throw new RuntimeException("Expected content, got null.");
    else
      return paramVal;
  }

  private @Nullable Object makeJsonParamValueImpl(@Nullable String content)
  {
    try
    {
      switch (getDatabaseType())
      {
        case ORACLE:
          return content;
        case POSTGRESQL:
          // (removed code here so as to not require postgres driver dependency)
          throw new RuntimeException("Unsupported database type.");
        default:
          throw new RuntimeException("Unsupported database type.");
      }
    }
    catch (Exception e)
    {
      throw new RuntimeException(e);
    }
  }

  private DatabaseUnquoatedIdentifierStorage getUnquotedIdentifierStorage()
  {
    switch (getDatabaseType())
    {
      case POSTGRESQL:
        return DatabaseUnquoatedIdentifierStorage.LOWERCASE;
      case ORACLE:
        return DatabaseUnquoatedIdentifierStorage.UPPERCASE;
      default:
        throw new RuntimeException("Database unquoted identifier setting needs to be configured.");
    }
  }
}
