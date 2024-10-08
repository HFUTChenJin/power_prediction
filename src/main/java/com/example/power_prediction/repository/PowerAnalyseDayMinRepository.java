package com.example.power_prediction.repository;

import com.example.power_prediction.entity.PowerAnalyseDayMax;
import com.example.power_prediction.entity.PowerAnalyseDayMin;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PowerAnalyseDayMinRepository extends JpaRepository<PowerAnalyseDayMin,Integer> {
    PowerAnalyseDayMin findByDeviceIdAndDataTime(int deviceId, Integer dataTime);
}
