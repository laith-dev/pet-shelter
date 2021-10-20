package com.example.android.petsshelter.ui;

import static com.example.android.petsshelter.data.PetContract.PetEntry;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;

import com.example.android.petsshelter.R;

/**
 * Allows user to create a new pet or edit an existing one.
 */
public class EditorActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = EditorActivity.class.getSimpleName();

    private static final int PET_LOADER_ID = 1;

    /**
     * EditText field to enter the pet's name
     */
    private EditText petNameET;

    /**
     * EditText field to enter the pet's breed
     */
    private EditText petBreedET;

    /**
     * EditText field to enter the pet's weight
     */
    private EditText petWeightET;

    /**
     * EditText field to enter the pet's gender
     */
    private Spinner petGenderSpinner;

    /**
     * Gender of the pet. The possible values are:
     * 0 for unknown gender, 1 for male, 2 for female.
     */
    private int petGender = 0;

    private Uri petToEditUri;

    private boolean petHasChanged = false;

    private View.OnTouchListener onTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            v.performClick();
            petHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        petNameET = findViewById(R.id.edit_pet_name);
        petNameET.setOnTouchListener(onTouchListener);

        petBreedET = findViewById(R.id.edit_pet_breed);
        petBreedET.setOnTouchListener(onTouchListener);

        petWeightET = findViewById(R.id.edit_pet_weight);
        petWeightET.setOnTouchListener(onTouchListener);

        petGenderSpinner = findViewById(R.id.spinner_gender);
        petGenderSpinner.setOnTouchListener(onTouchListener);

        setupSpinner();

        petToEditUri = getIntent().getData();
        // Check if the activity was started to edit a pet (edit mode).
        if (petToEditUri != null) {
            setTitle(getString(R.string.editor_activity_title_edit_pet));
            getSupportLoaderManager().initLoader(PET_LOADER_ID, null, this);
        } else {
            setTitle(getString(R.string.editor_activity_title_new_pet));
            invalidateOptionsMenu();
        }
    }

    /**
     * Setup the dropdown spinner that allows the user to select the gender of the pet.
     */
    private void setupSpinner() {
        /* Create an adapter for spinner. The list options are from the String array it will use.
         * The spinner will use the default layout. */
        ArrayAdapter genderSpinnerAdapter = ArrayAdapter.createFromResource(
                this,
                R.array.array_gender_options,
                android.R.layout.simple_spinner_item);

        // Specify dropdown layout style - simple list view with 1 item per line
        genderSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

        // Apply the adapter to the spinner
        petGenderSpinner.setAdapter(genderSpinnerAdapter);

        // Set the integer mSelected to the constant values
        petGenderSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                if (!TextUtils.isEmpty(selection)) {
                    if (selection.equals(getString(R.string.gender_male))) {
                        petGender = PetEntry.GENDER_MALE;
                    } else if (selection.equals(getString(R.string.gender_female))) {
                        petGender = PetEntry.GENDER_FEMALE;
                    } else {
                        petGender = PetEntry.GENDER_UNKNOWN;
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                petGender = PetEntry.GENDER_MALE;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        // Hide delete menu item when adding a new pet.
        if (petToEditUri == null) {
            MenuItem deleteItm = menu.findItem(R.id.action_delete);
            deleteItm.setVisible(false);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_save) {
            applyChanges();
            return true;
        } else if (id == R.id.action_delete) {
            showDeleteConfirmationDialog();
            return true;
        } else if (id == android.R.id.home) {
            handleHomeButton();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void updateUI(Cursor data) {
        String petName = "";
        String petBreed = "";
        int petWeight = 0;
        petGender = 0;

        /* Edit mode, fill the clicked pet's info into the text fields. */
        if (data != null) {
            if (data.moveToNext()) {
                petName = data.getString(
                        data.getColumnIndex(PetEntry.COL_PET_NAME));
                petBreed = data.getString(
                        data.getColumnIndex(PetEntry.COL_PET_BREED));
                petWeight = data.getInt(
                        data.getColumnIndex(PetEntry.COL_PET_WEIGHT));
                petGender = data.getInt(
                        data.getColumnIndex(PetEntry.COL_PET_GENDER));
            }
        }

        petNameET.setText(petName);
        petBreedET.setText(petBreed);

        if (petWeight != -1) {
            petWeightET.setText(String.valueOf(petWeight));
        } else {
            petWeightET.setText("");
        }

        petGenderSpinner.setSelection(petGender);
    }

    /**
     * Insert a new pet or update an existing one.
     */
    private void applyChanges() {
        String name = petNameET.getText().toString().trim();
        String breed = petBreedET.getText().toString().trim();
        String weightStr = petWeightET.getText().toString().trim();

        if (name.isEmpty()) {
            Toast.makeText(
                    this,
                    R.string.pet_name_required,
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        // The weight field can be empty, the default value is 0.
        int weight = 0;
        if (!weightStr.isEmpty()) {
            weight = Integer.parseInt(weightStr);
        }

        ContentValues values = new ContentValues();
        values.put(PetEntry.COL_PET_NAME, name);
        values.put(PetEntry.COL_PET_BREED, breed);
        values.put(PetEntry.COL_PET_GENDER, petGender);
        values.put(PetEntry.COL_PET_WEIGHT, weight);

        /* Edit mode */
        if (petToEditUri != null) {
            int rowsUpdated = getContentResolver().update(
                    petToEditUri, values, null, null);

            if (rowsUpdated > 0) {
                Toast.makeText(
                        this,
                        getString(R.string.pet_updated),
                        Toast.LENGTH_SHORT
                ).show();

                this.finish();
            } else {
                Toast.makeText(
                        this,
                        getString(R.string.error_updating_pet),
                        Toast.LENGTH_SHORT
                ).show();
            }
            /* Insert mode */
        } else {
            Uri newRowUri = getContentResolver().insert(PetEntry.CONTENT_URI, values);
            if (newRowUri != null) {
                Toast.makeText(
                        this,
                        getString(R.string.pet_saved),
                        Toast.LENGTH_SHORT
                ).show();

                this.finish();
            } else {
                Toast.makeText(
                        this,
                        getString(R.string.pet_not_saved),
                        Toast.LENGTH_SHORT
                ).show();
            }
        }
    }

    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_pet_dialog_msg);
        builder.setPositiveButton(
                R.string.delete,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deletePet();
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

        builder.show();
        /* AlertDialog alertDialog = builder.create();
        * alertDialog.show(); */
    }

    private void deletePet() {
        if (petToEditUri == null) {
            Log.e(TAG, "deletePet: this method should be called in edit mode only");
            return;
        }

        int rowsDeleted = getContentResolver().delete(
                petToEditUri, null, null);

        if (rowsDeleted > 0) {
            Toast.makeText(
                    this,
                    R.string.pet_deleted,
                    Toast.LENGTH_SHORT
            ).show();

            this.finish();
        } else {
            Toast.makeText(
                    this,
                    R.string.error_deleting_pet,
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    private void handleHomeButton() {
        if (!petHasChanged) {
            NavUtils.navigateUpFromSameTask(this);
        } else {
            DialogInterface.OnClickListener discardButtonClickListener =
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            NavUtils.navigateUpFromSameTask(EditorActivity.this);
                        }
                    };

            showUnsavedChangesDialog(discardButtonClickListener);
        }
    }

    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(
                R.string.keep_editing, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (dialog != null) {
                            dialog.dismiss();
                        }
                    }
                });

        builder.show();
        /* AlertDialog dialog = builder.create();
        * dialog.show(); */
    }

    @Override
    public void onBackPressed() {
        if (!petHasChanged) {
            super.onBackPressed();
            return;
        }

        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                };

        showUnsavedChangesDialog(discardButtonClickListener);
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        String[] projection = {
                PetEntry.COL_ID,
                PetEntry.COL_PET_NAME,
                PetEntry.COL_PET_BREED,
                PetEntry.COL_PET_GENDER,
                PetEntry.COL_PET_WEIGHT
        };

        return new CursorLoader(
                this,
                petToEditUri,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        Log.i(TAG, "onLoadFinished: cursor -> " + data.getColumnCount());
        updateUI(data);
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        updateUI(null);
    }
}