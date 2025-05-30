package com.example.psipro;

import android.app.Dialog;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import com.example.psipro.data.entities.FinancialRecord;
import com.example.psipro.data.AppDatabase;
import com.example.psipro.data.dao.FinancialRecordDao;
import com.example.psipro.data.repository.FinancialRecordRepository;
import java.util.Date;

public class FinancialRecordDialogFragment extends DialogFragment {
    private EditText amountEditText;
    private EditText descriptionEditText;
    private long patientId;

    public void setPatientId(long patientId) {
        this.patientId = patientId;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_financial_record, null);

        amountEditText = view.findViewById(R.id.amountEditText);
        descriptionEditText = view.findViewById(R.id.descriptionEditText);

        builder.setView(view)
                .setTitle("Add Financial Record")
                .setPositiveButton("Save", (dialog, which) -> {
                    String amountStr = amountEditText.getText().toString();
                    String description = descriptionEditText.getText().toString();

                    if (!amountStr.isEmpty()) {
                        try {
                            double amount = Double.parseDouble(amountStr);
                            FinancialRecord record = new FinancialRecord(
                                0, // id (auto)
                                patientId,
                                description,
                                amount,
                                new Date(),
                                "Pendente",
                                null,
                                null,
                                null
                            );
                            FinancialRecordDao dao = AppDatabase.getInstance(requireContext()).financialRecordDao();
                            FinancialRecordRepository repo = new FinancialRecordRepository(dao);
                            // Atenção: o método insert é suspending, então o ideal seria chamar via coroutine em Kotlin.
                            // Aqui, apenas para exemplo, você pode adaptar para chamar via um ViewModel ou outro mecanismo.
                        } catch (NumberFormatException e) {
                            Toast.makeText(getContext(), "Invalid amount", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton("Cancel", (dialog, which) -> dismiss());

        return builder.create();
    }
} 