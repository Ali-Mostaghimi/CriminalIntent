package com.example.criminalintent;


import java.text.DateFormat;
import java.util.Date;
import java.util.UUID;

public class Crime {
    private UUID mId;
    private String mTitle;
    private Date mDate;
    private boolean mSolved;
    private String mSuspect;
    private String mSuspectPhone;

    public Crime(){
        //Generate unique identifier
        this(UUID.randomUUID());
    }

    public Crime(UUID id){
        mId = id;
        mDate = new Date();
    }

    public Date getDate() {
        return mDate;
    }

    public void setDate(Date date) {
        mDate = date;
    }

    public boolean isSolved() {
        return mSolved;
    }

    public void setSolved(boolean solved) {
        mSolved = solved;
    }

    public UUID getId() {
        return mId;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String mTitle) {
        this.mTitle = mTitle;
    }

    public String getSuspect() {
        return mSuspect;
    }

    public void setSuspect(String suspect) {
        mSuspect = suspect;
    }

    public String getSuspectPhone() {
        return mSuspectPhone;
    }

    public void setSuspectPhone(String suspectPhone) {
        mSuspectPhone = suspectPhone;
    }

    public String getPhotoFilename(){
        return "IMG_" + getId().toString() + ".jpg";
    }

    public String getFormattedDate(){
        //formatting the date
        String formattedDate = DateFormat.getDateInstance().format(this.getDate());
        return formattedDate;

    }

    public String getFormattedTime(){
        //formatting the time
        String formattedTime = DateFormat.getTimeInstance(DateFormat.SHORT)
                .format(this.getDate());
        return formattedTime;
    }

}
