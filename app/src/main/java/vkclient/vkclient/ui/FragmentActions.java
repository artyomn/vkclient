package vkclient.vkclient.ui;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.BindView;
import butterknife.ButterKnife;
import vkclient.vkclient.R;
import vkclient.vkclient.ui.widget.ActionsAdapter;

public class FragmentActions extends Fragment {

    public interface Listener {
        void onStartCamera();

        void onStartMic();
    }

    private Listener listener;

    @BindView(R.id.list_actions)
    RecyclerView recyclerView;

    public static FragmentActions newInstance() {
        FragmentActions fragmentActions = new FragmentActions();
        Bundle args = new Bundle();
        fragmentActions.setArguments(args);
        return fragmentActions;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_actions, container, false);
        ButterKnife.bind(this, root);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
        recyclerView.setAdapter(new ActionsAdapter(new ActionsAdapter.ClickListener() {
            @Override
            public void onCameraClick() {
                listener.onStartCamera();
            }

            @Override
            public void onMicClick() {
                listener.onStartMic();
            }
        }));

        return root;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            listener = (Listener) context;
        } catch (ClassCastException e) {
            throw new IllegalArgumentException(context.getClass().getName() + " must implement FragmentActions.Listener");
        }
    }


}
