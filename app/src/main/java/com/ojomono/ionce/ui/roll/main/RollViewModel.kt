package com.ojomono.ionce.ui.roll.main

import androidx.databinding.ObservableBoolean
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.ojomono.ionce.R
import com.ojomono.ionce.firebase.repositories.GroupRepository
import com.ojomono.ionce.firebase.repositories.TaleRepository
import com.ojomono.ionce.models.BaseItemModel
import com.ojomono.ionce.models.TaleItemModel
import com.ojomono.ionce.models.UserItemModel
import com.ojomono.ionce.ui.bases.BaseViewModel

class RollViewModel : BaseViewModel() {

    /*************/
    /** Classes **/
    /*************/

    // The possible roll games
    enum class Game {
        ROLL, GROUP_ROLL, TRUTH_AND_LIE;

        fun next() = values()[(ordinal + 1) % values().size]
    }

    // Types of supported events
    sealed class EventType : BaseEventType() {
        object ShowRollGroupDialog : EventType()
    }

    /**************/
    /** LiveData **/
    /**************/

    // The current roll game
    private val _currentGame = MutableLiveData<Game>().apply { postValue(Game.ROLL) }
    val currentGame: LiveData<Game> = _currentGame

    // The user's tales list and current group
    val userTales = TaleRepository.userTales
    val group = GroupRepository.model

    // The rolled tale
    private val _rolledTale = MutableLiveData<TaleItemModel?>()
    val rolledTale: LiveData<TaleItemModel?> = _rolledTale

    // The rolled lie tale
    private val _rolledLie = MutableLiveData<TaleItemModel?>()
    val rolledLie: LiveData<TaleItemModel?> = _rolledLie

    // The rolled tale owner (for group rolls)
    private val _rolledMember = MutableLiveData<UserItemModel?>()
    val rolledMember: LiveData<UserItemModel?> = _rolledMember

    // Should show the owner name on the rolled tale card? (for group rolls)
    val showOwner = ObservableBoolean().apply { set(false) }

    // Should color the rolled cards to indicate which is true? (for truth and lie)
    val showResult = ObservableBoolean().apply { set(false) }

    /***************/
    /** Observers **/
    /***************/

    // Observe tales list in case the rolled tale cover finished upload, and refresh screen
    private val listObserver =
        Observer<MutableList<TaleItemModel>?> { list ->
            list?.find { it.id == rolledTale.value?.id }
                ?.let { if (rolledTale.value != it) _rolledTale.value = it }
        }.also { userTales.observeForever(it) }

    // Put a dummy observer on the heardTales LiveData or it's value will stay null
    private val dummyObserver =
        Observer<MutableList<TaleItemModel>?> { }
            .also { TaleRepository.heardTales.observeForever(it) }

    /***********************/
    /** Lifecycle methods **/
    /***********************/

    override fun onCleared() {
        super.onCleared()
        userTales.removeObserver(listObserver)
        TaleRepository.heardTales.removeObserver(dummyObserver)
    }

    /*********************/
    /** onClick methods **/
    /*********************/

    fun onGroupFabClicked() = postEvent(EventType.ShowRollGroupDialog)
    fun onShowOwnerClicked() = showOwner.set(!showOwner.get())
    fun onChangeGameClicked() = _currentGame.postValue(currentGame.value?.next())
    fun onTalResultClicked() = showResult.set(!showResult.get())
    fun onRollClicked() =
        when (currentGame.value) {
            Game.ROLL -> rollMyTales()
            Game.GROUP_ROLL -> rollGroupRoll()
            Game.TRUTH_AND_LIE -> rollTruthAndLie()
            else -> rollMyTales()
        }

    /*******************/
    /** logic methods **/
    /*******************/

    /**
     * Roll a random tale from current user's tales.
     */
    private fun rollMyTales() =
        // If the user has no tales yet - show him an error toast
        userTales.value?.let { talesList ->
            if (talesList.isEmpty()) showMessageByResId(R.string.roll_error_no_tales)
            // If he has some - get a random one and show it's title
            else {
                cleanRollResult()
                _rolledTale.value = getRandomItem(talesList, rolledTale.value)
            }
        }

    /**
     * Roll a random tale from a random member of the current user's group.
     */
    private fun rollGroupRoll() {

        // If the user is not in a group - show him an error toast
        if (group.value == null) showMessageByResId(R.string.roll_error_no_group)
        else {
            cleanRollResult()

            // Filter out the users that has no tales
            val membersWithTales = group.value?.members?.filter { it.value.tales.isNotEmpty() }
            membersWithTales?.let { membersList ->

                // If all users in group has no tales - show an error toast
                if (membersList.isNullOrEmpty())
                    showMessageByResId(R.string.roll_error_no_tales_in_group)
                else {

                    // Roll a random user and a random tale of that user
                    _rolledMember.value =
                        getRandomItem(membersList.values.toList(), rolledMember.value)
                    rolledMember.value?.tales?.let { talesList ->
                        _rolledTale.value = getRandomItem(talesList, rolledTale.value)
                    }
                    _rolledLie.value = null // Hide second card
                }
            }
        }
    }

    /**
     * Roll a random tale from current user's tales, and another one from the user's heard tales.
     */
    private fun rollTruthAndLie() =
        // If the user has no tales yet - show him an error toast
        userTales.value?.let { userTales ->
            if (userTales.isEmpty()) showMessageByResId(R.string.roll_error_no_tales)
            // If the user has no heard tales yet - show him an error toast
            else TaleRepository.heardTales.value?.let { heardTales ->
                if (heardTales.isEmpty()) showMessageByResId(R.string.roll_error_no_heard_tales)

                // If he has both - get a random one from each list and shuffle them
                // Attention! the "lie" isn't always in shown in the "rolledLie" card
                else {
                    cleanRollResult()
                    val rolled =
                        setOf(getRandomItem(userTales), getRandomItem(heardTales)).shuffled()
                    _rolledTale.value = rolled[0]
                    _rolledLie.value = rolled[1]
                }
            }
        }

    /**
     * Get a random tale, excluding the last rolled one (unless user has only one tale).
     */
    private fun <T : BaseItemModel> getRandomItem(list: List<T>, exclude: T? = null): T? =
        when {
            list.isNullOrEmpty() -> null    // list is empty - return null
            list.size == 1 -> list[0]       // return only element of list
            else -> {
                // Exclude last rolled item and return a random item
                val listMinusExclude = if (exclude != null) list.minusElement(exclude) else list
                listMinusExclude.minusElement(exclude).random()
            }
        }

    /**
     * Return all LiveData to their default state.
     */
    private fun cleanRollResult() {
        showOwner.set(false)        // Hide owner name
        showResult.set(false)       // Reset card colors
        _rolledMember.value = null  // Hide "show owner" option
        _rolledLie.value = null     // Hide second card
    }
}