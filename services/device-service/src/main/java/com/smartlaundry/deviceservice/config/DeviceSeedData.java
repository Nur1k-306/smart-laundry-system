package com.smartlaundry.deviceservice.config;

import com.smartlaundry.deviceservice.machine.MachineService;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class DeviceSeedData {

    @Bean
    public ApplicationRunner machineSeeder(MachineService machineService) {
        return args -> {
            machineService.seedMachineIfMissing("WM-101", "Floor 1");
            machineService.seedMachineIfMissing("WM-102", "Floor 1");
            machineService.seedMachineIfMissing("WM-201", "Floor 2");
            machineService.seedMachineIfMissing("WM-202", "Floor 2");
        };
    }
}
