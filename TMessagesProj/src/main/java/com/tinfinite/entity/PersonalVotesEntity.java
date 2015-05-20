package com.tinfinite.entity;

import com.fasterxml.jackson.core.type.TypeReference;

import java.util.ArrayList;
import java.util.Date;

import static com.tinfinite.entity.PersonalCommentsEntity.PostEntity;
import static com.tinfinite.entity.PersonalCommentsEntity.AuthorEntity;

/**
 * Created by PanJiafang on 15/4/21.
 */
public class PersonalVotesEntity extends ApiBasicEntity {

    private boolean result;
    private ArrayList<PersonalVoteEntity> data;
    private int page;
    private int limit;
    private long timestamp;

    @Override
    public PersonalVotesEntity jsonParse(String rootContent) {
        ApiResponse<PersonalVotesEntity> apiResponse = new ApiResponse<>(rootContent);
        return apiResponse.getData(new TypeReference<PersonalVotesEntity>() {});
    }

    public boolean isResult() {
        return result;
    }

    public void setResult(boolean result) {
        this.result = result;
    }

    public ArrayList<PersonalVoteEntity> getData() {
        return data;
    }

    public void setData(ArrayList<PersonalVoteEntity> data) {
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

    public static class PersonalVoteEntity {
        private PostEntity post;
        private AuthorEntity user;
        private String action;
        private Date create_at;

        public Date getCreate_at() {
            return create_at;
        }

        public void setCreate_at(Date create_at) {
            this.create_at = create_at;
        }

        public String getAction() {
            return action;
        }

        public void setAction(String action) {
            this.action = action;
        }

        public PostEntity getPost() {
            return post;
        }

        public void setPost(PostEntity post) {
            this.post = post;
        }

        public AuthorEntity getUser() {
            return user;
        }

        public void setUser(AuthorEntity user) {
            this.user = user;
        }
    }

}
