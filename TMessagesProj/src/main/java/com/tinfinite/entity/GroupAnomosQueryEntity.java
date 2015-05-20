package com.tinfinite.entity;

import com.fasterxml.jackson.core.type.TypeReference;

/**
 * Created by PanJiafang on 15/4/3.
 */
public class GroupAnomosQueryEntity extends ApiBasicEntity {

    private boolean result;
    private int status;

    public boolean isResult() {
        return result;
    }

    public void setResult(boolean result) {
        this.result = result;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    @Override
    public GroupAnomosQueryEntity jsonParse(String rootContent) {
        ApiResponse<GroupAnomosQueryEntity> apiResponse = new ApiResponse<>(rootContent);
        return apiResponse.getData(new TypeReference<GroupAnomosQueryEntity>() {});
    }
}
