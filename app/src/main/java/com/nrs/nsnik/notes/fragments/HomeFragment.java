package com.nrs.nsnik.notes.fragments;


import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionMenu;
import com.nrs.nsnik.notes.NewNoteActivity;
import com.nrs.nsnik.notes.R;
import com.nrs.nsnik.notes.data.TableNames;

import org.w3c.dom.Text;

import java.util.Calendar;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;


public class HomeFragment extends Fragment {

    private static final String TAG = HomeFragment.class.getSimpleName();
    @BindView(R.id.homeAdd) FloatingActionMenu mAdd;
    @BindView(R.id.homeEmptyState) TextView mEmpty;
    private String mFolderName = "nofolder";
    private Unbinder mUnbinder;

    public HomeFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_home, container, false);
        mUnbinder = ButterKnife.bind(this, v);
        initialize();
        listeners();
        return v;
    }

    private void getArgs() {
        if (getArguments() != null) {
            mFolderName = getArguments().getString(getActivity().getResources().getString(R.string.homefldnm));
        }
    }

    private void initialize() {
        getArgs();

        NotesFragment notesFragment = new NotesFragment();
        Bundle args = new Bundle();
        args.putString(getActivity().getResources().getString(R.string.foldernamebundle), mFolderName);
        notesFragment.setArguments(args);
        getFragmentManager().beginTransaction().add(R.id.homeNotesContainer, notesFragment).commit();

        FolderFragment folderFragment = new FolderFragment();
        Bundle folderArgs = new Bundle();
        folderArgs.putString(getActivity().getResources().getString(R.string.sunFldName), mFolderName);
        folderFragment.setArguments(folderArgs);
        getFragmentManager().beginTransaction().add(R.id.homeNotesFolderContainer, folderFragment).commit();

        setFolderFab();
        setupNoteFab();
    }


    private void listeners() {
    }

    private void setupNoteFab() {
        final com.github.clans.fab.FloatingActionButton newFile = new com.github.clans.fab.FloatingActionButton(getActivity());
        newFile.setButtonSize(FloatingActionButton.SIZE_NORMAL);
        newFile.setLabelText("New Note");
        newFile.setImageResource(R.drawable.ic_note_add_black_24dp);
        newFile.setColorNormal(ContextCompat.getColor(getActivity(), R.color.colorAccent));
        newFile.setColorPressed(ContextCompat.getColor(getActivity(), R.color.colorAccent));
        mAdd.addMenuButton(newFile);
        newFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAdd.close(true);
                Intent newNote = new Intent(getActivity(), NewNoteActivity.class);
                newNote.putExtra(getActivity().getResources().getString(R.string.newnotefolderbundle),mFolderName);
                startActivity(newNote);
            }
        });
    }

    private void setFolderFab() {
        com.github.clans.fab.FloatingActionButton newFolder = new com.github.clans.fab.FloatingActionButton(getActivity());
        newFolder.setButtonSize(FloatingActionButton.SIZE_NORMAL);
        newFolder.setLabelText("New Folder");
        newFolder.setImageResource(R.drawable.ic_create_new_folder_black_24dp);
        newFolder.setColorNormal(ContextCompat.getColor(getActivity(), R.color.colorAccent));
        newFolder.setColorPressed(ContextCompat.getColor(getActivity(), R.color.colorAccent));
        mAdd.addMenuButton(newFolder);
        newFolder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAdd.close(true);
                createFolderDialog();
            }
        });
    }



    private void createFolderDialog() {
        AlertDialog.Builder newFolder = new AlertDialog.Builder(getActivity());
        final View v = LayoutInflater.from(getActivity()).inflate(R.layout.new_folder_dialog,null);
        newFolder.setView(v);
        final EditText editText = (EditText) v.findViewById(R.id.dialogFolderName);
        editText.requestFocus();
        final InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
        newFolder.setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
            }
        });
        newFolder.setPositiveButton(getResources().getString(R.string.create), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                createFolder(editText.getText().toString());
            }
        });
        newFolder.create().show();
    }

    private void createFolder(String name) {
        ContentValues cv = new ContentValues();
        cv.put(TableNames.table2.mFolderName, name);
        Calendar c = Calendar.getInstance();
        cv.put(TableNames.table2.mFolderId, c.getTimeInMillis() + name);
        cv.put(TableNames.table2.mParentFolderName, mFolderName);
        Uri u = getActivity().getContentResolver().insert(TableNames.mFolderContentUri, cv);
        if (u != null) {
            Toast.makeText(getActivity(), getResources().getString(R.string.newfoldercreated), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getActivity(), getResources().getString(R.string.newfoldernotcreated), Toast.LENGTH_SHORT).show();
        }
    }

    private void cleanUp() {
        if (mUnbinder != null) {
            mUnbinder.unbind();
        }
    }

    @Override
    public void onDestroy() {
        cleanUp();
        super.onDestroy();
    }

}
