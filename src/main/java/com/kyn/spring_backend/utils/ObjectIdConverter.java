package com.kyn.spring_backend.utils;

import org.bson.types.ObjectId;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ObjectIdConverter {

    private static final Pattern OBJECT_ID_PATTERN = Pattern.compile("ObjectId\\(\"([^\"]+)\"\\)");

    public static ObjectId convertToObjectId(String id) {
        if (id == null || id.isEmpty()) {
            return null;
        }

        Matcher matcher = OBJECT_ID_PATTERN.matcher(id);
        if (matcher.find()) {
            id = matcher.group(1);
        }

        if (ObjectId.isValid(id)) {
            return new ObjectId(id);
        } else {
            return null;
        }
    }

}