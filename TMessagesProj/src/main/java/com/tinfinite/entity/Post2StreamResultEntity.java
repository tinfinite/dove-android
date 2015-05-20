package com.tinfinite.entity;

import com.fasterxml.jackson.core.type.TypeReference;

/**
 * Created by PanJiafang on 15/3/25.
 */
public class Post2StreamResultEntity extends ApiBasicEntity {
    private boolean result;
    private String post_id;

    public boolean isResult() {
        return result;
    }

    public void setResult(boolean result) {
        this.result = result;
    }

    public String getPost_id() {
        return post_id;
    }

    public void setPost_id(String post_id) {
        this.post_id = post_id;
    }

    @Override
    public Post2StreamResultEntity jsonParse(String rootContent) {
        ApiResponse<Post2StreamResultEntity> apiResponse = new ApiResponse<>(rootContent);
        return apiResponse.getData(new TypeReference<Post2StreamResultEntity>() {});
    }
}
