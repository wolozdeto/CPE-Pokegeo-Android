package fr.cpe.wolodiayannis.pokemongeo.entity.lists;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import fr.cpe.wolodiayannis.pokemongeo.entity.Type;

public class TypeList {
    /**
     * List of type
     */
    @SerializedName("data")
    private List<Type> typeList;

    /**
     * Get the type list.
     * @return List of type.
     */
    public List<Type> getTypeList() {
        return typeList;
    }
}
