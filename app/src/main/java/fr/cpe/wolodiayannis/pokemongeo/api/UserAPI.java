package fr.cpe.wolodiayannis.pokemongeo.api;

import fr.cpe.wolodiayannis.pokemongeo.dto.UserDto;
import fr.cpe.wolodiayannis.pokemongeo.entity.User;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface UserAPI extends BaseAPI {
    /**
     * Get one user.
     * @param email User email.
     * @param password User password.
     */
    @POST("login")
    Call<User> getUser(@Body UserDto userLoginDto);

    @POST("register")
    Call<User> createUser(@Body UserDto userDto);
}

