package com.example.banhangapp.utils;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class DateDeserializer implements JsonDeserializer<Date> {
    private static final String[] DATE_FORMATS = new String[] {
        "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
        "yyyy-MM-dd'T'HH:mm:ss'Z'",
        "yyyy-MM-dd HH:mm:ss",
        "yyyy-MM-dd"
    };

    @Override
    public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        try {
            if (json == null || json.isJsonNull()) {
                return null;
            }
            
            String dateString = json.getAsString();
            if (dateString == null || dateString.isEmpty()) {
                return null;
            }

            for (String format : DATE_FORMATS) {
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.US);
                    sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                    return sdf.parse(dateString);
                } catch (ParseException e) {
                    // Try next format
                }
            }

            // If all formats fail, return null
            android.util.Log.w("DateDeserializer", "Could not parse date: " + dateString);
            return null;
        } catch (Exception e) {
            android.util.Log.e("DateDeserializer", "Error deserializing date", e);
            return null;
        }
    }
}

