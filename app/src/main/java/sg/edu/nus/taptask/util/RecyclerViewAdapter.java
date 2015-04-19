package sg.edu.nus.taptask.util;

import android.content.Context;
import android.content.Intent;
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

import sg.edu.nus.taptask.R;
import sg.edu.nus.taptask.TaptaskService;
import sg.edu.nus.taptask.model.TapAction;
import sg.edu.nus.taptask.model.TapActionManager;

public class RecyclerViewAdapter extends RecyclerSwipeAdapter<RecyclerViewAdapter.SimpleViewHolder> {

    public static class SimpleViewHolder extends RecyclerView.ViewHolder {
        SwipeLayout swipeLayout;
        Button buttonDelete;
        public TextView taskName;
        public ImageView taskImage;
        public TextView onOffTextView;

        public SimpleViewHolder(View itemView) {
            super(itemView);
            swipeLayout = (SwipeLayout) itemView.findViewById(R.id.swipe);
            buttonDelete = (Button) itemView.findViewById(R.id.delete);
            taskName = (TextView) itemView.findViewById(R.id.taskName);
            taskImage = (ImageView)itemView.findViewById(R.id.taskImage);
            onOffTextView = (TextView) itemView.findViewById(R.id.toggleOnOff);
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

        // Set on off text
        String onOffText = tapAction.isEnabled() ? "ON" : "OFF";
        int onOffColor = tapAction.isEnabled() ?
                mContext.getResources().getColor(R.color.lightgreen) :
                mContext.getResources().getColor(R.color.disabled_grey);
        viewHolder.onOffTextView.setText(onOffText);
        viewHolder.onOffTextView.setBackgroundColor(onOffColor);

        // Toggle task on/off on click
        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tapAction.setEnabled(!tapAction.isEnabled());

                // Set on off text
                String onOffText = tapAction.isEnabled() ? "ON" : "OFF";
                int onOffColor = tapAction.isEnabled() ?
                        mContext.getResources().getColor(R.color.lightgreen) :
                        mContext.getResources().getColor(R.color.disabled_grey);
                viewHolder.onOffTextView.setText(onOffText);
                viewHolder.onOffTextView.setBackgroundColor(onOffColor);

                // Save on new thread, so UI thread does not lag
                // Restart service if running
                new Thread(new Runnable() {
                    public void run(){
                        tapActionManager.saveTapActionManager();
                        if (Utils.isMyServiceRunning(mContext, TaptaskService.class)) {
                            mContext.stopService(new Intent(mContext, TaptaskService.class));
                            mContext.startService(new Intent(mContext, TaptaskService.class));
                        }
                    }
                }).start();
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

                Log.d("x value", "xvalue: " + xvel);
                Log.d("y value", "yvalue: " + yvel);

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