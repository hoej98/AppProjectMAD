package dk.au.mad21fall.appproject.group17.adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

import dk.au.mad21fall.appproject.group17.R;
import dk.au.mad21fall.appproject.group17.models.Item;

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ItemViewHolder> {
    public interface IItemClickedListener {
        void onItemClicked(int index);
        void onCheckBoxClicked(int index);
    }

    private IItemClickedListener listener;
    private Activity activity;

    private ArrayList<Item> itemList = new ArrayList<Item>();

    public ItemAdapter(IItemClickedListener listener, Activity activity) {
        this.listener = listener;
        this.activity = activity;
    }

    public void updateItemList(ArrayList<Item> lists) {
        itemList = lists;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v;
        v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false);
        ItemViewHolder ivh = new ItemViewHolder(v, listener);
        return ivh;
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        holder.itemName.setText(itemList.get(position).getName());
        holder.itemAmount.setText(itemList.get(position).getAmount());
        holder.itemCheckBox.setChecked(itemList.get(position).getChecked());
        if (itemList.get(position).getPictureUrl() != null && !itemList.get(position).getPictureUrl().isEmpty()){
            Glide.with(this.activity).load(itemList.get(position).getPictureUrl()).into(holder.itemImage);
        } else{
            holder.itemImage.setImageResource(R.drawable.default_image);
        }
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public class ItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        ImageView itemImage;
        TextView itemName;
        TextView itemAmount;
        CheckBox itemCheckBox;

        IItemClickedListener listener;

        public ItemViewHolder(@NonNull View itemView, IItemClickedListener itemClickedListener) {
            super(itemView);

            itemName = itemView.findViewById(R.id.listItemName);
            itemAmount = itemView.findViewById(R.id.listItemAmount);
            itemCheckBox = itemView.findViewById(R.id.listItemCheckBox);
            itemImage = itemView.findViewById(R.id.listItemImage);

            listener = itemClickedListener;

            itemView.setOnClickListener(this);
            itemCheckBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listener.onCheckBoxClicked(getAdapterPosition());
                }
            });
        }

        @Override
        public void onClick(View view) {
            listener.onItemClicked(getAdapterPosition());
        }
    }
}