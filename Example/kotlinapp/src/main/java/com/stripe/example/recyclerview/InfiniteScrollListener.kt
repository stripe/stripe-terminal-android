package com.stripe.example.recyclerview

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

/**
 * Scroll Listener that invokes a callback when near the bottom.
 *
 * This can be used to create an infinite scroll or automatically load
 * more of a list when near the bottom.
 *
 * @param layoutManager The layoutManager of the Recyclerview being scrolled.
 * @param loadAction Action to take when within [threshold] position of the bottom of the list.
 * @param threshold The number of items from the bottom to invoke [loadAction]
 */
class InfiniteScrollListener(
    private val layoutManager: LinearLayoutManager,
    private val loadAction: () -> Unit,
    private val threshold: Int = 5,
) : RecyclerView.OnScrollListener() {
    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        super.onScrolled(recyclerView, dx, dy)
        val position = layoutManager.findLastVisibleItemPosition()
        val size = layoutManager.itemCount
        if (position < size - threshold) return

        recyclerView.post {
            loadAction()
        }
    }
}
