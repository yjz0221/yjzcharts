package com.ludak.batterytestchart.ui.base;

import android.os.Bundle;
import android.view.LayoutInflater;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewbinding.ViewBinding;

import com.blankj.utilcode.util.ScreenUtils;

/**
 * 作者:cl
 * 创建日期：2024/11/28
 * 描述:
 */
public abstract class BaseActivity<T extends ViewBinding> extends AppCompatActivity {

    protected T viewBinding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewBinding = getViewBinding(getLayoutInflater());
        setContentView(viewBinding.getRoot());
        initData(savedInstanceState);
        initView();
    }

    public abstract T getViewBinding(LayoutInflater inflater);

    public abstract void initView();

    public abstract void initData(Bundle savedInstanceState);

}
