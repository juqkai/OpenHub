

package com.thirtydegreesray.openhub.ui.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.View;

import com.thirtydegreesray.openhub.R;
import com.thirtydegreesray.openhub.inject.component.AppComponent;
import com.thirtydegreesray.openhub.inject.component.DaggerFragmentComponent;
import com.thirtydegreesray.openhub.inject.module.FragmentModule;
import com.thirtydegreesray.openhub.mvp.contract.INotificationsContract;
import com.thirtydegreesray.openhub.mvp.model.Notification;
import com.thirtydegreesray.openhub.mvp.model.NotificationSubject;
import com.thirtydegreesray.openhub.mvp.model.Repository;
import com.thirtydegreesray.openhub.mvp.presenter.NotificationsPresenter;
import com.thirtydegreesray.openhub.ui.activity.CommitDetailActivity;
import com.thirtydegreesray.openhub.ui.activity.IssueDetailActivity;
import com.thirtydegreesray.openhub.ui.activity.RepositoryActivity;
import com.thirtydegreesray.openhub.ui.adapter.NotificationsAdapter;
import com.thirtydegreesray.openhub.ui.adapter.base.DoubleTypesModel;
import com.thirtydegreesray.openhub.ui.fragment.base.ListFragment;
import com.thirtydegreesray.openhub.util.BundleHelper;

import java.util.ArrayList;

/**
 * Created by ThirtyDegreesRay on 2017/11/6 20:54:31
 */

public class NotificationsFragment extends ListFragment<NotificationsPresenter, NotificationsAdapter>
        implements INotificationsContract.View {

    public enum NotificationsType{
        Unread, Participating, All
    }

    public static Fragment create(NotificationsType type){
        NotificationsFragment fragment = new NotificationsFragment();
        fragment.setArguments(BundleHelper.builder().put("type", type).build());
        return fragment;
    }

    @Override
    protected void initFragment(Bundle savedInstanceState) {
        super.initFragment(savedInstanceState);
        setLoadMoreEnable(true);
    }

    @Override
    public void showNotifications(ArrayList<DoubleTypesModel<Repository, Notification>> notifications) {
        adapter.setData(notifications);
        postNotifyDataSetChanged();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_list;
    }

    @Override
    protected void setupFragmentComponent(AppComponent appComponent) {
        DaggerFragmentComponent.builder()
                .appComponent(appComponent)
                .fragmentModule(new FragmentModule(this))
                .build()
                .inject(this);
    }

    @Override
    protected void onReLoadData() {
        mPresenter.loadNotifications(1, true);
    }

    @Override
    protected String getEmptyTip() {
        return getString(R.string.no_notifications_tip);
    }

    @Override
    public void onFragmentShowed() {
        super.onFragmentShowed();
        if(mPresenter != null) mPresenter.prepareLoadData();
    }

    @Override
    public void onItemClick(int position, @NonNull View view) {
        super.onItemClick(position, view);
        Notification notification = adapter.getData().get(position).getM2();
        Repository repository = adapter.getData().get(position).getM1();
        if(adapter.getItemViewType(position) == 0){
            RepositoryActivity.show(getActivity(), repository.getOwner().getLogin(), repository.getName());
        }else{
            String url = notification.getSubject().getUrl();
            switch (notification.getSubject().getType()){
                case Issue:
                    IssueDetailActivity.show(getActivity(), url);
                    break;
                case Commit:
                    CommitDetailActivity.show(getActivity(), url);
                    break;
                case PullRequest:
                    showInfoToast(getString(R.string.developing));
                    break;
            }

            if(notification.isUnread() &&
                    !notification.getSubject().getType().equals(NotificationSubject.Type.PullRequest)){
                mPresenter.markNotificationAsRead(notification.getId());
                notification.setUnread(false);
                adapter.notifyItemChanged(position);
            }

        }

    }
}
