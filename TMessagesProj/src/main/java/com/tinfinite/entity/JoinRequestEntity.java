package com.tinfinite.entity;

import android.os.Parcel;
import android.os.Parcelable;

import com.fasterxml.jackson.core.type.TypeReference;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by PanJiafang on 15/5/5.
 */
public class JoinRequestEntity extends ApiBasicEntity {

    private boolean result;
    private ArrayList<RequestEntity> data;
    private String page;
    private String limit;
    private long timestamp;

    public boolean isResult() {
        return result;
    }

    public void setResult(boolean result) {
        this.result = result;
    }

    public ArrayList<RequestEntity> getData() {
        return data;
    }

    public void setData(ArrayList<RequestEntity> data) {
        this.data = data;
    }

    public String getPage() {
        return page;
    }

    public void setPage(String page) {
        this.page = page;
    }

    public String getLimit() {
        return limit;
    }

    public void setLimit(String limit) {
        this.limit = limit;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public JoinRequestEntity jsonParse(String rootContent) {
        ApiResponse<JoinRequestEntity> apiResponse = new ApiResponse<>(rootContent);
        return apiResponse.getData(new TypeReference<JoinRequestEntity>() {});
    }

    public static class RequestEntity implements Parcelable{
        private String id;
        private String telegram_group_id;
        private String telegram_user_id;
        private String telegram_username;
        private String telegram_user_avatar;
        private Date create_at;
        private String message;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getTelegram_group_id() {
            return telegram_group_id;
        }

        public void setTelegram_group_id(String telegram_group_id) {
            this.telegram_group_id = telegram_group_id;
        }

        public String getTelegram_user_id() {
            return telegram_user_id;
        }

        public void setTelegram_user_id(String telegram_user_id) {
            this.telegram_user_id = telegram_user_id;
        }

        public String getTelegram_username() {
            return telegram_username;
        }

        public void setTelegram_username(String telegram_username) {
            this.telegram_username = telegram_username;
        }

        public String getTelegram_user_avatar() {
            return telegram_user_avatar;
        }

        public void setTelegram_user_avatar(String telegram_user_avatar) {
            this.telegram_user_avatar = telegram_user_avatar;
        }

        public Date getCreate_at() {
            return create_at;
        }

        public void setCreate_at(Date create_at) {
            this.create_at = create_at;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }


        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(this.id);
            dest.writeString(this.telegram_group_id);
            dest.writeString(this.telegram_user_id);
            dest.writeString(this.telegram_username);
            dest.writeString(this.telegram_user_avatar);
            dest.writeLong(create_at != null ? create_at.getTime() : -1);
            dest.writeString(this.message);
        }

        public RequestEntity() {
        }

        private RequestEntity(Parcel in) {
            this.id = in.readString();
            this.telegram_group_id = in.readString();
            this.telegram_user_id = in.readString();
            this.telegram_username = in.readString();
            this.telegram_user_avatar = in.readString();
            long tmpCreate_at = in.readLong();
            this.create_at = tmpCreate_at == -1 ? null : new Date(tmpCreate_at);
            this.message = in.readString();
        }

        public static final Creator<RequestEntity> CREATOR = new Creator<RequestEntity>() {
            public RequestEntity createFromParcel(Parcel source) {
                return new RequestEntity(source);
            }

            public RequestEntity[] newArray(int size) {
                return new RequestEntity[size];
            }
        };
    }
}
