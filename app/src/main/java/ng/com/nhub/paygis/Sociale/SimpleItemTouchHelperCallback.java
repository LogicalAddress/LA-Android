package ng.com.nhub.paygis.Sociale;

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;

/**
 * Created by retnan on 6/4/16.
 */
public class SimpleItemTouchHelperCallback extends ItemTouchHelper.Callback {

    private final SwipePostListener mSwipPostListener;

    public SimpleItemTouchHelperCallback(SwipePostListener swipPostListener) {
        mSwipPostListener = swipPostListener;
    }

    @Override
    public boolean isLongPressDragEnabled() {
        return true;
    }

    @Override
    public boolean isItemViewSwipeEnabled() {
        return true;
    }

    @Override
    public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
        int swipeFlags = ItemTouchHelper.START | ItemTouchHelper.END;
        return makeMovementFlags(dragFlags, swipeFlags);
    }

    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
        return false;
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
        mSwipPostListener.onItemDismiss(viewHolder.getAdapterPosition());
    }

}