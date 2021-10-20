package com.example.android.petsshelter.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Contract class for the petsshelter database.
 */
public final class PetContract {

    /**
     * A utility class cannot be instantiated.
     */
    private PetContract() {
    }

    /* This should be the same as the authorities property given to the corresponding provider
     * tag inside the manifest. */
    public static final String CONTENT_AUTHORITY = "com.example.android.petsshelter";

    /* Base Uri with the schema and authority.
     * Paths will be appended to the uri inside each class. */
    public static final Uri BASE_CONTENT_URI = Uri.parse(
            ContentResolver.SCHEME_CONTENT + "://" + CONTENT_AUTHORITY);

    /* Represents the petsshelter table in the database to be used as a path with the
     * base content uri. */
    public static final String PATH_PETS = "petsshelter";

    /**
     * Represents Pets table in the database.
     */
    public static final class PetEntry implements BaseColumns {

        // Content Provider Uri (content://com.example.android.petsshelter/petsshelter)
        public static final Uri CONTENT_URI =
                Uri.withAppendedPath(BASE_CONTENT_URI, PATH_PETS);

        // MIME type for list of petsshelter.
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PETS;

        // MIME type for a single pet.
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PETS;

        public static final String TABLE_NAME = "petsshelter";

        /* Constants represent columns in the Pets table. */
        public static final String COL_ID = BaseColumns._ID;
        public static final String COL_PET_NAME = "name";
        public static final String COL_PET_BREED = "breed";
        public static final String COL_PET_GENDER = "gender";
        public static final String COL_PET_WEIGHT = "weight";

        /*
         * Possible values for gender.
         * */
        public static final int GENDER_UNKNOWN = 0;
        public static final int GENDER_MALE = 1;
        public static final int GENDER_FEMALE = 2;

        /**
         * Returns whether or not the given gender is {@link #GENDER_UNKNOWN}, {@link #GENDER_MALE},
         * or {@link #GENDER_FEMALE}.
         */
        public static boolean isValidGender(int gender) {
            return gender == GENDER_UNKNOWN || gender == GENDER_MALE || gender == GENDER_FEMALE;
        }
    }
}