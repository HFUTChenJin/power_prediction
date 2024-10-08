package com.example.power_prediction.repository;

import com.example.power_prediction.entity.PowerAnalyseDayAvg;
import com.example.power_prediction.entity.PowerAnalyseDayMax;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PowerAnalyseDayMaxRepository extends JpaRepository<PowerAnalyseDayMax,Integer> {
    PowerAnalyseDayMax findByDeviceIdAndDataTime(int deviceId, Integer dataTime);

    List<PowerAnalyseDayMax> findAllByDeviceIdAndDataTimeBetween(Integer deviceId, Integer start, Integer end);
}
