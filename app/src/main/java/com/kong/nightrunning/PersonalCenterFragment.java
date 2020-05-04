package com.kong.nightrunning;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bin.david.form.annotation.SmartColumn;
import com.bin.david.form.annotation.SmartTable;
import com.bin.david.form.core.TableConfig;

import com.bin.david.form.data.format.bg.IBackgroundFormat;
import com.bin.david.form.data.style.FontStyle;

import java.util.ArrayList;
import java.util.List;


public class PersonalCenterFragment extends Fragment {
    private ImageView mImageViewUserAvatar;
    private TextView mTextViewLoginStart;
    private TextView mTextViewHistoryData, mTextViewTrainingPlan, mTextViewChangeContacts;
    private Tool tool;
    private NightRunningDatabase helper;
    public static String TRAININGLATESTTIME = "22:00:00";
    public static String FILEPATH = "/sdcard/NightRunning";

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
        helper = tool.getRunningDatabase(getActivity());
    }

    private void clickHistoryData() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setIcon(R.drawable.logo);
        builder.setTitle("历史数据");

        builder.setView(getSmartTable());

        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        builder.setNeutralButton("导出", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                List<String[]> historyDataList = helper.selectHistoryData(helper.getReadableDatabase(), MainActivity.USERNAME);
                String fileName = MainActivity.USERNAME + "_历史跑步数据_" + tool.currentSystemTime() + ".txt";
                String tmp;
                for (String[] historyData : historyDataList) {
                    tmp = historyData[0] + "," + historyData[1] + "," + historyData[2] + "\n";
                    tool.saveRunningDate(fileName, tmp);
                }
                tool.showToast(getActivity(), "数据保存位置:" + FILEPATH + "/" + fileName);
            }
        });
        builder.show();

    }

    @SmartTable(name = "历史数据列表")
    public class RunningHistoryInfo {
        @SmartColumn(id = 1, name = "时间")
        private String name;
        @SmartColumn(id = 2, name = "步数(步)")
        private String stepNumber;
        @SmartColumn(id = 3, name = "跑步模式(公里)")
        private String mileage;
    }

    private View getSmartTable() {
        com.bin.david.form.core.SmartTable smartTable = new com.bin.david.form.core.SmartTable(getActivity());
        List<RunningHistoryInfo> list = new ArrayList<>();
        RunningHistoryInfo historyInfo;
        List<String[]> historyDataList = helper.selectHistoryData(helper.getReadableDatabase(), MainActivity.USERNAME);
        for (String[] data : historyDataList) {
            historyInfo = new RunningHistoryInfo();
            historyInfo.name = data[0].substring(5);
            historyInfo.stepNumber = data[1];
            historyInfo.mileage = data[2];
            list.add(historyInfo);
        }
        TableConfig tableConfig = smartTable.getConfig();
        tableConfig.setShowXSequence(false);
        tableConfig.setShowTableTitle(false);
        tableConfig.setColumnTitleStyle(new FontStyle(50, Color.BLACK));
        tableConfig.setContentStyle(new FontStyle(45, Color.BLACK));
        smartTable.setData(list);
        return smartTable;
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
        message += "今日目标:" + String.format("%.2f", targetMileage / 1000.0f)
                + "km\n当前完成:" + String.format("%.2f", currentMileage / 1000.0f) + "km\n";
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
                    tempStr += (remainingHour - 1) + "小时" + (60 + remainingMinute) + "分钟\n";
                } else {
                    tempStr += (60 + remainingMinute) + "分钟\n";
                }
            }
        } else {
            tempStr = "宝剑锋从磨砺出，梅花香自苦寒来。继续保持！\n";
        }
        message += tempStr;
        return message;
    }

    private void clickChangeContacts() {
        final EditText inputEditText = new EditText(getActivity());
        inputEditText.setMaxLines(1);
        inputEditText.setHint("紧急联系人手机号");
        inputEditText.setInputType(EditorInfo.TYPE_CLASS_NUMBER);
        inputEditText.setTextSize(24f);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setIcon(R.drawable.logo);
        builder.setTitle("更改紧急联系人");
        builder.setView(inputEditText);
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String inputText = inputEditText.getText().toString().trim();
                if (inputText == null || inputEditText.length() != 11) {
                    tool.showToast(getActivity(), "联系人手机号错误，请检查。");
                } else {
                    SQLiteDatabase db = helper.getReadableDatabase();
                    if (helper.updateEmergencyContact(db, MainActivity.USERNAME, inputText)) {
                        tool.showToast(getActivity(), "紧急联系人信息已更新:" + helper.selectEmergencyContact(db, MainActivity.USERNAME));
                    } else {
                        tool.showToast(getActivity(), "紧急联系人信息更新失败");
                    }
                }
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                tool.showToast(getActivity(), "取消紧急联系人信息更新");
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
