package eu.hansolo.steelseries.gauges;

import eu.hansolo.steelseries.tools.LcdColor;
import eu.hansolo.steelseries.tools.NumberSystem;
import java.awt.Color;
import java.awt.Font;
import java.awt.Paint;
import java.awt.Rectangle;

public interface Lcd {
   boolean isValueCoupled();

   void setValueCoupled(boolean var1);

   double getLcdValue();

   void setLcdValue(double var1);

   void setLcdValueAnimated(double var1);

   double getLcdThreshold();

   void setLcdThreshold(double var1);

   boolean isLcdThresholdVisible();

   void setLcdThresholdVisible(boolean var1);

   boolean isLcdThresholdBehaviourInverted();

   void setLcdThresholdBehaviourInverted(boolean var1);

   boolean isLcdBlinking();

   void setLcdBlinking(boolean var1);

   int getLcdDecimals();

   void setLcdDecimals(int var1);

   String getLcdUnitString();

   void setLcdUnitString(String var1);

   boolean isLcdUnitStringVisible();

   void setLcdUnitStringVisible(boolean var1);

   boolean isCustomLcdUnitFontEnabled();

   void setCustomLcdUnitFontEnabled(boolean var1);

   Font getCustomLcdUnitFont();

   void setCustomLcdUnitFont(Font var1);

   String getLcdInfoString();

   void setLcdInfoString(String var1);

   Font getLcdInfoFont();

   void setLcdInfoFont(Font var1);

   boolean isDigitalFont();

   void setDigitalFont(boolean var1);

   LcdColor getLcdColor();

   void setLcdColor(LcdColor var1);

   Paint getCustomLcdBackground();

   void setCustomLcdBackground(Paint var1);

   Paint createCustomLcdBackgroundPaint(Color[] var1);

   boolean isLcdBackgroundVisible();

   void setLcdBackgroundVisible(boolean var1);

   Color getCustomLcdForeground();

   void setCustomLcdForeground(Color var1);

   String formatLcdValue(double var1);

   boolean isLcdScientificFormat();

   void setLcdScientificFormat(boolean var1);

   Font getLcdValueFont();

   void setLcdValueFont(Font var1);

   Font getLcdUnitFont();

   void setLcdUnitFont(Font var1);

   NumberSystem getLcdNumberSystem();

   void setLcdNumberSystem(NumberSystem var1);

   Rectangle getLcdBounds();
}
