package com.tinfinite.entity;

import com.fasterxml.jackson.core.type.TypeReference;

import java.util.ArrayList;

/**
 * Created by PanJiafang on 15/4/23.
 */
public class PersonalPostsEntity extends ApiBasicEntity{

    private Boolean result;
    private ArrayList<NodeEntity> data;
    private int page;
    private int limit;
    private long timestamp;

    public Boolean getResult() {
        return result;
    }

    public void setResult(Boolean result) {
        this.result = result;
    }

    public ArrayList<NodeEntity> getData() {
        return data;
    }

    public void setData(ArrayList<NodeEntity> data) {
        this.data = data;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public PersonalPostsEntity jsonParse(String rootContent) {
        ApiResponse<PersonalPostsEntity> apiResponse = new ApiResponse<>(rootContent);
        return apiResponse.getData(new TypeReference<PersonalPostsEntity>() {});
    }
}
