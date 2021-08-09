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

    // The user's tales list and current group
    val tales = TaleRepository.userTales
    val group = GroupRepository.model

    // The rolled tale
    private val _rolledTale = MutableLiveData<TaleItemModel>()
    val rolledTale: LiveData<TaleItemModel> = _rolledTale

    // The rolled tale owner (for group rolls)
    private val _rolledMember = MutableLiveData<UserItemModel>()
    val rolledMember: LiveData<UserItemModel> = _rolledMember

    // Should show the owner name on the rolled tale card?
    val ownerShown = ObservableBoolean().apply { set(false) }

    // Observe tales list in case the rolled tale cover finished upload, and refresh screen
    private val listObserver =
        Observer<MutableList<TaleItemModel>?> { list ->
            list?.find { it.id == rolledTale.value?.id }
                ?.let { if (rolledTale.value != it) _rolledTale.value = it }
        }.also { tales.observeForever(it) }

    // Types of supported events
    sealed class EventType : BaseEventType() {
        object ShowRollGroupDialog : EventType()
    }

    /***********************/
    /** Lifecycle methods **/
    /***********************/

    override fun onCleared() {
        super.onCleared()
        tales.removeObserver(listObserver)
    }

    /************************/
    /** post event methods **/
    /************************/

    fun onGroupFabClicked() = postEvent(EventType.ShowRollGroupDialog)

    /*******************/
    /** logic methods **/
    /*******************/

    /**
     * Toggle owner name visibility.
     */
    fun onShowOwnerClicked() = ownerShown.set(!ownerShown.get())

    /**
     * Roll a random tale, according to the current game.
     */
    fun onRollClicked() =
        if (group.value == null) rollMyTales()
        else rollGroupRoll()

    /**
     * Roll a random tale from current user's tales.
     */
    private fun rollMyTales() {
        // If the user has no tales yet - show him an error toast
        tales.value?.let { talesList ->
            if (talesList.isEmpty()) showMessageByResId(R.string.roll_error_no_tales)
            // If he has some - get a random one and show it's title
            else _rolledTale.value = getRandomItem(talesList, rolledTale.value)
        }
    }

    /**
     * Roll a random tale from a random member of the current user's group.
     */
    private fun rollGroupRoll() {
        // Always hide owner name as default
        ownerShown.set(false)

        // Filter out the users that has no tales
        val membersWithTales = group.value?.members?.filter { it.value.tales.isNotEmpty() }
        membersWithTales?.let { membersList ->

            // If all users in group has no tales - show an error toast
            if (membersList.isNullOrEmpty())
                showMessageByResId(R.string.roll_error_no_tales_in_group)
            else {

                // Roll a random user and a random tale of that user
                _rolledMember.value = getRandomItem(membersList.values.toList(), rolledMember.value)
                rolledMember.value?.tales?.let { talesList ->
                    _rolledTale.value = getRandomItem(talesList, rolledTale.value)
                }
            }
        }
    }

    /**
     * Get a random tale, excluding the last rolled one (unless user has only one tale).
     */
    private fun <T : BaseItemModel> getRandomItem(list: List<T>, exclude: T?): T? =
        when {
            list.isNullOrEmpty() -> null    // list is empty - return null
            list.size == 1 -> list[0]       // return only element of list
            else -> {
                // Exclude last rolled item and return a random item
                val listMinusExclude = if (exclude != null) list.minusElement(exclude) else list
                listMinusExclude.minusElement(exclude).random()
            }
        }

}