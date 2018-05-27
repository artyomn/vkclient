package vkclient.vkclient.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.Group;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import vkclient.vkclient.R;

public class FragmentAudio extends Fragment {

    @BindView(R.id.recording_button)
    ImageButton recordButton;
    @BindView(R.id.recording_progress)
    ProgressBar recordingProgress;
    @BindView(R.id.recording_text)
    TextView recordingText;

    public interface Listener {
        void onAudioRecordingStart();

        void onAudioRecordingFinish();
    }

    private Listener listener;

    public static FragmentAudio newInstance() {
        FragmentAudio fragmentAudio = new FragmentAudio();
        Bundle args = new Bundle();
        fragmentAudio.setArguments(args);
        return fragmentAudio;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_audio, container, false);
        ButterKnife.bind(this, root);
        recordingProgress.setVisibility(View.INVISIBLE);
        recordingText.setVisibility(View.INVISIBLE);
        recordButton.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                listener.onAudioRecordingStart();
                recordingProgress.setVisibility(View.VISIBLE);
                recordingText.setVisibility(View.VISIBLE);
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                listener.onAudioRecordingFinish();
                recordingProgress.setVisibility(View.INVISIBLE);
                recordingText.setVisibility(View.INVISIBLE);
            }
            return false;
        });
        return root;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            listener = (Listener) context;
        } catch (ClassCastException e) {
            throw new IllegalArgumentException(context.getClass().getName() + " must implement FragmentAudio.Listener");
        }
    }


}
