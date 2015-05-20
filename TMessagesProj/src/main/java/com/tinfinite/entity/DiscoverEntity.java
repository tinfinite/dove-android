package com.tinfinite.entity;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by PanJiafang on 15/2/15.
 */
public class DiscoverEntity extends ApiBasicEntity{
    private boolean result;
    private Groups data;
    private int page;
    private int limit;
    private long timestamp;

    @Override
    public DiscoverEntity jsonParse(String rootContent) {
        return (DiscoverEntity) ApiResponse2.jsonParse(DiscoverEntity.class, rootContent);
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isResult() {
        return result;
    }

    public void setResult(boolean result) {
        this.result = result;
    }

    public Groups getData() {
        return data;
    }

    public void setData(Groups data) {
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

    public static class Groups {
        private ArrayList<Community> recommend;
        private ArrayList<Community> list;

        public ArrayList<Community> getRecommend() {
            return recommend;
        }

        public void setRecommend(ArrayList<Community> recommend) {
            this.recommend = recommend;
        }

        public ArrayList<Community> getList() {
            return list;
        }

        public void setList(ArrayList<Community> list) {
            this.list = list;
        }
    }
    public static class Community implements Parcelable {
        private String id;
        private String third_group_id;
        private String privilege;
        private Date create_at;
        private String name;
        private String description;
        private String image;
        private String member_count;
        private String language;
        private String third_group_image_key;
        private int points;

        public Date getCreate_at() {
            return create_at;
        }

        public void setCreate_at(Date create_at) {
            this.create_at = create_at;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getThird_group_id() {
            return third_group_id;
        }

        public void setThird_group_id(String third_group_id) {
            this.third_group_id = third_group_id;
        }

        public String getPrivilege() {
            return privilege;
        }

        public void setPrivilege(String privilege) {
            this.privilege = privilege;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getImage() {
            return image;
        }

        public void setImage(String image) {
            this.image = image;
        }

        public String getMember_count() {
            return member_count;
        }

        public void setMember_count(String member_count) {
            this.member_count = member_count;
        }

        public String getLanguage() {
            return language;
        }

        public void setLanguage(String language) {
            this.language = language;
        }

        public String getThird_group_image_key() {
            return third_group_image_key;
        }

        public void setThird_group_image_key(String third_group_image_key) {
            this.third_group_image_key = third_group_image_key;
        }

        public int getPoints() {
            return points;
        }

        public void setPoints(int points) {
            this.points = points;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(this.id);
            dest.writeString(this.third_group_id);
            dest.writeString(this.privilege);
            dest.writeLong(create_at != null ? create_at.getTime() : -1);
            dest.writeString(this.name);
            dest.writeString(this.description);
            dest.writeString(this.image);
            dest.writeString(this.member_count);
            dest.writeString(this.language);
            dest.writeString(this.third_group_image_key);
            dest.writeInt(this.points);
        }

        public Community() {
        }

        private Community(Parcel in) {
            this.id = in.readString();
            this.third_group_id = in.readString();
            this.privilege = in.readString();
            long tmpCreate_at = in.readLong();
            this.create_at = tmpCreate_at == -1 ? null : new Date(tmpCreate_at);
            this.name = in.readString();
            this.description = in.readString();
            this.image = in.readString();
            this.member_count = in.readString();
            this.language = in.readString();
            this.third_group_image_key = in.readString();
            this.points = in.readInt();
        }

        public static final Parcelable.Creator<Community> CREATOR = new Parcelable.Creator<Community>() {
            public Community createFromParcel(Parcel source) {
                return new Community(source);
            }

            public Community[] newArray(int size) {
                return new Community[size];
            }
        };
    }
}
