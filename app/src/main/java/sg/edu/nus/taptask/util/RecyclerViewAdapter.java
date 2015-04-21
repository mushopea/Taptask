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
        public TextView taskName;
        public ImageView taskImage;
        public TextView onOffTextView;
        public TextView lastTrigger;

        public SimpleViewHolder(View itemView) {
            super(itemView);
            swipeLayout = (SwipeLayout) itemView.findViewById(R.id.swipe);
            taskName = (TextView) itemView.findViewById(R.id.taskName);
            taskImage = (ImageView)itemView.findViewById(R.id.taskImage);
            onOffTextView = (TextView) itemView.findViewById(R.id.toggleOnOff);
            lastTrigger = (TextView) itemView.findViewById(R.id.lastTriggered);
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

        // set lastTrigger
        viewHolder.lastTrigger.setText(tapAction.getLastTriggerTime());

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
                synchronized (tapActionManager) {
                    boolean targetState = !tapAction.isEnabled();
                    tapAction.setEnabled(targetState);
                    if (targetState) {
                        Toast.makeText(mContext, "Enabled " + viewHolder.taskName.getText().toString() + "!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(mContext, "Disabled " + viewHolder.taskName.getText().toString() + "!", Toast.LENGTH_SHORT).show();
                    }
                    // Set on off text
                    String onOffText = tapAction.isEnabled() ? "ON" : "OFF";
                    int onOffColor = tapAction.isEnabled() ?
                            mContext.getResources().getColor(R.color.lightgreen) :
                            mContext.getResources().getColor(R.color.disabled_grey);
                    viewHolder.onOffTextView.setText(onOffText);
                    viewHolder.onOffTextView.setBackgroundColor(onOffColor);
                }

                // Save on new thread, so UI thread does not lag
                // Refresh service
                new Thread(new Runnable() {
                    public void run(){
                        tapActionManager.saveTapActionManager();
                        mContext.sendBroadcast(new Intent(TaptaskService.REFRESH_SERVICE_INTENT));
                    }
                }).start();
            }
        });


        viewHolder.swipeLayout.setShowMode(SwipeLayout.ShowMode.LayDown);
        viewHolder.swipeLayout.addSwipeListener(new SimpleSwipeListener() {

            @Override
            public void onStartOpen(SwipeLayout layout){
               YoYo.with(Techniques.Tada).duration(500).delay(100).playOn(layout.findViewById(R.id.trash));
               mItemManger.closeAllItems();
            }

            @Override
            public void onOpen(SwipeLayout layout) {

                mItemManger.removeShownLayouts(viewHolder.swipeLayout);
                tapActionManager.tapActions.remove(position);
                notifyItemRangeChanged(position, tapActionManager.tapActions.size());
                notifyItemRemoved(position);
                Toast.makeText(mContext, "Deleted " + viewHolder.taskName.getText().toString() + "!", Toast.LENGTH_SHORT).show();

                new Thread(new Runnable() {
                    public void run(){
                        tapActionManager.saveTapActionManager();
                        mContext.sendBroadcast(new Intent(TaptaskService.REFRESH_SERVICE_INTENT));
                    }
                }).start();

            }
        });
        viewHolder.swipeLayout.setOnDoubleClickListener(new SwipeLayout.DoubleClickListener() {
            @Override
            public void onDoubleClick(SwipeLayout layout, boolean surface) {
                Toast.makeText(mContext, tapAction.getDetails(), Toast.LENGTH_SHORT).show();
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