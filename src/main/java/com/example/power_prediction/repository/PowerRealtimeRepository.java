package com.example.power_prediction.repository;

import com.example.power_prediction.entity.PowerRealtime;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PowerRealtimeRepository extends JpaRepository<PowerRealtime,Integer> {
    /**
     *
     * @param deviceId
     * @param start
     * @param end
     * @return
     */
    List<PowerRealtime> findAllByDeviceIdAndDataTimeBetween(Integer deviceId,Integer start,Integer end);

}
