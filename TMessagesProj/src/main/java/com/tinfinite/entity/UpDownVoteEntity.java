package com.tinfinite.entity;

import com.fasterxml.jackson.core.type.TypeReference;

/**
 * Created by PanJiafang on 15/3/20.
 */
public class UpDownVoteEntity extends ApiBasicEntity {
    public static final String VOTE_UP = "up";
    public static final String VOTE_UP_CANCEL = "cancel up";
    public static final String VOTE_DOWN = "down";
    public static final String VOTE_DOWN_CANCEL = "cancel down";
    private boolean result;
    private String action;

    @Override
    public UpDownVoteEntity jsonParse(String rootContent) {
        ApiResponse<UpDownVoteEntity> apiResponse = new ApiResponse<>(rootContent);
        return apiResponse.getData(new TypeReference<UpDownVoteEntity>() {});
    }

    public boolean isResult() {
        return result;
    }

    public void setResult(boolean result) {
        this.result = result;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }
}
