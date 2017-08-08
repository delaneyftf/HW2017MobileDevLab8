package com.dramalho.hw2017mobiledev.lab8;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class MainFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_main, container, false);

        // Get references to text views to display database data.
        final ListView pingListView = v.findViewById(R.id.all_pings_list_view);
        final ListView specificUserPingListView = v.findViewById(R.id.queried_pings_list_view);

        // Set up pingListView
        PingSource.get(getContext()).getPings(new PingSource.PingListener() {
            @Override
            public void onPingsReceived(List<Ping> pingList) {
                PingArrayAdapter adapter = new PingArrayAdapter(getContext(), pingList);
                pingListView.setAdapter(adapter);
                // Whenever we set a new adapter (which usually scrolls to the top of the contents), scroll to the bottom of the contents.
                pingListView.setSelection(adapter.getCount() - 1);
            }
        });

        // handle click - get pings for a user id and set it on the _other_ list
        pingListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Ping ping = (Ping)adapterView.getAdapter().getItem(i);
                PingSource.get(getContext()).getPingsForUserId(ping.getUserId(), new PingSource.PingListener() {
                    @Override
                    public void onPingsReceived(List<Ping> pingList) {
                        PingArrayAdapter adapter = new PingArrayAdapter(getContext(), pingList);
                        specificUserPingListView.setAdapter(adapter);
                        // Whenever we set a new adapter (which usually scrolls to the top of the contents), scroll to the bottom of the contents.
                        specificUserPingListView.setSelection(adapter.getCount() - 1);
                    }
                });
            }
        });

        Button button = v.findViewById(R.id.send_ping_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Get logged-in user data, create Ping, send it.
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user == null) {
                    Toast.makeText(getContext(), "Can't send ping, not logged in", Toast.LENGTH_SHORT);
                    return;
                }
                Ping ping = new Ping(user.getDisplayName(), user.getUid());
                PingSource.get(getContext()).sendPing(ping);
            }
        });

        return v;
    }

    private class PingArrayAdapter extends BaseAdapter {
        protected Context mContext;
        protected List<Ping> mPingList;
        protected LayoutInflater mLayoutInflater;
        public PingArrayAdapter(Context context, List<Ping> pingList) {
            mContext = context;
            mPingList = pingList;
            mLayoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return mPingList.size();
        }

        @Override
        public Object getItem(int position) {
            return mPingList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final Ping ping = mPingList.get(position);
            View rowView = mLayoutInflater.inflate(R.layout.list_item_ping, parent, false);

            TextView title = rowView.findViewById(R.id.user_text_view);
            title.setText(ping.getUserName());

            TextView subtitle = rowView.findViewById(R.id.timestamp_text_view);
            // get a Long timestamp (of millis since epoch) into a human-readable string.
            Date date = new Date(ping.getTimestamp());
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
            subtitle.setText(sdf.format(date));

            return rowView;
        }
    }
}
