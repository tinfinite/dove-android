package com.tinfinite.entity;

import com.fasterxml.jackson.core.type.TypeReference;

import java.util.List;

public class GroupVoteMessageEntity extends ApiBasicEntity {
    private boolean result;
    private List<VoteData> data;

    public List<VoteData> getData() {
        return data;
    }

    public void setData(List<VoteData> data) {
        this.data = data;
    }

    @Override
    public GroupVoteMessageEntity jsonParse(String rootContent) {
        ApiResponse<GroupVoteMessageEntity> apiResponse = new ApiResponse<>(rootContent);
        return apiResponse.getData(new TypeReference<GroupVoteMessageEntity>() {});
    }

    public boolean isResult() {
        return result;
    }

    public void setResult(boolean result) {
        this.result = result;
    }

    public static class VoteData {
        String tg_dialog_id;
        String tg_message_key;
        String tg_message_text;
        int points;

        public String getTg_dialog_id() {
            return tg_dialog_id;
        }

        public void setTg_dialog_id(String tg_dialog_id) {
            this.tg_dialog_id = tg_dialog_id;
        }

        public String getTg_message_key() {
            return tg_message_key;
        }

        public void setTg_message_key(String tg_message_key) {
            this.tg_message_key = tg_message_key;
        }

        public String getTg_message_text() {
            return tg_message_text;
        }

        public void setTg_message_text(String tg_message_text) {
            this.tg_message_text = tg_message_text;
        }

        public int getPoints() {
            return points;
        }

        public void setPoints(int points) {
            this.points = points;
        }
    }
}
