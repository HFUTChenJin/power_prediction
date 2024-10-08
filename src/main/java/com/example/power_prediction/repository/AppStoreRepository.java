package com.example.power_prediction.repository;

import com.example.power_prediction.entity.AppStore;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface AppStoreRepository extends JpaRepository<AppStore, Integer> {

    List<AppStore> findAllByState(String state);
}
