package com.example.pm1e1293512;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class ContactList extends AppCompatActivity {

    private ContactDbHelper dbHelper;
    private ListView listView;
    private SearchView searchView;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> contactList;
    private int selectedPosition = -1; // Variable para mantener la posición del contacto seleccionado

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_contact_list);

        dbHelper = new ContactDbHelper(this);
        listView = findViewById(R.id.listView);
        searchView = findViewById(R.id.searchView);

        loadContacts();

        // Configurar listener para el ListView
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Actualizar la posición seleccionada y resaltar visualmente
                selectedPosition = position;
                listView.setItemChecked(position, true);
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.getFilter().filter(newText);
                return false;
            }
        });
    }

    private void loadContacts() {
        contactList = new ArrayList<>();
        Cursor cursor = dbHelper.getAllContacts();

        if (cursor.moveToFirst()) {
            do {
                @SuppressLint("Range") String name = cursor.getString(cursor.getColumnIndex(ContactContract.ContactEntry.COLUMN_NAME_NAME));
                @SuppressLint("Range") String phone = cursor.getString(cursor.getColumnIndex(ContactContract.ContactEntry.COLUMN_NAME_PHONE));
                String contact = name + " " + phone;
                contactList.add(contact);
            } while (cursor.moveToNext());
        }

        cursor.close();

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_activated_1, contactList);
        listView.setAdapter(adapter);
    }

    public void openMainWindow(View view) {
        startActivity(new Intent(this, MainActivity.class));
    }

    // Método para actualizar contacto (llamado desde el botón)
    public void updateContact(View view) {
        if (selectedPosition != -1) {
            String selectedContact = adapter.getItem(selectedPosition);
            if (selectedContact != null) {
                showUpdateDialog(selectedContact);
            }
        } else {
            Toast.makeText(this, "Seleccione un contacto para actualizar", Toast.LENGTH_SHORT).show();
        }
    }

    // Método para eliminar contacto (llamado desde el botón)
    public void deleteContact(View view) {
        if (selectedPosition != -1) {
            String selectedContact = adapter.getItem(selectedPosition);
            if (selectedContact != null) {
                showDeleteDialog(selectedContact);
            }
        } else {
            Toast.makeText(this, "Seleccione un contacto para eliminar", Toast.LENGTH_SHORT).show();
        }
    }

    // Método para mostrar un diálogo de confirmación de eliminación
    private void showDeleteDialog(final String contact) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Eliminar contacto")
                .setMessage("¿Desea eliminar el contacto: " + contact + "?")
                .setPositiveButton("Sí", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteSelectedContact();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

    // Método para eliminar el contacto seleccionado de la base de datos y actualizar la lista
    private void deleteSelectedContact() {
        String selectedContact = adapter.getItem(selectedPosition);
        if (selectedContact != null) {
            // Separar el nombre y el teléfono del contacto seleccionado
            String[] parts = selectedContact.split(" ");
            String name = parts[0]; // Suponiendo que el nombre es la primera parte
            String phone = parts[1]; // Suponiendo que el teléfono es la segunda parte

            // Aquí deberías implementar la lógica para eliminar el contacto de la base de datos
            dbHelper.deleteContact(name, phone);

            // Actualizar la lista de contactos
            loadContacts();

            // Notificar al usuario que el contacto ha sido eliminado
            Toast.makeText(this, "Contacto eliminado: " + selectedContact, Toast.LENGTH_SHORT).show();

            // Limpiar la selección actual
            selectedPosition = -1;
        }
    }

    // Método para mostrar un diálogo de actualización (opcional)
    private void showUpdateDialog(final String contact) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Actualizar contacto")
                .setMessage("¿Desea actualizar el contacto: " + contact + "?")
                .setPositiveButton("Sí", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        updateSelectedContact(contact);
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

    // Método para actualizar el contacto seleccionado de la base de datos y actualizar la lista
    private void updateSelectedContact(String contact) {
        // Aquí deberías implementar la lógica para actualizar el contacto en la base de datos
        // Puedes abrir una nueva actividad para la actualización o modificar directamente aquí
        Toast.makeText(this, "Actualizar contacto: " + contact, Toast.LENGTH_SHORT).show();
        // Ejemplo: abrir una nueva actividad para la actualización
        // Intent intent = new Intent(this, UpdateContactActivity.class);
        // intent.putExtra("contact", contact);
        // startActivity(intent);
    }
}
