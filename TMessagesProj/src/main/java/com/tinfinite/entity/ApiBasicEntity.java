package com.tinfinite.entity;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.Serializable;

/**
 * Created by caiying on 11/22/14.
 */
public abstract class ApiBasicEntity implements Serializable {
    public FailureDetail error;

    public FailureDetail getError() {
        return error;
    }

    public void setError(FailureDetail error) {
        this.error = error;
    }

    public abstract ApiBasicEntity jsonParse(String rootContent);

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    public static class FailureDetail implements Serializable {
        public String message;
        public String type;
        public int code;

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public int getCode() {
            return code;
        }

        public void setCode(int code) {
            this.code = code;
        }

        @Override
        public String toString() {
            return ToStringBuilder.reflectionToString(this);
        }
    }
}
