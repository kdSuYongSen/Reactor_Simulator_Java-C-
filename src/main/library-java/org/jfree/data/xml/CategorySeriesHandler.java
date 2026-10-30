package org.jfree.data.xml;

import org.jfree.data.DefaultKeyedValues;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class CategorySeriesHandler extends DefaultHandler implements DatasetTags {
   private final RootHandler root;
   private Comparable seriesKey;
   private final DefaultKeyedValues values;

   public CategorySeriesHandler(RootHandler root) {
      this.root = root;
      this.values = new DefaultKeyedValues();
   }

   public void setSeriesKey(Comparable key) {
      this.seriesKey = key;
   }

   public void addItem(Comparable key, Number value) {
      this.values.addValue(key, value);
   }

   @Override
   public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException {
      if (qName.equals("Series")) {
         this.setSeriesKey(atts.getValue("name"));
         ItemHandler subhandler = new ItemHandler(this.root, this);
         this.root.pushSubHandler(subhandler);
      } else {
         if (!qName.equals("Item")) {
            throw new SAXException("Expecting <Series> or <Item> tag...found " + qName);
         }

         ItemHandler subhandler = new ItemHandler(this.root, this);
         this.root.pushSubHandler(subhandler);
         subhandler.startElement(namespaceURI, localName, qName, atts);
      }
   }

   @Override
   public void endElement(String namespaceURI, String localName, String qName) {
      if (this.root instanceof CategoryDatasetHandler) {
         CategoryDatasetHandler handler = (CategoryDatasetHandler)this.root;

         for (Comparable key : (java.lang.Iterable<Comparable>) (this.values.getKeys())) {
            Number value = this.values.getValue(key);
            handler.addItem(this.seriesKey, key, value);
         }

         this.root.popSubHandler();
      }
   }
}
