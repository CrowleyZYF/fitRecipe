package cn.fitrecipe.android.PlanTest;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import cn.fitrecipe.android.R;

/**
 * Created by 奕峰 on 2015/4/11.
 */
public class Q01Fragment extends Fragment
{
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.fragment_plan_test_01, container, false);
    }
}
