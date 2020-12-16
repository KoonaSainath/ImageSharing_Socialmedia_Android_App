package firebase.kunasainath.imagesharing;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;

import firebase.kunasainath.imagesharing.fragments.ImageSendFragment;
import firebase.kunasainath.imagesharing.fragments.SigningFragment;

public class MainActivity extends AppCompatActivity implements SigningFragment.SigningInterface {

    SigningFragment mSigningFragment;
    ImageSendFragment mImageSendFragment;

    private static final String SIGN_UP_KEY = "signup fragment";
    private static final String SEND_POST_KEY = "send post fragment";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSigningFragment = SigningFragment.newInstance();
        mImageSendFragment = ImageSendFragment.newInstance();

        displaySiginingFragment();

    }

    private void displaySiginingFragment(){
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, mSigningFragment, SIGN_UP_KEY)
                .addToBackStack(null)
                .commit();
    }

    private void displaySendPostFragment(){
        getSupportFragmentManager().popBackStack();

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, mImageSendFragment, SEND_POST_KEY)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void signupIsDone() {
        displaySendPostFragment();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        switch (item.getItemId()) {
            case R.id.menu_item_logout:
                if(auth.getCurrentUser() != null){
                    AlertDialog.Builder alert = new AlertDialog.Builder(this);
                    alert.setTitle("LOGOUT");
                    alert.setMessage("Do you really want to logout?");
                    alert.setPositiveButton("Logout", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            auth.signOut();
                            Snackbar.make(findViewById(android.R.id.content), "Logged out", Snackbar.LENGTH_LONG);

                            //removing current fragment

                            getSupportFragmentManager().popBackStack();

                            displaySiginingFragment();

                        }
                    });
                    alert.create().show();
                }else{
                    Snackbar.make(findViewById(android.R.id.content), "You are not logged in.", Snackbar.LENGTH_LONG).show();
                }
                break;

            case R.id.menu_item_all_posts:
                if(FirebaseAuth.getInstance().getCurrentUser() != null) {
                    startActivity(new Intent(this, ViewPostsSentToUserActivity.class));
                }else{
                    Snackbar.make(findViewById(android.R.id.content), "You need to login to see the posts", Snackbar.LENGTH_LONG).show();
                }
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        Fragment fragment1 = getSupportFragmentManager().findFragmentByTag(SIGN_UP_KEY);
        Fragment fragment2 = getSupportFragmentManager().findFragmentByTag(SEND_POST_KEY);

        if(fragment1 == null && fragment2 == null){
            finish();
        }
    }
}