package com.example.criminalintent;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.CursorWrapper;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ShareCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import java.io.File;
import java.lang.reflect.ParameterizedType;
import java.util.Date;
import java.util.UUID;

public class CrimeFragment extends Fragment {

    private static final String KEY_ID_CHANGED_CRIME = "changed_crime_id";
    private static final String DIALOG_TIME = "DialogTime";
    private Crime mCrime;
    private File mPhotoFile;
    private EditText mTitleField;
    private ImageView mPhotoView;
    private ImageButton mPhotoButton;
    private Button mDateButton;
    private Button mTimeButton;
    private CheckBox mSolvedCheckBox;
    private Button mReportButton;
    private Button mSuspectButton;
    private Button mCallButton;
    private Callbacks mCallbacks;
    private static final String ARG_CRIME_ID = "crime_id";
    private static final String KEY_CRIME_CHANGED = "crime_changed";
    private static final String DIALOG_DATE = "DialogDate";
    private static final String DIALOG_PHOTO = "DialogPhoto";
    private static final int REQUEST_DATE = 0;
    private static final int REQUEST_CONTACT = 1;
    private static final int REQUEST_PHOTO = 2;

    /**
     * Required interface for hosting activity
     *
     */
    public interface Callbacks{
        void onCrimeUpdated(Crime crime);
        void onCrimeFragmentFinished();
    }

    public static CrimeFragment newInstance(UUID crimeId){
        Bundle args = new Bundle();
        args.putSerializable(ARG_CRIME_ID, crimeId);

        CrimeFragment fragment = new CrimeFragment();
        fragment.setArguments(args);
        return fragment;

    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mCallbacks = (Callbacks) context;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        UUID crimeId = (UUID) getArguments().getSerializable(ARG_CRIME_ID);
        mCrime = CrimeLab.get(getActivity()).getCrime(crimeId);
        mPhotoFile = CrimeLab.get(getActivity()).getPhotoFile(mCrime);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_crime, container, false);
        mTitleField = v.findViewById(R.id.crime_title);
        mTitleField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mCrime.setTitle(s.toString());
                updateCrime();
                //crimeIsChanged(true);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        mTitleField.setText(mCrime.getTitle());

