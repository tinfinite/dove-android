package com.tinfinite.entity;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

/**
 * Created by caiying on 11/22/14.
 */
public class ApiResponse<T> {

    private JsonNode mRootNode;
    private static ObjectMapper mObjectMapper = new ObjectMapper();
    static {
        mObjectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mObjectMapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
    }

    public ApiResponse(String content) {
        if(null != content) {
            try {
                this.mRootNode = ((JsonNode) this.mObjectMapper.readValue(content, JsonNode.class));
            } catch (JsonParseException e) {
                e.printStackTrace();
            } catch (JsonMappingException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean isSuccess() {
        if(null == mObjectMapper)
            return false;

        JsonNode result = null;
        if(mRootNode != null)
            return !mRootNode.has("error");

        return true;
    }

    public boolean hasMessage() {
        if(null == mObjectMapper) {
            return true;
        }

        JsonNode reason = null;
        if(mRootNode != null)
            reason = mRootNode.get("message");

        if(reason != null)
            return true;

        return false;
    }

    public String getMessage() {
        if(!hasMessage())
            return "";

        return String.valueOf(mRootNode.get("message"));
    }

    public T getData(TypeReference typeRef, String...keys) {
        JsonNode jsonNode = mRootNode;
        if(keys.length > 0) {
            for (int i = 0;i < keys.length; i++) {
                jsonNode = jsonNode.get(keys[i]);
            }
        }
        try {
            return mObjectMapper.readValue(String.valueOf(jsonNode), typeRef);
        } catch (JsonParseException e) {
            e.printStackTrace();
        } catch (JsonMappingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public Object getData(Class clazz, String...keys) {
        JsonNode jsonNode = mRootNode;
        if(keys.length > 0) {
            for (int i = 0;i < keys.length; i++) {
                jsonNode = jsonNode.get(keys[i]);
            }
        }
        try {
            return mObjectMapper.readValue(String.valueOf(jsonNode), clazz);
        } catch (JsonParseException e) {
            e.printStackTrace();
        } catch (JsonMappingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
