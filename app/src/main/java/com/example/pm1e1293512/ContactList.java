package com.example.pm1e1293512;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.i18n.phonenumbers.PhoneNumberMatch;
import com.google.i18n.phonenumbers.PhoneNumberUtil;

import java.util.ArrayList;
import java.util.Iterator;

public class ContactList extends AppCompatActivity {

    private ContactDbHelper dbHelper;
    private ListView listView;
    private SearchView searchView;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> contactList;
    private int selectedPosition = -1;
    private static final long DOUBLE_CLICK_TIME_DELTA = 500;
    private long lastClickTime = 0;

    // para verificación de permisos de llamada
    private static final String CALL_PERMISSION = Manifest.permission.CALL_PHONE;
    private static final int PERMISSION_REQ_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_list);

        dbHelper = new ContactDbHelper(this);
        listView = findViewById(R.id.listView);
        searchView = findViewById(R.id.searchView);

        loadContacts();

        // Configurar el ListView para manejar clics en los contactos
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                long clickTime = System.currentTimeMillis();

                if (clickTime - lastClickTime < DOUBLE_CLICK_TIME_DELTA) {
                    // Doble clic detectado
                    String number = getPhoneNumber(position);
                    callContact(number);
                } else {
                    // Clic único detectado
                    selectedPosition = position;
                    listView.setItemChecked(position, true);
                }

                lastClickTime = clickTime;
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

        // Configurar el botón "Compartir"
        findViewById(R.id.button5).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedPosition != -1) {
                    String selectedContact = adapter.getItem(selectedPosition);
                    if (selectedContact != null) {
                        shareContact(selectedContact);
                    }
                } else {
                    Toast.makeText(ContactList.this, "Seleccione un contacto para compartir", Toast.LENGTH_SHORT).show();
                }
            }
        });



        // Solicitar permisos si no están concedidos
        if (!checkPermission()) {
            requestPermission();
        }
    }

    private boolean checkPermission() {
        return ActivityCompat.checkSelfPermission(this, CALL_PERMISSION) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, CALL_PERMISSION)) {
            // Explicar al usuario por qué se necesitan los permisos
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Esta aplicación requiere permisos para realizar llamadas.")
                    .setTitle("Permiso necesario")
                    .setCancelable(false)
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(ContactList.this, new String[]{CALL_PERMISSION}, PERMISSION_REQ_CODE);
                            dialog.dismiss();
                        }
                    }).setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });

            builder.show();
        } else {
            // Solicitar permisos directamente
            ActivityCompat.requestPermissions(this, new String[]{CALL_PERMISSION}, PERMISSION_REQ_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQ_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permiso para realizar llamadas concedido", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permiso para realizar llamadas denegado", Toast.LENGTH_SHORT).show();
                if (!ActivityCompat.shouldShowRequestPermissionRationale(this, CALL_PERMISSION)) {
                    // El usuario marcó "No volver a preguntar", dirigir a configuraciones de la aplicación
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setMessage("Para realizar llamadas, habilite los permisos desde los ajustes de la aplicación.")
                            .setTitle("Solicitud de permiso")
                            .setCancelable(false)
                            .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            })
                            .setPositiveButton("Configuraciones", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                                    intent.setData(uri);
                                    startActivity(intent);
                                }
                            });

                    builder.show();
                }
            }
        }
    }

    private String getPhoneNumber(int position) {
        String itemValue = (String) listView.getItemAtPosition(position);
        Iterator<PhoneNumberMatch> existsPhone = PhoneNumberUtil.getInstance()
                .findNumbers(itemValue, "IN").iterator();

        while (existsPhone.hasNext()) {
            String num = String.valueOf(existsPhone.next().number().getNationalNumber());
            return num;
        }
        Log.e("ERROR", "getPhoneNumber: No se encontró número de teléfono");
        return "+50412345678"; // número ficticio
    }

    private void callContact(String phone) {
        Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phone));
        startActivity(intent);
    }

    private void loadContacts() {
        contactList = new ArrayList<>();
        Cursor cursor = dbHelper.getAllContacts();

        if (cursor.moveToFirst()) {
            do {
                @SuppressLint("Range") String name = cursor.getString(cursor.getColumnIndex(ContactContract.ContactEntry.COLUMN_NAME_NAME));
                @SuppressLint("Range") String phoneNumber = cursor.getString(cursor.getColumnIndex(ContactContract.ContactEntry.COLUMN_NAME_PHONE));
                String contact = name + " " + phoneNumber;
                contactList.add(contact);
            } while (cursor.moveToNext());
        }

        cursor.close();

        if (adapter == null) {
            adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_activated_1, contactList);
            listView.setAdapter(adapter);
        } else {
            adapter.clear();
            adapter.addAll(contactList);
            adapter.notifyDataSetChanged();
        }
    }

    public void openMainWindow(View view) {
        startActivity(new Intent(this, MainActivity.class));
    }

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

    public void showContactImage(View view) {
        if (selectedPosition != -1) {
            String selectedContact = adapter.getItem(selectedPosition);
            if (selectedContact != null) {
                byte[] imageBytes = dbHelper.getContactImageBytes(selectedContact);
                if (imageBytes != null) {
                    Bitmap contactImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                    showImageDialog(contactImage);
                } else {
                    Toast.makeText(this, "Imagen no disponible para este contacto", Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            Toast.makeText(this, "Seleccione un contacto para ver la imagen", Toast.LENGTH_SHORT).show();
        }
    }

    private void showImageDialog(Bitmap imageBitmap) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_image_preview, null);
        ImageView imageView = dialogView.findViewById(R.id.imageViewPreview);

        if (imageBitmap != null) {
            imageView.setImageBitmap(imageBitmap);
        } else {
            imageView.setImageResource(R.drawable.default_image); // Imagen por defecto si no hay imagen disponible
        }

        builder.setView(dialogView)
                .setPositiveButton("Cerrar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }



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

    private void deleteSelectedContact() {
        String selectedContact = adapter.getItem(selectedPosition);
        if (selectedContact != null) {
            String[] parts = selectedContact.split(" ");
            String name = parts[0]; // Suponiendo que el nombre es la primera parte
            String phoneNumber = parts[1]; // Suponiendo que el teléfono es la segunda parte

            dbHelper.deleteContact(name, phoneNumber);

            contactList.remove(selectedPosition);
            adapter.notifyDataSetChanged();

            Toast.makeText(this, "Contacto eliminado: " + selectedContact, Toast.LENGTH_SHORT).show();

            // Limpiar la selección
            selectedPosition = -1;
        }
    }

    private void showUpdateDialog(final String contact) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Actualizar contacto")
                .setMessage("¿Desea actualizar el contacto: " + contact + "?")
                .setPositiveButton("Sí", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String[] parts = contact.split(" ");
                        String currentName = parts[0];
                        String currentPhoneNumber = parts[1];
                        showEditDialog(currentName, currentPhoneNumber);
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

    private void showEditDialog(final String currentName, final String currentPhoneNumber) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Editar contacto");

        View view = getLayoutInflater().inflate(R.layout.dialog_edit_contact, null);
        builder.setView(view);

        final EditText editTextName = view.findViewById(R.id.editTextName);
        final EditText editTextPhone = view.findViewById(R.id.editTextPhone);
        editTextName.setText(currentName);
        editTextPhone.setText(currentPhoneNumber);

        builder.setPositiveButton("Guardar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String newName = editTextName.getText().toString();
                String newPhoneNumber = editTextPhone.getText().toString();

                if (!newName.isEmpty() && !newPhoneNumber.isEmpty()) {
                    dbHelper.updateContact(currentName, currentPhoneNumber, newName, newPhoneNumber);
                    loadContacts();
                    Toast.makeText(ContactList.this, "Contacto actualizado", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ContactList.this, "Ingrese nombre y teléfono válidos", Toast.LENGTH_SHORT).show();
                }
            }
        });

        builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.show();
    }

    private void shareContact(String selectedContact) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, selectedContact);
        startActivity(Intent.createChooser(intent, "Compartir contacto con"));
    }

}

