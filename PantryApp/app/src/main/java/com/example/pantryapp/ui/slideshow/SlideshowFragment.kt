package com.example.pantryapp.ui.slideshow

import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ExpandableListView
import android.widget.SearchView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.pantryapp.*
import com.example.pantryapp.databinding.FragmentSlideshowBinding
import com.example.pantryapp.ui.CustomExpandAdapter
import com.example.pantryapp.ui.ShoppingExpandAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/*
The Slideshow Fragment holds the ShoppingList UI and Functionality

Functions similarly to the Pantry UI but has fewer features.
Auto-Populates Shopping List with db elements by observing LiveData from
    SlideshowViewModel.
*/
class SlideshowFragment : Fragment() {
    // Declaration of view model vairable and Kotlin data binding for xml files.
    private lateinit var slideshowViewModel: SlideshowViewModel
    private var _binding: FragmentSlideshowBinding? = null

    // Scope of the Coroutine. Default is the IO background scope.
    private val co_scope = CoroutineScope(Dispatchers.IO)

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    // Android lifecycle override, called when views are created.
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Get View model from provider.
        slideshowViewModel =
            ViewModelProvider(this).get(SlideshowViewModel::class.java)
        // Initialize xml binding using inflater.
        _binding = FragmentSlideshowBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // Gets the expandable list view element from the layout.
        val listView: ExpandableListView = binding.pantryListView

        // Adapter which populates the listView
        var adapter: ShoppingExpandAdapter? = null

        // First Observer pattern. Tracks changes to the ShoppingList Table in the db.
        // LiveData returned from the db to variable in View Model is observered, calling
        //      th UI update function when the db is updated.
        slideshowViewModel.shoppingItems.observe(viewLifecycleOwner, Observer {
            slideshowViewModel.update_data()
        })
        // Observe live data from HomeViewModel. HomeViewModel supplies data to populate lists.
        // Second oberserver pattern that tracks when the actual db data has been placed in a HashMap
        //      and assigned to LiveData in view model after the above observer is triggered.
        slideshowViewModel.foodList.observe(viewLifecycleOwner, Observer {
            // Storage for current state of ExpandableListView
            var test = false
            var state: Parcelable = listView.onSaveInstanceState()!!
            var count: Int = 0
            var extended_group: ArrayList<Boolean> = ArrayList<Boolean>()
            var visible_pos: Int = 0
            // Check that the list adapter has been initialized.
            if (adapter != null){
                // Store current state of ListView/
                state = listView.onSaveInstanceState()!!
                count= adapter!!.groupCount
                extended_group = ArrayList<Boolean>()
                visible_pos = listView.firstVisiblePosition
                // Store the expanded state of each category in the list before update.
                for (i in 0..(count-1)) {
                    extended_group.add(listView.isGroupExpanded(i))
                }
                // To inform ListView to restore state.
                test = true
            }
            // Instantiate list adapter with new HashMap reference by it.
            adapter = ShoppingExpandAdapter(requireContext(), it)
            // Set adapter to ListView.
            listView.setAdapter(adapter)
            // Restore the state of the ListView to its pre-update position and expansion.
            if(test) {
                listView.onRestoreInstanceState(state)
                // Expand each element that was saved.
                for (i in 0..(count-1)) {
                    val new_count = adapter!!.groupCount
                    if (i < new_count) {
                        if (extended_group[i] == true) {
                            listView.expandGroup(i)
                        }
                    }
                }
                // Restore the position of list.
                listView.setSelection(visible_pos)
            }
        })

        //Instantiate the search bar for filtering the list
        val searchView : SearchView = binding.searchView1

        // Set a listener to look for any text entry into the SearchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener{

            override fun onQueryTextSubmit(query: String?): Boolean {
                // This function is unused, but required for setOnQueryTextListener to work
                return false
            }

            // If the any text at all is entered into the search view, then this function is called
            override fun onQueryTextChange(newText: String): Boolean{
                co_scope.launch{
                    withContext(Dispatchers.IO){
                        // If the user deletes their search query we want to return the Shopping List to its
                        // original state
                        if(newText == ""){
                            slideshowViewModel.update_data()

                        // If any text is entered into the search bar
                        }else {
                            // Call the filter update function in slideshowViewModel which updates the display to only
                            // show items that are similar to the query
                            slideshowViewModel.filter_update(newText)
                            // Get the category count for the adapter.
                            val n_count = adapter!!.groupCount
                            // Set all searched for categories in list to expand.
                            for (i in 0..(n_count-1)) {
                                listView.expandGroup(i)
                            }
                        }
                    }
                }
                // Generic return statement, has no impact on functionality
                return false
            }
        })

        // Instantiate the button that allows the user to add items to the shopping list
        val add_button : Button = binding.additemShopping
        // Set a listener to determine if said button has been clicked
        add_button.setOnClickListener()
        {
            // Create an intent to move from the current activity to the additem_shopping
            // activity where the user can enter information
            activity?.let{
                val intent = Intent(it, activity_additem_shopping::class.java)
                // Launch the activity
                it.startActivity(intent)
            }
        }

        return root
    }

    // Call update data when the SlideshowFragment activity is resumed.
    // This updates the UI if any changes have occurred to the db while it was paused.
    override fun onResume() {
        super.onResume()
        slideshowViewModel.update_data()

    }

    // Destroy fragment when app is closed.
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}