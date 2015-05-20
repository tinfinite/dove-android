package com.tinfinite.entity;

import com.fasterxml.jackson.core.type.TypeReference;

import java.util.ArrayList;

/**
 * Created by PanJiafang on 15/3/27.
 */
public class StreamResultEntity extends ApiBasicEntity {

    private boolean result;
    private ArrayList<NodeEntity> data;
    private ArrayList<String> ids;

    public boolean isResult() {
        return result;
    }

    public void setResult(boolean result) {
        this.result = result;
    }

    public ArrayList<NodeEntity> getData() {
        return data;
    }

    public void setData(ArrayList<NodeEntity> data) {
        this.data = data;
    }

    public ArrayList<String> getIds() {
        return ids;
    }

    public void setIds(ArrayList<String> ids) {
        this.ids = ids;
    }

    @Override
    public StreamResultEntity jsonParse(String rootContent) {
        ApiResponse<StreamResultEntity> apiResponse = new ApiResponse<>(rootContent);
        return apiResponse.getData(new TypeReference<StreamResultEntity>() {});
    }
}
