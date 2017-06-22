package com.ca.mas.masmessagingsample.activity;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.design.widget.TextInputEditText;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.ca.mas.foundation.MASCallback;
import com.ca.mas.foundation.MASGroup;
import com.ca.mas.foundation.MASUser;
import com.ca.mas.identity.common.MASFilteredRequest;
import com.ca.mas.identity.group.GroupAttributes;
import com.ca.mas.identity.group.MASMember;
import com.ca.mas.identity.util.IdentityConsts;
import com.ca.mas.masmessagingsample.R;
import com.ca.mas.masmessagingsample.adapter.DividerDecoration;
import com.ca.mas.masmessagingsample.adapter.GroupRecyclerAdapter;
import com.ca.mas.masmessagingsample.mas.DataManager;

import java.util.ArrayList;
import java.util.List;


public class SearchGroupActivity extends BaseActivity {
    private static final String TAG = SearchGroupActivity.class.getSimpleName();
    private TextInputEditText mSearchView;
    private RecyclerView mRecyclerView;
    private Context mContext;
    private List<MASGroup> mResultList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_group);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mContext = this;
        mSearchView = (TextInputEditText) findViewById(R.id.searchView);
        mRecyclerView = (RecyclerView) findViewById(R.id.searchResults);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.search_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.search_add_to_group:
                if (mSearchView.getText() != null) {
                    String input = mSearchView.getText().toString();
                    if (input == null || input.isEmpty() || input.trim().isEmpty()) {
                        Toast.makeText(mContext, "Please enter a valid group name", Toast.LENGTH_SHORT).show();
                        return false;
                    }
                }
                MASGroup.newInstance().getGroupMetaData(new MASCallback<GroupAttributes>() {
                    @Override
                    public void onSuccess(GroupAttributes result) {
                        searchForGroup(result);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "Failed to get group attributes: " + e);
                        Toast.makeText(mContext, "Unable to fetch group attributes:" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void searchForGroup(GroupAttributes attributes) {
        final MASFilteredRequest request = new MASFilteredRequest(attributes.getAttributes(),
                IdentityConsts.KEY_GROUP_ATTRIBUTES);
        request.isEqualTo(IdentityConsts.KEY_GROUPS_BY_NAME, mSearchView.getText().toString().trim());

        MASGroup.newInstance().getGroupsByFilter(request, new MASCallback<List<MASGroup>>() {

            @Override
            public Handler getHandler() {
                return new Handler(Looper.getMainLooper());
            }

            @Override
            public void onSuccess(List<MASGroup> result) {
                if (result == null || result.isEmpty()) {
                    Toast.makeText(mContext, "No groups found with this name", Toast.LENGTH_SHORT).show();
                    return;
                }

                LinearLayoutManager llm = new LinearLayoutManager(mContext);
                llm.setOrientation(LinearLayoutManager.VERTICAL);
                mRecyclerView.setLayoutManager(llm);

                mResultList = getFilteredList(result);
                if (mResultList == null || mResultList.isEmpty()) {
                    Toast.makeText(mContext, "You are not member of this group", Toast.LENGTH_SHORT).show();
                    return;
                }
                DataManager.INSTANCE.addGroups(mResultList);
                GroupRecyclerAdapter adapter = new GroupRecyclerAdapter(mResultList, SearchGroupActivity.class.getName());
                mRecyclerView.setAdapter(adapter);
                mRecyclerView.addItemDecoration(new DividerDecoration(mContext));
            }

            @Override
            public void onError(Throwable e) {
                Log.e(TAG, "Failed to search for groups: " + e);
                Toast.makeText(mContext, "Failed to search for groups:" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }

            private List<MASGroup> getFilteredList(List<MASGroup> result) {
                List<MASGroup> finalList = new ArrayList<MASGroup>();
                for (MASGroup grp : result) {
                    if (grp == null || grp.getMembers() == null && grp.getMembers().isEmpty()) {
                        continue;
                    }
                    if (MASUser.getCurrentUser().getUserName().equalsIgnoreCase(grp.getOwner().getValue())) {
                        finalList.add(grp);
                        continue;
                    }
                    for (MASMember member : grp.getMembers()) {
                        if (MASUser.getCurrentUser().getUserName().equalsIgnoreCase(member.getValue())) {
                            finalList.add(grp);
                            break;
                        }
                    }
                }
                return finalList;
            }
        });
    }
}
