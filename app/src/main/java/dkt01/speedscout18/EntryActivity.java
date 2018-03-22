package dkt01.speedscout18;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.ToggleButton;

import java.util.Calendar;

public class EntryActivity extends AppCompatActivity {
    private int matchTime;
    private ScoutingDataDBHelper matchesDB;
    private Cursor matchData;

    private final int INDEX_AUTO_LINE_NO = 0;
    private final int INDEX_AUTO_LINE_YES = 1;
    private final int INDEX_AUTO_LINE_ATT = 2;
    private final int INDEX_AUTO_SWITCH_NO = 0;
    private final int INDEX_AUTO_SWITCH_YES = 1;
    private final int INDEX_AUTO_SWITCH_ATT = 2;
    private final int INDEX_AUTO_SCALE_NO = 0;
    private final int INDEX_AUTO_SCALE_YES = 1;
    private final int INDEX_AUTO_SCALE_ATT = 2;
    private final int INDEX_CUBE_ABILITY_NONE = 0;
    private final int INDEX_CUBE_ABILITY_FLOOR = 1;
    private final int INDEX_CUBE_ABILITY_PORTAL = 2;
    private final int INDEX_CUBE_ABILITY_BOTH = 3;
    private final int INDEX_TELE_PARK_NO = 0;
    private final int INDEX_TELE_PARK_YES = 1;
    private final int INDEX_TELE_PARK_ATT = 2;
    private final int INDEX_TELE_CLIMB_NO = 0;
    private final int INDEX_TELE_CLIMB_YES = 1;
    private final int INDEX_TELE_CLIMB_ATT = 2;

    private final String YES = "Y";
    private final String NO = "N";
    private final String ATTEMPTED = "A";

    private final String CUBE_ABILITY_NONE = "None";
    private final String CUBE_ABILITY_FLOOR = "Floor";
    private final String CUBE_ABILITY_PORTAL = "Portal";
    private final String CUBE_ABILITY_BOTH = "Both";

    private int teleSwitchCount = 0;
    private int teleScaleCount = 0;
    private int teleOppSwitchCount = 0;
    private int teleExchangeCount = 0;