        mDateButton = v.findViewById(R.id.crime_date);
        updateDate();
        mDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager manager = getActivity().getSupportFragmentManager();
                DatePickerFragment dialog = DatePickerFragment
                        .newInstance(mCrime.getDate());
                dialog.setTargetFragment(CrimeFragment.this, REQUEST_DATE);
                dialog.show(manager, DIALOG_DATE);
            }
        });

        mTimeButton = v.findViewById(R.id.crime_time);
        updateTime();
        mTimeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager manager = getActivity().getSupportFragmentManager();
                TimePickerFragment timeDialog = TimePickerFragment
                        .newInstance(mCrime.getDate());
                timeDialog.setTargetFragment(CrimeFragment.this, REQUEST_DATE);
                timeDialog.show(manager, DIALOG_TIME);
            }
        });

        mSolvedCheckBox = v.findViewById(R.id.crime_solved);
        mSolvedCheckBox.setChecked(mCrime.isSolved());
        mSolvedCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mCrime.setSolved(isChecked);
                updateCrime();
                //crimeIsChanged(true);
            }
        });

        mReportButton = v.findViewById(R.id.crime_report);
        mReportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ShareCompat.IntentBuilder.from(getActivity())
                        .setChooserTitle(getString(R.string.send_report))
                        .setText(getCrimeReport())
                        .setSubject(getString(R.string.crime_report_subject))
                        .setType("text/plain")
                        .startChooser()
                        ;
                /*
                Intent i = new Intent(Intent.ACTION_SEND);
                i.setType("text/plain");
                i.putExtra(Intent.EXTRA_TEXT, getCrimeReport());
                i.putExtra(Intent.EXTRA_SUBJECT,
                        getString(R.string.crime_report_subject));
                i = Intent.createChooser(i, getString(R.string.send_report));
                startActivity(in);

                 */
            }
        });

        final Intent pickContact = new Intent(Intent.ACTION_PICK,
                ContactsContract.Contacts.CONTENT_URI);
        //dummy code for verify filter
        //pickContact.addCategory(Intent.CATEGORY_HOME);
        mSuspectButton = v.findViewById(R.id.crime_suspect);
        mSuspectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(pickContact, REQUEST_CONTACT);
            }
        });

        if (mCrime.getSuspect() != null){
            mSuspectButton.setText(mCrime.getSuspect());
        }

        PackageManager packageManager = getActivity().getPackageManager();
        if (packageManager.resolveActivity(pickContact,
                PackageManager.MATCH_DEFAULT_ONLY) == null){
            mSuspectButton.setEnabled(false);
        }

        mCallButton = v.findViewById(R.id.crime_call);
        mCallButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCrime.getSuspectPhone() == null){
                    return;
                }
                Intent i = new Intent(Intent.ACTION_DIAL);
                Uri number = Uri.parse("tel:" + mCrime.getSuspectPhone());
                i.setData(number);
                startActivity(i);
            }
        });
        mCallButton.setEnabled(mCrime.getSuspectPhone() != null);

        mPhotoView = v.findViewById(R.id.crime_photo);
        updatePhotoView();

        mPhotoButton = v.findViewById(R.id.crime_camera);
        final Intent captureImage = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        final boolean canTakePhoto = mPhotoFile != null &&
                captureImage.resolveActivity(packageManager) != null;
        mPhotoButton.setEnabled(canTakePhoto);


        if (canTakePhoto){
            Uri uri = FileProvider.getUriForFile(getContext(), getContext().getPackageName(), mPhotoFile);
            captureImage.putExtra(MediaStore.EXTRA_OUTPUT, uri);
            //captureImage.setFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        }

        mPhotoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (canTakePhoto){
                    FragmentManager manager = getActivity().getSupportFragmentManager();
                    CrimePhotoFragment photoFragment =
                            CrimePhotoFragment.newInstance(mCrime.getId());
                    photoFragment.show(manager, DIALOG_PHOTO);
                }
            }
        });

        mPhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(captureImage, REQUEST_PHOTO);
            }
        });

        return v;
    }


    @Override
    public void onPause() {
        super.onPause();

        CrimeLab.get(getActivity())
                .updateCrime(mCrime);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_crime_pager, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.delete_crime:
                CrimeLab.get(getActivity()).deleteCrime(mCrime);
                mCallbacks.onCrimeUpdated(mCrime);
                mCallbacks.onCrimeFragmentFinished();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode != Activity.RESULT_OK){
            return;
        }

        if (requestCode == REQUEST_DATE){
            if (data.getSerializableExtra(DatePickerFragment.EXTRA_DATE) != null){
                Date date = (Date) data.getSerializableExtra(DatePickerFragment.EXTRA_DATE);
                mCrime.setDate(date);
                updateDate();
            }else if (data.getSerializableExtra(TimePickerFragment.EXTRA_TIME) != null){
                Date date = (Date) data.getSerializableExtra(TimePickerFragment.EXTRA_TIME);
                mCrime.setDate(date);
                updateCrime();
                updateTime();
            }
        }else if (requestCode == REQUEST_CONTACT && data != null){
            Uri contactUri = data.getData();
            //Specify which fields you want your query to return
            //values for.
            String[] queryFields = new String[]{
                    ContactsContract.Contacts.DISPLAY_NAME,
                    ContactsContract.Contacts._ID
            };
            //perform your query - the contactUri is like a "where"
            //clause here
            Cursor c = getActivity().getContentResolver()
                    .query(contactUri, queryFields, null, null, null);

            try {
                //Double-check that you actually got results
                if (c.getCount() == 0){
                    return;
                }

                //pull out the first column oh the first row of data-
                //that is your suspect's name
                c.moveToFirst();
                String suspect = c.getString(0);
                String id = c.getString(
                        c.getColumnIndex(ContactsContract.Contacts._ID));
                Cursor cursor = getActivity().getContentResolver().query(
                        ContactsContract.Data.CONTENT_URI,
                        new String[] {ContactsContract.CommonDataKinds.Phone.NUMBER},
                        ContactsContract.Data.CONTACT_ID + "=?" , new String[] {id},
                        null);
                cursor.moveToFirst();
                String phone = cursor.getString(0);
                mCrime.setSuspect(suspect);
                mCrime.setSuspectPhone(phone);
                updateCrime();
                mSuspectButton.setText(suspect);
                mCallButton.setEnabled(mCrime.getSuspectPhone() != null);
            }finally {
                c.close();
            }

        }if (requestCode == REQUEST_PHOTO){
            updateCrime();
            updatePhotoView();
        }
    }

    private void updateCrime(){
        CrimeLab.get(getActivity()).updateCrime(mCrime);
        mCallbacks.onCrimeUpdated(mCrime);
    }


    private void updatePhotoView(){
        if (mPhotoView == null || !mPhotoFile.exists()){
            mPhotoView.setImageDrawable(null);
        }else{
            Bitmap bitmap = PictureUtils.getScaledBitmap(
                    mPhotoFile.getPath(), getActivity());
            mPhotoView.setImageBitmap(bitmap);
        }
    }

    private void updateDate() {
        mDateButton.setText(mCrime.getFormattedDate());
    }

    private void updateTime(){
        mTimeButton.setText(mCrime.getFormattedTime());
    }

    private String getCrimeReport(){
        String solvedString = null;
        if (mCrime.isSolved()){
            solvedString = getString(R.string.crime_report_solved);
        }else {
            solvedString = getString(R.string.crime_report_unsolved);
        }

        String dateString = mCrime.getFormattedTime()+ " " + mCrime.getFormattedDate();

        String suspect = mCrime.getSuspect();
        if (suspect == null){
            suspect = getString(R.string.crime_report_no_suspect);
        }else {
            suspect = getString(R.string.crime_report_suspect, suspect);
        }

        String report = getString(R.string.crime_report,
                mCrime.getTitle(), dateString, solvedString, suspect);

        return report;
    }

    /*
    private void crimeIsChanged(boolean isChanged) {
        Intent intent = new Intent();
        intent.putExtra(KEY_CRIME_CHANGED, isChanged);
        intent.putExtra(KEY_ID_CHANGED_CRIME, mCrime.getId());
        getActivity().setResult(Activity.RESULT_OK, intent);
    }

    public static boolean wasChanged(Intent intent){
        return intent.getBooleanExtra(KEY_CRIME_CHANGED, false);
    }

    public static UUID getChangedCrimeId(Intent intent){
        return (UUID) intent.getSerializableExtra(KEY_ID_CHANGED_CRIME);
    }
     */


}
