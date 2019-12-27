package com.example.learnloaderconcept


import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.provider.ContactsContract.Contacts.CONTENT_URI
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.*
import android.widget.SimpleCursorAdapter
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.loader.app.LoaderManager
import androidx.loader.content.CursorLoader
import androidx.loader.content.Loader
import kotlinx.android.synthetic.main.fragment_loader.*


/**
 * A simple [Fragment] subclass.
 */
class LoaderFragment : Fragment(), SearchView.OnQueryTextListener,
    LoaderManager.LoaderCallbacks<Cursor> {

    private var simpleCursorAdapter: SimpleCursorAdapter? = null
    private var curFilter: String? = null

    private val CONTACTS_SUMMARY_PROJECTION: Array<String> = arrayOf(
        ContactsContract.Contacts._ID,
        ContactsContract.Contacts.DISPLAY_NAME,
        ContactsContract.Contacts.CONTACT_STATUS,
        ContactsContract.Contacts.CONTACT_PRESENCE,
        ContactsContract.Contacts.PHOTO_ID,
        ContactsContract.Contacts.LOOKUP_KEY
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_loader, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        // We have a menu item to show in action bar.
        setHasOptionsMenu(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        simpleCursorAdapter = SimpleCursorAdapter(
            activity, android.R.layout.simple_list_item_2, null,
            arrayOf(
                ContactsContract.Contacts.DISPLAY_NAME,
                ContactsContract.Contacts.CONTACT_STATUS
            ),
            intArrayOf(android.R.id.text1, android.R.id.text2), 0
        )

        contactListView.adapter = simpleCursorAdapter

        if (context?.let {
                ContextCompat.checkSelfPermission(
                    it,
                    Manifest.permission.READ_CONTACTS
                )
            } != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.READ_CONTACTS), 1000)
        } else {
            loaderManager.initLoader(100, null, this)
        }

        contactListView.setOnItemClickListener { _, _, _, _ ->
            Log.i("FragmentComplexList", "Item clicked: $id")
        }

        etSearchContact.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(
                s: CharSequence, start: Int,
                count: Int, after: Int
            ) {
            }

            override fun onTextChanged(
                s: CharSequence, start: Int,
                before: Int, count: Int
            ) {
                curFilter = if (s.isNotEmpty()) s.toString() else null
                loaderManager.restartLoader(100, null, this@LoaderFragment)
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        // Place an action bar item for searching.
        menu.add("Search").apply {
            setIcon(android.R.drawable.ic_menu_search)
            setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM)
            actionView = SearchView(activity).apply {
                setOnQueryTextListener(this@LoaderFragment)
            }
        }
    }

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<Cursor> {

        val baseUri: Uri = if (curFilter != null) {
            Uri.withAppendedPath(CONTENT_URI, Uri.encode(curFilter))
        } else {
            CONTENT_URI
        }

        val select = ("((" + ContactsContract.Contacts.DISPLAY_NAME + " NOTNULL) AND ("
                + ContactsContract.Contacts.HAS_PHONE_NUMBER + "=1) AND ("
                + ContactsContract.Contacts.DISPLAY_NAME + " != '' ))")

        return (activity as? Context)?.let { context ->
            CursorLoader(
                context,
                baseUri,
                CONTACTS_SUMMARY_PROJECTION,
                select,
                null,
                "${ContactsContract.Contacts.DISPLAY_NAME} COLLATE LOCALIZED ASC"
            )
        } ?: throw Exception("Activity cannot be null")

    }

    override fun onLoadFinished(loader: Loader<Cursor>, data: Cursor?) {
        simpleCursorAdapter?.swapCursor(data)
    }

    override fun onLoaderReset(loader: Loader<Cursor>) {
        simpleCursorAdapter?.swapCursor(null)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1000) {
            loaderManager.initLoader(100, null, this)
        }
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        // Called when the action bar search text has changed.  Update
        // the search filter, and restart the loader to do a new query
        // with this filter.
        curFilter = if (newText?.isNotEmpty() == true) newText else null
        loaderManager.restartLoader(0, null, this)
        return true
    }

    override fun onQueryTextSubmit(query: String): Boolean {
        // Don't care about this.
        return true
    }

}


