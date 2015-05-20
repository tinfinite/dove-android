package com.tinfinite.entity;

import android.os.Parcel;
import android.os.Parcelable;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by PanJiafang on 15/3/24.
 */
public class NodeEntity implements Parcelable{

    private static ObjectMapper objectMapper = new ObjectMapper();

    public static final int NODE_TYPE_FORWARD = 1;
    public static final int NODE_TYPE_POST = 2;

    private String id;
    private ForwardNodeEntity forward;
    private PostNodeEntity post;
    private AuthorEntity author;
    private int is_public;
    private int type;
    private int total_score;
    private int total_reply;
    private boolean is_upvote;
    private boolean is_downvote;
    private Date create_at;

    public NodeEntity jsonParse(String rootContent) {
        return (NodeEntity) ApiResponse2.jsonParse(NodeEntity.class, rootContent);
//        ApiResponse<NodeEntity> apiResponse = new ApiResponse<>(rootContent);
//        return apiResponse.getData(new TypeReference<NodeEntity>() {});
    }

    @Override
    public String toString() {
        return ApiResponse2.turn2String(this);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public ForwardNodeEntity getForward() {
        return forward;
    }

    public void setForward(ForwardNodeEntity forward) {
        this.forward = forward;
    }

    public PostNodeEntity getPost() {
        return post;
    }

    public void setPost(PostNodeEntity post) {
        this.post = post;
    }

    public AuthorEntity getAuthor() {
        return author;
    }

    public void setAuthor(AuthorEntity author) {
        this.author = author;
    }

    public int getIs_public() {
        return is_public;
    }

    public void setIs_public(int is_public) {
        this.is_public = is_public;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getTotal_score() {
        return total_score;
    }

    public void setTotal_score(int total_score) {
        this.total_score = total_score;
    }

    public int getTotal_reply() {
        return total_reply;
    }

    public void setTotal_reply(int total_reply) {
        this.total_reply = total_reply;
    }

    public boolean isIs_upvote() {
        return is_upvote;
    }

    public void setIs_upvote(boolean is_upvote) {
        this.is_upvote = is_upvote;
    }

    public boolean isIs_downvote() {
        return is_downvote;
    }

    public void setIs_downvote(boolean is_downvote) {
        this.is_downvote = is_downvote;
    }

    public Date getCreate_at() {
        return create_at;
    }

    public void setCreate_at(Date create_at) {
        this.create_at = create_at;
    }

    public static class ForwardNodeEntity implements Parcelable{
        private ArrayList<ForwardContentEntity> content;
        private String comment;
        private String third_group_id;
        private String third_group_name;
        private String third_group_image;
        private String third_group_image_key;

        public ArrayList<ForwardContentEntity> getContent() {
            return content;
        }

        public void setContent(ArrayList<ForwardContentEntity> content) {
            this.content = content;
        }

        public String getComment() {
            return comment;
        }

        public void setComment(String comment) {
            this.comment = comment;
        }

        public String getThird_group_id() {
            return third_group_id;
        }

        public void setThird_group_id(String third_group_id) {
            this.third_group_id = third_group_id;
        }

        public String getThird_group_name() {
            return third_group_name;
        }

        public void setThird_group_name(String third_group_name) {
            this.third_group_name = third_group_name;
        }

        public String getThird_group_image() {
            return third_group_image;
        }

        public void setThird_group_image(String third_group_image) {
            this.third_group_image = third_group_image;
        }

        public String getThird_group_image_key() {
            return third_group_image_key;
        }

        public void setThird_group_image_key(String third_group_image_key) {
            this.third_group_image_key = third_group_image_key;
        }

        @Override
        public String toString() {
            try {
                return objectMapper.writeValueAsString(this);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
            return null;
        }


        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeSerializable(this.content);
            dest.writeString(this.comment);
            dest.writeString(this.third_group_id);
            dest.writeString(this.third_group_name);
            dest.writeString(this.third_group_image);
            dest.writeString(this.third_group_image_key);
        }

        public ForwardNodeEntity() {
        }

        private ForwardNodeEntity(Parcel in) {
            this.content = (ArrayList<ForwardContentEntity>) in.readSerializable();
            this.comment = in.readString();
            this.third_group_id = in.readString();
            this.third_group_name = in.readString();
            this.third_group_image = in.readString();
            this.third_group_image_key = in.readString();
        }

        public static final Creator<ForwardNodeEntity> CREATOR = new Creator<ForwardNodeEntity>() {
            public ForwardNodeEntity createFromParcel(Parcel source) {
                return new ForwardNodeEntity(source);
            }

            public ForwardNodeEntity[] newArray(int size) {
                return new ForwardNodeEntity[size];
            }
        };
    }

    public static class ForwardContentEntity implements Parcelable{
        public static final int MESSAGE_TPYE_TEXT = 1;
        public static final int MESSAGE_TPYE_IMAGE = 2;

        private ForwardUserEntity user;
        private String messagecontent;
        private int messagetype;
        private int messagepoint;
        private long messagetime;

        public ForwardUserEntity getUser() {
            return user;
        }

        public void setUser(ForwardUserEntity user) {
            this.user = user;
        }

        public String getMessagecontent() {
            return messagecontent;
        }

        public void setMessagecontent(String messagecontent) {
            this.messagecontent = messagecontent;
        }

        public int getMessagetype() {
            return messagetype;
        }

        public void setMessagetype(int messagetype) {
            this.messagetype = messagetype;
        }

        public int getMessagepoint() {
            return messagepoint;
        }

        public void setMessagepoint(int messagepoint) {
            this.messagepoint = messagepoint;
        }

        public long getMessagetime() {
            return messagetime;
        }

        public void setMessagetime(long messagetime) {
            this.messagetime = messagetime;
        }

        @Override
        public String toString() {
            try {
                return objectMapper.writeValueAsString(this);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
            return null;
        }


        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeParcelable(this.user, 0);
            dest.writeString(this.messagecontent);
            dest.writeInt(this.messagetype);
            dest.writeInt(this.messagepoint);
            dest.writeLong(this.messagetime);
        }

        public ForwardContentEntity() {
        }

        private ForwardContentEntity(Parcel in) {
            this.user = in.readParcelable(ForwardUserEntity.class.getClassLoader());
            this.messagecontent = in.readString();
            this.messagetype = in.readInt();
            this.messagepoint = in.readInt();
            this.messagetime = in.readLong();
        }

        public static final Creator<ForwardContentEntity> CREATOR = new Creator<ForwardContentEntity>() {
            public ForwardContentEntity createFromParcel(Parcel source) {
                return new ForwardContentEntity(source);
            }

            public ForwardContentEntity[] newArray(int size) {
                return new ForwardContentEntity[size];
            }
        };
    }

    public static class ForwardUserEntity implements Parcelable{
        private String user_id = "";
        private String first_name = "";
        private String last_name = "";
        private String username = "";
        private boolean anonymous;

        public String getUser_id() {
            return user_id;
        }

        public void setUser_id(String user_id) {
            this.user_id = user_id;
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

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public boolean isAnonymous() {
            return anonymous;
        }

        public void setAnonymous(boolean anonymous) {
            this.anonymous = anonymous;
        }


        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(this.user_id);
            dest.writeString(this.first_name);
            dest.writeString(this.last_name);
            dest.writeString(this.username);
            dest.writeByte(anonymous ? (byte) 1 : (byte) 0);
        }

        public ForwardUserEntity() {
        }

        private ForwardUserEntity(Parcel in) {
            this.user_id = in.readString();
            this.first_name = in.readString();
            this.last_name = in.readString();
            this.username = in.readString();
            this.anonymous = in.readByte() != 0;
        }

        public static final Creator<ForwardUserEntity> CREATOR = new Creator<ForwardUserEntity>() {
            public ForwardUserEntity createFromParcel(Parcel source) {
                return new ForwardUserEntity(source);
            }

            public ForwardUserEntity[] newArray(int size) {
                return new ForwardUserEntity[size];
            }
        };
    }

    public static class PostNodeEntity implements Parcelable{
        private String text = "";
        private String image = "";
        private String url = "";
        private String url_title = "";
        private String url_image = "";
        private String url_description = "";
        private String third_group_id;
        private String third_group_name;
        private String third_group_image;
        private String third_group_image_key;

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public String getImage() {
            return image;
        }

        public void setImage(String image) {
            this.image = image;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getUrl_title() {
            return url_title;
        }

        public void setUrl_title(String url_title) {
            this.url_title = url_title;
        }

        public String getUrl_image() {
            return url_image;
        }

        public void setUrl_image(String url_image) {
            this.url_image = url_image;
        }

        public String getUrl_description() {
            return url_description;
        }

        public void setUrl_description(String url_description) {
            this.url_description = url_description;
        }

        public String getThird_group_id() {
            return third_group_id;
        }

        public void setThird_group_id(String third_group_id) {
            this.third_group_id = third_group_id;
        }

        public String getThird_group_name() {
            return third_group_name;
        }

        public void setThird_group_name(String third_group_name) {
            this.third_group_name = third_group_name;
        }

        public String getThird_group_image() {
            return third_group_image;
        }

        public void setThird_group_image(String third_group_image) {
            this.third_group_image = third_group_image;
        }

        public String getThird_group_image_key() {
            return third_group_image_key;
        }

        public void setThird_group_image_key(String third_group_image_key) {
            this.third_group_image_key = third_group_image_key;
        }

        @Override
        public String toString() {
            try {
                return objectMapper.writeValueAsString(this);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
            return null;
        }

        public PostNodeEntity() {
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(this.text);
            dest.writeString(this.image);
            dest.writeString(this.url);
            dest.writeString(this.url_title);
            dest.writeString(this.url_image);
            dest.writeString(this.url_description);
            dest.writeString(this.third_group_id);
            dest.writeString(this.third_group_name);
            dest.writeString(this.third_group_image);
            dest.writeString(this.third_group_image_key);
        }

        private PostNodeEntity(Parcel in) {
            this.text = in.readString();
            this.image = in.readString();
            this.url = in.readString();
            this.url_title = in.readString();
            this.url_image = in.readString();
            this.url_description = in.readString();
            this.third_group_id = in.readString();
            this.third_group_name = in.readString();
            this.third_group_image = in.readString();
            this.third_group_image_key = in.readString();
        }

        public static final Creator<PostNodeEntity> CREATOR = new Creator<PostNodeEntity>() {
            public PostNodeEntity createFromParcel(Parcel source) {
                return new PostNodeEntity(source);
            }

            public PostNodeEntity[] newArray(int size) {
                return new PostNodeEntity[size];
            }
        };
    }

    public static class AuthorEntity implements Parcelable{
        private String id;
        private String locale;
        private String last_name;
        private String first_name;
        private String username;
        private String tg_user_id;
        private String avatar;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getLocale() {
            return locale;
        }

        public void setLocale(String locale) {
            this.locale = locale;
        }

        public String getLast_name() {
            return last_name;
        }

        public void setLast_name(String last_name) {
            this.last_name = last_name;
        }

        public String getFirst_name() {
            return first_name;
        }

        public void setFirst_name(String first_name) {
            this.first_name = first_name;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getTg_user_id() {
            return tg_user_id;
        }

        public void setTg_user_id(String tg_user_id) {
            this.tg_user_id = tg_user_id;
        }

        public String getAvatar() {
            return avatar;
        }

        public void setAvatar(String avatar) {
            this.avatar = avatar;
        }


        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(this.id);
            dest.writeString(this.locale);
            dest.writeString(this.last_name);
            dest.writeString(this.first_name);
            dest.writeString(this.username);
            dest.writeString(this.tg_user_id);
            dest.writeString(this.avatar);
        }

        public AuthorEntity() {
        }

        private AuthorEntity(Parcel in) {
            this.id = in.readString();
            this.locale = in.readString();
            this.last_name = in.readString();
            this.first_name = in.readString();
            this.username = in.readString();
            this.tg_user_id = in.readString();
            this.avatar = in.readString();
        }

        public static final Creator<AuthorEntity> CREATOR = new Creator<AuthorEntity>() {
            public AuthorEntity createFromParcel(Parcel source) {
                return new AuthorEntity(source);
            }

            public AuthorEntity[] newArray(int size) {
                return new AuthorEntity[size];
            }
        };
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.id);
        dest.writeParcelable(this.forward, 0);
        dest.writeParcelable(this.post, 0);
        dest.writeParcelable(this.author, 0);
        dest.writeInt(this.is_public);
        dest.writeInt(this.type);
        dest.writeInt(this.total_score);
        dest.writeInt(this.total_reply);
        dest.writeByte(is_upvote ? (byte) 1 : (byte) 0);
        dest.writeByte(is_downvote ? (byte) 1 : (byte) 0);
        dest.writeLong(create_at != null ? create_at.getTime() : -1);
    }

    public NodeEntity() {
    }

    private NodeEntity(Parcel in) {
        this.id = in.readString();
        this.forward = in.readParcelable(ForwardNodeEntity.class.getClassLoader());
        this.post = in.readParcelable(PostNodeEntity.class.getClassLoader());
        this.author = in.readParcelable(AuthorEntity.class.getClassLoader());
        this.is_public = in.readInt();
        this.type = in.readInt();
        this.total_score = in.readInt();
        this.total_reply = in.readInt();
        this.is_upvote = in.readByte() != 0;
        this.is_downvote = in.readByte() != 0;
        long tmpCreate_at = in.readLong();
        this.create_at = tmpCreate_at == -1 ? null : new Date(tmpCreate_at);
    }

    public static final Creator<NodeEntity> CREATOR = new Creator<NodeEntity>() {
        public NodeEntity createFromParcel(Parcel source) {
            return new NodeEntity(source);
        }

        public NodeEntity[] newArray(int size) {
            return new NodeEntity[size];
        }
    };
}
