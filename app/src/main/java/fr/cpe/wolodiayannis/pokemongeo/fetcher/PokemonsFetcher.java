package fr.cpe.wolodiayannis.pokemongeo.fetcher;

import static fr.cpe.wolodiayannis.pokemongeo.utils.Logger.logOnUiThread;
import static fr.cpe.wolodiayannis.pokemongeo.utils.Logger.logOnUiThreadError;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

import fr.cpe.wolodiayannis.pokemongeo.data.DataFetcher;
import fr.cpe.wolodiayannis.pokemongeo.entity.Pokemon;
import fr.cpe.wolodiayannis.pokemongeo.utils.Cache;

public class PokemonsFetcher {

    private Context ctx;

    public PokemonsFetcher(Context ctx) {
        this.ctx = ctx;
    }

    public List<Pokemon> fetchAndCache() {
        List<Pokemon> pokemonList = new ArrayList<>();
        try {
            pokemonList = (List<Pokemon>) Cache.readCache(this.ctx, "data_pokemons");
        } catch (Exception e) {
            try {
                pokemonList = DataFetcher.fetchPokemonList().getPokemonList();
                Cache.writeCache(this.ctx, "data_pokemons", pokemonList);
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }
        return pokemonList;
    }
}