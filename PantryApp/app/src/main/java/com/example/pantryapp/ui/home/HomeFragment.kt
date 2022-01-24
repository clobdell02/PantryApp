package com.example.pantryapp.ui.home

import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.pantryapp.databinding.FragmentHomeBinding
import com.example.pantryapp.activity_additem_pantry
import com.example.pantryapp.ui.CustomExpandAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class HomeFragment : Fragment() {

    // Kotlin declaration of HomeViewModel data operation class.
    private lateinit var homeViewModel: HomeViewModel
    // Kotlin binding holds avtivities associated views.
    // See layout files for fragment_home.xml and the many associated views found there.
    private var _binding: FragmentHomeBinding? = null

    // Scope of the Coroutine. Default is the IO background scope.
    private val co_scope = CoroutineScope(Dispatchers.IO)

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    // Android lifecycle management function. Called after activity view is created and populated.
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {


        // Get view model for pantry fragment.
        homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)
        // Inflate pantry layout using kotlin bindings.
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // Gets the expandable list view element from the layout.
        val listView: ExpandableListView = binding.pantryListView
        // Create adapter instance for Pantry List UI.
        var adapter: CustomExpandAdapter? = null

        // Set data observer pattern watching pantryItems LiveData variable in HomeViewModel.
        // pantryItems is LiveData returned from the DB such that when the DB is modified, pantryItems
        //      is updated.
        // Here we observe the pantryItems livedata item, calling the UI populate function which
        //      builds a HashMap out of the db categores and db items.
        homeViewModel.pantryItems.observe(viewLifecycleOwner, Observer {
            homeViewModel.update_data()
        })


        // Observe live data from HomeViewModel. HomeViewModel supplies data to populate lists.
        // Another observer patter that notices when the above mentioned HashMap changes,
        //      rebuilding the CustomExpandAdapter with the new data and repopulating the panty ui.
        homeViewModel.foodList.observe(viewLifecycleOwner, Observer {
            // Variables for saving current Pantry List state data before updating.
            var test = false
            var state: Parcelable = listView.onSaveInstanceState()!!
            var count: Int = 0
            var extended_group: ArrayList<Boolean> = ArrayList<Boolean>()
            var visible_pos: Int = 0

            // If adapter is not null (ie. not the first build).
            if (adapter != null){
                // Save the Exandable List View state.
                state = listView.onSaveInstanceState()!!
                // Get the category count fo the adapter.
                count= adapter!!.groupCount
                // Initialize expanded boolean array for saving which categories have been expanded.
                extended_group = ArrayList<Boolean>()
                // Get the current possition of the UI list.
                visible_pos = listView.firstVisiblePosition
                // Save the expanded state of each category in the UI
                for (i in 0..(count-1)) {
                    extended_group.add(listView.isGroupExpanded(i))
                }
                // Tell the list it must reload state data.
                test = true
            }

            // Create new adapter isntance with new list, reference by it in kotlin context.
            adapter = CustomExpandAdapter(requireContext(), it)
            // Set the adapter to the expandable list view.
            listView.setAdapter(adapter)
            // Reapply all state information, expanding previous expanded categories before data
            //      was updated.
            if(test) {
                // Restore ExandableListView state.
                listView.onRestoreInstanceState(state)
                // Expand each category that was previously expanded before update.
                for (i in 0..(count-1)) {
                    val new_count = adapter!!.groupCount
                    if (i < new_count) {
                        if (extended_group[i] == true) {
                            listView.expandGroup(i)
                        }
                    }
                }
                // Reset to pre-update position of list.
                listView.setSelection(visible_pos)
            }
        })


        // Instantiate the search bar for filtering the list
        val searchView : SearchView = binding.searchView

        // Set a listener to look for any text entry into the searchview
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
                            homeViewModel.update_data()

                        // If any text is entered into the search bar
                        }else {
                            // Call the filter update function in homeViewModel which updates the display to only
                            // show items that are similar to the query
                            homeViewModel.filter_update(newText)
                            // Get the category count for the adapter.
                            val n_count = adapter!!.groupCount
                            // Set all searched categories to expand.
                            for (i in 0..(n_count-1)) {
                                listView.expandGroup(i)
                            }

                        }
                    }
                }
                // Generic return statement, no impact on functionality
                return false
            }
        })

        //Instantiation of the Button that allows for adding items to the Pantry
        val add_button : Button = binding.additemPantry
        // Set a listener to track when the button is clicked
        add_button.setOnClickListener()
        {
            // Upon click, the button creates an intent and opens the activity_additems_pantry Activity
            activity?.let{
                val intent = Intent(it, activity_additem_pantry::class.java)
                // Launch the activity
                it.startActivity(intent)
            }
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
