package com.tinfinite.entity;

import com.fasterxml.jackson.core.type.TypeReference;

import java.util.List;

public class MyVoteMessageEntity extends ApiBasicEntity {
    private boolean result;
    private List<VoteData> data;

    public List<VoteData> getData() {
        return data;
    }

    public void setData(List<VoteData> data) {
        this.data = data;
    }

    @Override
    public MyVoteMessageEntity jsonParse(String rootContent) {
        ApiResponse<MyVoteMessageEntity> apiResponse = new ApiResponse<>(rootContent);
        return apiResponse.getData(new TypeReference<MyVoteMessageEntity>() {});
    }

    public boolean isResult() {
        return result;
    }

    public void setResult(boolean result) {
        this.result = result;
    }

    public static class VoteData {
        String tg_dialog_id;
        String tg_message_id;
        int points;

        public String getTg_dialog_id() {
            return tg_dialog_id;
        }

        public void setTg_dialog_id(String tg_dialog_id) {
            this.tg_dialog_id = tg_dialog_id;
        }

        public String getTg_message_id() {
            return tg_message_id;
        }

        public void setTg_message_id(String tg_message_id) {
            this.tg_message_id = tg_message_id;
        }

        public int getPoints() {
            return points;
        }

        public void setPoints(int points) {
            this.points = points;
        }
    }
}
