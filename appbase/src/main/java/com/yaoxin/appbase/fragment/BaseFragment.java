package com.yaoxin.appbase.fragment;

import androidx.fragment.app.Fragment;

import com.yaoxin.appbase.view.LoadingDialog;


/**
 * Created by will
 * on 2018/5/25.
 */
public class BaseFragment extends Fragment {


    /**
     * 显示进度条对话框
     */
    protected void showProgressDialog() {
        showProgressDialog("请稍等...");
    }

    protected void showProgressDialog(String msg) {
        LoadingDialog.showDialog(getChildFragmentManager(), msg);
    }

    /**
     * 隐藏进度条对话框
     */
    protected void hideProgressDialog() {
        LoadingDialog.dismissDialog();
    }

    protected void _requestData() {

    }
    protected void _initViews() {

    }

}
