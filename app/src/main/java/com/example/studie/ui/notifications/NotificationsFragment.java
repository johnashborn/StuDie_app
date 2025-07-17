package com.example.studie.ui.notifications;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.studie.MainActivity;
import com.example.studie.databinding.FragmentNotificationsBinding;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class NotificationsFragment extends Fragment {

    private FragmentNotificationsBinding binding;
   private ActivityResultLauncher<String> imagePickerLauncher;  /*an object para pang start ug activity  like sa gallery
     and returns a result like and picture*/

    ActivityResultLauncher<Intent> resultLauncher;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentNotificationsBinding.inflate(inflater, container, false);
        // if the user pressed the log out button

        binding.LogOut.setOnClickListener(v -> {
            //  Clear login state
            SharedPreferences prefs = requireActivity().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.clear();  // Clears all stored data (like name, gender, etc.)
            editor.apply();

            //  Navigate back to the login screen
            Intent intent = new Intent(requireActivity(), MainActivity.class);
            startActivity(intent);

            //  Finish the current activity so user can't press back
            requireActivity().finish();
        });

        // Load saved image URI (if naay image)
        SharedPreferences pref = requireActivity().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);

        String savedPath = pref.getString("profileImagePath", null);
        if (savedPath != null) {
            File file = new File(savedPath);
            if (file.exists()) {
                binding.profileImage.setImageURI(Uri.fromFile(file));
            }
        }


        View view = binding.getRoot();

        // for the user profile info
        SharedPreferences infos = requireActivity().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        String name = infos.getString("name", "Default Name");
        String gender = infos.getString("gender", "Unknown");
        String level = infos.getString("level", "Unknown");
        String age = infos.getString("age","Unknown");


        // i display sa respective containers
        binding.TrueProfileName.setText("Name: " + name);
        binding.TrueGender.setText("Gender: " +gender);
        binding.TrueEducLevel.setText("Educational Level: "+ level);
        binding.TrueAge.setText("Age: "+age);

        // para maka access sa gallery
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        try {
                            // Copy selected image to internal storage
                            String fileName = "profile_image.jpg";
                            File file = new File(requireContext().getFilesDir(), fileName);

                            try (InputStream inputStream = requireContext().getContentResolver().openInputStream(uri);
                                 OutputStream outputStream = new FileOutputStream(file)) {

                                byte[] buffer = new byte[1024];
                                int length;
                                while ((length = inputStream.read(buffer)) > 0) {
                                    outputStream.write(buffer, 0, length);
                                }
                            }

                            // Saves file path to SharedPreferences...I think?
                            SharedPreferences prefs = requireActivity().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
                            prefs.edit().putString("profileImagePath", file.getAbsolutePath()).apply();

                            // Show image into the ImageView... pls work
                            Uri imageUri = Uri.fromFile(file);
                            Uri noCacheUri = imageUri.buildUpon()
                                    .appendQueryParameter("timestamp", String.valueOf(System.currentTimeMillis()))
                                    .build();

                            binding.profileImage.setImageURI(noCacheUri);
                            binding.profileImage.invalidate(); // Force the view to redraw


                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
        );


        // Open the gallery after the button was pressed
       binding.changePic.setOnClickListener(v -> {
           imagePickerLauncher.launch("image/");
       });





        return view;



    }



    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}