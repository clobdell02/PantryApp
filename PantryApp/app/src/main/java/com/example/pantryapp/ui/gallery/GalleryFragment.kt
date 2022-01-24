package com.example.pantryapp.ui.gallery

import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.pantryapp.databinding.FragmentGalleryBinding
import android.util.Log
import android.widget.*
import com.example.pantryapp.R
import com.example.pantryapp.db.AppDB
import com.example.pantryapp.db.Entities
import com.example.pantryapp.saveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class GalleryFragment() : Fragment() {

    private lateinit var galleryViewModel: GalleryViewModel

    private var _binding: FragmentGalleryBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    // listener which is triggered when the
    // time is picked from the time picker dialog
    private val timePickerDialogListener: TimePickerDialog.OnTimeSetListener =
            object : TimePickerDialog.OnTimeSetListener {
                override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {
                    // logic to properly handle
                    // the picked timings by user
                    val formattedTime: String = when {
                        hourOfDay == 0 -> {
                            if (minute < 10) {
                                "${hourOfDay + 12}:0${minute} am"
                            } else {
                                "${hourOfDay + 12}:${minute} am"
                            }
                        }
                        hourOfDay > 12 -> {
                            if (minute < 10) {
                                "${hourOfDay - 12}:0${minute} pm"
                            } else {
                                "${hourOfDay - 12}:${minute} pm"
                            }
                        }
                        hourOfDay == 12 -> {
                            if (minute < 10) {
                                "${hourOfDay}:0${minute} pm"
                            } else {
                                "${hourOfDay}:${minute} pm"
                            }
                        }
                        else -> {
                            if (minute < 10) {
                                "${hourOfDay}:${minute} am"
                            } else {
                                "${hourOfDay}:${minute} am"
                            }
                        }
                    }
                    val saveData= saveData(requireActivity().application)
                    saveData.SaveData(hourOfDay,minute)
                    saveData.setAlarm()
                }
            }
    // Scope of the Coroutine. Default is the IO background scope.
    private val co_scope = CoroutineScope(Dispatchers.Default)
    // Android lifecycle management function. Called after activity view is created and populated.
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Create an instance of the database
        val db = AppDB.AppDatabase.getInstance(requireContext())


        co_scope.launch {
            withContext(Dispatchers.IO){
                //  If the settings table is empty, add a settings entity to it.
                // If the settings table has one entity in it, do nothing. The settings
                // table has just one entry, and should never have more or less.
                if (db.settingsDao().getTableSize() != 1){
                    val sett: Entities.Settings = Entities.Settings(
                        use_user_exp = 0,
                        notify_time = 0,
                        auto_populate = 0
                    )

                    val settings_list = ArrayList<Entities.Settings>()
                    settings_list.add(sett)

                    db.settingsDao().insertItems(settings_list.toList())
                }
            }
        }

        // Get view model for settings fragment
        galleryViewModel =
            ViewModelProvider(this).get(GalleryViewModel::class.java)

        // Inflate pantry layout using Kotlin bindings
        _binding = FragmentGalleryBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // Instantiate the spinner that holds onto notification periods
        val dateSpinner = binding.datespinner

        // Create an array adapter to display the items in the spinner
        ArrayAdapter.createFromResource(
            // Require the same context as the current fragment
            requireContext(),
            // Pull information from the notificationDates array in values.xml
            R.array.notificationDates,
            // Set the layout of the items in the spinner
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            // Structure items to populate into the drop down menu and set the adapter
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            dateSpinner.adapter = adapter
        }

        //button logic to save the given time interval to send out notifications
        val buttonPickDate : Button = binding.pickdate
        buttonPickDate.setOnClickListener{
            //the dateString saves the selected time interval
            val dateString = dateSpinner.selectedItem.toString()
            //debugging log
            Log.d("gallary_frag",dateString)
            //create an instance of the saveData class
            val saveDataobj= saveData(requireActivity().application)
            //Save the given date string in saveData class
            saveDataobj.saveDate(dateString)
            //And call setAlarm to sent the given date string to alarm_receiver
            saveDataobj.setAlarm()
        }

        // Instantiate the settings switch and a default state
        val switch : Switch = binding.switch2
        var state: Int = 0
        co_scope.launch {
            withContext(Dispatchers.IO){
                // Get the current state of the switch in the database
                state = db.settingsDao().getSwitchState()
            }
            // Run the switch state change on the Main thread
            withContext(Dispatchers.Main) {
                // Set the state of the switch in the settings menu to reflect the state
                // in the database.
                switch.isChecked = state == 1
            }
        }



        // Set a listener to determine if the switch has been clicked
        switch.setOnClickListener(){
            co_scope.launch {
                withContext(Dispatchers.IO){
                    // Get the current state of the switch in the database
                    val state = db.settingsDao().getSwitchState()
                    // If the state is set to Off, then we want to turn it on as the switch has now
                    // been clicked
                    if (state == 0){
                        // Set the switch state to true in the database
                        db.settingsDao().setSwitchTrue()

                        // Debug log, no impact on functionality
                        Log.d("SwitchTrue", db.settingsDao().getSwitchState().toString())

                    // If the switch state is currently set to On, then we want to turn it off as
                    // the switch has been clicked
                    }else{
                        // Set the switch state to true in the database
                        db.settingsDao().setSwitchFalse()

                        // Debug log, no impact on functionality.
                        Log.d("SwitchFalse", db.settingsDao().getSwitchState().toString())
                    }
                }
            }
        }


        val buttonPickTime : Button = binding.picktime
        //previewSelectedTimeTextView = binding.preview_picked_time_textView)

        // handle the pick time button to
        // open the TimePickerDialog
        buttonPickTime.setOnClickListener {
            val timePicker: TimePickerDialog = TimePickerDialog(
                    // pass the Context
                    requireContext(),
                    // listener to perform task
                    // when time is picked
                    timePickerDialogListener,
                    // default hour when the time picker
                    // dialog is opened
                    12,
                    // default minute when the time picker
                    // dialog is opened
                    0,
                    // 24 hours time picker is
                    // false (varies according to the region)
                    false
            )

            // then after building the timepicker
            // dialog show the dialog to user
            timePicker.show()
        }

        return root
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

