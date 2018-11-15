package de.tudarmstadt.maki.simonstrator.tc.events.dto;

public class Event
{
   // Ensures unique ID per event
   private static int counter = 0;

   private final String type;

   String formattedTime;

   long timestamp;

   private int number;

   public Event(final String type)
   {
      this.type = type;
      this.number = counter++;
   }

   public String getType()
   {
      return type;
   }

   public long getTimestamp()
   {
      return timestamp;
   }

   public String getFormattedTime()
   {
      return formattedTime;
   }

   public void setFormattedTime(String formattedTime)
   {
      this.formattedTime = formattedTime;
   }

   public void setTimestamp(long timestamp)
   {
      this.timestamp = timestamp;
   }

   /**
    * Unique number that is incremented during each constructor invocation
    * @return the unique number of this event
    */
   public int getNumber()
   {
      return number;
   }

   @Override
	public String toString() {
		return String.format("#%d:%s", getNumber(), getType());
	}
}