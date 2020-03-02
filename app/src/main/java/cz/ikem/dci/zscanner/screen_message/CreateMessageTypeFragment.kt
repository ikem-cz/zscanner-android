package cz.ikem.dci.zscanner.screen_message

import android.content.Context
import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import cz.ikem.dci.zscanner.OnCreateMessageViewsInteractionListener
import cz.ikem.dci.zscanner.R
import cz.ikem.dci.zscanner.persistence.DocumentType
import kotlinx.android.synthetic.main.fragment_message_properties.*



class CreateMessageTypeFragment : Fragment() {

    private lateinit var mViewModel: CreateMessageViewModel

    private var listener: OnCreateMessageViewsInteractionListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        activity?.let { _activity ->
            mViewModel = ViewModelProviders.of(_activity).get(CreateMessageViewModel::class.java)

        }
        super.onCreate(savedInstanceState)

    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_message_properties, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        createDocumentTypesAdapter(this.requireContext())

    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnCreateMessageViewsInteractionListener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement OnCreateMessageViewsInteractionListener")
        }
    }


    override fun onDetach() {
        super.onDetach()
        listener = null
    }


    private fun createDocumentTypesAdapter(context: Context) {
        val typesAdapter = TypesAdapter(context)

        typesAdapter.onItemSelected = { docType ->
            //val isSubTypeNull = (type.subtypeId == null)

            mViewModel.type.postValue(docType.id)

            if (docType.subtype == null) {
                //TODO
                // send the POST request
                Log.e("DEBUGGING", "send the POST request")
            } else {
                val action = CreateMessageTypeFragmentDirections.actionCreateMessageTypeFragmentToCreateMessageSubTypeFragment(docType.subtype)
                findNavController().navigate(action)
            }

        }

        document_types_recycler_view.adapter = typesAdapter
        document_types_recycler_view.layoutManager = LinearLayoutManager(context)

        // display possible types
        mViewModel.storedTypes.observe(viewLifecycleOwner, androidx.lifecycle.Observer { list: List<DocumentType>? ->
            typesAdapter.submitList(list)
        })
    }

    companion object {
        const val EXTRA_DEPARTMENT = "extra_department"
    }
}