    private EditText teamNumEditText;
    private ToggleButton teamColorButton;
    private EditText matchNumEditText;
    private Spinner autoBaselineSpinner;
    private Spinner autoSwitchSpinner;
    private Spinner autoScaleSpinner;
    private Button teleSwitchIncButton;
    private Button teleSwitchDecButton;
    private Button teleScaleIncButton;
    private Button teleScaleDecButton;
    private Button teleOppSwitchIncButton;
    private Button teleOppSwitchDecButton;
    private Button teleExchangeIncButton;
    private Button teleExchangeDecButton;
    private Spinner teleCubeAbilitySpinner;
    private Spinner teleParkSpinner;
    private Spinner teleClimbSpinner;
    private EditText commentEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entry);
        Intent genesisIntent = getIntent();

        teamNumEditText = findViewById(R.id.entry_team_number);
        teamColorButton = findViewById(R.id.entry_team_color);
        matchNumEditText = findViewById(R.id.entry_match_number);
        autoBaselineSpinner = findViewById(R.id.entry_auto_baseline);
        autoSwitchSpinner = findViewById(R.id.entry_auto_switch);
        autoScaleSpinner = findViewById(R.id.entry_auto_scale);
        teleSwitchIncButton = findViewById(R.id.entry_tele_red_switch_inc);
        teleSwitchDecButton = findViewById(R.id.entry_tele_red_switch_dec);
        teleScaleIncButton = findViewById(R.id.entry_tele_scale_inc);
        teleScaleDecButton = findViewById(R.id.entry_tele_scale_dec);
        teleOppSwitchIncButton = findViewById(R.id.entry_tele_blue_switch_inc);
        teleOppSwitchDecButton = findViewById(R.id.entry_tele_blue_switch_dec);
        teleExchangeIncButton = findViewById(R.id.entry_tele_exchange_inc);
        teleExchangeDecButton = findViewById(R.id.entry_tele_exchange_dec);
        teleCubeAbilitySpinner = findViewById(R.id.entry_tele_cube_ability);
        teleParkSpinner = findViewById(R.id.entry_tele_park);
        teleClimbSpinner = findViewById(R.id.entry_tele_climb);
        commentEditText = findViewById(R.id.entry_comments);

        matchTime = genesisIntent.getIntExtra(MainActivity.CREATE_MESSAGE, 0);
        matchesDB = new ScoutingDataDBHelper(this);
        if (matchTime != 0) {
            matchData = matchesDB.getMatch(matchTime);
            if (loadFromDB()) {
                getSupportActionBar().setTitle("Edit File");
            } else {
                matchTime = 0;
            }
        } else {
            // Address corner case where previous call to EntryActivity set the color to
            // blue and then the text changes back this time without changing the color
            teamColorButton.setBackgroundColor(ContextCompat.getColor(this, R.color.red));
            teleSwitchIncButton.setText("Red Switch");
            teleOppSwitchIncButton.setText("Blue Switch");
        }

    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.entry_activity_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                if (saveToDb()) {
                    Intent data = new Intent();
                    if (getParent() == null) {
                        setResult(Activity.RESULT_OK, data);
                    } else {
                        getParent().setResult(Activity.RESULT_OK, data);
                    }
                    finish();
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setMessage("You must include a team number and match number")
                            .setCancelable(false)
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                }
                            });
                    AlertDialog alert = builder.create();
                    alert.show();
                }
                return true;

            case R.id.action_cancel:
                Intent data = new Intent();
                if (getParent() == null) {
                    setResult(Activity.RESULT_CANCELED, data);
                } else {
                    getParent().setResult(Activity.RESULT_CANCELED, data);
                }
                finish();
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                Log.e("???", "WHY AM I HERE?");
                return super.onOptionsItemSelected(item);
        }
    }

    public boolean loadFromDB() {
        matchData.moveToFirst();
        if (matchData.isAfterLast()) {
            return false;
        }
        teamNumEditText.setText(String.valueOf(matchData.getInt(matchData.getColumnIndex(ScoutingDataDBHelper.TEAM_COL_NAME))));
        teamColorButton.setChecked(matchData.getString(matchData.getColumnIndex(ScoutingDataDBHelper.ALLIANCE_COL_NAME)).equals("Blue"));
        if (teamColorButton.isChecked())
            teamColorButton.setBackgroundColor(ContextCompat.getColor(this, R.color.blue));
        else
            teamColorButton.setBackgroundColor(ContextCompat.getColor(this, R.color.red));
        matchNumEditText.setText(String.valueOf(matchData.getInt(matchData.getColumnIndex(ScoutingDataDBHelper.MATCH_COL_NAME))));
        String autoBaseLineString = matchData.getString(matchData.getColumnIndex(ScoutingDataDBHelper.AUTO_BASELINE_COL_NAME));
        String autoSwitchString = matchData.getString(matchData.getColumnIndex(ScoutingDataDBHelper.AUTO_SWITCH_COL_NAME));
        String autoScaleString = matchData.getString(matchData.getColumnIndex(ScoutingDataDBHelper.AUTO_SCALE_COL_NAME));
        String teleCubeAbilityString = matchData.getString(matchData.getColumnIndex(ScoutingDataDBHelper.TELE_CUBE_ABILITY_COL_NAME));
        String teleParkString = matchData.getString(matchData.getColumnIndex(ScoutingDataDBHelper.TELE_PARK_COL_NAME));
        String teleClimbString = matchData.getString(matchData.getColumnIndex(ScoutingDataDBHelper.TELE_CLIMB_COL_NAME));
        teleSwitchCount = matchData.getInt(matchData.getColumnIndex(ScoutingDataDBHelper.TELE_SWITCH_COL_NAME));
        teleScaleCount = matchData.getInt(matchData.getColumnIndex(ScoutingDataDBHelper.TELE_SCALE_COL_NAME));
        teleOppSwitchCount = matchData.getInt(matchData.getColumnIndex(ScoutingDataDBHelper.TELE_OPP_SWITCH_COL_NAME));
        teleExchangeCount = matchData.getInt(matchData.getColumnIndex(ScoutingDataDBHelper.TELE_EXCHANGE_COL_NAME));

        teleSwitchDecButton.setText(String.valueOf(teleSwitchCount));
        teleScaleDecButton.setText(String.valueOf(teleScaleCount));
        teleOppSwitchDecButton.setText(String.valueOf(teleOppSwitchCount));
        teleExchangeDecButton.setText(String.valueOf(teleExchangeCount));

        String commentsString = matchData.getString(matchData.getColumnIndex(ScoutingDataDBHelper.COMMENTS_COL_NAME));
        if (null != commentsString)
        {
            commentEditText.setText(String.valueOf(commentsString));
        }
        else
        {
            commentEditText.setText("");
        }

        if (autoBaseLineString.equals(YES)) {
            autoBaselineSpinner.setSelection(INDEX_AUTO_LINE_YES);
        } else if (autoBaseLineString.equals(NO)) {
            autoBaselineSpinner.setSelection(INDEX_AUTO_LINE_NO);
        } else if (autoBaseLineString.equals(ATTEMPTED)) {
            autoBaselineSpinner.setSelection(INDEX_AUTO_LINE_ATT);
        } else {
            Log.e("LoadFromDb", "Invalid autoBaseLineString:" + autoBaseLineString);
        }

        if (autoSwitchString.equals(YES)) {
            autoSwitchSpinner.setSelection(INDEX_AUTO_SWITCH_YES);
        } else if (autoSwitchString.equals(NO)) {
            autoSwitchSpinner.setSelection(INDEX_AUTO_SWITCH_NO);
        } else if (autoSwitchString.equals(ATTEMPTED)) {
            autoSwitchSpinner.setSelection(INDEX_AUTO_SWITCH_ATT);
        } else {
            Log.e("LoadFromDb", "Invalid autoSwitchString:" + autoSwitchString);
        }

        if (autoScaleString.equals(YES)) {
            autoScaleSpinner.setSelection(INDEX_AUTO_SCALE_YES);
        } else if (autoScaleString.equals(NO)) {
            autoScaleSpinner.setSelection(INDEX_AUTO_SCALE_NO);
        } else if (autoScaleString.equals(ATTEMPTED)) {
            autoScaleSpinner.setSelection(INDEX_AUTO_SCALE_ATT);
        } else {
            Log.e("LoadFromDb", "Invalid autoScaleString:" + autoScaleString);
        }

        if (teleParkString.equals(YES)) {
            teleParkSpinner.setSelection(INDEX_TELE_PARK_YES);
        } else if (teleParkString.equals(NO)) {
            teleParkSpinner.setSelection(INDEX_TELE_PARK_NO);
        } else if (teleParkString.equals(ATTEMPTED)) {
            teleParkSpinner.setSelection(INDEX_TELE_PARK_ATT);
        } else {
            Log.e("LoadFromDb", "Invalid teleParkString:" + teleParkString);
        }

        if (teleClimbString.equals(YES)) {
            teleClimbSpinner.setSelection(INDEX_TELE_CLIMB_YES);
        } else if (teleClimbString.equals(NO)) {
            teleClimbSpinner.setSelection(INDEX_TELE_CLIMB_NO);
        } else if (teleClimbString.equals(ATTEMPTED)) {
            teleClimbSpinner.setSelection(INDEX_TELE_CLIMB_ATT);
        } else {
            Log.e("LoadFromDb", "Invalid teleClimbString:" + teleClimbString);
        }

        if (teleCubeAbilityString.equals(CUBE_ABILITY_NONE)) {
            teleCubeAbilitySpinner.setSelection(INDEX_CUBE_ABILITY_NONE);
        } else if (teleCubeAbilityString.equals(CUBE_ABILITY_FLOOR)) {
            teleCubeAbilitySpinner.setSelection(INDEX_CUBE_ABILITY_FLOOR);
        } else if (teleCubeAbilityString.equals(CUBE_ABILITY_PORTAL)) {
            teleCubeAbilitySpinner.setSelection(INDEX_CUBE_ABILITY_PORTAL);
        } else if (teleCubeAbilityString.equals(CUBE_ABILITY_BOTH)) {
            teleCubeAbilitySpinner.setSelection(INDEX_CUBE_ABILITY_BOTH);
        } else {
            Log.e("LoadFromDb", "Invalid teleCubeAbilityString:" + teleCubeAbilityString);
        }

        return true;
    }

    public boolean saveToDb() {
        if (teamNumEditText.getText().toString().length() == 0 ||
                matchNumEditText.getText().toString().length() == 0) {
            return false;
        }

        String autoBaselineString = "";
        switch (autoBaselineSpinner.getSelectedItemPosition()) {
            case INDEX_AUTO_LINE_YES:
                autoBaselineString = YES;
                break;
            case INDEX_AUTO_LINE_ATT:
                autoBaselineString = ATTEMPTED;
                break;
            case INDEX_AUTO_LINE_NO:
                // Fall through
            default:
                autoBaselineString = NO;
        }

        String autoSwitchString = "";
        switch (autoSwitchSpinner.getSelectedItemPosition()) {
            case INDEX_AUTO_SWITCH_YES:
                autoSwitchString = YES;
                break;
            case INDEX_AUTO_SWITCH_ATT:
                autoSwitchString = ATTEMPTED;
                break;
            case INDEX_AUTO_SWITCH_NO:
                // Fall through
            default:
                autoSwitchString = NO;
        }

        String autoScaleString = "";
        switch (autoScaleSpinner.getSelectedItemPosition()) {
            case INDEX_AUTO_SCALE_YES:
                autoScaleString = YES;
                break;
            case INDEX_AUTO_SCALE_ATT:
                autoScaleString = ATTEMPTED;
                break;
            case INDEX_AUTO_SCALE_NO:
                // Fall through
            default:
                autoScaleString = NO;
        }

        String teleCubeAbilityString = "";
        switch (teleCubeAbilitySpinner.getSelectedItemPosition()) {
            case INDEX_CUBE_ABILITY_FLOOR:
                teleCubeAbilityString = CUBE_ABILITY_FLOOR;
                break;
            case INDEX_CUBE_ABILITY_PORTAL:
                teleCubeAbilityString = CUBE_ABILITY_PORTAL;
                break;
            case INDEX_CUBE_ABILITY_BOTH:
                teleCubeAbilityString = CUBE_ABILITY_BOTH;
                break;
            case INDEX_CUBE_ABILITY_NONE:
                // Fall through
            default:
                teleCubeAbilityString = CUBE_ABILITY_NONE;
        }

        String teleClimbString = "";
        switch (teleClimbSpinner.getSelectedItemPosition()) {
            case INDEX_TELE_CLIMB_YES:
                teleClimbString = YES;
                break;
            case INDEX_TELE_CLIMB_ATT:
                teleClimbString = ATTEMPTED;
                break;
            case INDEX_TELE_CLIMB_NO:
                // Fall through
            default:
                teleClimbString = NO;
        }

        String teleParkString = "";
        switch (teleParkSpinner.getSelectedItemPosition()) {
            case INDEX_TELE_PARK_YES:
                teleParkString = YES;
                break;
            case INDEX_TELE_PARK_ATT:
                teleParkString = ATTEMPTED;
                break;
            case INDEX_TELE_PARK_NO:
                // Fall through
            default:
                teleParkString = NO;
        }

        String comments = commentEditText.getText().toString();

        int teamNum = Integer.parseInt(teamNumEditText.getText().toString());
        int matchNum = Integer.parseInt(matchNumEditText.getText().toString());
        String alliance = teamColorButton.isChecked() ? "Blue" : "Red";
        if (matchTime == 0) {
            matchTime = (int) (Calendar.getInstance().getTimeInMillis() % Integer.MAX_VALUE);
            matchesDB.insertMatch(matchTime, teamNum, alliance, matchNum, autoBaselineString,
                                  autoSwitchString, autoScaleString, teleSwitchCount, teleScaleCount,
                                  teleOppSwitchCount, teleExchangeCount, teleCubeAbilityString,
                                  teleParkString, teleClimbString, comments);
        } else {
            matchesDB.updateMatch(matchTime, teamNum, alliance, matchNum, autoBaselineString,
                                  autoSwitchString, autoScaleString, teleSwitchCount, teleScaleCount,
                                  teleOppSwitchCount, teleExchangeCount, teleCubeAbilityString,
                                  teleParkString, teleClimbString, comments);
        }

        return true;
    }

    public void colorClick(View view) {
        if (teamColorButton.isChecked()) {
            teamColorButton.setBackgroundColor(ContextCompat.getColor(this, R.color.blue));
            teleSwitchIncButton.setText("Blue Switch");
            teleOppSwitchIncButton.setText("Red Switch");
        } else {
            teamColorButton.setBackgroundColor(ContextCompat.getColor(this, R.color.red));
            teleSwitchIncButton.setText("Red Switch");
            teleOppSwitchIncButton.setText("Blue Switch");
        }
    }

    public void teleRedSwitchIncClick(View view) {
        teleSwitchCount++;
        teleSwitchDecButton.setText(String.valueOf(teleSwitchCount));
    }

    public void teleRedSwitchDecClick(View view) {
        if (teleSwitchCount > 0) {
            teleSwitchCount--;
            teleSwitchDecButton.setText(String.valueOf(teleSwitchCount));
        }
    }

    public void teleBlueSwitchIncClick(View view) {
        teleOppSwitchCount++;
        teleOppSwitchDecButton.setText(String.valueOf(teleOppSwitchCount));
    }

    public void teleBlueSwitchDecClick(View view) {
        if (teleOppSwitchCount > 0) {
            teleOppSwitchCount--;
            teleOppSwitchDecButton.setText(String.valueOf(teleOppSwitchCount));
        }
    }

    public void teleScaleIncClick(View view) {
        teleScaleCount++;
        teleScaleDecButton.setText(String.valueOf(teleScaleCount));
    }

    public void teleScaleDecClick(View view) {
        if (teleScaleCount > 0) {
            teleScaleCount--;
            teleScaleDecButton.setText(String.valueOf(teleScaleCount));
        }
    }

    public void teleExchangeIncClick(View view) {
        teleExchangeCount++;
        teleExchangeDecButton.setText(String.valueOf(teleExchangeCount));
    }

    public void teleExchangeDecClick(View view) {
        if (teleExchangeCount > 0) {
            teleExchangeCount--;
            teleExchangeDecButton.setText(String.valueOf(teleExchangeCount));
        }
    }

}
