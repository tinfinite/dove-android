package com.tinfinite.utils;

import android.app.Activity;
import android.content.SharedPreferences;

import org.telegram.android.LocaleController;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.R;

/**
 * Created by PanJiafang on 15/4/3.
 */
public class PreferenceUtils {
    public static boolean getGlobleStatus(){
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("groupconfig", Activity.MODE_PRIVATE);
        return preferences.getBoolean("forwardAnonymous", true);
    }

    public static void setGlobleStatus(boolean value){
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("groupconfig", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("forwardAnonymous", value);
        editor.commit();
    }

    public static boolean getChatStatus(int chat_id){
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("groupconfig", Activity.MODE_PRIVATE);
        return  preferences.getBoolean("forwardAnonymous_"+Math.abs(chat_id), true);
    }

    public static void setChatStatus(int chat_id, boolean value){
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("groupconfig", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("forwardAnonymous_"+Math.abs(chat_id), value);
        editor.commit();
    }

    public static void setUpdateStatus(String value){
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("userconfig", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("update_version", value);
        editor.commit();
    }

    public static String getUpdateStatus(){
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("userconfig", Activity.MODE_PRIVATE);
        return  preferences.getString("update_version", "");
    }

    public static void setGroupType(int chat_id, boolean value){
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("groupconfig", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("showInDiscover_" + chat_id, value);
        editor.commit();
    }

    public static boolean getGroupType(int chat_id){
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("groupconfig", Activity.MODE_PRIVATE);
        return preferences.getBoolean("showInDiscover_"+chat_id, true);
    }

    public static void setGroupLanguage(int chat_id, String value){
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("groupconfig", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("group_language_" + chat_id, value);
        editor.commit();
    }

    public static String getGroupLanguage(int chat_id){
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("groupconfig", Activity.MODE_PRIVATE);
        return preferences.getString("group_language_"+chat_id, LocaleController.getCurrentLanguageName().equals("English") ? "en" : "zh");
    }

    public static void setGroupDesc(int chat_id, String value){
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("groupconfig", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("group_description_" + chat_id, value);
        editor.commit();
    }

    public static String getGroupDesc(int chat_id){
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("groupconfig", Activity.MODE_PRIVATE);
        String result = preferences.getString("group_description_"+chat_id, LocaleController.getString("", R.string.Empty));
        if(result.equals(""))
            result = LocaleController.getString("", R.string.Empty);
        return result;
    }

    public static void setUnReadComments(int value){
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("userconfig", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("unread_comments", value);
        editor.commit();
    }

    public static int getUnReadComments(){
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("userconfig", Activity.MODE_PRIVATE);
        return  preferences.getInt("unread_comments", 0);
    }

    public static void setUnReadVotes(int value){
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("userconfig", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("unread_votes", value);
        editor.commit();
    }

    public static int getUnReadVotes(){
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("userconfig", Activity.MODE_PRIVATE);
        return  preferences.getInt("unread_votes", 0);
    }

    public static void setNewCommentNotification(boolean value){
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("Notifications", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("EnableNewComments", value);
        editor.commit();
    }

    public static boolean getNewCommentNotification(){
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("Notifications", Activity.MODE_PRIVATE);
        return preferences.getBoolean("EnableNewComments", true);
    }

    public static void setNewVoteNotification(boolean value){
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("Notifications", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("EnableNewVotes", value);
        editor.commit();
    }

    public static boolean getNewVoteNotification(){
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("Notifications", Activity.MODE_PRIVATE);
        return preferences.getBoolean("EnableNewVotes", true);
    }

    public static void setNewJoinNotification(boolean value){
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("Notifications", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("EnableNewJoins", value);
        editor.commit();
    }

    public static boolean getNewJoinNotification(){
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("Notifications", Activity.MODE_PRIVATE);
        return preferences.getBoolean("EnableNewJoins", true);
    }
}
