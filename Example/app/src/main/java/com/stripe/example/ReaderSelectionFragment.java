package com.stripe.example;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * The {@code ReaderSelectionFragment} shows the list of nearby readers and allows for one to be
 * selected for connection.
 */
public class ReaderSelectionFragment extends Fragment {

    private ReaderAdapter adapter;
    private RecyclerView readerRecyclerView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_reader_list, container, false);

        readerRecyclerView = view.findViewById(R.id.reader_recycler_view);
        readerRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        updateUI();

        return view;
    }

    /**
     * Update the UI with the current list of readers
     */
    private void updateUI() {
        adapter = new ReaderAdapter();
        readerRecyclerView.setAdapter(adapter);
    }

    /**
     * A simple ViewHolder that also acts as an OnClickListener to allow for selecting a reader.
     */
    private class ReaderHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final TextView mReaderTextView;
        private String mReader;

        public ReaderHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.list_item_reader, parent, false));

            mReaderTextView = itemView.findViewById(R.id.reader_name);
            itemView.setOnClickListener(this);
        }

        public void bind(String device) {
            mReader = device;
            mReaderTextView.setText(mReader);
        }

        @Override
        public void onClick(View view) {
            Activity currentActivity = getActivity();
            if (currentActivity != null) {
                getActivity().setResult(Activity.RESULT_OK, ReaderSelectionActivity.createResultIntent(mReader));
                getActivity().finish();
            }
        }
    }

    /**
     * Our Adapter implementation that allows to update the list of readers
     */
    private class ReaderAdapter extends RecyclerView.Adapter<ReaderHolder> implements ReaderList.ReaderSelectionListener {

        private String[] mReaders;

        public ReaderAdapter() {
            mReaders = ReaderList.registerListener(this);
        }

        @Override
        public void updateReaders(String[] readers) {
            this.mReaders = readers;
            notifyDataSetChanged();
        }

        @Override
        public ReaderHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            return new ReaderHolder(layoutInflater, parent);
        }

        @Override
        public void onBindViewHolder(ReaderHolder holder, int position) {
            String reader = mReaders[position];
            holder.bind(reader);
        }

        @Override
        public int getItemCount() {
            return mReaders.length;
        }
    }
}
