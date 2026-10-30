package com.darwish.nppsim;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import org.apache.commons.lang3.exception.ExceptionUtils;

/**
 * 报警声播放。
 *
 * 播放模型：**请求/释放**（reference counting）
 *  每个需要出声的地方（报警器、AZ-1K、BAZ、AZ-2/AZ-3/AZ-4/AZ-5…）都以自己的对象作为 owner
 *  向某条警声"请求播放"或"释放"。只要某条警声还有 owner 在请求，它就应该响；owner 全部释放，
 *  它立刻静音。于是 AZ-2 与 AZ-3 共用 alarm3、AZ-1K/BAZ/AZ-4/AZ-5 共用 alarm2 时，
 *  复位其中一个不会把另一个仍在跳闸的警声一起掐掉。
 *
 * 延迟相关（本轮改动，解决"AZ 声音开/关有延迟"）：
 *  1) **删除 500ms 停止防抖**：原实现对 stop() 做 500ms 延迟执行，听感就是"按下复位半秒后
 *     声音才停"。现在释放即停（refresh 当场切换），零延迟。
 *     防抖当初是为了压制参数在阈值附近抖动导致的"停-播-停"；现在每个保护都有 tripped 锁存，
 *     报警器也有 hornOn 边沿标志，不会每帧反复 play/stop，防抖已无必要。
 *  2) **跳闸声优先于通用警笛**：AZ-1K/BAZ/AZ-4/AZ-5（alarm2）、AZ-2/AZ-3（alarm3）优先于
 *     PCS 报警器警笛（alarm1）。原先报警器优先级最高，AZ 跳闸瞬间若恰好有未确认报警，
 *     听到的仍是 alarm1，要等玩家按确认后 AZ 声才出来 —— 表现为"AZ 声音开启延迟"。
 *     现在跳闸即刻可闻；跳闸复位后若报警仍未确认，警笛自动接续。
 *  3) 同一时刻仍只出一条警声（互斥），避免多条蜂鸣叠加产生拍频"杂音"。
 */
public class SoundProvider {

    // ---- 静态默认值：设置对话框在音频对象尚未创建时也能先记住选择 ----
    private static float defaultVolume = 0.8f;         // 主音量
    private static float defaultAlarmVolume = 1.0f;    // 警报通道音量
    private static float defaultAmbientVolume = 1.0f;  // 环境通道音量
    private static boolean defaultMuted = false;

    static {
        try {
            java.util.prefs.Preferences p = java.util.prefs.Preferences.userRoot().node("com/darwish/nppsim");
            defaultVolume = clamp01(p.getFloat("sound.master", p.getFloat("sound.volume", defaultVolume)));
            defaultAlarmVolume = clamp01(p.getFloat("sound.alarm", defaultAlarmVolume));
            defaultAmbientVolume = clamp01(p.getFloat("sound.ambient", defaultAmbientVolume));
            defaultMuted = p.getBoolean("sound.muted", defaultMuted);
        } catch (Throwable ignored) {
        }
    }

    /** 把三个通道音量与静音状态持久化（设置页"应用/确定"时调用） */
    static void saveVolumes() {
        try {
            java.util.prefs.Preferences p = java.util.prefs.Preferences.userRoot().node("com/darwish/nppsim");
            p.putFloat("sound.master", defaultVolume);
            p.putFloat("sound.alarm", defaultAlarmVolume);
            p.putFloat("sound.ambient", defaultAmbientVolume);
            p.putBoolean("sound.muted", defaultMuted);
            p.flush();
        } catch (Throwable ignored) {
        }
    }

    /** 无主请求（报警器警笛、兼容旧接口的调用）使用的 owner */
    private static final Object GLOBAL_OWNER = new Object();

    /** 每条警声当前的请求者集合；集合非空 ⇒ 该警声应出声 */
    private final Map<Clip, Set<Object>> requests = new ConcurrentHashMap<>();
    /** 实际正在响的那一条 */
    private volatile Clip playing;

