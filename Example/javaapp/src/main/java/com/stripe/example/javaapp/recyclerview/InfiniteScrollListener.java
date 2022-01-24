package com.stripe.example.javaapp.recyclerview;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Scroll Listener that invokes a callback when near the bottom.
 *
 * This can be used to create an infinite scroll or automatically load
 * more of a list when near the bottom.
 */
final public class InfiniteScrollListener extends RecyclerView.OnScrollListener {
    private LinearLayoutManager layoutManager;
    private Listener loadAction;
    private int threshold;

    /**
     * @param layoutManager The layoutManager of the Recyclerview being scrolled.
     * @param threshold The number of items from the bottom to invoke [loadAction]
     * @param loadAction Action to take when within [threshold] position of the bottom of the list.
     */
    public InfiniteScrollListener(
        LinearLayoutManager layoutManager,
        int threshold,
        Listener loadAction
    ) {
        this.layoutManager = layoutManager;
        this.loadAction = loadAction;
        this.threshold = threshold;
    }

    /**
     * @param layoutManager The layoutManager of the Recyclerview being scrolled.
     * @param loadAction Action to take when within [threshold] position of the bottom of the list.
     */
    public InfiniteScrollListener(
        LinearLayoutManager layoutManager,
        Listener loadAction
    ) {
        this(layoutManager, 5, loadAction);
    }

    @Override
    public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);

        int position = layoutManager.findLastVisibleItemPosition();
        int size = layoutManager.getItemCount();

        if (position < size - threshold) {
            return;
        }

        recyclerView.post(() -> {
            loadAction.onLoadMore();
        });
    }

    public interface Listener {
        /**
         * Invoked when an infinite scroll recyclerview is near the bottom.
         */
        void onLoadMore();
    }
}
