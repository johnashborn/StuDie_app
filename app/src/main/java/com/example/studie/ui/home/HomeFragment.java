package com.example.studie.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.renderscript.ScriptGroup;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.studie.camCalculator;
import com.example.studie.databinding.FragmentHomeBinding;
import com.example.studie.pdfConvert;
import com.example.studie.pngToText;
import com.example.studie.speechToText;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();



        binding.pdf.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), pdfConvert.class);
            startActivity(intent);
        });

        binding.calculator.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), camCalculator.class);
            startActivity(intent);
        });

        binding.photo.setOnClickListener(v->{
            Intent intent = new Intent(requireContext(), pngToText.class);
            startActivity(intent);
        });

        binding.voice.setOnClickListener(v->{
            Intent intent = new Intent(requireContext(), speechToText.class);
            startActivity(intent);
        });





        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }





}