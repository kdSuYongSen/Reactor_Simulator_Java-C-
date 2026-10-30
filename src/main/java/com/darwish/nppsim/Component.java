package com.darwish.nppsim;

import java.io.Serializable;

abstract class Component implements Serializable {
}

abstract class WaterSteamComponent extends Component {
    protected double waterInflow, waterInflowRate;
    protected double waterOutflow, waterOutflowRate;
    protected double steamInflow, steamInflowRate;
    protected double steamOutflow, steamOutflowRate;
    protected double waterTemperature = 20.0, steamTemperature = 20.0;
    protected double waterLevel = 0.0;
    protected double pressure = 0.10142;
    
    protected void resetFlows() {
        waterInflowRate = waterInflow;
        waterOutflowRate = waterOutflow;
        steamInflowRate = steamInflow;
        steamOutflowRate = steamOutflow;
        waterInflow = 0;
        waterOutflow = 0;
        steamInflow = 0;
        steamOutflow = 0;
    }

    public double getSteamInflowRate() {
        return steamInflowRate * 20;
    }

    public double getSteamOutflowRate() {
        return steamOutflowRate * 20;
    }

    public double getWaterInflowRate() {
        return waterInflowRate * 20;
    }

    public double getWaterOutflowRate() {
        return waterOutflowRate * 20;
    }
    
    public double getWaterLevel() {
        return waterLevel;
    }
    
    public double getWaterTemperature() {
        return waterTemperature;
    }
    
    public double getSteamTemperature() {
        return steamTemperature;
    }
    
    public double getPressure() {
        return pressure;
    }
}

abstract class WaterSteamSubComponent implements Serializable {
    protected double waterInflow, waterInflowRate;
    protected double waterOutflow, waterOutflowRate;
    protected double steamInflow, steamInflowRate;
    protected double steamOutflow, steamOutflowRate;
    protected double waterTemperature = 20.0, steamTemperature = 20.0;
    protected double waterLevel = 0.0;
    protected double pressure = 0.10142;
    
    protected void resetFlows() {
        waterInflowRate = waterInflow;
        waterOutflowRate = waterOutflow;   // 修复：原先误写为 waterInflowRate，导致流出显示恒为 0
        steamInflowRate = steamInflow;
        steamOutflowRate = steamOutflow;
        waterInflow = 0;
        waterOutflow = 0;
        steamInflow = 0;
        steamOutflow = 0;
    }

    public double getSteamInflowRate() {
        return steamInflowRate * 20;
    }

    public double getSteamOutflowRate() {
        return steamOutflowRate * 20;
    }

    public double getWaterInflowRate() {
        return waterInflowRate * 20;
    }

    public double getWaterOutflowRate() {
        return waterOutflowRate * 20;
    }
    
    public double getWaterLevel() {
        return waterLevel;
    }
    
    public double getWaterTemperature() {
        return waterTemperature;
    }
    
    public double getSteamTemperature() {
        return steamTemperature;
    }
    
    public double getPressure(){
        return pressure;
    }
}
