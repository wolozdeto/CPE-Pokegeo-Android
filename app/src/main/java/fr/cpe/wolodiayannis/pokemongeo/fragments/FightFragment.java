package fr.cpe.wolodiayannis.pokemongeo.fragments;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import java.sql.Timestamp;
import java.util.Objects;

import fr.cpe.wolodiayannis.pokemongeo.R;
import fr.cpe.wolodiayannis.pokemongeo.activities.MainActivity;
import fr.cpe.wolodiayannis.pokemongeo.data.Datastore;
import fr.cpe.wolodiayannis.pokemongeo.databinding.PokemonFightPopupBinding;
import fr.cpe.wolodiayannis.pokemongeo.entity.CaughtPokemon;
import fr.cpe.wolodiayannis.pokemongeo.entity.Pokemon;
import fr.cpe.wolodiayannis.pokemongeo.entity.PokemonFight;
import fr.cpe.wolodiayannis.pokemongeo.entity.item.Item;
import fr.cpe.wolodiayannis.pokemongeo.entity.item.ItemBall;
import fr.cpe.wolodiayannis.pokemongeo.entity.item.ItemPotion;
import fr.cpe.wolodiayannis.pokemongeo.entity.item.ItemRevive;
import fr.cpe.wolodiayannis.pokemongeo.fetcher.CaughtInventoryFetcher;
import fr.cpe.wolodiayannis.pokemongeo.fetcher.ItemInventoryFetcher;
import fr.cpe.wolodiayannis.pokemongeo.fetcher.UserUpdateFetcher;
import fr.cpe.wolodiayannis.pokemongeo.listeners.PokemonSwitchInterface;

public class FightFragment extends Fragment {

    /**
     * Binding for fight.
     */
    private PokemonFightPopupBinding binding;

    /**
     * User pokemon.
     */
    private Pokemon userPokemon;

    /**
     * Opponent pokemon.
     */
    private Pokemon opponentPokemon;

    /**
     * Pokemon fight
     */
    PokemonFight pokemonFight;
    private Item lastItemToUse;
    private CaughtPokemon useItemOn;

    @SuppressLint("SetTextI18n")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        System.out.println("FightFragment.onCreateView");
        // Hide navigation bar
        requireActivity().findViewById(R.id.bottom_navigation).setVisibility(View.GONE);

        this.binding = DataBindingUtil.inflate(inflater, R.layout.pokemon_fight_popup, container, false);
        this.binding.enemyPokemonName.setText(
                this.opponentPokemon.getName().substring(0, 1).toUpperCase() + this.opponentPokemon.getName().substring(1)
        );
        this.binding.pokemonfightPlayerPokemonName.setText(
                this.userPokemon.getName().substring(0, 1).toUpperCase() + this.userPokemon.getName().substring(1)
        );

        Drawable drawableWildPokemon = ContextCompat.getDrawable(requireContext(), this.opponentPokemon.getImageID());
        Drawable drawableUserPokemon = ContextCompat.getDrawable(requireContext(), this.userPokemon.getImageID());
        this.binding.pokemonfightImageWildPokemon.setImageDrawable(drawableWildPokemon);
        this.binding.pokemonfightImagePlayerPokemon.setImageDrawable(drawableUserPokemon);

        CaughtPokemon userCaughtPokemon = Datastore.getInstance()
                .getCaughtInventory()
                .getCaughtPokemonFromPokemonID(this.userPokemon.getId());

        if (this.pokemonFight == null) {
            this.pokemonFight = new PokemonFight(this.userPokemon, this.opponentPokemon, userCaughtPokemon.getCurrentLifeState(), this.opponentPokemon.getHp());
            this.updateOpponentLifeBar(this.opponentPokemon, this.pokemonFight.getOpponentLifePoints());
            this.updateUserLifeBar(this.userPokemon, userCaughtPokemon);
        } else {
            this.updateLifeBarProgress();
            this.updateLifeBarColor();
        }

        // Return to map fragment
        this.binding.pokemonfightActionsBox.fightpopupButtonRun.setOnClickListener(v -> this.onEscape());

