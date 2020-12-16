package firebase.kunasainath.imagesharing.fragments;

import android.app.ProgressDialog;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.regex.Pattern;

import firebase.kunasainath.imagesharing.R;

public class SigningFragment extends Fragment implements View.OnClickListener{
    private EditText edtEmail, edtUsername, edtPassword;
    private Button btnSignup, btnLogin;

    public interface SigningInterface{
        public void signupIsDone();
    }

    private FirebaseAuth mAuth;
    private SigningInterface mInterface;
    public SigningFragment() {
    }

    public static SigningFragment newInstance() {
        SigningFragment fragment = new SigningFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().setTitle("Sign Up or Log In");

        FirebaseApp.initializeApp(getActivity());
        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        btnLogin.setOnClickListener(this);
        btnSignup.setOnClickListener(this);

        mInterface = (SigningInterface) getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_signing, container, false);
        edtEmail = view.findViewById(R.id.edt_email);
        edtUsername = view.findViewById(R.id.edt_username);
        edtPassword = view.findViewById(R.id.edt_password);
        btnSignup = view.findViewById(R.id.btn_signup);
        btnLogin = view.findViewById(R.id.btn_login);
        return view;
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.btn_signup:
                if(checkSignupCondition()){

                    ProgressDialog dialog = new ProgressDialog(getActivity(), ProgressDialog.THEME_DEVICE_DEFAULT_DARK);
                    dialog.setCancelable(false);
                    dialog.setMessage("Creating your account...");
                    if(Build.VERSION.SDK_INT >= 21) {
                        dialog.create();
                    }
                    dialog.show();


                    mAuth.createUserWithEmailAndPassword(edtEmail.getText().toString(), edtPassword.getText().toString())
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if(task.isSuccessful()){
                                        //root
                                        DatabaseReference database = FirebaseDatabase.getInstance().getReference();

                                        String userid = mAuth.getCurrentUser().getUid();

                                        database.child("Users").child(userid)
                                                .child("Username").setValue(edtUsername.getText().toString());

                                        dialog.dismiss();
                                        Snackbar.make(view, "Account created successfully!", Snackbar.LENGTH_LONG).show();

                                        mInterface.signupIsDone();

                                    }else{
                                        Snackbar.make(view, "Cannot create account. Please verify your credentials!", Snackbar.LENGTH_LONG).show();
                                        dialog.dismiss();
                                    }
                                }
                            });
                }
                break;
            case R.id.btn_login:
                if(checkLoginCondition()){

                    ProgressDialog dialog = new ProgressDialog(getActivity(),ProgressDialog.THEME_DEVICE_DEFAULT_DARK);
                    dialog.setMessage("Logging you in...");
                    dialog.setCancelable(false);
                    if(Build.VERSION.SDK_INT >= 21){
                        dialog.create();
                    }
                    dialog.show();

                    mAuth.signInWithEmailAndPassword(edtEmail.getText().toString(), edtPassword.getText().toString())
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if(task.isSuccessful()){
                                        Snackbar.make(view, "You have logged in successfully. Welcome!", Snackbar.LENGTH_LONG).show();
                                        mInterface.signupIsDone();

                                        dialog.dismiss();
                                    }else{
                                        Snackbar.make(view, "Cannot log into your account. Please verify your email and password", Snackbar.LENGTH_LONG).show();
                                        mInterface.signupIsDone();

                                        dialog.dismiss();
                                    }
                                }
                            });
                }
                break;
        }
    }

    private boolean checkSignupCondition(){
        String email = edtEmail.getText().toString(), username = edtUsername.getText().toString(), password = edtPassword.getText().toString();
        if(email.length() == 0 || !(isEmailValid(email))) {
            edtEmail.setError("Invalid email");
            return false;
        }
        if(username.length() <= 3){
            edtUsername.setError("Username should be of 4 letters minimum");
            return false;
        }
        if(password.length() < 6){
            edtPassword.setError("Password should contain atleast 6 letters");
            return false;
        }
        return true;
    }

    private boolean checkLoginCondition(){
        String email = edtEmail.getText().toString(), password = edtPassword.getText().toString();
        if(email.length() == 0 || !(isEmailValid(email))) {
            edtEmail.setError("Invalid email");
            return false;
        }

        if(password.length() < 6){
            edtPassword.setError("Password should contain atleast 6 letters");
            return false;
        }
        return true;
    }

    private boolean isEmailValid(String email){
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\."+
                "[a-zA-Z0-9_+&*-]+)*@" +
                "(?:[a-zA-Z0-9-]+\\.)+[a-z" +
                "A-Z]{2,7}$";

        Pattern pat = Pattern.compile(emailRegex);
        if (email == null)
            return false;
        return pat.matcher(email).matches();
    }

    @Override
    public void onStart() {
        super.onStart();

        if(mAuth.getCurrentUser() != null){
            mInterface.signupIsDone();
        }
    }
}