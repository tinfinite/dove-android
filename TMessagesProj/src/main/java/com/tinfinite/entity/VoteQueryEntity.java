package com.tinfinite.entity;

import com.fasterxml.jackson.core.type.TypeReference;

import java.io.Serializable;
import java.util.List;

/**
 * Created by PanJiafang on 15/3/21.
 */
public class VoteQueryEntity extends ApiBasicEntity {
    private boolean result;
    private List<VoteEntity> data;

    @Override
    public VoteQueryEntity jsonParse(String rootContent) {
        ApiResponse<VoteQueryEntity> apiResponse = new ApiResponse<>(rootContent);
        return apiResponse.getData(new TypeReference<VoteQueryEntity>() {});
    }

    public boolean isResult() {
        return result;
    }

    public void setResult(boolean result) {
        this.result = result;
    }

    public List<VoteEntity> getData() {
        return data;
    }

    public void setData(List<VoteEntity> data) {
        this.data = data;
    }

    public static class VoteEntity implements Serializable{
        private String tg_dialog_id;
        private String tg_message_key;
        private int upvotes;
        private int downvotes;
        private boolean upvoted;
        private boolean downvoted;

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

        public int getUpvotes() {
            return upvotes;
        }

        public void setUpvotes(int upvotes) {
            this.upvotes = upvotes;
        }

        public int getDownvotes() {
            return downvotes;
        }

        public void setDownvotes(int downvotes) {
            this.downvotes = downvotes;
        }

        public boolean isUpvoted() {
            return upvoted;
        }

        public void setUpvoted(boolean upvoted) {
            this.upvoted = upvoted;
        }

        public boolean isDownvoted() {
            return downvoted;
        }

        public void setDownvoted(boolean downvoted) {
            this.downvoted = downvoted;
        }
    }
}
