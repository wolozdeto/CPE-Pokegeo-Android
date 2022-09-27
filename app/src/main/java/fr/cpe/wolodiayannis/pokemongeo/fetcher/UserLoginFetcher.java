package fr.cpe.wolodiayannis.pokemongeo.fetcher;

import static fr.cpe.wolodiayannis.pokemongeo.utils.Logger.logOnUiThread;
import static fr.cpe.wolodiayannis.pokemongeo.utils.Logger.logOnUiThreadError;

import android.content.Context;

import fr.cpe.wolodiayannis.pokemongeo.data.DataFetcher;
import fr.cpe.wolodiayannis.pokemongeo.entity.User;
import fr.cpe.wolodiayannis.pokemongeo.utils.Cache;

public class UserLoginFetcher {

    private Context ctx;

    public UserLoginFetcher(Context ctx) {
        this.ctx = ctx;
    }

    public void fetchAndCache(String pseudo, String password) {
        // Check if user is already cached
        try {
            User user = (User) Cache.readCache(this.ctx, "data_user");
            logOnUiThread("[CACHE] User loaded from cache");
        } catch (Exception e) {
            // If not, fetch it from the server
            try {
                User user = DataFetcher.checkUser(pseudo, password);
                Cache.writeCache(this.ctx, "data_user", user);
                logOnUiThread("[CACHE] User cached");
            } catch (Exception exception) {
                logOnUiThreadError("[CACHE] User cannot be cached : " + exception.getMessage());
                exception.printStackTrace();
            }
        }
    }
}
