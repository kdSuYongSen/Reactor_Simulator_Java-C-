package com.darwish.nppsim;

/**
 * 除氧器蒸汽出口联箱的逐 tick 质量守恒测试。
 *
 * 复刻 NPPSim 的实际更新顺序：
 *   主汽门(下游) update → 除氧器出口阀(上游) update → 联箱 update
 * 检查：上游送进来的汽能不能被下游拿到；联箱是否在 update 里把未取走的汽直接丢掉。
 */
public class HeaderOrderTest {

    public static void main(String[] args) {
        Loader.tables = new SteamTables();
        NPPSim.tables = Loader.tables;

        SteamCollectorHeader header = new SteamCollectorHeader();
        Sink sink = new Sink();

        // 模拟 4 台除氧器，每台每 tick 送 27.5 kg（=550 kg/s）
        Connectable[] sources = new Connectable[4];
        for (int i = 0; i < 4; i++) {
            sources[i] = new SourceStub(27.5);
        }
        header.setSources(sources);

        // 2 只主汽门（每只 55 kg/tick = 1100 kg/s）
        SteamValve[] valves = new SteamValve[2];
        for (int i = 0; i < 2; i++) {
            valves[i] = new SteamValve(2.47e4, 20, header, sink);
            valves[i].setPosition(1.0f);
        }

        System.out.println("tick  下游取汽(kg)  上游送入(kg)  联箱余量(kg)");
        for (int tick = 0; tick < 6; tick++) {
            // 1) 下游先取（与 NPPSim 相同顺序）
            valves[0].update();
            valves[1].update();
            double drawn = valves[0].getFlowRate() + valves[1].getFlowRate();
            // 2) 上游除氧器出口阀送汽
            for (Connectable s : sources) {
                header.updateSteamInflow(27.5, 165.0);
            }
            // 3) 联箱结算
            header.update();
            System.out.printf("%4d  %10.2f  %12.1f  %12.2f%n",
                    tick, drawn, 4 * 27.5, header.getSteamMass());
        }

        double total = sink.received;
        double leftover = header.getSteamMass();
        System.out.printf("6 tick：下游取汽 %.1f kg，联箱余量 %.1f kg（送进 %d kg）%n",
                total, leftover, (int) (6 * 4 * 27.5));
        // 判定：① 下游确实拿到了汽（不是 0）；② 没被取走的汽留在联箱里而不是被清零丢掉
        boolean ok = total > 100.0 && leftover > 0.0;
        System.out.println(ok ? "PASS" : "FAIL：联箱把汽清零了，下游拿不到");
    }

    /** 只记账的汽源/汽包 */
    static class SourceStub implements Connectable, SteamSupplyLimit {
        double available;
        SourceStub(double kg) { available = kg; }
        @Override public double availableSteamKg() { return available; }
        @Override public void updateSteamOutflow(double flow, double tempC) { available = Math.max(0, available - flow); }
        @Override public void updateSteamInflow(double flow, double tempC) { }
        @Override public void updateWaterInflow(double flow, double tempC) { }
        @Override public void updateWaterOutflow(double flow, double tempC) { }
        @Override public double getPressure() { return 0.63; }
        @Override public double getSteamDensity() { return Loader.tables.getSteamDensityByPressure(0.63); }
        @Override public double getWaterDensity() { return Loader.tables.getWaterDensityByTemp(160); }
        @Override public double getSteamMass() { return 0; }
        @Override public double getWaterMass() { return 0; }
        @Override public double getSteamVolume() { return 0; }
        @Override public double getWaterTemperature() { return 160; }
        @Override public double getSteamTemperature() { return 165; }
        @Override public double getWaterLevel() { return 0; }
    }

    static class Sink implements Connectable {
        double received = 0;
        @Override public void updateSteamInflow(double flow, double tempC) { received += flow; }
        @Override public void updateSteamOutflow(double flow, double tempC) { }
        @Override public void updateWaterInflow(double flow, double tempC) { }
        @Override public void updateWaterOutflow(double flow, double tempC) { }
        @Override public double getPressure() { return 0.0061; }
        @Override public double getSteamDensity() { return Loader.tables.getSteamDensityByPressure(0.0061); }
        @Override public double getWaterDensity() { return Loader.tables.getWaterDensityByTemp(36); }
        @Override public double getSteamMass() { return 0; }
        @Override public double getWaterMass() { return 1e9; }
        @Override public double getSteamVolume() { return 0; }
        @Override public double getWaterTemperature() { return 36; }
        @Override public double getSteamTemperature() { return 36; }
        @Override public double getWaterLevel() { return 0; }
    }
}
