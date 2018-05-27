package vkclient.vkclient.ui;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.ButterKnife;
import vkclient.vkclient.R;

public class FragmentProcessing extends Fragment {

    public static FragmentProcessing newInstance() {
        FragmentProcessing fragmentProcessing = new FragmentProcessing();
        Bundle args = new Bundle();
        fragmentProcessing.setArguments(args);
        return fragmentProcessing;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_processing, container, false);
        ButterKnife.bind(this, root);

        return root;
    }


}
