package com.tinfinite.entity;

import java.util.ArrayList;

/**
 * Created by PanJiafang on 15/2/15.
 */
public class GroupPostUnreadNumEntity extends ApiBasicEntity{
    private boolean result;
    private ArrayList<UnreadPostNum> data;

    public GroupPostUnreadNumEntity jsonParse(String rootContent) {
        return (GroupPostUnreadNumEntity) ApiResponse2.jsonParse(GroupPostUnreadNumEntity.class, rootContent);
    }

    public boolean isResult() {
        return result;
    }

    public void setResult(boolean result) {
        this.result = result;
    }

    public ArrayList<UnreadPostNum> getData() {
        return data;
    }

    public void setData(ArrayList<UnreadPostNum> data) {
        this.data = data;
    }

    public static class UnreadPostNum {
        private String tg_group_id;
        private long count;

        public String getTg_group_id() {
            return tg_group_id;
        }

        public void setTg_group_id(String tg_group_id) {
            this.tg_group_id = tg_group_id;
        }

        public long getCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
        }
    }
}
