/*
 *     Notes  Copyright (C) 2018  Nikhil Soni
 *     This program comes with ABSOLUTELY NO WARRANTY; for details type `show w'.
 *     This is free software, and you are welcome to redistribute it
 *     under certain conditions; type `show c' for details.
 *
 * The hypothetical commands `show w' and `show c' should show the appropriate
 * parts of the General Public License.  Of course, your program's commands
 * might be different; for a GUI interface, you would use an "about box".
 *
 *   You should also get your employer (if you work as a programmer) or school,
 * if any, to sign a "copyright disclaimer" for the program, if necessary.
 * For more information on this, and how to apply and follow the GNU GPL, see
 * <http://www.gnu.org/licenses/>.
 *
 *   The GNU General Public License does not permit incorporating your program
 * into proprietary programs.  If your program is a subroutine library, you
 * may consider it more useful to permit linking proprietary applications with
 * the library.  If this is what you want to do, use the GNU Lesser General
 * Public License instead of this License.  But first, please read
 * <http://www.gnu.org/philosophy/why-not-lgpl.html>.
 */

package com.nrs.nsnik.notes.view.fragments


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.nrs.nsnik.notes.R
import com.nrs.nsnik.notes.view.adapters.ImageAdapter
import com.nrs.nsnik.notes.view.listeners.AdapterType
import com.nrs.nsnik.notes.view.listeners.OnItemRemoveListener
import com.twitter.serial.serializer.CollectionSerializers
import com.twitter.serial.serializer.CoreSerializers
import com.twitter.serial.stream.bytebuffer.ByteBufferSerial
import kotlinx.android.synthetic.main.recycler_view.*


class ImageListFragment : Fragment(), OnItemRemoveListener {

    private lateinit var imageAdapter: ImageAdapter
    private lateinit var imageList: MutableList<String>
    private var position: Int = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.recycler_view, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initialize()
    }

    private fun initialize() {
        val resources = activity?.resources
        imageList = ByteBufferSerial().fromByteArray(arguments?.getByteArray(resources?.getString(R.string.bundleImageList)), CollectionSerializers.getListSerializer(CoreSerializers.STRING))!!
        position = arguments?.getInt(resources?.getString(R.string.bundleImageListPosition), 0)!!
        imageAdapter = ImageAdapter(true, this)

        recyclerView.apply {
            layoutManager = LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
            itemAnimator = DefaultItemAnimator()
            adapter = imageAdapter
            //scrollToPosition(position)
        }

        imageAdapter.submitList(imageList)
    }

    override fun onItemRemoved(position: Int, adapterType: AdapterType) {

    }

}
