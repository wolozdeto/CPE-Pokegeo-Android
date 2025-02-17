package fr.cpe.wolodiayannis.pokemongeo.threading;

import android.content.Context;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import fr.cpe.wolodiayannis.pokemongeo.data.Datastore;
import fr.cpe.wolodiayannis.pokemongeo.entity.Ability;
import fr.cpe.wolodiayannis.pokemongeo.entity.CaughtInventory;
import fr.cpe.wolodiayannis.pokemongeo.entity.Pokemon;
import fr.cpe.wolodiayannis.pokemongeo.entity.PokemonStat;
import fr.cpe.wolodiayannis.pokemongeo.entity.Stat;
import fr.cpe.wolodiayannis.pokemongeo.entity.Type;
import fr.cpe.wolodiayannis.pokemongeo.entity.item.ItemBall;
import fr.cpe.wolodiayannis.pokemongeo.entity.item.ItemInventory;
import fr.cpe.wolodiayannis.pokemongeo.entity.item.ItemPotion;
import fr.cpe.wolodiayannis.pokemongeo.entity.item.ItemRevive;
import fr.cpe.wolodiayannis.pokemongeo.entity.lists.ItemList;
import fr.cpe.wolodiayannis.pokemongeo.fetcher.AbilitiesFetcher;
import fr.cpe.wolodiayannis.pokemongeo.fetcher.CaughtInventoryFetcher;
import fr.cpe.wolodiayannis.pokemongeo.fetcher.ItemInventoryFetcher;
import fr.cpe.wolodiayannis.pokemongeo.fetcher.ItemsFetcher;
import fr.cpe.wolodiayannis.pokemongeo.fetcher.PokemonAbilitiesFetcher;
import fr.cpe.wolodiayannis.pokemongeo.fetcher.PokemonStatsFetcher;
import fr.cpe.wolodiayannis.pokemongeo.fetcher.PokemonTypesFetcher;
import fr.cpe.wolodiayannis.pokemongeo.fetcher.PokemonsFetcher;
import fr.cpe.wolodiayannis.pokemongeo.fetcher.StatsFetcher;
import fr.cpe.wolodiayannis.pokemongeo.fetcher.TypesFetcher;

public class FetchThreading extends Threading {

    /**
     * Pokemon list.
     */
    private final AtomicReference<List<Pokemon>> pokemonList = new AtomicReference<>(new ArrayList<>());
    /**
     * Pokemon abilities.
     */
    private final AtomicReference<HashMap<Integer, List<Integer>>> pokemonAbilities = new AtomicReference<>(new HashMap<>());
    /**
     * Pokemon types.
     */
    private final AtomicReference<HashMap<Integer, List<Integer>>> pokemonTypes = new AtomicReference<>(new HashMap<>());
    /**
     * Pokemon stats.
     */
    private final AtomicReference<HashMap<Integer, List<PokemonStat>>> pokemonStats = new AtomicReference<>(new HashMap<>());
    /**
     * Caught inventory.
     */
    private final AtomicReference<CaughtInventory> caughtInventory = new AtomicReference<>(new CaughtInventory());
    /**
     * stats list.
     */
    private final List<Stat> statsList = new ArrayList<>();
    /**
     * types list.
     */
    private final List<Type> typesList = new ArrayList<>();
    /**
     * abilities list.
     */
    private final List<Ability> abilitiesList = new ArrayList<>();
    /**
     * items list.
     */
    private final ItemList itemsList = new ItemList();
    /**
     * item ball list.
     */
    private List<ItemBall> itemBallList = new ArrayList<>();
    /**
     * item potion list.
     */
    private List<ItemPotion> itemPotionList = new ArrayList<>();
    /**
     * item revive list.
     */
    private List<ItemRevive> itemReviveList = new ArrayList<>();
    /**
     * item inventory.
     */
    private ItemInventory itemInventory = new ItemInventory();

    /**
     * Constructor.
     */
    public FetchThreading() {
    }

