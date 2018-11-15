package de.tudarmstadt.maki.simonstrator.tc.events.dto;

import java.util.ArrayList;
import java.util.List;

public class EventLog {
   List<Event> events = new ArrayList<>();

   public List<Event> getEvents()
   {
      return events;
   }

   public void addEvent(final Event event)
   {
      this.events.add(event);
   }

   public void clear()
   {
      this.events.clear();
   }

   @Override
	public String toString() {
		return String.format("Event log: %s", this.events);
	}
}