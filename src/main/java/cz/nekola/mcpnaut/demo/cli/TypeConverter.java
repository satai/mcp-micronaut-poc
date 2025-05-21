package cz.nekola.mcpnaut.demo.cli;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.micronaut.core.annotation.NonNull;
import jakarta.inject.Singleton;

import java.util.HashMap;
import java.util.Map;

@Singleton
public class TypeConverter {

    private final JsonNodeFactory nodeFactory = JsonNodeFactory.instance;

    public Map<String, JsonNode> jdkType2McpType(@NonNull Class<?> type) {
        if (type.equals(String.class)) {
            return Map.of("type", nodeFactory.textNode("String"));
        } else if (type.equals(Integer.TYPE) || type.equals(Integer.class) ||
                   type.equals(Long.TYPE) || type.equals(Long.class)) {
            return Map.of("type", nodeFactory.textNode("integer"));
        } else if (type.equals(Double.TYPE) || type.equals(Double.class) ||
                   type.equals(Float.TYPE) || type.equals(Float.class)) {
            return Map.of("type", nodeFactory.textNode("number"));
        } else if (type.equals(Boolean.TYPE) || type.equals(Boolean.class)) {
            return Map.of("type", nodeFactory.textNode("boolean"));
        } else if (type.isArray()) {
            ObjectNode itemsNode = nodeFactory.objectNode();
            Map<String, JsonNode> componentTypeMap = jdkType2McpType(type.getComponentType());
            componentTypeMap.forEach(itemsNode::set); // Correctly set map entries to ObjectNode

            Map<String, JsonNode> arrayMap = new HashMap<>();
            arrayMap.put("type", nodeFactory.textNode("array"));
            arrayMap.put("items", itemsNode);
            return arrayMap;
        } else {
            throw new IllegalArgumentException(type + " is not supported");
        }
    }
}
