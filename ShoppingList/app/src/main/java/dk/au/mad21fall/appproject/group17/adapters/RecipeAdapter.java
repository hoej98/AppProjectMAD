package dk.au.mad21fall.appproject.group17.adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

import dk.au.mad21fall.appproject.group17.R;
import dk.au.mad21fall.appproject.group17.models.Recipe;

public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder> {
    public interface IRecipeClickedListener {
        void onRecipeClicked(int index);
    }

    private IRecipeClickedListener listener;
    private Activity activity;

    private ArrayList<Recipe> recipeList = new ArrayList<Recipe>();

    public RecipeAdapter(IRecipeClickedListener listener, Activity activity) {
        this.listener = listener;
        this.activity = activity;
    }

    public void updateRecipeList(ArrayList<Recipe> lists) {
        recipeList = lists;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecipeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v;
        v = LayoutInflater.from(parent.getContext()).inflate(R.layout.recipe_list_item, parent, false);
        RecipeViewHolder rvh = new RecipeViewHolder(v, listener);
        return rvh;
    }

    @Override
    public void onBindViewHolder(@NonNull RecipeViewHolder holder, int position) {
        holder.recipeName.setText(recipeList.get(position).getName());
        if (recipeList.get(position).getPictureUrl() != null && !recipeList.get(position).getPictureUrl().isEmpty()){
            //Glide.with(this.activity).load(recipeList.get(position).getPictureUrl()).into(holder.recipeImage);

            Glide.with(this.activity)
                    .load(recipeList.get(position).getPictureUrl())
                    .placeholder(R.drawable.default_image)
                    .error(R.drawable.default_image)
                    .into(holder.recipeImage);
        } else{
            holder.recipeImage.setImageResource(R.drawable.default_image);
        }
    }

    @Override
    public int getItemCount() {
        return recipeList.size();
    }

    public class RecipeViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        ImageView recipeImage;
        TextView recipeName;

        IRecipeClickedListener listener;

        public RecipeViewHolder(@NonNull View itemView, IRecipeClickedListener recipeClickedListener) {
            super(itemView);

            recipeName = itemView.findViewById(R.id.recipeListItemName);
            recipeImage = itemView.findViewById(R.id.recipeListItemImage);

            listener = recipeClickedListener;

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            listener.onRecipeClicked(getAdapterPosition());
        }
    }
}