    Clip ALARM_1, ALARM_2, ALARM_3;
    private float volume = defaultVolume;
    private float alarmVolume = defaultAlarmVolume;
    private float ambientVolume = defaultAmbientVolume;
    private boolean muted = defaultMuted;

    public static float getDefaultVolume() {
        return defaultVolume;
    }

    public static void setDefaultVolume(float v) {
        defaultVolume = clamp01(v);
        saveVolumes();
    }

    public static boolean isDefaultMuted() {
        return defaultMuted;
    }

    public static void setDefaultMuted(boolean m) {
        defaultMuted = m;
        saveVolumes();
    }

    public static float getDefaultAlarmVolume() {
        return defaultAlarmVolume;
    }

    public static void setDefaultAlarmVolume(float v) {
        defaultAlarmVolume = clamp01(v);
        saveVolumes();
    }

    public static float getDefaultAmbientVolume() {
        return defaultAmbientVolume;
    }

    public static void setDefaultAmbientVolume(float v) {
        defaultAmbientVolume = clamp01(v);
        saveVolumes();
    }

    private static float clamp01(float v) {
        return Math.max(0f, Math.min(1f, v));
    }

    public SoundProvider() {
        try {
            ALARM_1 = loadAsset("alarm1.wav");
            ALARM_2 = loadAsset("alarm2.wav");
            ALARM_3 = loadAsset("alarm3.wav");
            applyGain(ALARM_1);
            applyGain(ALARM_2);
            applyGain(ALARM_3);
        } catch (Exception e) {
            new ErrorWindow("An error occured loading audio assets", ExceptionUtils.getStackTrace(e), false).setVisible(true);
        }
    }

    // ------------------------------------------------------------------
    // 请求 / 释放（新接口）
    // ------------------------------------------------------------------

    /**
     * 请求持续播放某条警声。可重复调用（幂等）。
     * 调用即刻生效：若当前正被别的高优先级警声占着，会在对方释放的瞬间自动接上。
     *
     * @param owner 请求者（一般直接传 this），用于多保护共享同一音源时互不干扰
     */
    public synchronized void request(Clip clip, Object owner) {
        if (clip == null) {
            return;
        }
        Object key = owner == null ? GLOBAL_OWNER : owner;
        requests.computeIfAbsent(clip, k -> ConcurrentHashMap.newKeySet()).add(key);
        refresh();
    }

    /** 释放某条警声（立即停止，无防抖）；该音源仍有其他 owner 请求时继续响 */
    public synchronized void release(Clip clip, Object owner) {
        if (clip == null) {
            return;
        }
        Object key = owner == null ? GLOBAL_OWNER : owner;
        Set<Object> owners = requests.get(clip);
        if (owners != null) {
            owners.remove(key);
            if (owners.isEmpty()) {
                requests.remove(clip, owners);
            }
        }
        refresh();
    }

    /** 释放该 owner 对所有警声的请求（复位/清场用，立即生效） */
    public synchronized void releaseAll(Object owner) {
        Object key = owner == null ? GLOBAL_OWNER : owner;
        requests.forEach((clip, owners) -> {
            owners.remove(key);
            if (owners.isEmpty()) {
                requests.remove(clip, owners);
            }
        });
        refresh();
    }

    // ------------------------------------------------------------------
    // 兼容旧接口（报警器/试听/设置页仍在用）
    // ------------------------------------------------------------------

    /** 请求持续播放（以全局 owner 记账） */
    public synchronized void playContinuously(Clip clip) {
        request(clip, GLOBAL_OWNER);
    }

    /** 立即停止（不走防抖） */
    public synchronized void stopNow(Clip clip) {
        release(clip, GLOBAL_OWNER);
    }

    /** 旧名字：历史上有 500ms 防抖，现在与 stopNow 等价（避免调用方误以为还有延迟） */
    public synchronized void stop(Clip clip) {
        release(clip, GLOBAL_OWNER);
    }

