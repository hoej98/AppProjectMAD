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

// identical to other adapters, except no OnItemClicked listener, as we do not want to
// do anything when an item is clicked
public class DetailRecipeAdapter extends RecyclerView.Adapter<DetailRecipeAdapter.DetailRecipeItemViewHolder> {
    private Activity activity;

    private ArrayList<Item> itemList = new ArrayList<Item>();

    public DetailRecipeAdapter(Activity activity) {
        this.activity = activity;
    }

    public void updateItemList(ArrayList<Item> lists) {
        itemList = lists;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public DetailRecipeItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v;
        v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false);
        DetailRecipeItemViewHolder aivh = new DetailRecipeItemViewHolder(v);
        return aivh;
    }

    @Override
    public void onBindViewHolder(@NonNull DetailRecipeItemViewHolder holder, int position) {
        holder.itemName.setText(itemList.get(position).getName());
        holder.itemAmount.setText(itemList.get(position).getAmount());
        holder.itemCheckBox.setChecked(false);
        holder.itemCheckBox.setEnabled(false);
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

    public class DetailRecipeItemViewHolder extends RecyclerView.ViewHolder {

        ImageView itemImage;
        TextView itemName;
        TextView itemAmount;
        CheckBox itemCheckBox;

        public DetailRecipeItemViewHolder(@NonNull View itemView) {
            super(itemView);

            itemName = itemView.findViewById(R.id.listItemName);
            itemAmount = itemView.findViewById(R.id.listItemAmount);
            itemCheckBox = itemView.findViewById(R.id.listItemCheckBox);
            itemImage = itemView.findViewById(R.id.listItemImage);
        }
    }
}
