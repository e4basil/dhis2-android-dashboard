/*
 * Copyright (c) 2015, University of Oslo
 *
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * Neither the name of the HISP project nor the names of its contributors may
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.hisp.dhis.android.dashboard.ui.fragments.dashboard;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.hisp.dhis.android.dashboard.DhisService;
import org.hisp.dhis.android.dashboard.R;
import org.hisp.dhis.android.dashboard.ui.events.UiEvent;
import org.hisp.dhis.android.dashboard.ui.fragments.BaseDialogFragment;
import org.hisp.dhis.android.dashboard.utils.EventBusProvider;
import org.hisp.dhis.android.sdk.core.api.Dhis2;
import org.hisp.dhis.android.sdk.models.dashboard.Dashboard;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnFocusChange;

/**
 * Handles editing (changing name) and removal of given dashboard.
 */
public final class DashboardManageFragment extends BaseDialogFragment {
    private static final String TAG = DashboardManageFragment.class.getSimpleName();
    private static final String DASHBOARD_ID = "arg:dashboardId";

    @Bind(R.id.fragment_bar)
    View mFragmentBar;

    @Bind(R.id.fragment_bar_mode_editing)
    View mFragmentBarEditingMode;

    @Bind(R.id.dialog_label)
    TextView mDialogLabel;

    @Bind(R.id.action_name)
    TextView mActionName;

    @Bind(R.id.dashboard_name)
    EditText mDashboardName;

    @Bind(R.id.delete_dashboard_button)
    Button mDeleteButton;

    Dashboard mDashboard;

    public static DashboardManageFragment newInstance(long dashboardId) {
        Bundle args = new Bundle();
        args.putLong(DASHBOARD_ID, dashboardId);

        DashboardManageFragment fragment = new DashboardManageFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE,
                R.style.Theme_AppCompat_Light_Dialog);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dashboard_manage, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        mDashboard = Dhis2.dashboards()
                .get(getArguments().getLong(DASHBOARD_ID));

        ButterKnife.bind(this, view);

        mDialogLabel.setText(getString(R.string.manage_dashboard));
        mActionName.setText(getString(R.string.edit_name));

        mDashboardName.setText(mDashboard.getDisplayName());
        mDeleteButton.setEnabled(mDashboard.getAccess().isDelete());

        setFragmentBarActionMode(false);
    }


    @OnClick({R.id.close_dialog_button, R.id.cancel_action,
            R.id.accept_action, R.id.delete_dashboard_button,})
    @SuppressWarnings("unused")
    public void onButtonClick(View view) {
        switch (view.getId()) {
            case R.id.cancel_action: {
                mDashboardName.setText(
                        mDashboard.getDisplayName());
                mDashboardName.clearFocus();
                break;
            }
            case R.id.accept_action: {
                mDashboardName.clearFocus();

                mDashboard.setName(mDashboardName.getText().toString());
                Dhis2.dashboards().update(mDashboard);

                DhisService.getInstance().syncDashboards();
                EventBusProvider.post(new UiEvent(UiEvent.UiEventType.SYNC_DASHBOARDS));
                break;
            }
            case R.id.delete_dashboard_button: {
                Dhis2.dashboards().remove(mDashboard);

                DhisService.getInstance().syncDashboards();
                EventBusProvider.post(new UiEvent(UiEvent.UiEventType.SYNC_DASHBOARDS));
            }
            case R.id.close_dialog_button: {
                dismiss();
            }
        }
    }

    @OnFocusChange(R.id.dashboard_name)
    @SuppressWarnings("unused")
    public void onFocusChanged(boolean focused) {
        setFragmentBarActionMode(focused);
    }

    /* set fragment bar in editing mode, by hiding standard
    layout and showing layout with actions*/
    void setFragmentBarActionMode(boolean enabled) {
        mFragmentBarEditingMode.setVisibility(enabled ? View.VISIBLE : View.GONE);
        mFragmentBar.setVisibility(enabled ? View.GONE : View.VISIBLE);
    }

    public void show(FragmentManager manager) {
        super.show(manager, TAG);
    }
}
