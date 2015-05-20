package com.tinfinite.provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.tinfinite.provider.contract.AuthorModel;
import com.tinfinite.provider.contract.IdsModel;
import com.tinfinite.provider.contract.PostModel;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static DatabaseHelper singleton = null;
    private static final String DATABASE_NAME = "t8.db";
    private static final int DATABASE_VERSION = 2;

    static final String CREATE_IDS_TABLE_SQL = "create table "+ IdsModel.TABLE_NAME
            + "("
            + IdsModel.ID + " INTEGER primary key autoincrement,"
            + IdsModel.FILTER_ID + " INTEGER,"
            + IdsModel.POST_ID + " text,"
            + IdsModel.DISPLAY + " INTEGER NOT NULL DEFAULT 0"
            + ");";

    static final String CREATE_AUTHORS_TABLE_SQL = "create table "+ AuthorModel.TABLE_NAME
            + "("
            + AuthorModel.ID + " INTEGER primary key autoincrement,"
            + AuthorModel.T8_ID + " TEXT,"
            + AuthorModel.LOCALE + " TEXT,"
            + AuthorModel.TELEGRAM_ID + " TEXT,"
            + AuthorModel.USERNAME + " TEXT,"
            + AuthorModel.FIRST_NAME + " TEXT,"
            + AuthorModel.LAST_NAME + " TEXT,"
            + AuthorModel.AVATAR_URL + " TEXT,"
            + AuthorModel.BLOCKED + " INTEGER NOT NULL DEFAULT 0"
            + ");";

    static final String CREATE_NODES_TABLE_SQL = "create table "+ PostModel.TABLE_NAME
            + "("
            + PostModel.ID + " INTEGER primary key autoincrement,"
            + PostModel.T8_ID + " TEXT,"
            + PostModel.FILTER_ID + " INTEGER,"
            + PostModel.AUTHOR_ID + " TEXT,"
            + PostModel.VOTE_SCORE + " INTEGER,"
            + PostModel.REPLY_COUNT + " INTEGER,"
            + PostModel.IS_UP_VOTE + " INTEGER NOT NULL DEFAULT 0,"
            + PostModel.IS_DOWN_VOTE + " INTEGER NOT NULL DEFAULT 0,"
            + PostModel.BLOCKED + " INTEGER NOT NULL DEFAULT 0,"
            + PostModel.JSON+ " TEXT"
            + ");";

    public interface Views {
        public static final String VIEW_NODE = "view_node";
    }

    DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_IDS_TABLE_SQL);
        db.execSQL(CREATE_NODES_TABLE_SQL);
        db.execSQL(CREATE_AUTHORS_TABLE_SQL);

        createNodesViews(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion <= 33) {
            deleteAllTable(db);
            onCreate(db);
        }
    }

    public static synchronized DatabaseHelper getInstance(Context context) {
        if (singleton == null) {
            singleton = new DatabaseHelper(context);
        }
        return singleton;
    }

    private void deleteAllTable(SQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS " + IdsModel.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + PostModel.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + AuthorModel.TABLE_NAME);
    }

    private void createNodesViews(SQLiteDatabase db) {
        db.execSQL("DROP VIEW IF EXISTS " + Views.VIEW_NODE + ";");

        String idsColumns =
                "ids." + IdsModel.ID+ ", "
                + "ids." + IdsModel.FILTER_ID + ", "
                + "ids." + IdsModel.POST_ID + ", "
                + "ids." + IdsModel.DISPLAY + ", ";

        String postNameColumns =
            "post_table." + PostModel.T8_ID + ", "
            + "post_table." + PostModel.FILTER_ID + ", "
            + "post_table." + PostModel.AUTHOR_ID + ", "
            + "post_table." + PostModel.VOTE_SCORE + ", "
            + "post_table." + PostModel.REPLY_COUNT+ ", "
            + "post_table." + PostModel.IS_UP_VOTE + ", "
            + "post_table." + PostModel.IS_DOWN_VOTE + ", "
            + "post_table." + PostModel.BLOCKED + ", "
            + "post_table." + PostModel.JSON + ", ";

        String authorNameColumns =
            "author." + AuthorModel.T8_ID + ", "
            + "author." + AuthorModel.LOCALE + ", "
            + "author." + AuthorModel.TELEGRAM_ID + ", "
            + "author." + AuthorModel.USERNAME + ", "
            + "author." + AuthorModel.FIRST_NAME + ", "
            + "author." + AuthorModel.LAST_NAME + ", "
            + "author." + AuthorModel.AVATAR_URL + ", "
            + "author." + AuthorModel.BLOCKED;

        String nodeSelect = "SELECT "
            + idsColumns
            + postNameColumns
            + authorNameColumns
            + " FROM " + IdsModel.TABLE_NAME + " AS ids"
            + " JOIN " + PostModel.TABLE_NAME + " AS post_table ON("
            +   IdsModel.POST_ID + "=" + PostModel.T8_ID + " AND " + IdsModel.FILTER_ID + "=" + PostModel.FILTER_ID +")"
            + " LEFT OUTER JOIN " + AuthorModel.TABLE_NAME + " AS author ON ("
            + PostModel.AUTHOR_ID + "=" + AuthorModel.TELEGRAM_ID+ ")" ;

        db.execSQL("CREATE VIEW " + Views.VIEW_NODE + " AS " + nodeSelect);
    }
}
