package fr.cpe.wolodiayannis.pokemongeo.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import fr.cpe.wolodiayannis.pokemongeo.R;
import fr.cpe.wolodiayannis.pokemongeo.adapters.InventoryListAdapter;
import fr.cpe.wolodiayannis.pokemongeo.data.Datastore;
import fr.cpe.wolodiayannis.pokemongeo.databinding.InventoryFragmentBinding;
import fr.cpe.wolodiayannis.pokemongeo.entity.item.ItemInventory;
import fr.cpe.wolodiayannis.pokemongeo.listeners.InventoryListenerInterface;
import fr.cpe.wolodiayannis.pokemongeo.listeners.InventoryUseInterface;

/**
 * Inventory Fragment.
 */
public class InventoryFragment extends Fragment {

    /**
     * Listener on click on item
     */
    private InventoryListenerInterface listener;

    /**
     * Datastore instance.
     */
    private Datastore datastore;

    /**
     * Listener on item Inventory switch (for fight fragment)
     */
    private InventoryUseInterface useListener;

    /**
     * onCreateView.
     * @param inflater inflater
     * @param container container
     * @param savedInstanceState savedInstanceState
     * @return view
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        // Get datastore instance
        this.datastore = Datastore.getInstance();
        // Bind layout
        InventoryFragmentBinding binding = DataBindingUtil.inflate(inflater, R.layout.inventory_fragment, container, false);
        // set grid layout
        binding.inventoryList.setLayoutManager(new StaggeredGridLayoutManager(2, LinearLayoutManager.VERTICAL));
        // new adapter

        ItemInventory itemInventoryPossedItem = (new ItemInventory()).getPossedItems();

        InventoryListAdapter adapter = null;
        if (listener == null) {
            adapter = new InventoryListAdapter(itemInventoryPossedItem, useListener);
        } else {
            adapter = new InventoryListAdapter(itemInventoryPossedItem, listener);
        }

        // bind adapter to recycler view
        binding.inventoryList.setAdapter(adapter);

        return binding.getRoot();
    }

    /**
     * Set listener.
     * @param listener listener
     */
    public void setListener(InventoryListenerInterface listener) {
        this.listener = listener;
        this.useListener = null;
    }

    public void setUseListener(InventoryUseInterface useListener) {
        this.useListener = useListener;
        this.listener = null;
    }
}