    public synchronized void stopAll() {
        requests.clear();
        refresh();
    }

    public synchronized void pause() {
        if (playing != null) {
            playing.stop();
        }
    }

    public synchronized void resume() {
        refresh(true);
    }

    /** 音量 0.0~1.0 */
    public synchronized void setVolume(float v) {
        volume = Math.max(0f, Math.min(1f, v));
        applyGain(ALARM_1);
        applyGain(ALARM_2);
        applyGain(ALARM_3);
    }

    public float getVolume() {
        return volume;
    }

    /** 警报通道音量（0~1），与主音量相乘 */
    public synchronized void setAlarmVolume(float v) {
        alarmVolume = clamp01(v);
        applyGain(ALARM_1);
        applyGain(ALARM_2);
        applyGain(ALARM_3);
    }

    /** 环境通道音量（0~1），与主音量相乘（当前环境音源待接入，接口先就绪） */
    public synchronized void setAmbientVolume(float v) {
        ambientVolume = clamp01(v);
    }

    public synchronized void setMuted(boolean m) {
        if (muted == m) {
            return;
        }
        muted = m;
        refresh(true);
    }

    public boolean isMuted() {
        return muted;
    }

    // ------------------------------------------------------------------
    // 内部：按优先级挑一条实际出声的报警
    // ------------------------------------------------------------------
    private void refresh() {
        refresh(false);
    }

    private void refresh(boolean forceRestart) {
        Clip want = pickActive();
        if (want == playing && !forceRestart) {
            /* 看门狗（2026-10-18）：本该在响却停了就重启。
             * 切换音频设备、系统独占、Clip 被外部 stop 之后，playing 仍指向这条 Clip，
             * 旧逻辑会认为"已经在放"直接返回，于是警笛从此哑掉（要等状态再变一次才恢复）。 */
            if (want != null && !muted && !want.isRunning()) {
                want.setFramePosition(0);
                want.loop(Clip.LOOP_CONTINUOUSLY);
            }
            return;
        }
        if (playing != null && playing != want) {
            playing.stop();
            playing.setFramePosition(0);
        }
        playing = want;
        if (want == null) {
            return;
        }
        if (muted) {
            want.stop();
            want.setFramePosition(0);
            return;
        }
        applyGain(want);
        if (want.getFramePosition() != 0 || !want.isRunning()) {
            want.setFramePosition(0);
        }
        if (!want.isRunning()) {
            want.loop(Clip.LOOP_CONTINUOUSLY);
        }
    }

    /**
     * 优先级：跳闸声（ALARM_2 = alarm2.wav，AZ-1K/BAZ/AZ-4/AZ-5）与备用跳闸声
     * （ALARM_3 = alarm3.wav，AZ-2/AZ-3）恒高于报警器警笛（ALARM_1 = alarm1.wav）。
     * 跳闸是最严重的事件，必须立刻可闻；跳闸复位后若报警仍未确认，警笛自动接续。
     */
    private Clip pickActive() {
        if (isRequested(ALARM_2)) {
            return ALARM_2;
        }
        if (isRequested(ALARM_3)) {
            return ALARM_3;
        }
        if (isRequested(ALARM_1)) {
            return ALARM_1;
        }
        return null;
    }

    private boolean isRequested(Clip clip) {
        if (clip == null) {
            return false;
        }
        Set<Object> owners = requests.get(clip);
        return owners != null && !owners.isEmpty();
    }

    /** 周期性重估当前应出声的报警（轮替用）；无变化时是廉价空操作 */
    public synchronized void tick() {
        refresh();
    }

