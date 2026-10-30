package com.darwish.nppsim;

/**
 * 凝汽器热阱能力探针。
 *
 * 按额定排汽量（每台机 ~900 kg/s、165C 饱和汽）喂凝汽器，观察：
 *   凝汽器压力 / 蒸汽温度 / 热井温度 / 冷却水温度
 * 并核对热平衡：Q = m_steam·(h_g - h_f) 是否等于 m_cooling·cp·ΔT。
 *
 * 冷却水泵额定 5.14 m³/s（单台凝汽器），20C 河水。
 */
public class CondenserProbe {

    public static void main(String[] args) {
        Loader.tables = new SteamTables();
        NPPSim.tables = Loader.tables;
        NPPSim.atmosphere = new Atmosphere(15);

        TG tg = new TG(new Condenser[]{new Condenser(45.0)});
        Condenser c = tg.condenser.unit(0);
        c.condenserPump.setActive(true);   // 循环水泵必须先投运

        // 真空系统必须先投运：2026-10-27 起凝汽器按"常压起步、抽空气建立真空"建模，
        // 不抽气的话汽空间里 200 多公斤空气会让压力一直贴在常压上。
        VacuumPump vp = new VacuumPump(tg.condenser);
        vp.setRunning(true);

        double steamKgs = Double.parseDouble(System.getProperty("flow", "900"));
        int steps = Integer.parseInt(System.getProperty("steps", "12000"));   // 600 s

        for (int i = 0; i < steps; i++) {
            c.updateSteamInflow(steamKgs * 0.05, 165.0);
            c.update();
            vp.update();
            if (i % 1200 == 0 || i == steps - 1) {
                System.out.printf("t=%5.0fs 进汽 %6.0f kg/s | 凝汽器 %.4f MPa（饱和 %.1fC）| 热井 %.1fC | 空气 %.0f kg | 循环水 %6.0f kg/s active=%s%n",
                        i * 0.05, steamKgs, c.getPressure(),
                        Loader.tables.getSteamTemperatureByPressure(c.getPressure()),
                        c.getWaterTemperature(), c.getGasMassKg(),
                        c.condenserPump.getFlowRate(), c.condenserPump.isActive()
                        + " rpm=" + c.condenserPump.getRPM() + " set=" + c.condenserPump.getSetPoint()
                        + " valve=" + c.condenserPump.dischargeValve.getPosition());
                System.out.flush();
            }
        }
    }
}
