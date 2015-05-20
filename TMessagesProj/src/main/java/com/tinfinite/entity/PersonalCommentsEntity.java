package com.tinfinite.entity;

import com.fasterxml.jackson.core.type.TypeReference;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by PanJiafang on 15/4/21.
 */
public class PersonalCommentsEntity extends ApiBasicEntity {

    private boolean result;
    private ArrayList<PersonalCommentEntity> data;
    private int page;
    private int limit;
    private long timestamp;

    @Override
    public PersonalCommentsEntity jsonParse(String rootContent) {
        ApiResponse<PersonalCommentsEntity> apiResponse = new ApiResponse<>(rootContent);
        return apiResponse.getData(new TypeReference<PersonalCommentsEntity>() {});
    }

    public boolean isResult() {
        return result;
    }

    public void setResult(boolean result) {
        this.result = result;
    }

    public ArrayList<PersonalCommentEntity> getData() {
        return data;
    }

    public void setData(ArrayList<PersonalCommentEntity> data) {
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

    public static class PersonalCommentEntity{
        private PostEntity post;
        private CommentEntity comment;
        private AuthorEntity user;

        public PostEntity getPost() {
            return post;
        }

        public void setPost(PostEntity post) {
            this.post = post;
        }

        public CommentEntity getComment() {
            return comment;
        }

        public void setComment(CommentEntity comment) {
            this.comment = comment;
        }

        public AuthorEntity getUser() {
            return user;
        }

        public void setUser(AuthorEntity user) {
            this.user = user;
        }
    }

    public static class PostEntity{
        private String id;
        private NodeEntity.PostNodeEntity content;
        private Date create_at;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public NodeEntity.PostNodeEntity getContent() {
            return content;
        }

        public void setContent(NodeEntity.PostNodeEntity content) {
            this.content = content;
        }

        public Date getCreate_at() {
            return create_at;
        }

        public void setCreate_at(Date create_at) {
            this.create_at = create_at;
        }
    }

    public static class CommentEntity{
        private String id;
        private String content;
        private Date create_at;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
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

    public static class AuthorEntity{
        private String id;
        private String username;
        private String avatar;
        private String first_name;
        private String last_name;
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

        public String getAvatar() {
            return avatar;
        }

        public void setAvatar(String avatar) {
            this.avatar = avatar;
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

        public String getTg_user_id() {
            return tg_user_id;
        }

        public void setTg_user_id(String tg_user_id) {
            this.tg_user_id = tg_user_id;
        }
    }
}
