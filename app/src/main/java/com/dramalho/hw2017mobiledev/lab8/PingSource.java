package com.dramalho.hw2017mobiledev.lab8;

/**
 * Created by dramalho on 8/7/17.
 */

import android.content.Context;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class PingSource {

    public interface PingListener {
        void onPingsReceived(List<Ping> pingList);
    }

    private static PingSource sNewsSource;

    private Context mContext;

    public static PingSource get(Context context) {
        if (sNewsSource == null) {
            sNewsSource = new PingSource(context);
        }
        return sNewsSource;
    }

    private PingSource(Context context) {
        mContext = context;
    }

    // Firebase methods for you to implement.

    public void getPings(final PingListener pingListener) {

        DatabaseReference pingsRef = FirebaseDatabase.getInstance().getReference("pings");
        Query last50PingsQuery = pingsRef.limitToLast(50);

        last50PingsQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Iterable<DataSnapshot> pingSnapshots = dataSnapshot.getChildren();
                List<Ping> pingList = new ArrayList<Ping>();
                for (DataSnapshot pingsnap : pingSnapshots) {
                    Ping new_ping = new Ping(pingsnap);
                    pingList.add(new_ping);
                }
                pingListener.onPingsReceived(pingList);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    //Gets the pings made by a user (userid)
    public void getPingsForUserId(String userId, final PingListener pingListener) {

        DatabaseReference pingsRef = FirebaseDatabase.getInstance().getReference("pings");
        Query userQuery = pingsRef.orderByChild("userId").equalTo(userId).limitToLast(50);

        userQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Iterable<DataSnapshot> pingSnapshots = dataSnapshot.getChildren();
                ArrayList<Ping> ping_list = new ArrayList<Ping>();
                for (DataSnapshot pingsnap : pingSnapshots) {
                    Ping newPing = new Ping(pingsnap);
                    ping_list.add(newPing);
                }
                pingListener.onPingsReceived(ping_list);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("DCR", "database error");
            }
        });

    }

    public void sendPing(Ping ping) {

        DatabaseReference pingsRef = FirebaseDatabase.getInstance().getReference("pings");
        DatabaseReference newPingRef = pingsRef.push();

        HashMap<String, Object> pingValMap = new HashMap<String, Object>();

        //Set ping vals for sending ping
        pingValMap.put("userName", ping.getUserName());
        pingValMap.put("userId", ping.getUserId());
        pingValMap.put("timestamp", ServerValue.TIMESTAMP);

        newPingRef.setValue(pingValMap);

    }
}


