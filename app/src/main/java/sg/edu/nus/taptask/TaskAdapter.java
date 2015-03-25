package sg.edu.nus.taptask;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import sg.edu.nus.taptask.model.Task;

/**
 * Created by musho on 24/3/2015.
 */
public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.ViewHolder> {
    private List<Task> tasks;
    private int rowLayout;
    private Context mContext;

    public TaskAdapter(List<Task> tasks, int rowLayout, Context context) {
        this.tasks = tasks;
        this.rowLayout = rowLayout;
        this.mContext = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(rowLayout, viewGroup, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int i) {
        String imageName;
        Task task = tasks.get(i);

        // set name
        viewHolder.taskName.setText(task.name);

        // set icon
        switch (task.type) {
            case 1: imageName = "task_icon_call";
                break;
            case 2: imageName = "task_icon_call_reject";
                break;
            case 3: imageName = "task_icon_message";
                break;
            case 4: imageName = "task_icon_volume";
                break;
            default: imageName = "task_icon_message";
                Log.e("Invalid task type", "Invalid task type");
                break;
        }
        viewHolder.taskImage.setImageDrawable(mContext.getResources().getDrawable(mContext.getResources().getIdentifier(imageName, "drawable", mContext.getPackageName())));
    }

    @Override
    public int getItemCount() {
        return tasks == null ? 0 : tasks.size();
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
