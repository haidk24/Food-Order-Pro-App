package com.example.foodorderapp.ui.admin;


import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.foodorderapp.data.model.ReportStats;
import com.example.foodorderapp.data.repository.ReportRepository;

public class ReportViewModel extends ViewModel {

    private final ReportRepository repo = new ReportRepository();

    private final MutableLiveData<ReportStats> reportStats
            = new MutableLiveData<>();
    private final MutableLiveData<Boolean>     isLoading
            = new MutableLiveData<>(false);
    private final MutableLiveData<String>      errorMessage
            = new MutableLiveData<>();

    // Kỳ hiện tại đang chọn
    private ReportRepository.Period currentPeriod
            = ReportRepository.Period.TODAY;

    public ReportViewModel() {
        loadReport(ReportRepository.Period.TODAY);
    }

    public void loadReport(ReportRepository.Period period) {
        currentPeriod = period;
        repo.loadStats(period, reportStats, isLoading, errorMessage);
    }

    public void refresh() {
        loadReport(currentPeriod);
    }

    public LiveData<ReportStats> getReportStats() { return reportStats; }
    public LiveData<Boolean>     getIsLoading()   { return isLoading; }
    public LiveData<String>      getErrorMessage(){ return errorMessage; }
}