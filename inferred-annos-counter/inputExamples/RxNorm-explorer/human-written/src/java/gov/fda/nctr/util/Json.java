package gov.fda.nctr.util;

import java.util.List;

import org.checkerframework.checker.nullness.qual.Nullable;

import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import static com.fasterxml.jackson.annotation.PropertyAccessor.FIELD;


public final class Json
{
  public static ObjectMapper makeJsonObjectMapper()
  {
    return
      Jackson2ObjectMapperBuilder.json()
      .failOnUnknownProperties(true)
      .visibility(FIELD, JsonAutoDetect.Visibility.ANY)
      .build();
  }

  public static <T> @Nullable T readJsonn(ObjectMapper objMapper, @Nullable String s, Class<T> c)
  {
    if (s == null)
      return null;
    return readJson(objMapper, s, c);

  }

  public static <T> T readJson(ObjectMapper objMapper, String s, Class<T> c)
  {
    try
    {
      return objMapper.readValue(s.getBytes(), c);
    }
    catch (Exception e)
    {
      throw new RuntimeException(e);
    }
  }

  public static JsonNode readJsonNode(ObjectMapper objMapper, String s)
  {
    try
    {
      return objMapper.readTree(s);
    }
    catch (Exception e)
    {
      throw new RuntimeException(e);
    }
  }

  public static ObjectNode readJsonObjectNode(ObjectMapper objMapper, String s)
  {
    return (ObjectNode) readJsonNode(objMapper, s);
  }

  public static String toJson(Object o, ObjectMapper objMapper)
  {
    try
    {
      return objMapper.writeValueAsString(o);
    }
    catch (Exception e)
    {
      throw new RuntimeException(e);
    }
  }

  @SuppressWarnings("nullness")
  public static @Nullable JsonNode getJsonPart(ObjectNode sourceNode, List<String> partPath)
  {
    JsonNode curr = sourceNode;

    for (String propertyName : partPath)
    {
      curr = curr.get(propertyName);
      if (curr == null)
        return null;
    }

    return curr;
  }

  public static @Nullable ObjectNode getJsonObjectPart(ObjectNode sourceNode, List<String> partPath)
  {
    JsonNode partNode = getJsonPart(sourceNode, partPath);
    return (partNode != null) ? (ObjectNode) partNode : null;
  }


  public static @Nullable Long getJsonLongPart(ObjectNode sourceNode, List<String> partPath)
  {
    JsonNode partNode = getJsonPart(sourceNode, partPath);
    return (partNode != null) ? partNode.longValue() : null;
  }

  public static @Nullable String getJsonStringPart(ObjectNode sourceNode, List<String> partPath)
  {
    JsonNode partNode = getJsonPart(sourceNode, partPath);
    return (partNode != null) ? partNode.textValue() : null;
  }

  @SuppressWarnings("nullness")
  public static ObjectNode withReplacedObjectNode(ObjectNode sourceNode, List<String> replacementPath, ObjectNode replacementNode)
  {
    if (replacementPath.isEmpty())
      return replacementNode;

    ObjectNode res = sourceNode.deepCopy();

    ObjectNode curr = res;
    for (String propertyName : replacementPath.subList(0, replacementPath.size() - 1))
    {
      ObjectNode next = (ObjectNode) curr.get(propertyName);
      curr = (next == null) ? curr.putObject(propertyName) : next;
    }

    String lastKey = replacementPath.get(replacementPath.size() - 1);
    curr.replace(lastKey, replacementNode);

    return res;
  }

  private Json()
  {
  }
}
