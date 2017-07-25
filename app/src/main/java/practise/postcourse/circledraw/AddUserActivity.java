package practise.postcourse.circledraw;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.UUID;

/**
 * Created by Jim on 12/01/2016.
 */
public class AddUserActivity extends AppCompatActivity {

    final static String TAG = "AddUserActivity";
    private MySQLiteHelper db;
    private EditText short_name;
    private EditText full_name;
    private ImageView thumbnail;
    private Spinner color;
    private EditText email;
    private Bitmap tempBitmap; // this is the last bitmap captured through this activity
    boolean isUpdating; // used to declare whether an existing user is being updated
    User user; // if the activity is editing an existing user, this is where the current user information will be contained
    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int REQUEST_IMAGE_SELECT = 2;

    private String[] colorHexList = { "#CC0099", "#CC0033", "#CC3300", "#FFC20A", "#CC9900", "#00CC99", "#99CC00", "#33CC00",
            "#4775FF", "#0099CC", "#0A47FF", "#0033CC", "#3300CC", "#9900CC" };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_user_main);
        // get database reference
        db = new MySQLiteHelper(this);

        //set action bar / toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        toolbar.setTitle("USER DETAILS");
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);
        setSupportActionBar(toolbar);

        // set short_name focus on start and show keyboard
        short_name = (EditText) findViewById(R.id.shortname);
        short_name.requestFocus();
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

        full_name = (EditText) findViewById(R.id.fullname);

        // get email reference
        email = (EditText) findViewById(R.id.email);

        // set up spinner and it's adapter to show colours
        color = (Spinner) findViewById(R.id.color);
        color.setAdapter(new ColorListAdapter(this, R.layout.color_spinner, colorHexList));

        // when spinner is touched, close the soft input keyboard (without consuming touch event)
        color.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                InputMethodManager imm = (InputMethodManager) getApplicationContext().getSystemService(Activity.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                return false;
            }
        });

        // use reflection to set height of spinner dropdown
        try {
            Field popup = Spinner.class.getDeclaredField("mPopup");
            popup.setAccessible(true);
            // Get private mPopup member variable and try cast to ListPopupWindow
            android.widget.ListPopupWindow popupWindow = (android.widget.ListPopupWindow) popup.get(color);

            // Set popupWindow height to 500px
            popupWindow.setHeight(500);
        }
        catch (NoClassDefFoundError | ClassCastException | NoSuchFieldException | IllegalAccessException e) {
            // silently fail...no harm no foul
        }

        // get handle for thumbnail
        thumbnail = (ImageView) findViewById(R.id.thumbnail);

        // get extras and if present - i.e. if amending existing user details - set current user information in activity
        Bundle extras = getIntent().getExtras();
        getAndSetUserDetails(extras);


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                super.onOptionsItemSelected(item);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_adduser, menu);
        return true;
    }

    private void getAndSetUserDetails(Bundle e) {
        if (e != null) {
            // declare that this instance of the activity is to update existing user
            isUpdating = true;
            // get current user information and set as global variable
            user = e.getParcelable("user_info");
            // set short name and full name to current information
            short_name.setText(user.getShortName());
            full_name.setText(user.getFullName());
            email.setText(user.getEmail());
            // set color spinner to current user colour
            int ringColor = user.getRingColor();
            for (int i = 0; i < colorHexList.length; i++) {
                if (Color.parseColor(colorHexList[i]) == ringColor) {
                    color.setSelection(i,false);
                    break;
                }
            }
            // try to locate and display image associated with this user
            File f = this.getFileStreamPath(user.getId() + ".jpg");
            RoundedBitmapDrawable bmp;
            if (f.exists()) {
                bmp = RoundedBitmapDrawableFactory.create
                        (this.getResources(), BitmapFactory.decodeFile(f.toString()));
            } else {
                // - otherwise display generic pic
                bmp = RoundedBitmapDrawableFactory.create
                        (this.getResources(), BitmapFactory.decodeResource(this.getResources(), R.drawable.default_profile));
            }
            bmp.setCircular(true);
            thumbnail.setImageDrawable(bmp);
        } else {
            // - otherwise display generic pic
            RoundedBitmapDrawable bmp = RoundedBitmapDrawableFactory.create
                    (this.getResources(), BitmapFactory.decodeResource(this.getResources(), R.drawable.default_profile));
            bmp.setCircular(true);
            thumbnail.setImageDrawable(bmp);
        }
    }

    public void confirmUser(View v) {
        User u;

        if (!isUpdating) { // i.e. if new user

            // generate random id for the new user
            String id = UUID.randomUUID().toString();

            // create new user entry in the SQL database
            u = new User(id,
                    Color.parseColor(colorHexList[color.getSelectedItemPosition()]),
                    short_name.getText().toString(),
                    full_name.getText().toString(),
                    email.getText().toString());
            db.createNewUser(u);

            // save picture thumbnail if a new one has been taken/selected
            if (tempBitmap != null) {
                saveToInternalStorage(id, tempBitmap);
            }
            // return to main activity
            finish();

        } else { // i.e. if existing user being updated

            // update existing user entry in the User database
            u = new User(user.getId(),
                    Color.parseColor(colorHexList[color.getSelectedItemPosition()]),
                    short_name.getText().toString(),
                    full_name.getText().toString(),
                    email.getText().toString());
            db.updateUser(u);

            // update name details in Transaction database
            db.updateTransNameDetails(user.getId(), full_name.getText().toString());

            // save picture thumbnail if a new one has been taken/selected
            if (tempBitmap != null) {
                saveToInternalStorage(user.getId(), tempBitmap);
            }
            // restart the main activity and clear the other activities on the backstack.
            Intent i = new Intent(this,MainActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);
        }
    }

    private void saveToInternalStorage(String filename, Bitmap bitmapImage){

        File path = this.getFileStreamPath(filename + ".jpg");
        FileOutputStream fos = null;

        try {
            fos = new FileOutputStream(path);
            // Use the compress method on the BitMap object to write image to the OutputStream
            // any existing file will be replaced
            bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.close();
            Log.d(TAG, "New file \"" + filename + "\" created in: " + path.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void takePicture(View v) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        PackageManager pm = getPackageManager();
        // check if there is an app to handle this action
        if (pm.hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            if (takePictureIntent.resolveActivity(pm) != null) {
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            } else {
                Toast.makeText(this, "No Camera App Available", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "No Camera Hardware Available", Toast.LENGTH_SHORT).show();
        }
    }

    public void getPicture (View v) {
        Intent intent = new Intent(Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_IMAGE_SELECT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap rawBitmap = (Bitmap) extras.get("data");
            int dimension = Math.min(rawBitmap.getHeight(), rawBitmap.getWidth());
            tempBitmap = ThumbnailUtils.extractThumbnail(rawBitmap, dimension, dimension); // crop bitmap before rounding it
            RoundedBitmapDrawable roundedBitmap = RoundedBitmapDrawableFactory.create(this.getResources(), tempBitmap);
            roundedBitmap.setCircular(true);
            thumbnail.setImageDrawable(roundedBitmap);
        } else if (requestCode == REQUEST_IMAGE_SELECT && resultCode == RESULT_OK) {
            Uri imageUri = data.getData();
            try {
                Bitmap rawBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                int dimension = Math.min(rawBitmap.getHeight(), rawBitmap.getWidth());
                tempBitmap = ThumbnailUtils.extractThumbnail(rawBitmap, dimension, dimension); // crop bitmap before rounding it
                RoundedBitmapDrawable roundedBitmap = RoundedBitmapDrawableFactory.create(this.getResources(), tempBitmap);
                roundedBitmap.setCircular(true);
                thumbnail.setImageDrawable(roundedBitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
