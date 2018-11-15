package de.tudarmstadt.maki.simonstrator.tc.reconfiguration;

/**
 * Identifier for a non-functional property
 * @author Roland Kluge - Initial implementation
 *
 */
public class NonfunctionalProperty
{
   private final String name;

   public NonfunctionalProperty(final String name)
   {
      if (name == null)
         throw new IllegalArgumentException("Non-null NFP name required");

      this.name = name;
   }

   public String getName()
   {
      return name;
   }

   @Override
   public String toString()
   {
      return this.getName();
   }
}
