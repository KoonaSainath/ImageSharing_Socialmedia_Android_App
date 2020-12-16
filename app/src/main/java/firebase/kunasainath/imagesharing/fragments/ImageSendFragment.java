package firebase.kunasainath.imagesharing.fragments;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import firebase.kunasainath.imagesharing.R;
public class ImageSendFragment extends Fragment implements View.OnClickListener, AdapterView.OnItemClickListener{

    private TextView txtWelcome;
    private ImageView imgSelectImage;
    private EditText edtDescription;
    private ListView listAllUsers;
    private DatabaseReference database;
    private FirebaseAuth mAuth;
    private Bitmap mBitmap;

    private ArrayList<String> usernames;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> userIds;

    String imagePath, imageUrl;

    private static final int IMAGE_REQUEST_KEY = 770;

    public ImageSendFragment() {
    }

    public static ImageSendFragment newInstance() {
        ImageSendFragment fragment = new ImageSendFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getActivity().setTitle("Create Post");
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        database = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        imgSelectImage.setOnClickListener(this);
        usernames = new ArrayList<String>();
        userIds = new ArrayList<>();
        adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, usernames);
        listAllUsers.setAdapter(adapter);
        listAllUsers.setOnItemClickListener(this);

        database.child("Users").child(mAuth.getCurrentUser().getUid()).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                String key = snapshot.getKey();
                if(key.equals("Username")){
                    txtWelcome.setText("Welcome " + snapshot.getValue());
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_image_send, container, false);
        txtWelcome = view.findViewById(R.id.txt_welcome_user);
        imgSelectImage = view.findViewById(R.id.img_selected_image);
        edtDescription = view.findViewById(R.id.edt_post_description);
        listAllUsers = view.findViewById(R.id.list_all_users);
        return view;
    }

    private void selectImage(){
        if(Build.VERSION.SDK_INT <= 23){
            Intent intent = new Intent(Intent.ACTION_PICK , MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, IMAGE_REQUEST_KEY);
        }else{
            if(getActivity().checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){  //if permissions are not allowd
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, IMAGE_REQUEST_KEY);
            }else{
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, IMAGE_REQUEST_KEY);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == IMAGE_REQUEST_KEY && permissions[0] == Manifest.permission.READ_EXTERNAL_STORAGE && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            selectImage();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Uri imageUri;
        if(requestCode == IMAGE_REQUEST_KEY && resultCode == Activity.RESULT_OK && data != null){
            imageUri = data.getData();
            try {
                mBitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), imageUri);
                imgSelectImage.setImageBitmap(mBitmap);

                imgSelectImage.setScaleType(ImageView.ScaleType.CENTER_CROP);

                uploadImageToFirebaseStorage();

                edtDescription.setVisibility(View.VISIBLE);
                listAllUsers.setVisibility(View.VISIBLE);

                usernames.clear();

                database.child("Users").addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                        usernames.add(snapshot.child("Username").getValue().toString());
                        userIds.add(snapshot.getKey());
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                    }

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot snapshot) {

                    }

                    @Override
                    public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.img_selected_image:
                selectImage();
                break;
        }
    }

    private void uploadImageToFirebaseStorage(){

        ProgressDialog progress = new ProgressDialog(getActivity(), ProgressDialog.THEME_DEVICE_DEFAULT_DARK);
        progress.setTitle("Image upload");
        progress.setMessage("Please wait while we upload your image....");
        progress.setCancelable(false);
        if(Build.VERSION.SDK_INT >= 21){
            progress.create();
        }
        progress.show();


        // Get the data from an ImageView as bytes
        imgSelectImage.setDrawingCacheEnabled(true);
        imgSelectImage.buildDrawingCache();
        Bitmap bitmap = ((BitmapDrawable) imgSelectImage.getDrawable()).getBitmap();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();

        //UploadTask uploadTask = mountainsRef.putBytes(data);

        StorageReference reference = FirebaseStorage.getInstance().getReference().child("ImagesSent");

        imagePath = UUID.randomUUID() + ".jpg";  //RANDOM NAME

        UploadTask uploadTask = reference.child(imagePath).putBytes(data);

        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Snackbar.make(getActivity().findViewById(android.R.id.content), "Something went wrong. Try again please...", Snackbar.LENGTH_LONG).show();
                progress.dismiss();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Snackbar.make(getActivity().findViewById(android.R.id.content), "Image uploaded. Please enter some description for the post", Snackbar.LENGTH_LONG).show();
                progress.dismiss();

                taskSnapshot.getMetadata().getReference().getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        imageUrl = task.getResult().toString();
                    }
                });
            }
        });
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        switch (adapterView.getId()) {
            case R.id.list_all_users:
                if(edtDescription.getText().toString().length() == 0){
                    Snackbar.make(getActivity().findViewById(android.R.id.content), "Give some description to your post", Snackbar.LENGTH_LONG).show();
                    edtDescription.setError("Give some description.");
                }else{


                    String description = edtDescription.getText().toString();
                    String username = usernames.get(i);
                    String userId = userIds.get(i);

                    edtDescription.setText("");

                    String currentUsername = "";
                    String[] nameParts = txtWelcome.getText().toString().split(" ");
                    for(int index = 1; index < nameParts.length; index++){
                        if(index == nameParts.length-1){
                            currentUsername = currentUsername + nameParts[index];
                        }else{
                            currentUsername = currentUsername + nameParts[index] + " ";
                        }
                    }

                    HashMap<String, String> data = new HashMap<>();
                    data.put("GotThePostFrom", currentUsername);
                    data.put("ImagePath", imagePath);
                    data.put("ImageUrl", imageUrl);
                    data.put("Description", description);


                    FirebaseDatabase.getInstance().getReference().child("Users").child(userId).child("PostsReceived").push().setValue(data).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                Snackbar.make(getActivity().findViewById(android.R.id.content), "Post sent to " + username, Snackbar.LENGTH_LONG).show();
                            }else{
                                Snackbar.make(getActivity().findViewById(android.R.id.content), "Post not sent. Try again.", Snackbar.LENGTH_LONG).show();
                            }
                        }
                    });
                }
            break;
        }
    }

}