package com.example.android.petsshelter.ui;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;

import com.example.android.petsshelter.R;
import com.example.android.petsshelter.adapters.PetCursorAdapter;
import com.example.android.petsshelter.data.PetContract.PetEntry;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

/**
 * Displays list of petsshelter that were entered and stored in the app.
 * This is the MainActivity.
 */
public class CatalogActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = CatalogActivity.class.getSimpleName();

    private static final int PET_LOADER_ID = 1;

    private ListView petsLV;

    private PetCursorAdapter petCursorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);

        // Setup FAB to open EditorActivity.
        FloatingActionButton insertPetFAB = findViewById(R.id.insert_new_pet_fab);
        insertPetFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });

        petsLV = findViewById(R.id.pets_lv);
        petsLV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent editorIntent = new Intent(
                        getApplicationContext(),
                        EditorActivity.class);

                Uri clickedPetUri = ContentUris.withAppendedId(
                        PetEntry.CONTENT_URI, id);
                editorIntent.setData(clickedPetUri);

                startActivity(editorIntent);
            }
        });

        petsLV.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                // TODO: show a popup menu to remove the long-clicked item.
                return true;
            }
        });

        petCursorAdapter = new PetCursorAdapter(this, null);
        petsLV.setAdapter(petCursorAdapter);

        View emptyView = findViewById(R.id.empty_view);
        petsLV.setEmptyView(emptyView);

        getSupportLoaderManager().initLoader(PET_LOADER_ID, null, this);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_catalog, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_insert_dummy_data) {
            insertDummyPetData();
            return true;
        } else if (id == R.id.action_delete_all_entries) {
            showDeletePetsConfirmationDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void insertDummyPetData() {
        ContentValues values = new ContentValues();
        values.put(PetEntry.COL_PET_NAME, "Some Pet");
        values.put(PetEntry.COL_PET_BREED, "Some Breed");
        values.put(PetEntry.COL_PET_GENDER, PetEntry.GENDER_UNKNOWN);
        values.put(PetEntry.COL_PET_WEIGHT, 0);

        Uri insertionUri = getContentResolver().insert(PetEntry.CONTENT_URI, values);
        if (insertionUri == null) {
            Toast.makeText(
                    this,
                    getString(R.string.pet_not_saved),
                    Toast.LENGTH_SHORT
            ).show();
        } else {
            Toast.makeText(
                    this,
                    getString(R.string.pet_saved),
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    private void showDeletePetsConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_all_pets_dialog_msg);
        builder.setPositiveButton(
                R.string.confirm,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteAllPets();
                    }
                });
        builder.setNegativeButton(
                R.string.cancel,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (dialog != null) {
                            dialog.dismiss();
                        }
                    }
                });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void deleteAllPets() {
        int rowsDeleted = getContentResolver().delete(
                PetEntry.CONTENT_URI, null, null);

        Toast.makeText(
                this,
                rowsDeleted + " pets deleted",
                Toast.LENGTH_SHORT
        ).show();
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        Log.i(TAG, "onCreateLoader: creating the loader");

        String[] projection = {PetEntry.COL_ID, PetEntry.COL_PET_NAME, PetEntry.COL_PET_BREED};
        return new CursorLoader(
                this,
                PetEntry.CONTENT_URI,
                projection,
                null,
                null,
                null
        );
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        Log.i(TAG, "onLoadFinished: loading finished");

        petCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        // Callback called when the data becomes invalid.
        petCursorAdapter.swapCursor(null);
    }
}
