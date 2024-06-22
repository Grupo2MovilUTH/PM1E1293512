package com.example.pm1e1293512;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.widget.Button;
import android.content.Intent;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private Spinner spinner;
    private ArrayAdapter<String> spinnerAdapter;
    private ArrayList<String> countries;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);



        countries = new ArrayList<>();
        countries.add("USA"); // Initial country
        // Add more initial countries if needed

        spinner = findViewById(R.id.spinner);
        spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, countries);
        spinner.setAdapter(spinnerAdapter);

        Button buttonAdd = findViewById(R.id.buttonAdd);
        buttonAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openAddCountryDialog();
            }
        });

        Button buttonSave = findViewById(R.id.button);
        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveContact();
            }
        });
    }

    private void openAddCountryDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Agregue un pais");

        final EditText input = new EditText(this);
        input.setInputType(android.text.InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String newCountry = input.getText().toString();
                if (!newCountry.isEmpty()) {
                    countries.add(newCountry);
                    spinnerAdapter.notifyDataSetChanged();
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    private void saveContact() {
        EditText nameField = findViewById(R.id.editTextTextPersonName);
        EditText phoneField = findViewById(R.id.editTextPhone);
        EditText noteField = findViewById(R.id.editTextTextMultiLine);

        String name = nameField.getText().toString().trim();
        String phone = phoneField.getText().toString().trim();
        String note = noteField.getText().toString().trim();

        if (name.isEmpty()) {
            Toast.makeText(this, "Debe escribir un nombre", Toast.LENGTH_SHORT).show();
            return;
        }

        if (phone.isEmpty()) {
            Toast.makeText(this, "Debe escribir un teléfono", Toast.LENGTH_SHORT).show();
            return;
        }

        if (note.isEmpty()) {
            Toast.makeText(this, "Debe escribir una nota", Toast.LENGTH_SHORT).show();
            return;
        }

        // Save to database
        ContactDbHelper dbHelper = new ContactDbHelper(this);
        dbHelper.addContact(name, phone, note); // Aquí pasas los valores individuales

        Toast.makeText(this, "Guardado exitosamente", Toast.LENGTH_SHORT).show();
    }

    public void openSaved(View view) {
        startActivity(new Intent(this, ContactList.class));
    }
}
