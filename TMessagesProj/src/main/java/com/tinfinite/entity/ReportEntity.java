package com.tinfinite.entity;

import com.fasterxml.jackson.core.type.TypeReference;
import com.tinfinite.android.sdk.api.ApiBasicEntity;

/**
 * Created by PanJiafang on 15/3/15.
 */
public class ReportEntity extends ApiBasicEntity {
    private boolean result;

    @Override
    public ReportEntity jsonParse(String rootContent) {
        ApiResponse<ReportEntity> apiResponse = new ApiResponse<>(rootContent);
        return apiResponse.getData(new TypeReference<ReportEntity>() {});
    }

    public boolean isResult() {
        return result;
    }

    public void setResult(boolean result) {
        this.result = result;
    }
}
