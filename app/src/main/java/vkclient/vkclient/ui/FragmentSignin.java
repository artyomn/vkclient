package vkclient.vkclient.ui;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import butterknife.BindView;
import butterknife.ButterKnife;
import vkclient.vkclient.R;

public class FragmentSignin extends Fragment {

    public interface Listener {
        void onStartSignin();
    }

    private Listener listener;

    @BindView(R.id.button_signin)
    Button buttonSignIn;

    public static FragmentSignin newInstance() {
        FragmentSignin fragmentSignin = new FragmentSignin();
        Bundle args = new Bundle();
        fragmentSignin.setArguments(args);
        return fragmentSignin;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_signin, container, false);
        ButterKnife.bind(this, root);

        buttonSignIn.setOnClickListener(view -> listener.onStartSignin());

        return root;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            listener = (Listener) context;
        } catch (ClassCastException e) {
            throw new IllegalArgumentException(context.getClass().getName() + " must implement FragmentSignin.Listener");
        }
    }


}
