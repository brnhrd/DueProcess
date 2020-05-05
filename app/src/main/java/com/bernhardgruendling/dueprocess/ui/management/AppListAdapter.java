package com.bernhardgruendling.dueprocess.ui.management;

import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bernhardgruendling.dueprocess.R;
import com.bernhardgruendling.dueprocess.model.AppInfo;

import java.util.List;

public class AppListAdapter extends RecyclerView.Adapter<AppListAdapter.ViewHolder> {

    private final RecyclerViewClickListener itemListener;
    private final List<AppInfo> appInfos;
    private int colorPrimary;
    private int colorAccent;

    AppListAdapter(List<AppInfo> appInfos, RecyclerViewClickListener itemListener) {
        this.appInfos = appInfos;
        this.itemListener = itemListener;
    }

    public interface RecyclerViewClickListener {
        void recyclerViewToggleHiddenClicked(View v, AppInfo appInfo);

        void recyclerViewSwitchSensitiveClicked(View v, AppInfo appInfo, boolean active);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_item_row, parent, false);
        colorPrimary = ContextCompat.getColor(parent.getContext(), R.color.colorPrimary);
        colorAccent = ContextCompat.getColor(parent.getContext(), R.color.colorAccent);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AppInfo appInfo = appInfos.get(position);

        holder.tVAppName.setText(appInfo.getAppName());
        holder.iVAppIcon.setImageDrawable(appInfo.getApplicationIcon());
        holder.bHidden.setChecked(appInfo.isHidden());
        holder.sSensitive.setChecked(appInfo.isSensitive());

        if (appInfo.isSensitive()) {
            holder.sSensitive.getTrackDrawable().setColorFilter(new PorterDuffColorFilter(colorPrimary, PorterDuff.Mode.SRC_IN));
        } else {
            holder.sSensitive.getTrackDrawable().setColorFilter(new PorterDuffColorFilter(colorAccent, PorterDuff.Mode.SRC_IN));
        }
    }

    @Override
    public int getItemCount() {
        return appInfos.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final TextView tVAppName;
        private final ImageView iVAppIcon;
        private final ToggleButton bHidden;
        private final Switch sSensitive;


        public ViewHolder(View view) {
            super(view);
            tVAppName = view.findViewById(R.id.tVAppName);
            iVAppIcon = view.findViewById(R.id.iVAppIcon);
            bHidden = view.findViewById(R.id.toggleHidden);
            sSensitive = view.findViewById(R.id.switchSensitive);
            bHidden.setOnClickListener(this);
            sSensitive.setOnClickListener(this);

        }

        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.toggleHidden:
                    itemListener.recyclerViewToggleHiddenClicked(view, appInfos.get(this.getLayoutPosition()));
                    break;
                case R.id.switchSensitive:
                    itemListener.recyclerViewSwitchSensitiveClicked(view, appInfos.get(this.getLayoutPosition()), ((Switch) view).isChecked());
            }
        }
    }
}
