package com.ojomono.ionce.ui.roll.group

import android.os.Bundle
import android.view.*
import android.widget.ProgressBar
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.ojomono.ionce.R
import com.ojomono.ionce.databinding.FragmentGroupRollDialogBinding
import com.ojomono.ionce.models.UserItemModel
import com.ojomono.ionce.ui.bases.BaseDialogFragment
import com.ojomono.ionce.utils.StringResource
import com.ojomono.ionce.utils.TAG
import com.ojomono.ionce.ui.bases.BaseViewModel
import com.ojomono.ionce.utils.proxies.DialogShower
import com.ojomono.ionce.utils.proxies.DialogShower.withListItemsMultiChoice

/**
 * A [DialogFragment] representing the management screen for a roll group.
 * Use the [GroupRollDialogFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class GroupRollDialogFragment : BaseDialogFragment() {

    /**********************/
    /** Companion object **/
    /**********************/

    companion object {

        // Constants for setFragmentResultListener
        private const val RK_GROUP_ID = "group-id"
        private const val BK_GROUP_ID = "group-id"

        /**
         * Use this factory method to create a new instance of this fragment.
         */
        fun newInstance() = GroupRollDialogFragment()
    }

    /************/
    /** Fields **/
    /************/

    override val layoutId = R.layout.fragment_group_roll_dialog
    override lateinit var binding: FragmentGroupRollDialogBinding
    override lateinit var viewModel: GroupRollViewModel
    override val progressBar: ProgressBar? = null

    /***********************/
    /** Lifecycle methods **/
    /***********************/

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Init the viewModel using it's factory. If no taleId is found, use empty.
        viewModel = ViewModelProvider(this).get(GroupRollViewModel::class.java)
        binding = getDataBinding(inflater, container)
        binding.viewModel = viewModel
        observeEvents()

        // Set the action bar
        setActionBar(binding.toolbar, StringResource(R.string.group_roll_screen_title))

        // Populate the recycler view
        populateRecyclerView()

        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        activity?.menuInflater?.inflate(R.menu.group_roll_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_refresh -> {
                viewModel.onRefreshClicked()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    /********************************************/
    /** EventStateHolder.EventObserver methods **/
    /********************************************/

    override fun handleEvent(event: BaseViewModel.BaseEventType) {
        super.handleEvent(event)
        when (event) {
            is GroupRollViewModel.EventType.OpenQRCodeScanner -> openQRCodeScanner()
            is GroupRollViewModel.EventType.ShowUserTalesDialog -> showUserTalesDialog(event.user)
        }
    }

    /***********************/
    /** private methods **/
    /***********************/

    /**
     * Open the camera for scanning QR-codes.
     */
    private fun openQRCodeScanner() {
        setFragmentResultListener(RK_GROUP_ID) { _, bundle ->
            val result = bundle.getString(BK_GROUP_ID)
            if (result != null) {
                viewModel.joinGroup(result)
            }
        }
        QRCodeScannerDialogFragment.newInstance(RK_GROUP_ID, BK_GROUP_ID)
            .let { it.show(parentFragmentManager, it.TAG) }
    }

    /**
     * Populate the recycler view.
     */
    // TODO move to common ListFragment Interface/abstract class
    private fun populateRecyclerView() {
        with(binding.recyclerMembersList) {
            // Choose layout manager
            layoutManager = LinearLayoutManager(context)

            // Init the adapter
            viewModel.currentUser.value?.let { currentUser ->
                val usersAdapter =
                    UsersListAdapter(
                        getString(R.string.group_roll_member_list_header_text),
                        viewModel,
                        currentUser.id
                    )
                adapter = usersAdapter

                // Observe changes in members list
                viewModel.members.observe(viewLifecycleOwner, {
                    it?.let { usersAdapter.addHeaderAndSubmitList(it) }
                })
            }
        }
    }

    /**
     * Show dialog for selecting tales from the list of the [user].
     */
    private fun showUserTalesDialog(user: UserItemModel) {

        // The given user's tales
        val tales = viewModel.members.value?.find { it.id == user.id }?.tales

        // If the user has no tales, show error message
        if (tales.isNullOrEmpty())
            showMessage(getString(R.string.group_roll_member_has_no_tales_message))
        else {

            // Preselect the tales that are already in the logged-in user's collection
            val initialSelection =
                viewModel.currentUser.value?.friends?.get(user.id)?.tales?.map { heardTale ->
                    // "indexOf(element: Any)" doesn't use the object's "equals(other: Any?)"
                    tales.indexOfFirst { friendTale -> friendTale.id == heardTale.id }
                }?.toIntArray()

            // Show the dialog
            DialogShower.show(context) {
                title(R.string.group_roll_tales_dialog_title)
                message(
                    text = getString(R.string.group_roll_tales_dialog_message, user.displayName)
                )
                withListItemsMultiChoice(
                    items = tales.map { it.title },
                    initialSelection = initialSelection ?: IntArray(0),
                    allowEmptySelection = true
                ) { _, indices, _ ->
                    viewModel.setFriendTales(
                        user.id,
                        tales.filterIndexed { index, _ -> index in indices })
                }
                positiveButton(R.string.group_roll_tales_dialog_button_text)
                negativeButton(R.string.dialog_cancel)
            }
        }
    }

}