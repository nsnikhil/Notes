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
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.jakewharton.rxbinding2.view.RxView
import com.nrs.nsnik.notes.R
import com.nrs.nsnik.notes.data.IntroData
import com.nrs.nsnik.notes.util.AppUtil
import com.nrs.nsnik.notes.data.IntroData
import com.nrs.nsnik.notes.databinding.FragmentIntroBinding // Added ViewBinding import
import com.nrs.nsnik.notes.util.AppUtil
import com.nrs.nsnik.notes.view.adapters.IntroAdapter
import com.nrs.nsnik.notes.view.customViews.CirclePagerIndicatorDecoration
import io.reactivex.disposables.CompositeDisposable
// import kotlinx.android.synthetic.main.fragment_intro.* // Removed synthetic import
// import kotlinx.android.synthetic.main.recycler_view.* // Removed synthetic import


class IntroFragment : Fragment() {

    private var _binding: FragmentIntroBinding? = null // Added nullable binding variable
    private val binding get() = _binding!! // Non-null accessor

    private lateinit var introAdapter: IntroAdapter
    private lateinit var imageList: List<String>
    private val compositeDisposable: CompositeDisposable = CompositeDisposable()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentIntroBinding.inflate(inflater, container, false) // Inflate binding
        return binding.root // Return binding root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initialize()
        listeners()
    }

    private fun initialize() {

        introAdapter = IntroAdapter()
        imageList = IntroData.introDataList

        binding.recyclerView.apply { // Use binding
            layoutManager = LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
            itemAnimator = DefaultItemAnimator()
            adapter = introAdapter
        }
        val snapHelper = PagerSnapHelper()
        snapHelper.attachToRecyclerView(binding.recyclerView) // Use binding
        binding.recyclerView.addItemDecoration(CirclePagerIndicatorDecoration()) // Use binding

        introAdapter.submitList(imageList)
    }

    private fun listeners() {
        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() { // Use binding
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                binding.introNext.visibility = if (endReached()) View.VISIBLE else View.GONE // Use binding
            }
        })
        compositeDisposable.addAll(
                RxView.clicks(binding.introNext).subscribe { // Use binding
                    AppUtil.saveIsVisitedIntro(activity)
                    activity?.findNavController(R.id.mainNavHost)?.popBackStack()
                }
        )
    }

    private fun endReached(): Boolean {
        val layoutManager = binding.recyclerView.layoutManager as LinearLayoutManager // Use binding
        val pos = layoutManager.findLastCompletelyVisibleItemPosition()
        val numItems = binding.recyclerView.adapter?.itemCount // Use binding
        return pos >= numItems?.minus(1)!!
    }

    private fun cleanUp() {
        compositeDisposable.clear()
        compositeDisposable.dispose()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Set binding to null
        cleanUp()
    }

    override fun onDestroy() {
        super.onDestroy()
        // If cleanUp has non-view related disposables, it might still be needed here.
        // For now, assuming cleanUp is mostly for RxView subscriptions tied to views.
    }
}
