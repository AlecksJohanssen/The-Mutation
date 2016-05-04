package com.example.mapdemo;

import android.util.Log;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by JohnWhisker on 4/11/16.
 */
public class Police implements ClusterItem {
    @JsonIgnoreProperties(ignoreUnknown = true)

    private static final int SECOND_MILLIS = 1000;
    private static final int MINUTE_MILLIS = 60 * SECOND_MILLIS;
    private static final int HOUR_MILLIS = 60 * MINUTE_MILLIS;
    private static final int DAY_MILLIS = 24 * HOUR_MILLIS;
    private int id;
    private Double longitude;
    private Double latitude;
    private Date time;
    private Long timeInLong;
    private int like;
    private int dislike;

    public Police() {

    }

    public Police(LatLng loc) {
        this.latitude = loc.latitude;
        this.longitude = loc.longitude;
        this.time = new Date();
        timeInLong = getDateInMillis(this.time);

    }

    public Police(LatLng loc, Long time) {
        this.longitude = loc.longitude;
        this.latitude = loc.latitude;
        this.timeInLong = time;

    }

    @JsonIgnore
    @Override
    public LatLng getPosition() {
        return new LatLng(latitude, longitude);
    }

    public static long getDateInMillis(Date dateinput) {
        SimpleDateFormat format = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy");

        long dateInMillis = 0;
        try {
            Date mDate = format.parse("" + dateinput);
            long timeInMilliseconds = mDate.getTime();

            return timeInMilliseconds;
        } catch (ParseException e) {
            Log.d("time", "Exception while parsing date. " + e.getMessage());
            e.printStackTrace();
        }

        return 0;
    }

    public static String getTimeAgo(long time) {
        if (time < 1000000000000L) {
            // if timestamp given in seconds, convert to millis
            time *= 1000;
        }
        Date date = new Date();
        long now = getDateInMillis(date);
        if (time > now || time <= 0) {
            return null;
        }
        // TODO: localize
        final long diff = now - time;
        if (diff < MINUTE_MILLIS) {
            return "just now";
        } else if (diff < 2 * MINUTE_MILLIS) {
            return "a minute ago";
        } else if (diff < 50 * MINUTE_MILLIS) {
            return diff / MINUTE_MILLIS + " minutes ago";
        } else if (diff < 90 * MINUTE_MILLIS) {
            return "an hour ago";
        } else if (diff < 24 * HOUR_MILLIS) {
            return diff / HOUR_MILLIS + " hours ago";
        } else if (diff < 48 * HOUR_MILLIS) {
            return "yesterday";
        } else {
            return diff / DAY_MILLIS + " days ago";
        }
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Long getTimeInLong() {
        return timeInLong;
    }

    public void setTimeInLong(Long timeInLong) {
        this.timeInLong = timeInLong;
    }

    public int getLike() {
        return like;
    }

    public void setLike(int like) {
        this.like = like;
    }

    public int getDislike() {
        return dislike;
    }

    public void setDislike(int dislike) {
        this.dislike = dislike;
    }

    @Override
    public String toString() {
        return "" + latitude + ":" + longitude + ":" + getDateInMillis(time);
    }
}

