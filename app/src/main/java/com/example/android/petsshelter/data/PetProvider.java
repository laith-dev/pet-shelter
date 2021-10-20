package com.example.android.petsshelter.data;

import static com.example.android.petsshelter.data.PetContract.PetEntry;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * {@link ContentProvider} for Pets app.
 */
public class PetProvider extends ContentProvider {

    private static final String LOG_TAG = PetProvider.class.getSimpleName();

    // Codes/Paths for each URI pattern case.
    private static final int PETS = 100;    // performing an operation on the whole pets table
    private static final int PET_ID = 101;  // performing an operation on a single row by ID

    private static final UriMatcher uriMatcher =
            new UriMatcher(UriMatcher.NO_MATCH);

    // Static initialization block
    static {
        // Uri for performing operation on the whole petsshelter table.
        uriMatcher.addURI(
                PetContract.CONTENT_AUTHORITY,
                PetContract.PATH_PETS,
                PETS);

        /* Uri for performing operation on a single row in the petsshelter table
         * based on the given ID. */
        uriMatcher.addURI(
                PetContract.CONTENT_AUTHORITY,
                PetContract.PATH_PETS + "/#",
                PET_ID);
    }

    private PetDbHelper petDbHelper;

    /**
     * Initialize the provider and the database helper object.
     */
    @Override
    public boolean onCreate() {
        petDbHelper = new PetDbHelper(getContext());
        return true;
    }

    /**
     * Perform the query for the given URI. Use the given projection, selection,
     * selection arguments, and sort order.
     */
    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection,
                        @Nullable String selection, @Nullable String[] selectionArgs,
                        @Nullable String sortOrder) {
        Cursor cursor;
        final int match = uriMatcher.match(uri);
        switch (match) {
            case PETS:
                cursor = queryPets(
                        projection, selection,
                        selectionArgs, sortOrder);
                break;

            case PET_ID:
                selection = PetEntry.COL_ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};

                cursor = queryPets(
                        projection, selection,
                        selectionArgs, null);
                break;

            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }

        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    private Cursor queryPets(String[] projection, String selection,
                             String[] selectionArgs, String sortOrder) {

        SQLiteDatabase db = petDbHelper.getReadableDatabase();

        return db.query(
                PetEntry.TABLE_NAME, projection, selection,
                selectionArgs, null, null, sortOrder);
    }

    /**
     * Returns the MIME type of data for the content URI.
     */
    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        String type;

        int match = uriMatcher.match(uri);
        switch (match) {
            case PETS:
                type = PetEntry.CONTENT_LIST_TYPE;
                break;

            case PET_ID:
                type = PetEntry.CONTENT_ITEM_TYPE;
                break;

            default:
                throw new IllegalArgumentException("Unknown URI " + uri.toString());
        }

        return type;
    }

    /**
     * Insert new data into the provider with the given ContentValues.
     */
    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @NonNull ContentValues values) {
        Uri newRowUri;

        final int match = uriMatcher.match(uri);
        switch (match) {
            case PETS:
                newRowUri = insertPet(uri, values);
                break;

            default:
                throw new IllegalArgumentException("No match found in this uri " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);

        return newRowUri;
    }

    /**
     * Insert a pet into the database with the given content values. Return the new content URI
     * for that specific row in the database.
     */
    private Uri insertPet(Uri uri, ContentValues values) {
        /* Self note: why don't we add these checks in the insert() directly, since it can branch
         * to other branches other than this method. */

        String name = values.getAsString(PetEntry.COL_PET_NAME);
        if (name == null) {
            throw new IllegalArgumentException("Pet requires a name!");
        }

        // No need to check the breed, any value is valid (including null (empty string)).

        Integer gender = values.getAsInteger(PetEntry.COL_PET_GENDER);
        if (gender == null || !PetEntry.isValidGender(gender)) {
            throw new IllegalArgumentException(
                    "Pet gender can be only: 0 for unknown, 1 for male, 2 for female");
        }

        /* The weight can be null, if so, the database will default it to 0. */
        Integer weight = values.getAsInteger(PetEntry.COL_PET_WEIGHT);
        // Negative weights are not accepted.
        if (weight != null && weight < 0) {
            throw new IllegalArgumentException("Weight must be > 0");
        }

        SQLiteDatabase db = petDbHelper.getWritableDatabase();
        long newRowId = db.insert(PetEntry.TABLE_NAME, null, values);
        if (newRowId == -1) {
            Log.e(LOG_TAG, "insertPet: Failed to insert row for " + uri);
            return null;
        }

        return ContentUris.withAppendedId(uri, newRowId);
    }

    /**
     * Delete row/s at the given selection and selection arguments.
     */
    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection,
                      @Nullable String[] selectionArgs) {
        int rowsDeleted;

        int match = uriMatcher.match(uri);
        switch (match) {
            // Delete all rows that match the selection and selection args.
            case PETS:
                rowsDeleted = deleteFromPets(selection, selectionArgs);
                break;

            case PET_ID:
                // Delete a single row given by the ID.
                selection = PetEntry.COL_ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};

                rowsDeleted = deleteFromPets(selection, selectionArgs);
                break;

            default:
                throw new IllegalArgumentException("Unsupported URI " + uri);
        }

        if (rowsDeleted > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowsDeleted;
    }

    private int deleteFromPets(String selection, String[] selectionArgs) {
        SQLiteDatabase db = petDbHelper.getWritableDatabase();

        return db.delete(PetEntry.TABLE_NAME, selection, selectionArgs);
    }

    /**
     * Updates the data at the given selection and selection arguments, with the new ContentValues.
     */
    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values,
                      @Nullable String selection, @Nullable String[] selectionArgs) {
        int rowsUpdated;

        final int match = uriMatcher.match(uri);
        switch (match) {
            case PETS:
                rowsUpdated = updatePets(values, selection, selectionArgs);
                break;

            case PET_ID:
                selection = PetEntry.COL_ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};

                rowsUpdated = updatePets(values, selection, selectionArgs);
                break;

            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        if (rowsUpdated > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowsUpdated;
    }

    /**
     * Update petsshelter in the database with the given content values. Apply the changes to the rows
     * specified in the selection and selection arguments (which could be 0, 1 or more pets).
     *
     * @return the number of rows that were successfully updated.
     */
    private int updatePets(ContentValues values, String selection, String[] selectionArgs) {
        if (values == null || values.size() == 0) {
            return 0;
        }

        /* Since the update() doesn't require all the attributes/columns to be present, make sure
         * that each attribute exists in the ContentValues object before checking its value. */

        if (values.containsKey(PetEntry.COL_PET_NAME)) {
            String name = values.getAsString(PetEntry.COL_PET_NAME);
            if (name == null) {
                throw new IllegalArgumentException("Name cannot be null");
            }
        }

        // No need to check the breed, any value is valid (including null).

        if (values.containsKey(PetEntry.COL_PET_GENDER)) {
            Integer gender = values.getAsInteger(PetEntry.COL_PET_GENDER);
            if (gender == null || !PetEntry.isValidGender(gender)) {
                throw new IllegalArgumentException("Invalid gender " + gender);
            }
        }

        if (values.containsKey(PetEntry.COL_PET_WEIGHT)) {
            Integer weight = values.getAsInteger(PetEntry.COL_PET_WEIGHT);
            if (weight != null && weight < 0) {
                throw new IllegalArgumentException("Invalid weight " + weight);
            }
        }

        SQLiteDatabase db = petDbHelper.getWritableDatabase();
        return db.update(PetEntry.TABLE_NAME, values,
                selection, selectionArgs);
    }
}