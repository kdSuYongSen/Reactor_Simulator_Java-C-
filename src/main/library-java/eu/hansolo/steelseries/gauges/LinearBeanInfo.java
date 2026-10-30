package eu.hansolo.steelseries.gauges;

import java.awt.Image;
import java.beans.SimpleBeanInfo;

public class LinearBeanInfo extends SimpleBeanInfo {
   @Override
   public Image getIcon(int var1) {
      switch (var1) {
         case 1:
            return this.loadImage("/eu/hansolo/steelseries/resources/Linear16.png");
         case 2:
            return this.loadImage("/eu/hansolo/steelseries/resources/Linear32.png");
         case 3:
            return null;
         case 4:
            return null;
         default:
            return null;
      }
   }
}
