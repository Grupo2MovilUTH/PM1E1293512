package com.example.pm1e1293512;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.pm1e1293512.ContactDbHelper;
import com.example.pm1e1293512.ContactList;
import com.example.pm1e1293512.R;

import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private Spinner spinner;
    private ArrayAdapter<String> spinnerAdapter;
    private ArrayList<String> countries;

    private ImageView imageViewSelected;
    private Uri selectedImageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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

        imageViewSelected = findViewById(R.id.imageViewSelected);

        Button buttonSelectPhoto = findViewById(R.id.buttonSelectPhoto);
        buttonSelectPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectPhoto();
            }
        });
    }

    private void openAddCountryDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Agregue un país");

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
        } else if (!phone.contains("+504")) {
            phone = "+504" + phone;
        }

        if (note.isEmpty()) {
            Toast.makeText(this, "Debe escribir una nota", Toast.LENGTH_SHORT).show();
            return;
        }

        // Save to database
        ContactDbHelper dbHelper = new ContactDbHelper(this);
        long contactId = dbHelper.addContact(name, phone, note, selectedImageUri); // Aquí pasas también la URI de la imagen

        if (contactId != -1) {
            Toast.makeText(this, "Guardado exitosamente", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Error al guardar el contacto", Toast.LENGTH_SHORT).show();
        }
    }

    public void openSaved(View view) {
        startActivity(new Intent(this, ContactList.class));
    }

    private void selectPhoto() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        resultLauncher.launch(intent);
    }

    private final ActivityResultLauncher<Intent> resultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri selectedImage = result.getData().getData();
                        try {
                            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImage);
                            imageViewSelected.setImageBitmap(bitmap);
                            selectedImageUri = selectedImage; // Guardar la URI seleccionada para usarla al guardar el contacto
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
}
