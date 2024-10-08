package com.example.power_prediction.repository;

import com.example.power_prediction.entity.PowerAnalyseMonthMax;
import com.example.power_prediction.entity.PowerAnalyseYearAvg;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PowerAnalyseYearAvgRepository extends JpaRepository<PowerAnalyseYearAvg,Integer> {
    PowerAnalyseYearAvg findByDeviceIdAndDataTime(int deviceId, Integer dataTime);
}
