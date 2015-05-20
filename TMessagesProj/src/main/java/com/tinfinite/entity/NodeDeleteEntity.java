package com.tinfinite.entity;

import com.fasterxml.jackson.core.type.TypeReference;

/**
 * Created by PanJiafang on 15/4/2.
 */
public class NodeDeleteEntity extends ApiBasicEntity {
    private boolean result;

    public boolean isResult() {
        return result;
    }

    public void setResult(boolean result) {
        this.result = result;
    }

    @Override
    public NodeDeleteEntity jsonParse(String rootContent) {
        ApiResponse<NodeDeleteEntity> apiResponse = new ApiResponse<>(rootContent);
        return apiResponse.getData(new TypeReference<NodeDeleteEntity>() {});
    }
}
