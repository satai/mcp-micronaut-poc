package cz.nekola.mcpnaut.demo.cli;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.type.Argument;
import io.modelcontextprotocol.sdk.CallToolRequest; // Java SDK
import jakarta.inject.Singleton;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

@Singleton
public class ArgumentConverter {

    public Object mcpValue2jvmValue(
            CallToolRequest call, // Java SDK CallToolRequest
            @NonNull Argument<?> parameter) {
        JsonNode element = call.getArguments().get(parameter.getName());
        if (element == null || element.isNull()) {
            return null;
        }
        return convertType(parameter.getType(), element);
    }

    private Object convertType(
            @NonNull Class<?> type,
            JsonNode element) {
        if (element.isNull()) return null;

        if (type.equals(Integer.TYPE) || type.equals(Integer.class)) {
            return element.isInt() ? element.asInt() : null;
        } else if (type.equals(Long.TYPE) || type.equals(Long.class)) {
            return element.isLong() || element.isInt() ? element.asLong() : null;
        } else if (type.equals(Boolean.TYPE) || type.equals(Boolean.class)) {
            return element.isBoolean() ? element.asBoolean() : null;
        } else if (type.equals(Float.TYPE) || type.equals(Float.class)) {
            return element.isFloat() || element.isDouble() ? (float) element.asDouble() : null;
        } else if (type.equals(Double.TYPE) || type.equals(Double.class)) {
            return element.isDouble() || element.isFloat() || element.isInt() || element.isLong() ? element.asDouble() : null;
        } else if (type.equals(String.class)) {
            return element.isTextual() ? element.asText() : null;
        } else if (type.isArray()) {
            return convertArray(type, element);
        } else {
            throw new IllegalArgumentException("Unsupported type: " + type);
        }
    }

    private Object convertArray(
            @NonNull Class<?> type,
            JsonNode element) {
        if (!element.isArray()) {
            return null; // Or throw exception, depending on desired strictness
        }
        ArrayNode arrayNode = (ArrayNode) element;
        Class<?> componentType = type.getComponentType();
        int size = arrayNode.size();

        if (componentType.isPrimitive()) {
            if (componentType.equals(Integer.TYPE)) {
                int[] targetArray = new int[size];
                for (int i = 0; i < size; i++) {
                    JsonNode item = arrayNode.get(i);
                    targetArray[i] = (item != null && item.isInt()) ? item.asInt() : 0; // Default to 0 if null/not int
                }
                return targetArray;
            } else if (componentType.equals(Long.TYPE)) {
                long[] targetArray = new long[size];
                for (int i = 0; i < size; i++) {
                    JsonNode item = arrayNode.get(i);
                    targetArray[i] = (item != null && (item.isLong() || item.isInt())) ? item.asLong() : 0L;
                }
                return targetArray;
            } else if (componentType.equals(Double.TYPE)) {
                double[] targetArray = new double[size];
                for (int i = 0; i < size; i++) {
                    JsonNode item = arrayNode.get(i);
                    targetArray[i] = (item != null && item.isNumber()) ? item.asDouble() : 0.0;
                }
                return targetArray;
            } else if (componentType.equals(Float.TYPE)) {
                float[] targetArray = new float[size];
                for (int i = 0; i < size; i++) {
                    JsonNode item = arrayNode.get(i);
                    targetArray[i] = (item != null && item.isNumber()) ? (float) item.asDouble() : 0.0f;
                }
                return targetArray;
            } else if (componentType.equals(Boolean.TYPE)) {
                boolean[] targetArray = new boolean[size];
                for (int i = 0; i < size; i++) {
                    JsonNode item = arrayNode.get(i);
                    targetArray[i] = (item != null && item.isBoolean()) ? item.asBoolean() : false;
                }
                return targetArray;
            } else {
                throw new IllegalArgumentException("Unsupported primitive componentType: " + componentType);
            }
        } else if (componentType.isRecord()) {
            // TODO: Handle record types if necessary
            throw new UnsupportedOperationException("Record types in arrays are not yet supported.");
        } else {
            // Object array
            Object[] targetArray = (Object[]) Array.newInstance(componentType, size);
            for (int i = 0; i < size; i++) {
                targetArray[i] = convertType(componentType, arrayNode.get(i));
            }
            return targetArray;
        }
    }
}
