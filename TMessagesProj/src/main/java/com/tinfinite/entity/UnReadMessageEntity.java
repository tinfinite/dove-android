package com.tinfinite.entity;

import com.fasterxml.jackson.core.type.TypeReference;

/**
 * Created by PanJiafang on 15/4/24.
 */
public class UnReadMessageEntity extends ApiBasicEntity {

    private boolean result;
    private MessageEntity data;

    public boolean isResult() {
        return result;
    }

    public void setResult(boolean result) {
        this.result = result;
    }

    public MessageEntity getData() {
        return data;
    }

    public void setData(MessageEntity data) {
        this.data = data;
    }

    @Override
    public UnReadMessageEntity jsonParse(String rootContent) {
        ApiResponse<UnReadMessageEntity> apiResponse = new ApiResponse<>(rootContent);
        return apiResponse.getData(new TypeReference<UnReadMessageEntity>() {});
    }

    public static class MessageEntity{
        private int new_comments_count;
        private int new_upvotes_count;

        public int getNew_comments_count() {
            return new_comments_count;
        }

        public void setNew_comments_count(int new_comments_count) {
            this.new_comments_count = new_comments_count;
        }

        public int getNew_upvotes_count() {
            return new_upvotes_count;
        }

        public void setNew_upvotes_count(int new_upvotes_count) {
            this.new_upvotes_count = new_upvotes_count;
        }
    }
}
