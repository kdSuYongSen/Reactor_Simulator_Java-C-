package com.darwish.nppsim;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.rbmk.real.RbmkCore;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import org.junit.Assume;
import org.junit.Test;

/** SaveGame v2 的 Java 序列化 + native 堆芯状态往返。 */
public class SaveGameTest {

    @Test
    public void saveGameSurvivesSerializationAndRestoresCore() throws Exception {
        RbmkCore source = new RbmkCore();
        RbmkCore restored = new RbmkCore();
        try {
            Assume.assumeTrue("当前 DLL 不支持状态导出", source.supportsStateRestore());

            source.reset();
            for (int i = 0; i < 40; i++) {
                source.step(0.05);
            }
            source.refresh();

            SaveGame game = new SaveGame();
            game.nativeCoreState = source.saveState();
            game.savedAt = 1234L;

            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(bytes);
            out.writeObject(game);
            out.close();

            ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()));
            SaveGame loaded = (SaveGame) in.readObject();
            in.close();

            assertEquals(SaveGame.CURRENT_VERSION, loaded.version);
            assertEquals(1234L, loaded.savedAt);
            assertNotNull(loaded.nativeCoreState);

            assertTrue("从存档恢复堆芯失败", restored.restoreState(loaded.nativeCoreState));
            assertEquals(source.powerMW(), restored.powerMW(), 1e-9);
        } finally {
            source.close();
            restored.close();
        }
    }
}
