package com.darwish.nppsim;

/**
 * 整机初始化自检（2026-10-27）。
 *
 * 不靠界面肉眼看：把每个系统"开机头几秒"的状态打出来，逐项做合理性判断。
 * 重点是**凝汽器真空**——口径与原版 jar 一致（真空表读 kPa 绝压、低真空报警 23 kPa、
 * 汽轮机低真空跳闸带 rpm>300 前提），即：冷态开机时凝汽器是常压、没有真空，
 * 靠真空泵/抽气器把空气抽走才建立真空。
 *
 * 用法：
 *   javac -encoding UTF-8 -cp "target\RBMK-1500-Simulator-1.00Alpha-jar-with-dependencies.jar" ^
 *         -d target\verify tools\dev\SystemInitCheck.java
 *   java -cp "target\RBMK-1500-Simulator-1.00Alpha-jar-with-dependencies.jar;target\verify" ^
 *        com.darwish.nppsim.SystemInitCheck
 */
public class SystemInitCheck {

    static final double P_ATM = 0.10142;      // 大气压 MPa（原版口径）
    static int fails = 0;
    static int checks = 0;

    static void check(String what, boolean ok, String detail) {
        checks++;
        if (!ok) {
            fails++;
        }
        System.out.printf("  [%s] %-44s %s%n", ok ? "OK" : "FAIL", what, detail);
    }

    static boolean num(double v) {
        return !Double.isNaN(v) && !Double.isInfinite(v);
    }

    static String f(double v, int d) {
        if (!num(v)) {
            return "NaN";
        }
        return String.format("%." + d + "f", v);
    }

