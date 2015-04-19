package sg.edu.nus.taptask.util;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.daimajia.swipe.SimpleSwipeListener;
import com.daimajia.swipe.SwipeLayout;
import com.daimajia.swipe.adapters.RecyclerSwipeAdapter;
import com.daimajia.swipe.implments.SwipeItemRecyclerMangerImpl;

import sg.edu.nus.taptask.R;
import sg.edu.nus.taptask.model.TapAction;
import sg.edu.nus.taptask.model.TapActionManager;

import java.util.ArrayList;

public class RecyclerViewAdapter extends RecyclerSwipeAdapter<RecyclerViewAdapter.SimpleViewHolder> {

    public static class SimpleViewHolder extends RecyclerView.ViewHolder {
        SwipeLayout swipeLayout;
        Button buttonDelete;
        public TextView taskName;
        public ImageView taskImage;

        public SimpleViewHolder(View itemView) {
            super(itemView);
            swipeLayout = (SwipeLayout) itemView.findViewById(R.id.swipe);
            buttonDelete = (Button) itemView.findViewById(R.id.delete);
            taskName = (TextView) itemView.findViewById(R.id.taskName);
            taskImage = (ImageView)itemView.findViewById(R.id.taskImage);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.d(getClass().getSimpleName(), "onItemSelected: " + taskName.getText().toString());
                    Toast.makeText(view.getContext(), "onItemSelected: " + taskName.getText().toString(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private Context mContext;
    private int rowLayout;
    private TapActionManager tapActionManager;

    //protected SwipeItemRecyclerMangerImpl mItemManger = new SwipeItemRecyclerMangerImpl(this);

    public RecyclerViewAdapter(int rowLayout, Context context) {
        this.mContext = context;
        this.rowLayout = rowLayout;
        this.tapActionManager = TapActionManager.getInstance(mContext);
    }

    @Override
    public SimpleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(rowLayout, parent, false);
        return new SimpleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final SimpleViewHolder viewHolder, final int position) {
        final TapAction tapAction = tapActionManager.tapActions.get(position);
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


        viewHolder.swipeLayout.setShowMode(SwipeLayout.ShowMode.LayDown);
        viewHolder.swipeLayout.addSwipeListener(new SimpleSwipeListener() {
            @Override
            public void onOpen(SwipeLayout layout) {
                YoYo.with(Techniques.Tada).duration(500).delay(100).playOn(layout.findViewById(R.id.trash));
            }
            @Override
            public void onHandRelease(SwipeLayout layout, float xvel, float yvel){

                Log.e("x value", "xvalue: " + xvel);
                Log.e("y value", "yvalue: " + yvel);

            }
        });
        viewHolder.swipeLayout.setOnDoubleClickListener(new SwipeLayout.DoubleClickListener() {
            @Override
            public void onDoubleClick(SwipeLayout layout, boolean surface) {
                Toast.makeText(mContext, "DoubleClick", Toast.LENGTH_SHORT).show();
            }
        });
        viewHolder.buttonDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mItemManger.removeShownLayouts(viewHolder.swipeLayout);
                tapActionManager.tapActions.remove(position);
                notifyItemRemoved(position);
                notifyItemRangeChanged(position, tapActionManager.tapActions.size());
                mItemManger.closeAllItems();
                Toast.makeText(view.getContext(), "Deleted " + viewHolder.taskName.getText().toString() + "!", Toast.LENGTH_SHORT).show();
            }
        });
        mItemManger.bindView(viewHolder.itemView, position);
    }

    @Override
    public int getItemCount() {
        return tapActionManager.tapActions.size();
    }

    @Override
    public int getSwipeLayoutResourceId(int position) {
        return R.id.swipe;
    }
}