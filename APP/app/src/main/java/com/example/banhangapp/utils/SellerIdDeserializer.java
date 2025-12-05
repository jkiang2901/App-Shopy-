package com.example.banhangapp.utils;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import java.lang.reflect.Type;

public class SellerIdDeserializer implements JsonDeserializer<String> {
    @Override
    public String deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        try {
            if (json == null || json.isJsonNull()) {
                return null;
            }
            if (json.isJsonPrimitive() && json.getAsJsonPrimitive().isString()) {
                // If it's a string ID, return it directly
                return json.getAsString();
            }
            if (json.isJsonObject()) {
                // If it's an object, extract the _id field
                JsonElement idElement = json.getAsJsonObject().get("_id");
                if (idElement != null && !idElement.isJsonNull()) {
                    return idElement.getAsString();
                }
                // If _id doesn't exist, try "id"
                idElement = json.getAsJsonObject().get("id");
                if (idElement != null && !idElement.isJsonNull()) {
                    return idElement.getAsString();
                }
                // If neither exists, return null
                return null;
            }
            // If it's an array or other type, return null
            return null;
        } catch (Exception e) {
            // Return null on any parsing error instead of throwing
            android.util.Log.e("SellerIdDeserializer", "Error deserializing SellerId: " + (json != null ? json.toString() : "null"), e);
            return null;
        }
    }
}

