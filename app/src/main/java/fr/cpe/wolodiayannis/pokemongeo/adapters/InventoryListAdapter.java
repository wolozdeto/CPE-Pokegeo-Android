package fr.cpe.wolodiayannis.pokemongeo.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import fr.cpe.wolodiayannis.pokemongeo.R;
import fr.cpe.wolodiayannis.pokemongeo.databinding.InventoryItemBinding;
import fr.cpe.wolodiayannis.pokemongeo.entity.item.Item;
import fr.cpe.wolodiayannis.pokemongeo.entity.item.ItemInventory;
import fr.cpe.wolodiayannis.pokemongeo.viewmodel.ItemViewModel;

/**
 * Adapter for the inventory list.
 */
public class InventoryListAdapter extends RecyclerView.Adapter<InventoryListAdapter.ViewHolder> {

    /**
     * The inventory.
     */
    private final ItemInventory itemsInventory;
    /**
     * The context.
     */
    private final Context context;

    /**
     * Constructor.
     * @param itemsInventory the inventory
     * @param context the context
     */
    public InventoryListAdapter(ItemInventory itemsInventory, Context context) {
        this.itemsInventory = itemsInventory;
        this.context = context;
    }

    /**
     * Create a new ViewHolder.
     * @param parent Parent ViewGroup.
     * @param viewType ViewType.
     * @return ViewHolder.
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        InventoryItemBinding binding = DataBindingUtil.inflate(
                LayoutInflater.from(parent.getContext()),
                R.layout.inventory_item, parent, false);

        return new ViewHolder(binding);
    }

    /**
     * Bind the ViewHolder.
     * @param holder ViewHolder.
     * @param position Position.
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        // Get the item if item do not exist create empty one

        // TODO
        Item itemToAdd = new Item(0, "default", 0);

        // Set the item to the view model
        holder.viewModel.setItem(
                itemToAdd
        );
    }

    /**
     * Get the number of items in the inventory.
     * @return the number of items in the inventory.
     */
    @Override
    public int getItemCount() {
        // TODO
        return 1;
    }


    /**
     * ViewHolder for the inventory list.
     */
    static class ViewHolder extends RecyclerView.ViewHolder {
        private final InventoryItemBinding binding;
        // create
        private final ItemViewModel viewModel = new ItemViewModel();

        ViewHolder(InventoryItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            // set the view model
            this.binding.setItemViewModel(viewModel);
        }
    }
}



