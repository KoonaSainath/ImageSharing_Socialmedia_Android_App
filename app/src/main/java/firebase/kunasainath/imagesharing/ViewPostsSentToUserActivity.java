package firebase.kunasainath.imagesharing;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;

public class ViewPostsSentToUserActivity extends AppCompatActivity implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener{


    private TextView txtUsername, txtDescription;
    private ImageView imgPostedImage;
    private ListView listOfSentPostUsers;

    ArrayList<DataSnapshot> allPosts;
    ArrayList<String> usernames;
    ArrayAdapter<DataSnapshot> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_posts_sent_to_user);
        setTitle("Posts sent to you");
        txtUsername = findViewById(R.id.txt_sent_by);
        txtDescription = findViewById(R.id.txt_desc_of_post);
        imgPostedImage = findViewById(R.id.txt_image_of_post);
        listOfSentPostUsers = findViewById(R.id.list_of_sent_posts);
        allPosts = new ArrayList<>();
        usernames = new ArrayList<>();

        FirebaseDatabase.getInstance().getReference().child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("PostsReceived").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                allPosts.add(snapshot);
                usernames.add(snapshot.child("GotThePostFrom").getValue().toString());
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

                int index = 0;
                for(DataSnapshot dataSnapshot : allPosts){
                    if(dataSnapshot.getKey().equals(snapshot.getKey())){
                        allPosts.remove(index);
                        usernames.remove(index);
                    }
                    index++;
                }

                adapter.notifyDataSetChanged();

                Snackbar.make(findViewById(android.R.id.content), "Post deleted successfully", Snackbar.LENGTH_LONG).show();

                imgPostedImage.setImageResource(R.drawable.select_image);
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, usernames);
        listOfSentPostUsers.setAdapter(adapter);
        listOfSentPostUsers.setOnItemClickListener(this);
        listOfSentPostUsers.setOnItemLongClickListener(this);

    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        switch (adapterView.getId()){
            case R.id.list_of_sent_posts:
                DataSnapshot data = allPosts.get(i);
                String username = data.child("GotThePostFrom").getValue().toString();
                String description = data.child("Description").getValue().toString();
                String imageUrl = data.child("ImageUrl").getValue().toString();

                txtUsername.setText("Sent by: " + username);
                txtDescription.setText("About post\n\n" + description);
                Glide.with(this).load(imageUrl).into(imgPostedImage);

                break;
        }
    }


    @Override
    public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
        switch (adapterView.getId()) {
            case R.id.list_of_sent_posts:
                AlertDialog.Builder alert = new AlertDialog.Builder(this);
                alert.setTitle("Delete post");
                alert.setMessage("Do you want to delete this post for sure?");
                alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int index) {

                        FirebaseStorage.getInstance().getReference().child("ImagesSent").child(allPosts.get(i).child("ImagePath").getValue().toString()).delete();

                        DataSnapshot snapshot = allPosts.get(i);
                        FirebaseDatabase.getInstance().getReference().child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                .child("PostsReceived").child(allPosts.get(i).getKey()).removeValue();
                    }
                });
                alert.create().show();
                break;
        }
        return true;
    }

}