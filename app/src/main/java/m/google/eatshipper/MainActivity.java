package m.google.eatshipper;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rengwuxian.materialedittext.MaterialEditText;

import info.hoang8f.widget.FButton;
import m.google.eatshipper.common.Common;
import m.google.eatshipper.model.Shipper;

public class MainActivity extends AppCompatActivity {

    private MaterialEditText edtxPhone, edtxPassword;
    private FButton btnSignIn;

    private FirebaseDatabase database;
    private DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        edtxPhone= findViewById(R.id.IdedtxPhone);
        edtxPassword= findViewById(R.id.IdedtxPassword);
        btnSignIn= findViewById(R.id.IdbtnSignIn);

        //init firebase
        database= FirebaseDatabase.getInstance();
        reference= database.getReference("Shippers");

        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (edtxPassword.getText().toString().isEmpty() || edtxPhone.getText().toString().isEmpty())
                {
                    Toast.makeText(MainActivity.this, "Ingrese Todos los datos", Toast.LENGTH_SHORT).show();
                }
                else {
                    login(edtxPhone.getText().toString(), edtxPassword.getText().toString());
                }
            }
        });
    }

    private void login(String phone, final String password) {

        reference.child(phone).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists())
                {
                    Shipper shipper= dataSnapshot.getValue(Shipper.class);

                    if (shipper.getPassword().equals(password))
                    {
                        startActivity(new Intent(MainActivity.this,HomeActivity.class));
                        Common.currentShipper= shipper;
                        finish();
                    }
                    else
                    {
                        Toast.makeText(MainActivity.this, "Password Incorrecto..", Toast.LENGTH_SHORT).show();
                    }

                }else
                {
                    Toast.makeText(MainActivity.this, "Tu phone no existe en la base de datos", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

}
