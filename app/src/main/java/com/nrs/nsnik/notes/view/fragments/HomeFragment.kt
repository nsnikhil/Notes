/*
 * Copyright (C) 2017 nsnikhil
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package com.nrs.nsnik.notes.view.fragments


import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.nrs.nsnik.notes.R
import com.nrs.nsnik.notes.util.events.FolderClickEvent
import io.reactivex.disposables.CompositeDisposable
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

class HomeFragment : Fragment() {


    private var mFolderName: String? = "nofolder"
    private var compositeDisposable: CompositeDisposable = CompositeDisposable()


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.fragment_home, container, false)
        initialize()
        return v
    }

    private fun getArgs() {
        if (activity != null && arguments != null) {
            mFolderName = arguments?.getString(activity?.resources?.getString(R.string.homefldnm))
        }
    }

    private fun initialize() {
        getArgs()
        attachFragment(mFolderName)
    }

    private fun attachFragment(folderName: String?) {

//        if (getFragmentManager() != null && getActivity() != null) {
//
//            val bundle = Bundle()
//            bundle.putString(getActivity().getResources().getString(R.string.bundleListFragmentFolderName), folderName)
//            val listFragment = ListFragment()
//            listFragment.setArguments(bundle)
//
//            getFragmentManager()
//                    .beginTransaction()
//                    .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out, android.R.anim.fade_in, android.R.anim.fade_out)
//                    .replace(R.id.homeContainer, listFragment)
//                    .addToBackStack("backStack")
//                    .commit()
//        }
    }


    override fun onAttach(context: Context) {
        super.onAttach(context)
        EventBus.getDefault().register(this)
    }

    override fun onDetach() {
        super.onDetach()
        EventBus.getDefault().unregister(this)
    }

    @Subscribe
    fun onFolderClickEvent(folderClickEvent: FolderClickEvent) {
        mFolderName = folderClickEvent.folderName
        attachFragment(mFolderName)
    }


}