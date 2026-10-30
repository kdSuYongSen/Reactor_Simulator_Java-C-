package com.darwish.nppsim;

import java.io.File;

/**
 * 无交互的存档往返检查：启动完整仿真 → save → load → 打印前后堆芯状态。
 *
 * <p>用法（用 fat jar 运行，避免手工拼依赖）：
 * <pre>
 *   javac -encoding UTF-8 -cp "target\RBMK-1500-Simulator-1.00Alpha-jar-with-dependencies.jar" ^
 *         -d target\savecheck tools\save_load_check\com\darwish\nppsim\SaveLoadCheck.java
 *   java -cp "target\RBMK-1500-Simulator-1.00Alpha-jar-with-dependencies.jar;target\savecheck" ^
 *        com.darwish.nppsim.SaveLoadCheck
 * </pre>
 */
public final class SaveLoadCheck {

    public static void main(String[] args) throws Exception {
        Loader.tables = new SteamTables();
        Loader.soundProvider = new SoundProvider();

        NPPSim sim = new NPPSim();
        Thread.sleep(1500);

        double powerBefore = NPPSim.core.getThermalPower();
        double neutronBefore = NPPSim.core.getNeutronCount();
        long timeBefore = NPPSim.autoControl.getSimulationTime();

        File file = File.createTempFile("rbmk-save-", ".ic");
        NPPSim.save(file);
        long size = file.length();
        System.out.println("[SaveLoadCheck] saved " + size + " bytes to " + file);

        NPPSim.load(file);
        Thread.sleep(1500);

        double powerAfter = NPPSim.core.getThermalPower();
        double neutronAfter = NPPSim.core.getNeutronCount();
        long timeAfter = NPPSim.autoControl.getSimulationTime();

        System.out.println("[SaveLoadCheck] before: power=" + powerBefore
                + " neutron=" + neutronBefore + " time=" + timeBefore);
        System.out.println("[SaveLoadCheck] after : power=" + powerAfter
                + " neutron=" + neutronAfter + " time=" + timeAfter);

        if (size <= 0) {
            System.out.println("[SaveLoadCheck] FAIL: empty save file");
            System.exit(2);
        }
        if (Math.abs(powerAfter - powerBefore) > 1.0) {
            System.out.println("[SaveLoadCheck] FAIL: core power not restored");
            System.exit(3);
        }
        System.out.println("[SaveLoadCheck] PASS");
        NPPSim.endSimulation();
        System.exit(0);
    }
}
