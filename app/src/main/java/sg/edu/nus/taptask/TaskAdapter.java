package sg.edu.nus.taptask;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import sg.edu.nus.taptask.model.TapAction;
import sg.edu.nus.taptask.model.TapActionManager;

/**
 * Created by musho on 24/3/2015.
 */

@Deprecated
public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.ViewHolder> {
    private int rowLayout;
    private Context mContext;
    private TapActionManager tapActionManager;


    public TaskAdapter(int rowLayout, Context context) {
        this.rowLayout = rowLayout;
        this.mContext = context;
        this.tapActionManager = TapActionManager.getInstance(mContext);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(rowLayout, viewGroup, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int i) {

        final TapAction tapAction = tapActionManager.tapActions.get(i);

        // set name
        viewHolder.taskName.setText(tapAction.getName());

        // set icon
        String imageName = tapAction.getImage();
        viewHolder.taskImage.setImageDrawable(mContext.getResources().getDrawable(mContext.getResources().getIdentifier(imageName, "drawable", mContext.getPackageName())));

        // Set icon onClickListener
        // Vibrate pattern when icon is clicked.
        viewHolder.taskImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tapAction.getPattern().vibratePattern(mContext);
            }
        });

    }

    @Override
    public int getItemCount() {
        return tapActionManager.tapActions.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView taskName;
        public ImageView taskImage;

        public ViewHolder(View itemView) {
            super(itemView);
            taskName = (TextView) itemView.findViewById(R.id.taskName);
            taskImage = (ImageView)itemView.findViewById(R.id.taskImage);
        }

    }
}
