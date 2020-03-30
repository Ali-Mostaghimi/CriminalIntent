package com.example.criminalintent;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import java.io.File;
import java.util.UUID;

public class CrimePhotoFragment extends DialogFragment {

    private static final String ARG_ID = "crimeId";
    ImageView mCrimeView;
    File mPhotoFile;

    public static CrimePhotoFragment newInstance(UUID crimeId){
        Bundle args = new Bundle();
        args.putSerializable(ARG_ID, crimeId);

        CrimePhotoFragment fragment = new CrimePhotoFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        UUID crimeId = (UUID) getArguments().getSerializable(ARG_ID);
        CrimeLab crimeLab =  CrimeLab.get(getContext());

        mPhotoFile = crimeLab.getPhotoFile(crimeLab.getCrime(crimeId));

    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        LinearLayout
                view = (LinearLayout) LayoutInflater.from(getContext()).inflate(R.layout.dialog_photo, null);

        mCrimeView = view.findViewById(R.id.dialog_photo);

        mCrimeView.setImageBitmap(PictureUtils.getScaledBitmap(
                mPhotoFile.getPath(),
                getActivity()));

        return new AlertDialog.Builder(getContext())
                .setTitle(R.string.crime_photo_dialog_title)
                .setView(view)
                .create();
    }
}
