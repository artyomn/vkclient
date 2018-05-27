package vkclient.vkclient.ui.widget;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import vkclient.vkclient.R;

public class ActionsAdapter extends RecyclerView.Adapter<ActionsAdapter.ViewHolder> {

    public interface ClickListener {
        void onCameraClick();

        void onMicClick();
    }

    private ClickListener clickListener;

    private int[] icons = new int[]{R.drawable.ic_camera, R.drawable.ic_mic};
    private View.OnClickListener[] clickListeners = new View.OnClickListener[]{
            view -> clickListener.onCameraClick(),
            view -> clickListener.onMicClick()
    };

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageButton imageButton;

        ViewHolder(ImageButton v) {
            super(v);
            imageButton = v;
        }
    }

    public ActionsAdapter(ClickListener clickListener) {
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public ActionsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ImageButton v = (ImageButton) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.view_action_button, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.imageButton.setImageResource(icons[position]);
        holder.imageButton.setOnClickListener(clickListeners[position]);
    }

    @Override
    public int getItemCount() {
        return icons.length;
    }

}
