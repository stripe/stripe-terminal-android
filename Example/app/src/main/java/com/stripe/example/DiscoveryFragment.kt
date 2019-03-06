package com.stripe.example

import android.app.Activity
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.stripe.stripeterminal.*
import kotlinx.android.synthetic.main.fragment_discovery.view.*

/**
 * The `DiscoveryFragment` shows the list of recognized readers and allows the user to
 * select one to connect to.
 */
class DiscoveryFragment : Fragment() {

    private var adapter: ReaderAdapter? = null
    private var readerRecyclerView: RecyclerView? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_discovery, container, false)

        readerRecyclerView = view.reader_recycler_view
        readerRecyclerView?.layoutManager = LinearLayoutManager(activity)

        activity?.let {
            adapter = ReaderAdapter(it)
            readerRecyclerView?.adapter = adapter
        }

        Terminal.getInstance().discoverReaders(DiscoveryConfiguration(0,
                DeviceType.CHIPPER_2X), adapter, DiscoveryCallback())

        return view
    }

    /**
     * A simple [RecyclerView.ViewHolder] that also acts as a [View.OnClickListener] to allow for
     * selecting a reader.
     */
    private class ReaderHolder(val activity: Activity, parent: ViewGroup) :
            RecyclerView.ViewHolder(activity.layoutInflater.inflate(R.layout.list_item_reader, parent, false)),
            View.OnClickListener {

        private val readerTextView: Button? = itemView.findViewById(R.id.reader_name)
        private var reader: Reader? = null

        init {
            readerTextView?.setOnClickListener(this)
        }

        override fun onClick(view: View?) {
            if (activity is NavigationListener) {
                reader?.let { activity.onSelectReader(it) }
            }
        }

        fun bind(reader: Reader) {
            this.reader = reader
            readerTextView?.text = reader.serialNumber
        }

    }

    /**
     * Our [RecyclerView.Adapter] implementation that allows us to update the list of readers
     */
    private class ReaderAdapter(val activity: Activity) :
            RecyclerView.Adapter<ReaderHolder>(), DiscoveryListener {

        var readers = emptyList<Reader>()

        override fun onUpdateDiscoveredReaders(readers: List<Reader>) {
            this.readers = readers
            notifyDataSetChanged()
        }

        override fun getItemCount(): Int {
            return readers.size
        }

        override fun onBindViewHolder(holder: ReaderHolder, position: Int) {
            holder.bind(readers[position])
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReaderHolder {
            return ReaderHolder(activity, parent)
        }

    }

    /**
     * No-op [Callback] implementation to use when discovery completes
     */
    private class DiscoveryCallback : Callback {
        override fun onSuccess() {
            // No need to do anything here. Next steps are handled in the connection callback
        }

        override fun onFailure(e: TerminalException) {
            // Just log the exception
            Log.e(javaClass.simpleName, e.errorMessage, e)
        }
    }


}