    /** 把 volume/muted 映射到 MASTER_GAIN（分贝） */
    private void applyGain(Clip clip) {
        if (clip == null) {
            return;
        }
        try {
            FloatControl gain = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
            // 有效音量 = 主音量 × 通道音量（报警器/跳闸走警报通道）
            float channel = (clip == ALARM_1 || clip == ALARM_2 || clip == ALARM_3) ? alarmVolume : ambientVolume;
            float effective = volume * channel;
            float db = muted ? gain.getMinimum()
                             : (float) (20.0 * Math.log10(Math.max(0.0001, effective)));
            db = Math.max(gain.getMinimum(), Math.min(gain.getMaximum(), db));
            gain.setValue(db);
        } catch (Exception ignored) {
            // 某些混音器没有 MASTER_GAIN，忽略即可
        }
    }

    /** 循环接缝交叉淡化时长（ms）：警笛循环播放时，波形首尾不连续会每圈"咔"一声 */
    private static final int LOOP_FADE_MS = 8;

    private Clip loadAsset(String asset) throws Exception {
        Clip loadedAsset;
        try {
            InputStream input = getClass().getResourceAsStream("/res/" + asset);
            if (input == null) {
                throw new Exception("missing resource /res/" + asset);
            }
            InputStream bufferedInput = new BufferedInputStream(input);
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(bufferedInput);
            AudioFormat format = audioStream.getFormat();
            // 逐块读取（工程编译级别为 Java 8，不能用 readAllBytes）
            java.io.ByteArrayOutputStream collector = new java.io.ByteArrayOutputStream();
            byte[] chunk = new byte[8192];
            int read;
            while ((read = audioStream.read(chunk)) > 0) {
                collector.write(chunk, 0, read);
            }
            byte[] pcm = collector.toByteArray();
            audioStream.close();
            bufferedInput.close();
            input.close();

            if (format.getSampleSizeInBits() == 16 && !format.isBigEndian()) {
                pcm = crossfadeLoopSeam(pcm, format, LOOP_FADE_MS);
            }
            AudioInputStream fixed = new AudioInputStream(
                    new java.io.ByteArrayInputStream(pcm), format,
                    pcm.length / Math.max(1, format.getFrameSize()));
            loadedAsset = AudioSystem.getClip();
            loadedAsset.open(fixed);
            fixed.close();
        } catch (Exception e) {
            throw new Exception("AudioAssetException");
        }
        return loadedAsset;
    }

    /**
     * 把音频尾部交叉淡化进头部，使 loop 回绕处连续（原地修改副本，不改长度）。
     *
     * 做法：out[0..fade) = 尾部*(1-t) + 头部*t（t 线性 0→1）。
     * 于是回绕瞬间从末尾样本接到 out[0]（≈ 尾部起点），两者在原始波形上相差仅 fade 毫秒，
     * 相当于连续；头部再平滑过渡回原波形，听不出拼接。
     */
    private static byte[] crossfadeLoopSeam(byte[] pcm, AudioFormat format, int fadeMs) {
        int frameSize = Math.max(1, format.getFrameSize());
        int channels = Math.max(1, format.getChannels());
        int frames = pcm.length / frameSize;
        int fade = Math.min(frames / 4, (int) (format.getSampleRate() * fadeMs / 1000.0));
        if (fade < 2 || channels * 2 > frameSize) {
            return pcm;
        }
        byte[] out = pcm.clone();
        for (int i = 0; i < fade; i++) {
            double t = (double) i / (double) fade;
            int tailFrame = frames - fade + i;
            for (int c = 0; c < channels; c++) {
                int headIdx = i * frameSize + c * 2;
                int tailIdx = tailFrame * frameSize + c * 2;
                short head = (short) ((pcm[headIdx] & 0xFF) | (pcm[headIdx + 1] << 8));
                short tail = (short) ((pcm[tailIdx] & 0xFF) | (pcm[tailIdx + 1] << 8));
                int mixed = (int) Math.round(tail * (1.0 - t) + head * t);
                out[headIdx] = (byte) (mixed & 0xFF);
                out[headIdx + 1] = (byte) ((mixed >> 8) & 0xFF);
            }
        }
        return out;
    }
}
