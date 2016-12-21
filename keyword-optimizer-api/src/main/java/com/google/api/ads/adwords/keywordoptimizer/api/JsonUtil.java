// Copyright 2016 Google Inc. All Rights Reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.api.ads.adwords.keywordoptimizer.api;

import com.google.api.ads.adwords.axis.v201609.cm.Keyword;
import com.google.api.ads.adwords.axis.v201609.cm.Money;
import com.google.api.ads.adwords.keywordoptimizer.KeywordCollection;
import com.google.api.ads.adwords.keywordoptimizer.KeywordInfo;
import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import java.lang.reflect.Field;

/**
 * Utility functions to convert objects from and to JSON.
 */
public class JsonUtil {
  
  // Two separate Gson instances for pretty and compact printing. Gson is thread-safe.
  private static final Gson gsonCompact = initGson(false);
  private static final Gson gsonPretty = initGson(true);
  
  /**
   * Converts an {@link Exception} object to a JSON string.
   */
  public static String convertToJson(Exception e, boolean prettyPrint) {
    if (prettyPrint) {
      return gsonPretty.toJson(e);
    }
    return gsonCompact.toJson(e);
  }
 
  /**
   * Converts a {@link KeywordCollection} object to a JSON string.
   */
  public static String convertToJson(KeywordCollection keywords, boolean prettyPrint) {
    if (prettyPrint) {
      return gsonPretty.toJson(keywords);
    }
    return gsonCompact.toJson(keywords);
  }
  
  /**
   * Initializes Gson to convert objects to and from JSON. This method customizes a "plain" Gson by
   * adding appropriate exclusions strategies / adapters as needed in this project for a "pretty"
   * output. 
   */
  private static Gson initGson(boolean prettyPrint) {
    GsonBuilder builder = new GsonBuilder();
    
    // Exclude superclasses.
    ExclusionStrategy superclassExclusionStrategy = new SuperclassExclusionStrategy();
    builder.addDeserializationExclusionStrategy(superclassExclusionStrategy);
    builder.addSerializationExclusionStrategy(superclassExclusionStrategy);
    
    // Exclude underscore fields in client lib objects.
    ExclusionStrategy underscoreExclusionStrategy = new ExclusionStrategy(){
      @Override
      public boolean shouldSkipField(FieldAttributes field) {
        if (field.getName().startsWith("_")) {
          return true;
        }
        
        return false;
      }
      @Override
      public boolean shouldSkipClass(Class<?> clazz) {
        return false;
      }
    };
    builder.addDeserializationExclusionStrategy(underscoreExclusionStrategy);
    builder.addSerializationExclusionStrategy(underscoreExclusionStrategy);
    
    // Render KeywordCollection as an array of KeywordInfos.
    builder.registerTypeAdapter(
        KeywordCollection.class,
        new JsonSerializer<KeywordCollection>() {
          @Override
          public JsonElement serialize(
              KeywordCollection src,
              java.lang.reflect.Type typeOfSrc,
              JsonSerializationContext context) {
            JsonArray out = new JsonArray();
            for (KeywordInfo info : src.getListSortedByScore()) {
              out.add(context.serialize(info));
            }
            return out;
          }
        });
    // Render Money as a primitive.
    builder.registerTypeAdapter(
        Money.class,
        new JsonSerializer<Money>() {
          @Override
          public JsonElement serialize(
              Money src, java.lang.reflect.Type typeOfSrc, JsonSerializationContext context) {
            JsonElement out = new JsonPrimitive(src.getMicroAmount() / 1000000);
            return out;
          }
        });
    // Render Keyword in a simple way.
    builder.registerTypeAdapter(
        Keyword.class,
        new JsonSerializer<Keyword>() {
          @Override
          public JsonElement serialize(
              Keyword src, java.lang.reflect.Type typeOfSrc, JsonSerializationContext context) {
            JsonObject out = new JsonObject();
            out.addProperty("text", src.getText());
            out.addProperty("matchtype", src.getMatchType().toString());
            return out;
          }
        });
    // Render Throwable in a simple way (for all subclasses).
    builder.registerTypeHierarchyAdapter(
        Throwable.class,
        new JsonSerializer<Throwable>() {
          @Override
          public JsonElement serialize(
              Throwable src, java.lang.reflect.Type typeOfSrc, JsonSerializationContext context) {
            JsonObject out = new JsonObject();
            out.addProperty("message", src.getMessage());
            out.addProperty("type", src.getClass().getName());
            
            JsonArray stack = new JsonArray();
            for (StackTraceElement stackTraceElement : src.getStackTrace()) {
              JsonObject stackElem = new JsonObject();
              stackElem.addProperty("file", stackTraceElement.getFileName());
              stackElem.addProperty("line", stackTraceElement.getLineNumber());
              stackElem.addProperty("method", stackTraceElement.getMethodName());
              stackElem.addProperty("class", stackTraceElement.getClassName());
              stack.add(stackElem);
            }
            out.add("stack", stack);
            
            if (src.getCause() != null) {
              out.add("cause", context.serialize(src.getCause()));
            }
            return out;
          }
        });
    
    if (prettyPrint) {
      builder.setPrettyPrinting();
    }
    
    return builder.create();
  }
 
  /**
   * Strategy for excluding fields derived from superclasses when serializing to JSON.
   */
  private static class SuperclassExclusionStrategy implements ExclusionStrategy {
    
    @Override
    public boolean shouldSkipClass(Class<?> arg0) {
      return false;
    }

    @Override
    public boolean shouldSkipField(FieldAttributes fieldAttributes) {
      String fieldName = fieldAttributes.getName();
      Class<?> theClass = fieldAttributes.getDeclaringClass();

      return isFieldInSuperclass(theClass, fieldName);
    }

    private boolean isFieldInSuperclass(Class<?> subclass, String fieldName) {
      Class<?> superclass = subclass.getSuperclass();
      Field field;

      while (superclass != null) {
        field = getField(superclass, fieldName);

        if (field != null) {
          return true;
        }

        superclass = superclass.getSuperclass();
      }

      return false;
    }

    private Field getField(Class<?> theClass, String fieldName) {
      try {
        return theClass.getDeclaredField(fieldName);
      } catch (Exception e) {
        return null;
      }
    }
  }
}
