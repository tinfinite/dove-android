package com.tinfinite.entity;

import com.fasterxml.jackson.core.type.TypeReference;

/**
 * Created by PanJiafang on 15/4/1.
 */
public class ReplyResultEntity extends ApiBasicEntity {

    private boolean result;
    private String id;

    public boolean isResult() {
        return result;
    }

    public void setResult(boolean result) {
        this.result = result;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public ReplyResultEntity jsonParse(String rootContent) {
        ApiResponse<ReplyResultEntity> apiResponse = new ApiResponse<>(rootContent);
        return apiResponse.getData(new TypeReference<ReplyResultEntity>() {});
    }
}
