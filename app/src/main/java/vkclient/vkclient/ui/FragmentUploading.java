package vkclient.vkclient.ui;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import butterknife.BindView;
import butterknife.ButterKnife;
import vkclient.vkclient.R;

public class FragmentUploading extends Fragment {

    @BindView(R.id.uploading_progress)
    ProgressBar uploadingProgress;

    public static FragmentUploading newInstance() {
        FragmentUploading fragmentUploading = new FragmentUploading();
        Bundle args = new Bundle();
        fragmentUploading.setArguments(args);
        return fragmentUploading;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_uploading, container, false);
        ButterKnife.bind(this, root);
        return root;
    }

    public void updateProgress(int progress) {
        uploadingProgress.setProgress(progress);
    }

}
