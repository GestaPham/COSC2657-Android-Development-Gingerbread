package com.gingerbread.asm3.Views.Calendar;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.gingerbread.asm3.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class AddMemoryBottomSheetDialog extends BottomSheetDialogFragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v =inflater.inflate(R.layout.add_memory_modal,container,false);
        EditText dateInput =v.findViewById(R.id.dateInput);
        EditText noteInput = v.findViewById(R.id.noteInput);
        EditText memoryNameInput = v.findViewById(R.id.memoryNameInput);
        ImageButton addPhotoButton = v.findViewById(R.id.addPhotoButton);
        Button addMemoryButton = v.findViewById(R.id.addMemoryButton);
        addPhotoButton.setOnClickListener(view->{

        });
        addMemoryButton.setOnClickListener(view->{
            String date = dateInput.getText().toString();
            String note = noteInput.getText().toString();
            String name = memoryNameInput.getText().toString();

            listener.onMemoryAdded(name, note, date);
            dismiss();
        });
        return v;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            listener = (AddMemoryListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement AddMemoryListener");
        }
    }

    public interface AddMemoryListener{
        void onMemoryAdded(String name,String note,String date);

    }
    private AddMemoryListener listener;


}
