package com.kong.nightrunning;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;


public class PersonalCenterFragment extends Fragment {
    private ImageView mImageViewUserAvatar;
    private TextView mTextViewLoginStart;
    private TextView mTextViewHistoryData, mTextViewTrainingPlan, mTextViewChangeContacts;
    private Tool tool;
    public static String TRAININGLATESTTIME = "22:00:00";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_personal_center, container, false);
        mImageViewUserAvatar = fragmentView.findViewById(R.id.ImageViewPerUserAvatar);
        mTextViewLoginStart = fragmentView.findViewById(R.id.TextViewPerLoginStart);
        mTextViewHistoryData = fragmentView.findViewById(R.id.TextViewHistoryData);
        mTextViewTrainingPlan = fragmentView.findViewById(R.id.TextViewTrainingPlan);
        mTextViewChangeContacts = fragmentView.findViewById(R.id.TextViewChangeContacts);
        initPersonalCenterFragment();
        return fragmentView;
    }

    private void initPersonalCenterFragment() {
        if (MainActivity.USERNAME != null) {
            mTextViewLoginStart.setText("已登录");
        }
        if (MainActivity.USERAVATAR != null) {
            mImageViewUserAvatar.setImageURI(Uri.parse(MainActivity.USERAVATAR));
        }
        ClickListener clickListener = new ClickListener();
        mTextViewHistoryData.setOnClickListener(clickListener);
        mTextViewChangeContacts.setOnClickListener(clickListener);
        mTextViewTrainingPlan.setOnClickListener(clickListener);
        mTextViewTrainingPlan.setOnClickListener(clickListener);
        tool = new Tool();
    }

    private void clickHistoryData() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setIcon(R.drawable.logo);
        builder.setTitle("历史数据");
//        builder.setMessage();
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.show();
    }

    private void clickTrainingPlan() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setIcon(R.drawable.logo);
        builder.setTitle("训练计划");
        builder.setMessage(getTrainingPlanMessage());
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.show();
    }

    private String getTrainingPlanMessage() {
        String message = "";
        double targetMileage = tool.getMileage(SportsShowFragment.todayTargetStepNumber, MainActivity.USERHEIGHT, MainActivity.USERAGE, MainActivity.USERSEX);
        double currentMileage = tool.getMileage(NightRunningSensorEventListener.getTodayAddStepNumber(), MainActivity.USERHEIGHT, MainActivity.USERAGE, MainActivity.USERSEX);
        message += "今日目标:" + String.format("%.2f", targetMileage/1000.0f)
                + "km\n当前完成:" + String.format("%.2f", currentMileage/1000.0f) + "km\n";
        String currentTime = tool.currentSystemTime();
        String tempStr = "剩余时间:";
        if (currentMileage - targetMileage < 0) {
            int remainingHour = Integer.parseInt(TRAININGLATESTTIME.substring(0, 2)) - Integer.parseInt(currentTime.substring(0, 2));
            int remainingMinute = Integer.parseInt(TRAININGLATESTTIME.substring(3, 5)) - Integer.parseInt(currentTime.substring(3, 5));

            if (remainingHour < 0 || (remainingHour == 0 && remainingMinute <= 0)) {
                tempStr = "行百里者半九十,明天努力完成任务";
            } else if (remainingMinute > 0) {
                tempStr += remainingHour + "小时" + remainingMinute + "分钟\n";
            } else if (remainingMinute <= 0) {
                if (remainingHour - 1 != 0) {
                    tempStr += remainingHour - 1 + "小时" + 60 + remainingMinute + "分钟\n";
                } else {
                    tempStr += 60 + remainingMinute + "分钟\n";
                }
            }
        } else {
            tempStr = "宝剑锋从磨砺出，梅花香自苦寒来。继续保持！\n";
        }
        message += tempStr;
        return message;
    }

    private void clickChangeContacts() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setIcon(R.drawable.logo);
        builder.setTitle("更改紧急联系人");
//        builder.setMessage();
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.show();
    }

    private class ClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.TextViewHistoryData: {
                    clickHistoryData();
                    break;
                }
                case R.id.TextViewTrainingPlan: {
                    clickTrainingPlan();
                    break;
                }
                case R.id.TextViewChangeContacts: {
                    clickChangeContacts();
                    break;
                }
            }
        }
    }


    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
    }
}
