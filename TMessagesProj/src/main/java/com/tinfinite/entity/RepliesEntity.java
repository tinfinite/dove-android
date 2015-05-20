package com.tinfinite.entity;

import com.fasterxml.jackson.core.type.TypeReference;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by PanJiafang on 15/3/31.
 */
public class RepliesEntity extends ApiBasicEntity {
    private boolean result;
    private ArrayList<ReplyEntity> data;
    private int page;
    private int limit;
    private long timestamp;

    @Override
    public RepliesEntity jsonParse(String rootContent) {
        ApiResponse<RepliesEntity> apiResponse = new ApiResponse<>(rootContent);
        return apiResponse.getData(new TypeReference<RepliesEntity>() {});
    }

    public boolean isResult() {
        return result;
    }

    public void setResult(boolean result) {
        this.result = result;
    }

    public ArrayList<ReplyEntity> getData() {
        return data;
    }

    public void setData(ArrayList<ReplyEntity> data) {
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


    public static class ReplyEntity {
        private String id;
        private ReplyAuthorEntity author;
        private String content;
        private Date create_at;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public ReplyAuthorEntity getAuthor() {
            return author;
        }

        public void setAuthor(ReplyAuthorEntity author) {
            this.author = author;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public Date getCreate_at() {
            return create_at;
        }

        public void setCreate_at(Date create_at) {
            this.create_at = create_at;
        }
    }

    public static class ReplyAuthorEntity{
        private String id;
        private String username;
        private String first_name;
        private String last_name;
        private String avatar;
        private String tg_user_id;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getFirst_name() {
            return first_name;
        }

        public void setFirst_name(String first_name) {
            this.first_name = first_name;
        }

        public String getLast_name() {
            return last_name;
        }

        public void setLast_name(String last_name) {
            this.last_name = last_name;
        }

        public String getAvatar() {
            return avatar;
        }

        public void setAvatar(String avatar) {
            this.avatar = avatar;
        }

        public String getTg_user_id() {
            return tg_user_id;
        }

        public void setTg_user_id(String tg_user_id) {
            this.tg_user_id = tg_user_id;
        }
    }
}
