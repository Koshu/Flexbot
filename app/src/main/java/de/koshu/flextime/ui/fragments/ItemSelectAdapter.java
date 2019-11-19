package de.koshu.flextime.ui.fragments;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import de.koshu.flextime.R;

public class ItemSelectAdapter extends RecyclerView.Adapter<ItemSelectAdapter.ViewHolder> {
    private List<SelectItem> mDataset;
    private ItemSelectListener listener;

    public ItemSelectAdapter(ItemSelectListener listener, List<SelectItem> mDataset) {
        setHasStableIds(false);
        this.mDataset = mDataset;
        this.listener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent,
                                           int viewType) {

        View eventView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_select, parent, false);
        return new ViewHolder(eventView,listener);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final SelectItem obj = mDataset.get(position);
        holder.data = obj;
        holder.txtStringSelect.setText(holder.data.name);
        holder.chkStringChecked.setChecked(holder.data.checked);
    }

    public void clearAllChecks(){
        for(SelectItem item : mDataset){
            item.checked = false;
        }
    }

    public void setChecked(String name, boolean checked){
        for(SelectItem item : mDataset){
            if(item.name.equals(name)) item.checked = checked;
        }
    }

    public List<String> getCheckedItems(){
        List<String> checkedItems = new ArrayList<>();
        for(SelectItem item : mDataset){
            if(item.checked) checkedItems.add(item.name);
        }

        return checkedItems;
    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView txtStringSelect;
        public CheckBox chkStringChecked;

        public SelectItem data;
        public ItemSelectListener onClickListener;

        public ViewHolder(View itemView, final ItemSelectListener listener) {
            super(itemView);

            txtStringSelect = itemView.findViewById(R.id.txt_selectName);
            chkStringChecked = itemView.findViewById(R.id.chk_selected);

            this.onClickListener = listener;

            chkStringChecked.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    data.checked = isChecked;
                    listener.onClick(data.name, data.checked);
                }
            });
        }
    }

    public interface  ItemSelectListener{
        void onClick(String name, boolean checked);
    }
    public static class SelectItem{
        String name;
        boolean checked;

        public SelectItem(String name, boolean checked){
            this.name = name;
            this.checked = checked;
        }
    }
}