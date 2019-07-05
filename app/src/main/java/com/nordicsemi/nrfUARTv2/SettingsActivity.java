package com.nordicsemi.nrfUARTv2;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.internal.widget.AdapterViewCompat;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;


public class SettingsActivity extends ActionBarActivity {

    private SharedPreferences sharedSettings;

    private final String SOUND="sound";
    private final String CURRENT_DOOR="current_door";
    private final String DOOR1_ADDRESS="door1_address";
    private final String DOOR2_ADDRESS="door2_address";
    private final String DOOR3_ADDRESS="door3_address";
    private final String DOOR1_PWD="door1_pwd";
    private final String DOOR2_PWD="door2_pwd";
    private final String DOOR3_PWD="door3_pwd";
    private final String DOOR1="door1";
    private final String DOOR2="door2";
    private final String DOOR3="door3";

    private int selectedDoor = 0;
    private EditText passwordBox;
    private EditText addressBox;
    private Button setSettingsBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        passwordBox = (EditText) findViewById(R.id.passwordBox);
        addressBox = (EditText) findViewById(R.id.addressBox);
        setSettingsBtn = (Button)findViewById(R.id.setSettings);
        Spinner spinner = (Spinner) findViewById(R.id.gateSelector);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.doors_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);

        sharedSettings = PreferenceManager.getDefaultSharedPreferences(this);
        Toast.makeText(this, sharedSettings.getString("gate1", ""), Toast.LENGTH_LONG).show();

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id)
            {
                //Toast.makeText(topThis, "derp", Toast.LENGTH_LONG).show();
                showToast(Integer.toString(position));

                //String da = readSharedPreferences(DOOR1_ADDRESS);
                //String dp = readSharedPreferences(DOOR1_PWD);

                selectedDoor = position;

                switch(position)
                {
                    case 0:
//                        if(readSharedPreferences(DOOR1_ADDRESS) != "")
//                        {
//                            addressBox.setText(readSharedPreferences(DOOR1_ADDRESS), TextView.BufferType.EDITABLE);
//                        }
//                        if(readSharedPreferences(DOOR1_PWD) != "")
//                        {
//                            passwordBox.setText(readSharedPreferences(DOOR1_PWD), TextView.BufferType.EDITABLE);
//                        }

                        addressBox.setText(readSharedPreferences(DOOR1_ADDRESS), TextView.BufferType.EDITABLE);
                        passwordBox.setText(readSharedPreferences(DOOR1_PWD), TextView.BufferType.EDITABLE);

                        break;
                    case 1:
//                        if(readSharedPreferences(DOOR2_ADDRESS) != "")
//                        {
//                            addressBox.setText(readSharedPreferences(DOOR2_ADDRESS), TextView.BufferType.EDITABLE);
//                        }
//                        if(readSharedPreferences(DOOR2_PWD) != "")
//                        {
//                            passwordBox.setText(readSharedPreferences(DOOR2_PWD), TextView.BufferType.EDITABLE);
//                        }
                        addressBox.setText(readSharedPreferences(DOOR2_ADDRESS), TextView.BufferType.EDITABLE);
                        passwordBox.setText(readSharedPreferences(DOOR2_PWD), TextView.BufferType.EDITABLE);
                        break;
                    case 2:
//                        if(readSharedPreferences(DOOR3_ADDRESS) != "")
//                        {
//                            addressBox.setText(readSharedPreferences(DOOR3_ADDRESS), TextView.BufferType.EDITABLE);
//                        }
//                        if(readSharedPreferences(DOOR3_PWD) != "")
//                        {
//                            passwordBox.setText(readSharedPreferences(DOOR3_PWD), TextView.BufferType.EDITABLE);
//                        }

                        addressBox.setText(readSharedPreferences(DOOR3_ADDRESS), TextView.BufferType.EDITABLE);
                        passwordBox.setText(readSharedPreferences(DOOR3_PWD), TextView.BufferType.EDITABLE);
                        break;
                    default:
                        break;
                }
                //passwordBox.setText(Integer.toString(position), TextView.BufferType.EDITABLE);
                //addressBox.setText(Integer.toString(position), TextView.BufferType.EDITABLE);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView)
            {
                //Toast.makeText(topThis, "herf", Toast.LENGTH_LONG).show();
            }
        });


        setSettingsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String strAddress = addressBox.getText().toString();
                String strPwd = passwordBox.getText().toString();

                if(strAddress != "" || strPwd != "" )
                {
                    showToast("Device address or password cannot be null");

                }
                switch(selectedDoor)
                {
                    case 0:
                        if(strAddress != "")
                        {
                            writeSharedPreferences(DOOR1_ADDRESS,strAddress);
                        }
                        if(strPwd != "")
                        {
                            writeSharedPreferences(DOOR1_PWD,strPwd);
                        }
                        break;
                    case 1:
                        if(strAddress != "")
                        {
                            writeSharedPreferences(DOOR2_ADDRESS,strAddress);
                        }
                        if(strPwd != "")
                        {
                            writeSharedPreferences(DOOR2_PWD,strPwd);
                        }
                        break;
                    case 2:
                        if(strAddress != "")
                        {
                            writeSharedPreferences(DOOR3_ADDRESS,strAddress);
                        }
                        if(strPwd != "")
                        {
                            writeSharedPreferences(DOOR3_PWD,strPwd);
                        }
                        break;
                    default:
                        break;
                }

            }
        });





    }

    public void showToast(String s)
    {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }
    private void writeSharedPreferences(String key, String value) {

        SharedPreferences.Editor editor = sharedSettings.edit();
        editor.putString(key, value);
        editor.commit();

    }

    private String readSharedPreferences(String key) {

        return sharedSettings.getString(key, "");

    }
}
