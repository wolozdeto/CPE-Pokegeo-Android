package fr.cpe.wolodiayannis.pokemongeo.data;

import fr.cpe.wolodiayannis.pokemongeo.api.request.AbilityRequest;
import fr.cpe.wolodiayannis.pokemongeo.api.request.ItemRequest;
import fr.cpe.wolodiayannis.pokemongeo.api.request.PokemonRequest;
import fr.cpe.wolodiayannis.pokemongeo.api.request.StatRequest;
import fr.cpe.wolodiayannis.pokemongeo.api.request.TypeRequest;
import fr.cpe.wolodiayannis.pokemongeo.entity.lists.AbilityList;
import fr.cpe.wolodiayannis.pokemongeo.entity.lists.ItemList;
import fr.cpe.wolodiayannis.pokemongeo.entity.lists.PokemonList;
import fr.cpe.wolodiayannis.pokemongeo.entity.lists.StatList;
import fr.cpe.wolodiayannis.pokemongeo.entity.lists.TypeList;

public class DataFetcher {

    public static DataList fetchAllData() {

        PokemonList pokemonList = PokemonRequest.getPokemons();
        ItemList itemList = ItemRequest.getAllItems();
        StatList statList = StatRequest.getAllStats();
        TypeList typeList = TypeRequest.getAllTypes();
        AbilityList abilityList = AbilityRequest.getAllAbilities();

        return new DataList(
                pokemonList.getPokemonList(),
                itemList.getItemList(),
                statList.getStatsList(),
                typeList.getTypeList(),
                abilityList.getAbilityList()
        );
    }
}