        this.binding.pokemonfightActionsBox.fightpopupButtonFight.setOnClickListener(v -> {
            this.deactivateAllButtons();

            this.playerAttack();

            // 2% chance to exit the fight and disappear from the map
            if ((int) (Math.random() * 100) < 2) {
                onEscape();
            }

            this.opponentAttack();

            // Set 3 second timeout to avoid spamming
            this.binding.pokemonfightActionsBox.fightpopupButtonFight.postDelayed(this::activeAllButtons, 100);
        });

        // On click on bag button, open a modal to select a item
        this.binding.pokemonfightActionsBox.fightpopupButtonBag.setOnClickListener(v -> {
            this.deactivateAllButtons();

            // Switch fragment without closing the current one
            InventoryFragment inventoryFragment = new InventoryFragment();
            inventoryFragment.setItemListenerFight(this::setItem);

            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, inventoryFragment)
                    .addToBackStack("fight")
                    .setReorderingAllowed(true)
                    .commit();
        });

        // On click on pokemon button, open a modal to select a pokemon
        this.binding.pokemonfightActionsBox.fightpopupButtonPokemon.setOnClickListener(v -> {
            this.deactivateAllButtons();

            // Switch fragment without closing the current one
            CaughtFragment fragment = new CaughtFragment();
            fragment.setSwitchListener(this::onSwitchPokemon);

            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack("fight")
                    .setReorderingAllowed(true)
                    .commit();

            this.activeAllButtons();
        });

        return binding.getRoot();
    }

    /**
     * On win
     */
    private void onWin() {
        Toast.makeText(requireContext(), "You win !", Toast.LENGTH_SHORT).show();
        // remove pokemon from the map
        Datastore.getInstance().getSpawnedPokemons().remove(this.opponentPokemon);

        // Update user pokemon life in the caught inventory
        this.updateUserPokemon();

        // Clear backstack
        requireActivity().getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        // set backstack to map fragment
        requireActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new MapFragment()).commit();

        // navigation bar
        requireActivity().findViewById(R.id.bottom_navigation).setVisibility(View.VISIBLE);
        requireActivity().getSupportFragmentManager().popBackStack();
    }

    /**
     * Update user pokemon in datastore
     */
    private void updateUserPokemon() {
        // Update the pokemon in the user inventory
        Datastore.getInstance().getCaughtInventory().updateCaughtPokemonLife(
                userPokemon,
                this.pokemonFight.getPlayerLifePoints()
        );

        CaughtPokemon userCaughtPokemon = Datastore.getInstance()
                .getCaughtInventory()
                .getCaughtPokemonFromPokemonID(this.userPokemon.getId());

        if (userCaughtPokemon.getCurrentLifeState() < 0) {
            userCaughtPokemon.setCurrentLifeState(0);
        }

        new Thread(() -> (new CaughtInventoryFetcher(requireContext())).updatePokemonAndCache(userCaughtPokemon)).start();
    }

    /**
     * Update user pokemon without updating db
     */
    private void updateUserPokemonWithoutDB() {
        // Update the pokemon in the user inventory
        Datastore.getInstance().getCaughtInventory().updateCaughtPokemonLife(
                userPokemon,
                this.pokemonFight.getPlayerLifePoints()
        );

        CaughtPokemon userCaughtPokemon = Datastore.getInstance()
                .getCaughtInventory()
                .getCaughtPokemonFromPokemonID(this.userPokemon.getId());

        if (userCaughtPokemon.getCurrentLifeState() < 0) {
            userCaughtPokemon.setCurrentLifeState(0);
        }
    }

    /**
     * On loose :
     * - remove pokemon from the map
     * - update user pokemon life in the caught inventory
     * - set the navigation bar visible
     */
    private void onLoose() {
        Toast.makeText(requireContext(), "You loose !", Toast.LENGTH_SHORT).show();
        // remove pokemon from the map
        Datastore.getInstance().getSpawnedPokemons().remove(this.opponentPokemon);

        // Update user pokemon life in the caught inventory
        this.updateUserPokemon();

        // Clear backstack
        requireActivity().getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        // set backstack to map fragment
        requireActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new MapFragment()).commit();

        // navigation bar
        requireActivity().findViewById(R.id.bottom_navigation).setVisibility(View.VISIBLE);

        requireActivity().getSupportFragmentManager().popBackStack();
    }

    /**
     * On capture :
     * - add pokemon to the caught inventory
     * - remove pokemon from the map
     * - update user pokemon life in the caught inventory
     * - set the navigation bar visible
     */
    private void onCapture() {
        Toast.makeText(requireContext(), "Pokemon captured !", Toast.LENGTH_SHORT).show();
        // remove pokemon from the map
        Datastore.getInstance().getSpawnedPokemons().remove(this.opponentPokemon);

        // Update user pokemon life in the caught inventory
        this.updateUserPokemon();

        this.updateCaughtPokemon();

        // navigation bar
        requireActivity().findViewById(R.id.bottom_navigation).setVisibility(View.VISIBLE);
        requireActivity().getSupportFragmentManager().popBackStack();
    }

    /**
     * Update the caught pokemon in the datastore
     */
    private void updateCaughtPokemon() {
        CaughtPokemon caughtPokemon = new CaughtPokemon(
                Datastore.getInstance().getUser().getId(),
                this.opponentPokemon.getId(),
                0,
                this.pokemonFight.getOpponentLifePoints(),
                new Timestamp(System.currentTimeMillis())
        );
        Datastore.getInstance().getCaughtInventory().addPokemon(this.opponentPokemon, caughtPokemon);

        new Thread(() -> (new CaughtInventoryFetcher(requireContext())).addPokemonAndCache(caughtPokemon)).start();
    }

    /**
     * On pokemon escape
     */
    private void onEscape() {
        Toast.makeText(requireContext(), "Pokemon escaped !", Toast.LENGTH_SHORT).show();
        // remove pokemon from the map
        Datastore.getInstance().getSpawnedPokemons().remove(this.opponentPokemon);

        // Update user pokemon life in the caught inventory
        this.updateUserPokemon();

        // navigation bar
        requireActivity().findViewById(R.id.bottom_navigation).setVisibility(View.VISIBLE);
        requireActivity().getSupportFragmentManager().popBackStack();
    }

    /**
     * Animate capture
     */
    private void animateCapture(int imageID) {
        // replace opponent pokemon with pokeball image
        Drawable pokeball = ContextCompat.getDrawable(requireContext(), imageID);

        this.binding.pokemonfightImageWildPokemon.setImageDrawable(pokeball);

        // Animate the pokeball
        this.binding.pokemonfightImageWildPokemon.animate().rotation(20).setDuration(500);
        this.binding.pokemonfightImageWildPokemon.animate().scaleX(0.5f).setDuration(500);
        this.binding.pokemonfightImageWildPokemon.animate().scaleY(0.5f).setDuration(500);
        // remove animation after 1.5s
        this.binding.pokemonfightImageWildPokemon.postDelayed(() -> {
            this.binding.pokemonfightImageWildPokemon.animate().rotation(-20).setDuration(500);
            this.binding.pokemonfightImageWildPokemon.postDelayed(() -> {
                this.binding.pokemonfightImageWildPokemon.animate().rotation(20).setDuration(500);
                this.binding.pokemonfightImageWildPokemon.postDelayed(() -> {
                    this.binding.pokemonfightImageWildPokemon.animate().rotation(0).setDuration(500);
                    this.binding.pokemonfightImageWildPokemon.animate().scaleX(1f).setDuration(500);
                    this.binding.pokemonfightImageWildPokemon.animate().scaleY(1f).setDuration(500);
                }, 500);
            }, 500);
        }, 500);
    }

    /**
     * Opponent attack
     */
    private void opponentAttack() {
        this.pokemonFight.attack(this.opponentPokemon, this.userPokemon);
        this.binding.pokemonfightLifebarPlayer.setProgress(this.pokemonFight.getPlayerLifePoints());

        // replace opponent pokemon with pokemon image
        this.binding.pokemonfightImageWildPokemon.setImageDrawable(ContextCompat.getDrawable(requireContext(), this.opponentPokemon.getImageID()));
        this.pokemonFight.attack(this.opponentPokemon, this.userPokemon);
        this.binding.pokemonfightLifebarPlayer.setProgress(this.pokemonFight.getPlayerLifePoints());

        Drawable playerProgressDrawable = this.binding.pokemonfightLifebarPlayer.getProgressDrawable().mutate();
        playerProgressDrawable.setTint(ContextCompat.getColor(requireContext(), this.pokemonFight.getPlayerProgressBarColor()));

        this.updateLifeBarProgress();
        this.updateLifeBarColor();

        this.updateUserPokemonWithoutDB();

        if (this.pokemonFight.isPlayerPokemonDead()) {
            this.onLoose();
            this.updateUserPokemon();
            requireActivity().getSupportFragmentManager().popBackStack();
        }
    }

    /**
     * Player attack
     */
    private void playerAttack() {
        this.pokemonFight.attack(this.userPokemon, this.opponentPokemon);
        this.binding.lifeBarRight.setProgress(this.pokemonFight.getOpponentLifePoints());

        Drawable enemyProgressDrawable = this.binding.lifeBarRight.getProgressDrawable().mutate();
        enemyProgressDrawable.setTint(ContextCompat.getColor(requireContext(), this.pokemonFight.getEnemyProgressBarColor()));

        this.updateLifeBarProgress();
        this.updateLifeBarColor();

        if (this.pokemonFight.isOpponentPokemonDead()) {
            this.onWin();
            this.updateUserPokemon();
            requireActivity().getSupportFragmentManager().popBackStack();
        }
    }

    /**
     * On switch pokemon
     *
     * @param pokemon       pokemon
     * @param caughtPokemon caught pokemon
     */
    private void onSwitchPokemon(Pokemon pokemon, CaughtPokemon caughtPokemon) {
        // check if the pokemon is KO
        if (caughtPokemon.getCurrentLifeState() <= 0 || pokemon == null) {
            Toast.makeText(requireContext(), "This pokemon is KO !", Toast.LENGTH_SHORT).show();
        } else {
            this.updateUserPokemon();
            this.userPokemon = pokemon;
            pokemonFight.switchPlayerPokemon(pokemon, caughtPokemon.getCurrentLifeState());
        }
        // go back to this fragment and reupdate bar
        requireActivity().getSupportFragmentManager().popBackStack();
    }

    /**
     * On using item
     */
    private void onUseItem(Item item, CaughtPokemon cp) {
        this.lastItemToUse = item;
        Datastore.getInstance().setActualItem(item);
        this.useItemOn = cp;
        // go back to this fragment and re-update bar
        requireActivity().getSupportFragmentManager().popBackStack();
    }


    /**
     * Update lifebar
     */
    private void updateLifeBarProgress() {
        this.binding.pokemonfightLifebarPlayer.setProgress(this.pokemonFight.getPlayerLifePoints());
        this.binding.lifeBarRight.setProgress(this.pokemonFight.getOpponentLifePoints());
        // reset max
        this.binding.pokemonfightLifebarPlayer.setMax(this.userPokemon.getHp());
        this.binding.lifeBarRight.setMax(this.opponentPokemon.getHp());

    }

    /**
     * Deactivate all buttons of the fight except the escape button
     */
    private void deactivateAllButtons() {
        this.binding.pokemonfightActionsBox.fightpopupButtonFight.setEnabled(false);
        this.binding.pokemonfightActionsBox.fightpopupButtonBag.setEnabled(false);
        this.binding.pokemonfightActionsBox.fightpopupButtonPokemon.setEnabled(false);
        this.binding.pokemonfightActionsBox.fightpopupButtonRun.setEnabled(false);
    }

    /**
     * Activate all buttons of the fight
     */
    private void activeAllButtons() {
        this.binding.pokemonfightActionsBox.fightpopupButtonFight.setEnabled(true);
        this.binding.pokemonfightActionsBox.fightpopupButtonBag.setEnabled(true);
        this.binding.pokemonfightActionsBox.fightpopupButtonPokemon.setEnabled(true);
        this.binding.pokemonfightActionsBox.fightpopupButtonRun.setEnabled(true);
    }

    /**
     * Update opponent life bar.
     *
     * @param opponentPokemon opponent pokemon
     * @param lifePoints      life points
     */
    private void updateOpponentLifeBar(Pokemon opponentPokemon, int lifePoints) {
        this.binding.lifeBarRight.setMax(opponentPokemon.getHp());
        this.binding.lifeBarRight.setProgress(lifePoints);
    }

    /**
     * Update user life bar.
     *
     * @param userPokemon       user pokemon
     * @param userCaughtPokemon user caught pokemon
     */
    private void updateUserLifeBar(Pokemon userPokemon, CaughtPokemon userCaughtPokemon) {
        this.binding.pokemonfightLifebarPlayer.setMax(userPokemon.getHp());
        this.binding.pokemonfightLifebarPlayer.setProgress(userCaughtPokemon.getCurrentLifeState());
    }

    /**
     * Update lifebar color according to the life points
     */
    private void updateLifeBarColor() {
        Drawable enemyProgressDrawable = this.binding.lifeBarRight.getProgressDrawable().mutate();
        enemyProgressDrawable.setTint(ContextCompat.getColor(requireContext(), this.pokemonFight.getEnemyProgressBarColor()));

        Drawable playerProgressDrawable = this.binding.pokemonfightLifebarPlayer.getProgressDrawable().mutate();
        playerProgressDrawable.setTint(ContextCompat.getColor(requireContext(), this.pokemonFight.getPlayerProgressBarColor()));
    }

    /**
     * Set the user pokemon.
     *
     * @param userPokemon user pokemon
     */
    public void setUserPokemon(Pokemon userPokemon) {
        this.userPokemon = userPokemon;
    }

    /**
     * Set the opponent pokemon.
     *
     * @param opponentPokemon opponent pokemon
     */
    public void setOpponentPokemon(Pokemon opponentPokemon) {
        this.opponentPokemon = opponentPokemon;
    }

    /**
     * On Going back to this fragment, update the lifebar
     */
    @Override
    public void onResume() {
        super.onResume();
        this.updateLifeBarProgress();
        this.updateLifeBarColor();
        System.out.println("[Fight] On resume");

        if (this.lastItemToUse != null) {
            // if the user try to use a potion on a dead pokemon
            if (this.useItemOn != null && this.useItemOn.getCurrentLifeState() <= 0 && this.lastItemToUse instanceof ItemPotion) {
                Toast.makeText(requireContext(), "You can't use a potion on a dead pokemon", Toast.LENGTH_SHORT).show();
            }
            // if the user try to use a revive on a alive pokemon
            else if (this.useItemOn != null && this.useItemOn.getCurrentLifeState() > 0 && this.lastItemToUse instanceof ItemRevive) {
                Toast.makeText(requireContext(), "You can't use a revive on a alive pokemon", Toast.LENGTH_SHORT).show();
            } else {
                this.useItem();
            }
        }
    }


    /**
     * set the item with the one chosen
     * and display caught pokemon if needed
     *
     * @param item item to set as last item to use
     */
    private void setItem(Item item) {
        this.lastItemToUse = item;
        Datastore.getInstance().setActualItem(item);

        requireActivity().getSupportFragmentManager().popBackStack();

        if (!(item instanceof ItemBall)) {
            PokemonSwitchInterface pokemonSwitchInterface = this::setCaughtPokemonToUseItem;

            CaughtFragment caughtFragment = new CaughtFragment();
            caughtFragment.setSwitchListener(pokemonSwitchInterface);

            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, caughtFragment)
                    .addToBackStack("fight")
                    .setReorderingAllowed(true)
                    .commit();
        }
    }

    /**
     * Set the caught pokemon to use item
     *
     * @param pokemon       NOT USED
     * @param caughtPokemon caught pokemon to use item on
     */
    private void setCaughtPokemonToUseItem(Pokemon pokemon, CaughtPokemon caughtPokemon) {
        this.useItemOn = caughtPokemon;
        requireActivity().getSupportFragmentManager().popBackStack();
        this.activeAllButtons();
    }

    /**
     * Use the item chose as needed (on the chosen pokemon if needed)
     */
    public void useItem() {

        Item item = this.lastItemToUse;
        CaughtPokemon cp = this.useItemOn;
        System.out.println("Using " + item.getName());
        int random = (int) (Math.random() * 100);
        double totalRate = 0;

        if (item instanceof ItemPotion) {
            if (cp != null) {


                int maxPokemonLife = Datastore.getInstance().getPokemons().get(cp.getPokemonId()).getHp();
                int currentLife = cp.getCurrentLifeState();

                // if the pokemon is already at max life
                if (currentLife == maxPokemonLife) {
                    Toast.makeText(requireContext(), "This pokemon is already at max life", Toast.LENGTH_SHORT).show();
                } else {
                    // if the pokemon is dead
                    if (currentLife <= 0) {
                        Toast.makeText(requireContext(), "This pokemon is dead", Toast.LENGTH_SHORT).show();
                    } else {

                        int newLife = Math.min(currentLife + ((ItemPotion) item).getBonus(), maxPokemonLife);

                        // if the pokemon is not dead and not at max life
                        Objects.requireNonNull(Datastore.getInstance().getCaughtInventory().getCaughtInventoryList().get(cp.getCorrespondingPokemon()))
                                .setCurrentLifeState(
                                        newLife
                                );

                        // if the pokemon to heal is the actual user pokemon
                        if (cp.getCorrespondingPokemon().equals(this.userPokemon)) {
                            pokemonFight.healPlayerPokemon(newLife);
                        }

                        Toast.makeText(requireContext(), "The potion worked", Toast.LENGTH_SHORT).show();
                    }
                }
                this.opponentAttack();
            }

        } else if (item instanceof ItemRevive) {
            if (cp != null) {
                Objects.requireNonNull(Datastore.getInstance().getCaughtInventory().getCaughtInventoryList().get(cp.getCorrespondingPokemon())).
                        setCurrentLifeState(((ItemRevive) item).getExactHpToHeal(cp.getCorrespondingPokemon()));
                this.opponentAttack();
            }
        } else if (item instanceof ItemBall) {
            // deactivating all buttons
            this.deactivateAllButtons();
            ItemBall ball = (ItemBall) item;
            this.animateCapture(ball.getImageID());

            // calculate catch rate
            double minimumRate = 0.05;
            double ballRate = ((ItemBall) item).getAccuracy() * minimumRate;
            double lifeRate = 1 - ((double) this.pokemonFight.getOpponentLifePoints() / (double) this.pokemonFight.getOpponentPokemon().getHp());
            totalRate = ballRate + (lifeRate * 0.5);

            // auto catch if the item is a master-ball
            if (item.getName().equals("master-ball")) {
                totalRate = 1;
            }

            // try to catch
            if (random < totalRate * 100) {
                // 3s time out for animation
                this.binding.pokemonfightImageWildPokemon.postDelayed(this::onCapture, 3000);
                Datastore.getInstance().getUser().addMoney(2000);
                Datastore.getInstance().getUser().addExperience(1000);
                new Thread(() -> {
                    // Give 2000 ₽ and 1000 exp to the player
                    (new UserUpdateFetcher(requireContext())).updateAndCacheMoneyAndExp(
                            Datastore.getInstance().getUser(),
                            Datastore.getInstance().getUser().getMoney(),
                            Datastore.getInstance().getUser().getExperience()
                    );
                }).start();
            } else {
                this.binding.pokemonfightImageWildPokemon.postDelayed(this::opponentAttack, 3000);
            }
        }
        // update lifeState
        this.updateLifeBarProgress();
        this.updateLifeBarColor();

        // 1% chance to exit the fight and disappear from the map
        if (random < 1 && totalRate < 1 && random > totalRate * 100) {
            this.onEscape();
        }

        // remove 1 item from the inventory and update into the API
        Datastore.getInstance().getItemInventory().removeItem(item, 1);
        new Thread(() -> (new ItemInventoryFetcher(requireContext())).updateAndCache(Datastore.getInstance().getItemInventory())).start();

        this.binding.pokemonfightImageWildPokemon.postDelayed(this::activeAllButtons, 3000);
        this.lastItemToUse = null;
        this.useItemOn = null;
    }
}
