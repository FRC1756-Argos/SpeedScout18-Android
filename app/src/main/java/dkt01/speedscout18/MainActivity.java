package dkt01.speedscout18;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Pair;
import android.util.SparseBooleanArray;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Random;

public class MainActivity extends AppCompatActivity
{
    public final static String CREATE_MESSAGE = "com.dkt01.speedscout17.CREATE_MESSAGE";

    public final static int ENTRY_REQUEST = 1756;

    private ScoutingDataDBHelper matchesDB;
    private ArrayList<Pair<Integer, String> > matchesList;
    private ListView matchesListView;

    private MenuItem selectButton;
    private MenuItem newButton;
    private MenuItem allButton;
    private MenuItem cancelButton;
    private MenuItem deleteButton;
    private MenuItem shareButton;

    MatchListAdapter matchesListAdapter;

    private AdapterView.OnItemClickListener matchClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                                long id) {
            Pair<Integer,String> selected = (Pair<Integer,String>)parent.getAdapter().getItem(position);
            Intent newFileIntent = new Intent(MainActivity.this,EntryActivity.class);
            newFileIntent.putExtra(CREATE_MESSAGE,selected.first);
            startActivityForResult(newFileIntent,ENTRY_REQUEST);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        matchesListView = (ListView) findViewById(R.id.matchesListView);
        Random r = new Random();
        matchesDB = new ScoutingDataDBHelper(this);

        matchesList = matchesDB.getMatches();
        matchesListAdapter = new MatchListAdapter(this,matchesList);
        matchesListView.setAdapter(matchesListAdapter);
        matchesListView.setOnItemClickListener(matchClickListener);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_activity_menu, menu);
        selectButton = menu.findItem(R.id.action_select);
        newButton = menu.findItem(R.id.action_new);
        allButton = menu.findItem(R.id.action_all);
        cancelButton = menu.findItem(R.id.action_cancel);
        deleteButton = menu.findItem(R.id.action_delete);
        shareButton = menu.findItem(R.id.action_share);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        SparseBooleanArray selected;
        switch (item.getItemId()) {
            case R.id.action_select:
                selectButton.setVisible(false);
                newButton.setVisible(false);
                allButton.setVisible(true);
                cancelButton.setVisible(true);
                shareButton.setVisible(true);
                deleteButton.setVisible(true);
                matchesListAdapter.showCheckBoxes(true);
                matchesListView.setOnItemClickListener(null);
                matchesListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
                return true;

            case R.id.action_new:
                Intent newFileIntent = new Intent(this,EntryActivity.class);
                newFileIntent.putExtra(CREATE_MESSAGE,0);
                startActivityForResult(newFileIntent,ENTRY_REQUEST);
                return true;

            case R.id.action_cancel:
                selectButton.setVisible(true);
                newButton.setVisible(true);
                allButton.setVisible(false);
                cancelButton.setVisible(false);
                shareButton.setVisible(false);
                deleteButton.setVisible(false);
                selectAll(false);
                matchesListAdapter.showCheckBoxes(false);
                matchesListView.setOnItemClickListener(matchClickListener);
                matchesListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
                return true;

            case R.id.action_delete:
                new AlertDialog.Builder(this)
                    .setTitle("Delete files?")
                    .setMessage("Are you sure you want to delete the selected match files?")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // continue with delete
                            deleteSelections();
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // do nothing
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
                return true;

            case R.id.action_share:
                ArrayList<Integer> times = new ArrayList<>();
                for(int position = 0; position < matchesListAdapter.getCount(); position++)
                {
                    if(matchesListView.isItemChecked(position))
                    {
                        times.add(matchesListAdapter.getEntry(position).first);
                    }
                }

                ArrayList<Uri> matchCsvs = matchesDB.getCsv(times);

                Intent shareDataIntent = new Intent();
                shareDataIntent.setAction(Intent.ACTION_SEND_MULTIPLE);
                shareDataIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM,matchCsvs);
                shareDataIntent.setType("text/csv");
                shareDataIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivity(Intent.createChooser(shareDataIntent, "Share scouting data to..."));

                selectButton.setVisible(true);
                newButton.setVisible(true);
                allButton.setVisible(false);
                cancelButton.setVisible(false);
                shareButton.setVisible(false);
                deleteButton.setVisible(false);
                selectAll(false);
                matchesListAdapter.showCheckBoxes(false);
                matchesListView.setOnItemClickListener(matchClickListener);
                matchesListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
                return true;

            case R.id.action_all:
                selectAll(true);
                return true;

            case R.id.action_about:
                Intent aboutIntent = new Intent(this,AboutActivity.class);
                startActivity(aboutIntent);
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);
        }
    }

    private void deleteSelections()
    {
        selectButton.setVisible(true);
        newButton.setVisible(true);
        allButton.setVisible(false);
        cancelButton.setVisible(false);
        shareButton.setVisible(false);
        deleteButton.setVisible(false);
        // Go in reverse to preserve indices
        for(int position = matchesListAdapter.getCount() - 1; position >= 0; position--)
        {
            if(matchesListView.isItemChecked(position))
            {
                Pair<Integer, String> temp = matchesListAdapter.getEntry(position);
                matchesDB.deleteMatch(temp.first);
                matchesListAdapter.remove(temp.first);
            }
        }
        matchesListAdapter.notifyDataSetChanged();
        selectAll(false);
        matchesListAdapter.showCheckBoxes(false);
        matchesListView.setOnItemClickListener(matchClickListener);
        matchesListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
    }

    private void selectAll(boolean selected)
    {
        for(int i = 0; i < matchesListAdapter.getCount(); i++)
        {
            matchesListView.setItemChecked(i,selected);
        }
        matchesListAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == ENTRY_REQUEST) {
            // Check if an update was made
            if (resultCode == RESULT_OK) {
                matchesList.clear();
                matchesList = matchesDB.getMatches();
                matchesListAdapter.Update(matchesList);
            }
            // Other option is RESULT_CANCELED (not used)
            selectAll(false);
        }
    }

}
