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

import java.util.ArrayList;

import com.bumptech.glide.Glide;

import dk.au.mad21fall.appproject.group17.R;
import dk.au.mad21fall.appproject.group17.models.Item;

public class AddItemAdapter extends RecyclerView.Adapter<AddItemAdapter.AddItemViewHolder> {
    public interface IAddItemClickedListener {
        void onAddItemClicked(int index);
    }

    private IAddItemClickedListener listener;
    private Activity activity;

    private ArrayList<Item> itemList = new ArrayList<Item>();

    public AddItemAdapter(IAddItemClickedListener listener, Activity activity) {
        this.listener = listener;
        this.activity = activity;
    }

    public void updateItemList(ArrayList<Item> lists) {
        itemList = lists;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public AddItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v;
        v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false);
        AddItemViewHolder aivh = new AddItemViewHolder(v, listener);
        return aivh;
    }

    @Override
    public void onBindViewHolder(@NonNull AddItemViewHolder holder, int position) {
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

    public class AddItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        ImageView itemImage;
        TextView itemName;
        TextView itemAmount;
        CheckBox itemCheckBox;

        IAddItemClickedListener listener;

        public AddItemViewHolder(@NonNull View itemView, IAddItemClickedListener itemClickedListener) {
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
                    listener.onAddItemClicked(getAdapterPosition());
                }
            });
        }

        @Override
        public void onClick(View view) {
            listener.onAddItemClicked(getAdapterPosition());
        }
    }
}
