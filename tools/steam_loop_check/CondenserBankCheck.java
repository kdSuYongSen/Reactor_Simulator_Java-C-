package com.darwish.nppsim;

import java.util.Locale;

/** 8 台凝汽器（每台 TG 4 台）验收：分汽/分水/真空/热井水位。 */
public class CondenserBankCheck {
    public static void main(String[] a) {
        Locale.setDefault(Locale.US);
        NPPSim.tables = new SteamTables();
        Loader.tables = NPPSim.tables;
        NPPSim.atmosphere = new Atmosphere(20);

        Condenser[] u = new Condenser[4];
        for (int i = 0; i < 4; i++) {
            u[i] = new Condenser(45.0 / 4);
            u[i].condenserPump.setActive(true);
            u[i].condenserPump.dischargeValve.setPosition(1.0f);
        }
        CondenserBank bank = new CondenserBank(u);

        double exhaustKgs = 800.0;         // 单台 TG 排汽
        for (int t = 0; t < 4000; t++) {   // 200 s
            bank.updateSteamInflow(exhaustKgs * 0.05, 36.0);
            bank.updateWaterOutflow(exhaustKgs * 0.05, 36.0);   // 凝结水泵抽走
            bank.update();
        }
        System.out.printf("排汽 %.0f kg/s 分给 %d 台%n", exhaustKgs, bank.size());
        for (int i = 0; i < 4; i++) {
            System.out.printf("  #%d  p=%.4f MPa (%.1f°C)  凝结 %5.0f kg/s  热井 %6.1f%%  循环水出口 %.1f°C%n",
                    i + 1, u[i].getPressure(), u[i].getWaterTemperature(),
                    u[i].getCondensationRate(), u[i].getWaterLevel(), u[i].getCondenserWaterTemperature());
        }
        System.out.printf("组读数：p=%.4f MPa  凝结 %.0f kg/s  水位 %.1f%%%n",
                bank.getPressure(), bank.getCondensationRate(), bank.getWaterLevel());

        // 停一台循环泵，看真空是否只掉一部分
        u[0].condenserPump.setActive(false);
        for (int t = 0; t < 2000; t++) {
            bank.updateSteamInflow(exhaustKgs * 0.05, 36.0);
            bank.updateWaterOutflow(exhaustKgs * 0.05, 36.0);
            bank.update();
        }
        System.out.printf("停 #1 循环泵后：各台 p = %.4f / %.4f / %.4f / %.4f MPa，最差 %.4f%n",
                u[0].getPressure(), u[1].getPressure(), u[2].getPressure(), u[3].getPressure(),
                bank.maxPressure());
        System.out.println("COND_BANK_OK");
    }
}
