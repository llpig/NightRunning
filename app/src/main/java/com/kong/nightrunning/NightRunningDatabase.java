package com.kong.nightrunning;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NightRunningDatabase extends SQLiteOpenHelper {
    //表名,字段
    private Tool tool = new Tool();
    private Tool.NightRunningDB nightRunningDB;

    public NightRunningDatabase(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
        nightRunningDB = tool.new NightRunningDB();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //仅在数据创建时调用一次
        db.execSQL(createUserInfoTable());
        db.execSQL(createMotionInfoTable());
        db.execSQL(createMovementLocusTable());
        db.execSQL(createAchievementTable());
    }

    //返回创建用户信息表的SQL语句
    private String createUserInfoTable() {
        String sql = "create Table UserInfoTable (" +
                "[UserName] varchar(64) PRIMARY KEY  Not Null," +
                "[Password] varchar(32) Not Null," +
                "[Sex] boolean Not Null," +
                "[Age] int check(Age>0) Not Null," +
                "[Height] double check(Height>0) Not Null," +
                "[Weight] double check(Weight>0) Not Null," +
                "[EmergencyContact] varchar(11) Not Null," +
                "[Avatar] varchar(255)" +
                ");";
        return sql;
    }


    //插入记录（用户信息表）将用户信息存储在本地，通过文件查询
    public boolean insertRecordsToUserInfoTable(SQLiteDatabase db, String userName, String password, int sex, int age,
                                                double height, double weight, String avatar, String emergencyContact) {
        ContentValues values = new ContentValues();
        values.put(nightRunningDB.userInfoTable.userName, userName);
        values.put(nightRunningDB.userInfoTable.password, password);
        values.put(nightRunningDB.userInfoTable.sex, sex);
        values.put(nightRunningDB.userInfoTable.age, age);
        values.put(nightRunningDB.userInfoTable.height, height);
        values.put(nightRunningDB.userInfoTable.weight, weight);
        values.put(nightRunningDB.userInfoTable.avatar, avatar);
        values.put(nightRunningDB.userInfoTable.emergencyContact, emergencyContact);
        //如果结果返回-1，则说明插入失败
        return (db.insert(nightRunningDB.userInfoTable.tableName, null, values) == -1 ? false : true);
    }

    //更新紧急联系人
    public boolean updateEmergencyContact(SQLiteDatabase db, String userName, String contactPhone) {
        String whereStr = nightRunningDB.userInfoTable.userName + "=\"" + userName + "\"";
        ContentValues values = new ContentValues();
        values.put(nightRunningDB.userInfoTable.emergencyContact, contactPhone);
        return (db.update(nightRunningDB.userInfoTable.tableName, values, whereStr, null) != 1 ? false : true);
    }

    public String selectEmergencyContact(SQLiteDatabase db, String userName) {
        String whereStr = "UserName=" + "\"" + userName + "\"" + ";";
        String temp = "";
        String[] select = new String[]{
                nightRunningDB.userInfoTable.emergencyContact
        };
        Cursor cursor = db.query(nightRunningDB.userInfoTable.tableName, select, whereStr, null, null, null, null);
        while (cursor.moveToNext()) {
            temp = cursor.getString(cursor.getColumnIndex(nightRunningDB.userInfoTable.emergencyContact));
        }
        return temp.trim();
    }

    //查询
    public ContentValues selectRecordsToUserInfoTable(SQLiteDatabase db, String userName) {
        ContentValues values = new ContentValues();
        String whereStr = "UserName=" + "\"" + userName + "\"" + ";";
        String[] select = new String[]{
                nightRunningDB.userInfoTable.password,
                nightRunningDB.userInfoTable.avatar,
                nightRunningDB.userInfoTable.age,
                nightRunningDB.userInfoTable.height,
                nightRunningDB.userInfoTable.weight,
                nightRunningDB.userInfoTable.sex,
                nightRunningDB.userInfoTable.emergencyContact
        };
        Cursor cursor = db.query(nightRunningDB.userInfoTable.tableName, select, whereStr, null, null, null, null);
        while (cursor.moveToNext()) {
            values.put(nightRunningDB.userInfoTable.password, cursor.getString(cursor.getColumnIndex(nightRunningDB.userInfoTable.password)));
            values.put(nightRunningDB.userInfoTable.avatar, cursor.getString(cursor.getColumnIndex(nightRunningDB.userInfoTable.avatar)));
            values.put(nightRunningDB.userInfoTable.age, cursor.getString(cursor.getColumnIndex(nightRunningDB.userInfoTable.age)));
            values.put(nightRunningDB.userInfoTable.height, cursor.getString(cursor.getColumnIndex(nightRunningDB.userInfoTable.height)));
            values.put(nightRunningDB.userInfoTable.weight, cursor.getString(cursor.getColumnIndex(nightRunningDB.userInfoTable.weight)));
            values.put(nightRunningDB.userInfoTable.sex, cursor.getString(cursor.getColumnIndex(nightRunningDB.userInfoTable.sex)));
            values.put(nightRunningDB.userInfoTable.emergencyContact, cursor.getString(cursor.getColumnIndex(nightRunningDB.userInfoTable.emergencyContact)));
        }
        return values;
    }

    //返回创建运动信息表的SQL语句
    private String createMotionInfoTable() {
        String sql = "Create Table MotionInfoTable (" +
                "[UserName]            varchar(64)," +
                "[Date]                date Not Null DEFAULT (date('now','localtime'))," +
                "[StepNumber]          int Not Null default 0," +
                "[Mileage]             double default 0," +
                "[RunningTime]         long default 0," +
                "[EquipmentInfo]       boolean Not Null default 0," +
                "PRIMARY KEY (UserName,Date)," +
                "FOREIGN KEY (UserName) REFERENCES UserInfoTable(UserName)" +
                ");";
        return sql;
    }

    //插入记录（运动信息表）
    public boolean insertRecordsToMotionInfoTable(SQLiteDatabase db, String userName) {
        ContentValues values = new ContentValues();
        //添加一条只有用户名的新记录，其他值采用默认值。
        values.put(nightRunningDB.motionInfoTable.userName, userName);
        return (db.insert(nightRunningDB.motionInfoTable.tableName, null, values) == -1 ? false : true);
    }

    //查询记录（运动信息表）
    public ContentValues selectRecordsToMotionInfoTable(SQLiteDatabase db, String userName, String date) {
        ContentValues values = new ContentValues();
        String whereStr = nightRunningDB.motionInfoTable.date + "=(select(" + date + ")) and " + nightRunningDB.motionInfoTable.userName + "=\"" + userName + "\"";
        String[] select = new String[]{
                nightRunningDB.motionInfoTable.stepNumber,
                nightRunningDB.motionInfoTable.mileage,
                nightRunningDB.motionInfoTable.runningTime,
                nightRunningDB.motionInfoTable.equipmentInfo};
        Cursor cursor = db.query(nightRunningDB.motionInfoTable.tableName, select, whereStr, null, null, null, null);
        //返回查询到的内容
        while (cursor.moveToNext()) {

            values.put(nightRunningDB.motionInfoTable.stepNumber, cursor.getInt(cursor.getColumnIndex(nightRunningDB.motionInfoTable.stepNumber)));
            values.put(nightRunningDB.motionInfoTable.mileage, cursor.getDouble(cursor.getColumnIndex(nightRunningDB.motionInfoTable.mileage)));
            values.put(nightRunningDB.motionInfoTable.runningTime, cursor.getString(cursor.getColumnIndex(nightRunningDB.motionInfoTable.runningTime)));
            values.put(nightRunningDB.motionInfoTable.equipmentInfo, cursor.getInt(cursor.getColumnIndex(nightRunningDB.motionInfoTable.equipmentInfo)));
        }
        return values;
    }

    public List<String[]> selectHistoryData(SQLiteDatabase db, String userName) {
        String whereStr = "UserName=" + "\"" + userName + "\"" + ";";
        String[] select = new String[]{
                nightRunningDB.motionInfoTable.date,
                nightRunningDB.motionInfoTable.stepNumber,
                nightRunningDB.motionInfoTable.mileage,
        };
        List<String[]> historyDataList = new ArrayList<>();
        String[] historyData;
        Cursor cursor = db.query(nightRunningDB.motionInfoTable.tableName, select, whereStr, null, null, null, null);
        while (cursor.moveToNext()) {
            historyData = new String[3];
            historyData[0] = cursor.getString(cursor.getColumnIndex(nightRunningDB.motionInfoTable.date));
            historyData[1] = cursor.getString(cursor.getColumnIndex(nightRunningDB.motionInfoTable.stepNumber));
            historyData[2] = cursor.getString(cursor.getColumnIndex(nightRunningDB.motionInfoTable.mileage));
            historyDataList.add(historyData);
        }
        historyDataList.remove(historyDataList.size() - 1);
        Collections.reverse(historyDataList);
        return historyDataList;
    }

    //更新记录(运行信息表,普通)
    public boolean upDateRecordsToMotionInfoTableNormal(SQLiteDatabase db, String userName, String date,
                                                        int stepNumber, int equipmentInfo) {
        String whereStr = nightRunningDB.motionInfoTable.date + "=(select(" + date + ")) and " + nightRunningDB.motionInfoTable.userName + "=\"" + userName + "\"";
        ContentValues values = new ContentValues();
        values.put(nightRunningDB.motionInfoTable.stepNumber, stepNumber);
        values.put(nightRunningDB.motionInfoTable.equipmentInfo, equipmentInfo);
        return (db.update(nightRunningDB.motionInfoTable.tableName, values, whereStr, null) != 1 ? false : true);
    }

    //更新记录(运行信息表,跑步)
    public boolean upDateRecordsToMotionInfoTableRunning(SQLiteDatabase db, String userName, String date,
                                                         double mileage, long runningTime) {
        String whereStr = nightRunningDB.motionInfoTable.date + "=(select(" + date + ")) and " + nightRunningDB.motionInfoTable.userName + "=\"" + userName + "\"";
        ContentValues values = new ContentValues();
        values.put(nightRunningDB.motionInfoTable.mileage, mileage);
        values.put(nightRunningDB.motionInfoTable.runningTime, runningTime);
        return (db.update(nightRunningDB.motionInfoTable.tableName, values, whereStr, null) != 1 ? false : true);
    }

    //查询近七天的数据
    public List<Float> selectRecentTimeStepNumber(SQLiteDatabase db, String userName, String date) {
        List<Float> stepNumberList = new ArrayList<Float>();
        String whereStr = nightRunningDB.motionInfoTable.date + ">=(select(" + date + ")) and " +
                nightRunningDB.motionInfoTable.userName + "=\"" + userName + "\"";
        String[] select = new String[]{
                nightRunningDB.motionInfoTable.stepNumber};
        Cursor cursor = db.query(nightRunningDB.motionInfoTable.tableName, select, whereStr, null, null, null, null);
        while (cursor.moveToNext()) {
            stepNumberList.add(new Float(cursor.getInt(cursor.getColumnIndex(nightRunningDB.motionInfoTable.stepNumber))));
        }
        return stepNumberList;
    }

    //返回运动轨迹表的SQL语句
    private String createMovementLocusTable() {
        String sql = "Create Table MovementLocusTable (" +
                "[UserName] varchar(64)," +
                "[MovementLocus] varchar(255)," +
                "[CreateTime] datetime Not Null DEFAULT(datetime('now','localtime'))," +
                "Primary Key (UserName,MovementLocus)," +
                "Foreign Key(UserName) References UserInfoTable(UserName)" +
                ");";
        return sql;
    }

    //插入记录（运动轨迹表）
    public boolean insertRecordsToMovementLocusTable(SQLiteDatabase db, String userName, String movementLocus) {
        ContentValues values = new ContentValues();
        values.put(nightRunningDB.movementLocusTable.userName, userName);
        values.put(nightRunningDB.movementLocusTable.movementLocus, movementLocus);
        return (db.insert(nightRunningDB.movementLocusTable.tableName, null, values) == -1 ? false : true);
    }

    //返回创建个人成就表SQL语句
    private String createAchievementTable() {
        String sql = "Create Table AchievementTable (" +
                "[UserName]     varchar(64) ," +
                "[Achievement]  varchar(255)," +
                "[completeTime] datetime Not Null DEFAULT(datetime('now','localtime'))," +
                "Primary Key(UserName,Achievement)," +
                "Foreign Key(UserName) References UserInfoTable(UserName)" +
                ");";
        return sql;
    }

    //插入记录（个人成就表）
    public boolean insertRecordsToAchievementTable(SQLiteDatabase db, String userName, String achievement) {
        ContentValues values = new ContentValues();
        values.put(nightRunningDB.achievementTable.userName, userName);
        values.put(nightRunningDB.achievementTable.achievement, achievement);
        return (db.insert(nightRunningDB.achievementTable.tableName, null, values) == -1 ? false : true);
    }

    //更新数据库版本是调用
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