    public static void main(String[] a) throws Exception {
        Localization.init();
        UiStyle.applyLaf();
        Fonts.init();
        Loader.tables = new SteamTables();
        Loader.soundProvider = new SoundProvider();
        new NPPSim();

        // 第一拍就读：冷态应该是"常压、没有真空"
        double pCold = NPPSim.tg1.condenser.getPressure();
        System.out.println("================ 系统初始化自检 ================");
        System.out.println("[凝汽器 / 真空]");
        check("冷态凝汽器 = 常压（无真空）", num(pCold) && pCold > 0.06,
                f(pCold * 1000, 1) + " kPa  真空=" + f((1 - pCold / P_ATM) * 100, 1) + "%"
                        + "  空气=" + f(NPPSim.tg1.condenser.getGasMassKg(), 0) + " kg");

        Thread.sleep(1500);          // ~30 tick：真空泵默认**停运**，凝汽器应一直保持常压

        check("真空泵默认停运", !NPPSim.vacuumPump1.isRunning() && !NPPSim.vacuumPump2.isRunning(),
                "running=" + NPPSim.vacuumPump1.isRunning() + " / " + NPPSim.vacuumPump2.isRunning());
        check("泵停运 1.5 s 后仍为常压（没抽气）",
                NPPSim.tg1.condenser.getPressure() > 0.06,
                f(NPPSim.tg1.condenser.getPressure() * 1000, 1) + " kPa");

        dumpBank("TG-1", NPPSim.tg1);
        dumpBank("TG-2", NPPSim.tg2);
        dumpVacuum("TG-1", NPPSim.vacuumPump1, false);
        dumpVacuum("TG-2", NPPSim.vacuumPump2, false);

        // ---------------- 2. 一回路 ----------------
        System.out.println("[一回路：堆芯 / MCP 鼓泡塔 / 汽水分离器 / 换热器]");
        Core core = NPPSim.core;
        check("堆芯热功率 MW", num(core.getThermalPower()) && core.getThermalPower() >= 0,
                f(core.getThermalPower(), 1));
        check("堆芯中子计数", num(core.getNeutronCount()), f(core.getNeutronCount(), 3));
        MCC mcc = NPPSim.mcc;
        MCC.SeparatorDrum[] drums = {mcc.drum1, mcc.drum2};
        for (int i = 0; i < drums.length; i++) {
            // 原版 jar 的鼓泡塔也是"汽空间充满空气、20 °C 水"，即冷态常压
            check("MCP 鼓泡塔#" + (i + 1) + " 冷态常压 MPa",
                    num(drums[i].getPressure()) && drums[i].getPressure() > 0.09
                            && drums[i].getPressure() < 0.12,
                    f(drums[i].getPressure(), 3) + "  水温=" + f(drums[i].getWaterTemperature(), 1)
                            + "C  水位=" + f(drums[i].getWaterLevel(), 0) + "%");
        }
        for (Separator s : NPPSim.separators) {
            // 冷态从常压起步（与原版 jar 的汽包初值一致），产汽后才升到 4.0 MPa 设定值
            check("汽水分离器 " + s.id + " 冷态常压 MPa",
                    num(s.pressureMPa) && s.pressureMPa > 0.05 && s.pressureMPa < 0.3,
                    f(s.pressureMPa, 3) + "  出口汽=" + f(s.outSteamKgs, 0) + "kg/s  水位=" + f(s.waterLevel, 0) + "%");
        }
        for (int i = 0; i < NPPSim.heatExchangers.size(); i++) {
            HeatExchanger h = NPPSim.heatExchangers.get(i);
            check("换热器#" + (i + 1) + " 二次侧压力 MPa",
                    num(h.getPressure()) && h.getPressure() > 0.05 && h.getPressure() < 0.3,
                    f(h.getPressure(), 2) + "  汽温=" + f(h.getSteamTemperature(), 1) + "C");
        }

        // ---------------- 3. 二回路 ----------------
        System.out.println("[二回路：除氧器 / 低压加热器 / 过滤段 / 泵]");
        for (int i = 0; i < NPPSim.dearators.size(); i++) {
            Dearator d = NPPSim.dearators.get(i);
            check("除氧器#" + (i + 1) + " 压力 MPa",
                    num(d.getPressure()) && d.getPressure() > 0.05 && d.getPressure() < 1.2,
                    f(d.getPressure(), 3) + "  水温=" + f(d.getWaterTemperature(), 1)
                            + "C  水位=" + f(d.getWaterLevel(), 0) + "%");
        }
        check("低压加热器 LP-1 压力 MPa",
                num(NPPSim.lpHeater.getPressure()) && NPPSim.lpHeater.getPressure() > 0.05,
                f(NPPSim.lpHeater.getPressure(), 3));
        check("凝结水过滤段 水位 %", num(NPPSim.pcsCondensateFilter.getWaterLevel()),
                f(NPPSim.pcsCondensateFilter.getWaterLevel(), 0) + "%");
        int fwBad = 0;
        double fwFlow = 0;
        for (Pump p : NPPSim.mainFeedwaterPumps) {
            if (p == null) {
                continue;
            }
            if (!num(p.getFlowRate())) {
                fwBad++;
            } else {
                fwFlow += p.getFlowRate();
            }
        }
        check("主给水泵 流量读数有效", fwBad == 0, "异常=" + fwBad + " 台，合计=" + f(fwFlow, 0) + " kg/s");
        int cpBad = 0;
        double cpFlow = 0;
        for (java.util.ArrayList<Pump> grp : new java.util.ArrayList[]{NPPSim.condensate1A,
                NPPSim.condensate1B, NPPSim.condensate2A, NPPSim.condensate2B}) {
            if (grp == null) {
                continue;
            }
            for (Pump p : grp) {
                if (p == null) {
                    continue;
                }
                if (!num(p.getFlowRate())) {
                    cpBad++;
                } else {
                    cpFlow += p.getFlowRate();
                }
            }
        }
        check("凝结水泵 流量读数有效", cpBad == 0, "异常=" + cpBad + " 台，合计=" + f(cpFlow, 0) + " kg/s");

        // ---------------- 3b. PCS / 除盐水箱 / 母管 / 大气 ----------------
        System.out.println("[PCS / 除盐水箱 / 母管 / 大气]");
        check("除盐水箱 水位 %", num(NPPSim.pcs.demineralizedWaterTank.getWaterLevel()),
                f(NPPSim.pcs.demineralizedWaterTank.getWaterLevel(), 0) + "%"
                        + "  水温=" + f(NPPSim.pcs.demineralizedWaterTank.getWaterTemperature(), 1) + "C");
        check("PCS 压力 MPa", num(NPPSim.pcs.pcsPressureHeader.getPressure()),
                f(NPPSim.pcs.pcsPressureHeader.getPressure(), 3));
        check("蒸汽母管 冷态常压 MPa", num(NPPSim.steamPiping.getPressure())
                        && NPPSim.steamPiping.getPressure() > 0.05 && NPPSim.steamPiping.getPressure() < 0.3,
                f(NPPSim.steamPiping.getPressure(), 3));
        check("凝结水联箱 压力 MPa", num(NPPSim.condensateHeader.getPressure()),
                f(NPPSim.condensateHeader.getPressure(), 3));
        check("大气罐 压力 MPa", Math.abs(NPPSim.atmosphere.getPressure() - P_ATM) < 0.01,
                f(NPPSim.atmosphere.getPressure(), 4));

        // ---------------- 4. 汽轮机 ----------------
        System.out.println("[汽轮机 / 发电机]");
        TG[] tgs = {NPPSim.tg1, NPPSim.tg2};
        for (int i = 0; i < tgs.length; i++) {
            TG tg = tgs[i];
            check("汽轮机#" + (i + 1) + " 转速 rpm", num(tg.getRpm()) && tg.getRpm() >= 0, f(tg.getRpm(), 0));
            check("发电机#" + (i + 1) + " 电功率 MW", num(tg.getGeneratorLoad()), f(tg.getGeneratorLoad(), 1));
            check("汽轮机#" + (i + 1) + " 进汽 kg/s", num(tg.getSteamInflow()), f(tg.getSteamInflow(), 1));
        }

        // ---------------- 5. 抽真空过程（核心：真空是"抽"出来的） ----------------
        System.out.println("[抽真空过程：手动启泵 → 压力应持续下降]");
        NPPSim.vacuumPump1.setRunning(true);      // 默认停运，测试里手动投运
        NPPSim.vacuumPump2.setRunning(true);
        Thread.sleep(600);
        dumpVacuum("TG-1@run", NPPSim.vacuumPump1, true);
        double t0 = 0;
        double prev = NPPSim.tg1.condenser.getPressure();
        boolean mono = true;
        for (int i = 0; i < 360; i++) {          // 最多等 180 s（本机约 6 Hz 帧率）
            Thread.sleep(500);
            t0 += 0.5;
            double p = NPPSim.tg1.condenser.getPressure();
            if (p > prev + 1e-6) {
                mono = false;
            }
            prev = p;
            if (p < 0.012) {
                break;
            }
        }
        check("抽真空单调下降", mono, "当前 " + f(prev * 1000, 1) + " kPa");
        check("建立工作真空 <12 kPa", prev < 0.012,
                f(prev * 1000, 2) + " kPa  真空=" + f((1 - prev / P_ATM) * 100, 1) + "%  用时≈" + f(t0, 1) + " s");
        dumpBank("TG-1@vacuum", NPPSim.tg1);

        // ---------------- 6. 真空泵停 → 漏气 → 真空缓慢破坏 ----------------
        System.out.println("[真空系统：停泵后真空应缓慢恶化（漏气），不应瞬间跳到常压]");
        NPPSim.vacuumPump1.setRunning(false);
        NPPSim.vacuumPump2.setRunning(false);
        Thread.sleep(6000);
        double pOff = NPPSim.tg1.condenser.getPressure();
        check("停泵 6 s 后仍保持真空", pOff < 0.03,
                f(pOff * 1000, 2) + " kPa（漏入 " + f(Condenser.AIR_LEAK_KGS, 3) + " kg/s·台）");
        NPPSim.vacuumPump1.setRunning(false);     // 恢复"默认停运"
        NPPSim.vacuumPump2.setRunning(false);

        System.out.printf("============ 汇总：%d 项检查，%d 项失败 ============%n", checks, fails);
        System.out.println(fails == 0 ? "SYSTEM_INIT_OK" : "SYSTEM_INIT_FAIL");
        System.exit(0);
    }

