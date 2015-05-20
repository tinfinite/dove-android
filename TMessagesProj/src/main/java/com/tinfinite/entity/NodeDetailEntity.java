package com.tinfinite.entity;

import com.fasterxml.jackson.core.type.TypeReference;

/**
 * Created by PanJiafang on 15/4/22.
 */
public class NodeDetailEntity extends ApiBasicEntity {

    private boolean result;
    private NodeEntity data;

    public boolean isResult() {
        return result;
    }

    public void setResult(boolean result) {
        this.result = result;
    }

    public NodeEntity getData() {
        return data;
    }

    public void setData(NodeEntity data) {
        this.data = data;
    }

    @Override
    public NodeDetailEntity jsonParse(String rootContent) {
        ApiResponse<NodeDetailEntity> apiResponse = new ApiResponse<>(rootContent);
        return apiResponse.getData(new TypeReference<NodeDetailEntity>() {});
    }
}
