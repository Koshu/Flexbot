package de.koshu.flextime.ui.fragments;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.appcompat.widget.PopupMenu;
import androidx.documentfile.provider.DocumentFile;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import de.koshu.flextime.R;

public class BackupListAdapter extends RecyclerView.Adapter<BackupListAdapter.ViewHolder>{
    private DocumentFile[] mDataset;
    private ItemMenuListener listener;
    private Context context;

    public BackupListAdapter(Context context, ItemMenuListener listener, DocumentFile[] mDataset) {
        setHasStableIds(false);
        this.mDataset = mDataset;
        this.listener = listener;
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent,
                                           int viewType) {

        View eventView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_backup, parent, false);
        return new ViewHolder(eventView,listener);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final DocumentFile obj = mDataset[position];
        holder.data = obj;
        holder.txtBackupName.setText(holder.data.getName());
    }

    @Override
    public int getItemCount() {
        return mDataset == null ? 0 : mDataset.length;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView txtBackupName;

        public DocumentFile data;
        public ItemMenuListener onClickListener;

        public ViewHolder(View itemView, final ItemMenuListener listener) {
            super(itemView);

            txtBackupName = itemView.findViewById(R.id.txt_backupName);

            this.onClickListener = listener;

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //creating a popup menu
                    PopupMenu popup = new PopupMenu(context, v);
                    //inflating menu from xml resource
                    popup.inflate(R.menu.backup_list);
                    //adding click listener
                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            switch (item.getItemId()) {
                                case R.id.nav_restore:
                                    onClickListener.onRestoreClick(data);
                                    return true;
                                case R.id.nav_delete:
                                    onClickListener.onDeleteClick(data);
                                    return true;
                                default:
                                    return false;
                            }
                        }
                    });
                    //displaying the popup
                    popup.show();
                }
            });
        }
    }

    public interface  ItemMenuListener{
        void onRestoreClick(DocumentFile name);
        void onDeleteClick(DocumentFile name);
    }
}