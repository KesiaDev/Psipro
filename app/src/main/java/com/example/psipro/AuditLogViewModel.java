package com.example.psipro;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import java.util.List;
import com.example.psipro.data.entities.AuditLog;
import com.example.psipro.data.repository.AuditLogRepository;
import com.example.psipro.data.AppDatabase;
import com.example.psipro.data.dao.AuditLogDao;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AuditLogViewModel extends AndroidViewModel {
    private final MutableLiveData<List<AuditLog>> auditLogs = new MutableLiveData<>();
    private final AuditLogRepository repository;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public AuditLogViewModel(Application application) {
        super(application);
        AuditLogDao dao = AppDatabase.getInstance(application).auditLogDao();
        repository = new AuditLogRepository(dao);
    }

    public LiveData<List<AuditLog>> getAuditLogs() {
        loadAuditLogs();
        return auditLogs;
    }

    private void loadAuditLogs() {
        executor.execute(() -> {
            try {
                List<AuditLog> logs = repository.getAllLogsBlocking();
                auditLogs.postValue(logs);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public void insert(AuditLog auditLog) {
        executor.execute(() -> {
            try {
                repository.insertBlocking(auditLog);
                loadAuditLogs();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
} 