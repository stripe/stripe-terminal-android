package com.stripe.example;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;

/**
 * The {@code ReaderSelectionActivity} shows the list of recognized readers and allows the user to
 * select one to connect to.
 */
public class ReaderSelectionActivity extends AppCompatActivity {

    private static final String READER_SELECTION = "com.stripe.example.reader_selection";

    private Fragment fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reader_selection);

        FragmentManager fragmentManager = getSupportFragmentManager();
        fragment = fragmentManager.findFragmentById(R.id.device_selection_container);

        if (fragment == null) {
            fragment = new ReaderSelectionFragment();
            fragmentManager.beginTransaction()
                    .add(R.id.device_selection_container, fragment)
                    .commit();
        }

    }

    /**
     * Retrieve the selected reader from the returned intent
     * @param data the intent returned by this activity
     * @return the selected reader
     */
    public static String getReaderSelection(Intent data) {
        return data.getStringExtra(READER_SELECTION);
    }

    /**
     * Create the intent to be returned by this activity
     * @param reader the reader that was selected
     * @return The intent object
     */
    public static Intent createResultIntent(String reader) {
        Intent intent = new Intent();
        intent.putExtra(READER_SELECTION, reader);
        return intent;
    }
}
