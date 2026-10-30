package eu.hansolo.steelseries.gauges;

import java.awt.Image;
import java.beans.SimpleBeanInfo;

public class Radial1SquareBeanInfo extends SimpleBeanInfo {
   @Override
   public Image getIcon(int var1) {
      switch (var1) {
         case 1:
            return this.loadImage("/eu/hansolo/steelseries/resources/Radial1Square16.png");
         case 2:
            return this.loadImage("/eu/hansolo/steelseries/resources/Radial1Square32.png");
         case 3:
            return null;
         case 4:
            return null;
         default:
            return null;
      }
   }
}
