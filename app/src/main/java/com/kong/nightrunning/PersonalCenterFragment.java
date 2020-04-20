package com.kong.nightrunning;

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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View personalCenterFragment=inflater.inflate(R.layout.fragment_personal_center,container,false);
        mImageViewUserAvatar=personalCenterFragment.findViewById(R.id.ImageViewPerUserAvatar);
        mTextViewLoginStart=personalCenterFragment.findViewById(R.id.TextViewPerLoginStart);
        initPersonalCenterFragment();
        return personalCenterFragment;
    }

    private void initPersonalCenterFragment(){
        if(MainActivity.USERNAME!=null){
            mTextViewLoginStart.setText("已登录");
        }
        if(MainActivity.USERAVATAR!=null){
            mImageViewUserAvatar.setImageURI(Uri.parse(MainActivity.USERAVATAR));
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        //super.onSaveInstanceState(outState);
    }
}
