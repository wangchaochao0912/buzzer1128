package com.example.buzzer.service;

import com.example.buzzer.entity.Inventory;
import com.example.buzzer.repository.InventoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InventoryService {

    @Autowired
    private InventoryRepository inventoryRepository;

    /**
     * 保存库存数据到数据库
     */
    public List<Inventory> saveInventoryData(List<Inventory> inventoryList) {
        return inventoryRepository.saveAll(inventoryList);
    }

    /**
     * 获取所有库存数据
     */
    public List<Inventory> getAllInventory() {
        return inventoryRepository.findAll();
    }
}