    static void dumpBank(String label, TG tg) {
        if (tg == null || tg.condenser == null) {
            check(label + " 凝汽器组存在", false, "null");
            return;
        }
        CondenserBank bank = tg.condenser;
        for (int i = 0; i < bank.size(); i++) {
            Condenser c = bank.unit(i);
            double p = c.getPressure();
            double vac = (1.0 - p / P_ATM) * 100.0;
            check(label + " 凝汽器#" + (i + 1) + " 绝压 kPa",
                    num(p) && p >= 0.0020 && p <= 0.1030,
                    f(p * 1000, 2) + " kPa  真空=" + f(vac, 1) + "%  汽温=" + f(c.getSteamTemperature(), 1)
                            + "C  热井=" + f(c.getHotwellMassKg() / 1000, 0) + "t"
                            + "  水位=" + f(c.getWaterLevel(), 0) + "%"
                            + "  空气=" + f(c.getGasMassKg(), 0) + "kg");
        }
    }

    static void dumpVacuum(String label, VacuumPump vp, boolean expectRunning) {
        if (vp == null) {
            check(label + " 真空泵存在", false, "null");
            return;
        }
        boolean ok = expectRunning
                ? (vp.isRunning() && vp.getRpm() > 0)
                : (!vp.isRunning() && vp.getRpm() < 1.0);
        check(label + " 真空泵（" + (expectRunning ? "运行" : "停运") + "）", ok,
                "running=" + vp.isRunning() + "  rpm=" + f(vp.getRpm(), 0)
                        + "  抽气=" + f(vp.getFlowRate(), 2) + " kg/s（空气）");
    }
}