    /**
     * Setup tasks.
     *
     * @param context The context to use.
     * @return The tasks.
     */
    @Override
    public FetchThreading setupTasks(Context context) {
        // Fetching tasks
        tasks.add(() -> {
            pokemonList.set((new PokemonsFetcher(context)).fetchAndCache());
            changeLoadingText("Discovering Pokémon...");
            setProgress();
            this.onEnd(1);
            return null;
        });

        tasks.add(() -> {
            pokemonAbilities.set((new PokemonAbilitiesFetcher(context)).fetchAndCache());
            changeLoadingText("Pokémon's abilities training...");
            setProgress();
            this.onEnd(2);
            return null;
        });

        tasks.add(() -> {
            pokemonTypes.set((new PokemonTypesFetcher(context)).fetchAndCache());
            changeLoadingText("Definition of Pokémon's types...");
            setProgress();
            this.onEnd(3);
            return null;
        });

        tasks.add(() -> {
            pokemonStats.set((new PokemonStatsFetcher(context)).fetchAndCache());
            changeLoadingText("Definition of Pokémon's stats...");
            setProgress();
            this.onEnd(4);
            return null;
        });

        tasks.add(() -> {
            statsList.addAll((new StatsFetcher(context)).fetchAndCache());
            changeLoadingText("Creation of statistics...");
            setProgress();
            this.onEnd(5);
            return null;
        });

        tasks.add(() -> {
            typesList.addAll((new TypesFetcher(context)).fetchAndCache());
            changeLoadingText("Creation of types...");
            setProgress();
            this.onEnd(6);
            return null;
        });

        tasks.add(() -> {
            itemBallList = (new ItemsFetcher(context)).fetchBall();
            itemsList.setPokeballList(itemBallList);
            changeLoadingText("Manufacturing of balls...");
            setProgress();
            this.onEnd(7);
            return null;
        });
        tasks.add(() -> {
            itemPotionList = (new ItemsFetcher(context)).fetchPotion();
            itemsList.setPotionList(itemPotionList);
            changeLoadingText("Manufacturing of potions...");
            setProgress();
            this.onEnd(8);
            return null;
        });
        tasks.add(() -> {
            itemReviveList = (new ItemsFetcher(context)).fetchRevive();
            itemsList.setReviveList(itemReviveList);
            changeLoadingText("Manufacturing of revives...");
            setProgress();
            this.onEnd(9);
            return null;
        });

        tasks.add(() -> {
            abilitiesList.addAll((new AbilitiesFetcher(context)).fetchAndCache());
            changeLoadingText("Creation of abilities...");
            setProgress();
            this.onEnd(10);
            return null;
        });
        tasks.add(() -> {
            caughtInventory.set((new CaughtInventoryFetcher(context)).fetch(Datastore.getInstance().getUser().getId()));
            changeLoadingText("Gathering of your Pokémon...");
            setProgress();
            this.onEnd(11);
            return null;
        });

        tasks.add(() -> {
            itemInventory = (new ItemInventoryFetcher(context)).fetch(Datastore.getInstance().getUser().getId());
            changeLoadingText("Gathering of your items...");
            setProgress();
            this.onEnd(12);
            return null;
        });

        return this;
    }

    /**
     * On end task.
     *
     * @param i task id.
     */
    private void onEnd(int i) {
        executorListener.onEnd(i);
    }

    /**
     * Change laoding text.
     *
     * @param s text.
     */
    private void changeLoadingText(String s) {
        executorListener.onLoadingTextChange(s);
    }

    /**
     * Set progress.
     */
    private void setProgress() {
        executorListener.onTaskendSetProgress();
    }

    /**
     * Get pokemon list.
     *
     * @return pokemon list.
     */
    public AtomicReference<List<Pokemon>> getPokemonList() {
        return pokemonList;
    }

    /**
     * Get pokemon abilities.
     *
     * @return pokemon abilities.
     */
    public AtomicReference<HashMap<Integer, List<Integer>>> getPokemonAbilities() {
        return pokemonAbilities;
    }

    /**
     * Get pokemon types.
     *
     * @return pokemon types.
     */
    public AtomicReference<HashMap<Integer, List<Integer>>> getPokemonTypes() {
        return pokemonTypes;
    }

    /**
     * Get pokemon stats.
     *
     * @return pokemon stats.
     */
    public AtomicReference<HashMap<Integer, List<PokemonStat>>> getPokemonStats() {
        return pokemonStats;
    }

    /**
     * Get caught inventory.
     *
     * @return caught inventory.
     */
    public AtomicReference<CaughtInventory> getCaughtInventory() {
        return caughtInventory;
    }

    /**
     * Get stats list.
     *
     * @return stats list.
     */
    public List<Stat> getStatsList() {
        return statsList;
    }


    /**
     * Get types list.
     *
     * @return types list.
     */
    public List<Type> getTypesList() {
        return typesList;
    }

    /**
     * Get items list.
     *
     * @return items list.
     */
    public ItemList getItemsList() {
        return itemsList;
    }

    /**
     * Get abilities list.
     *
     * @return abilities list.
     */
    public List<Ability> getAbilitiesList() {
        return abilitiesList;
    }

    /**
     * Get item inventory.
     *
     * @return item inventory.
     */
    public ItemInventory getItemInventory() {
        return itemInventory;
    }
}
