package de.tudarmstadt.maki.simonstrator.tc.reconfiguration;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class NonfunctionalProperties
{

   // Must appear in front of the nonfunctional properties
   private static final List<NonfunctionalProperty> NFPs = new ArrayList<>();

   public static final String NFP_PREFIX = "m";

   public static final NonfunctionalProperty EMEAN = new NonfunctionalPropertyWithPrefix("EMean");

   public static final NonfunctionalProperty EJAIN = new NonfunctionalPropertyWithPrefix("EJain");

   public static final NonfunctionalProperty EMEDIAN = new NonfunctionalPropertyWithPrefix("EMedian");

   public static final NonfunctionalProperty ESTDDEV = new NonfunctionalPropertyWithPrefix("EStddev");

   public static final NonfunctionalProperty END_TO_END_DROPRATE = new NonfunctionalPropertyWithPrefix("EndToEndDropRate");

   public static final NonfunctionalProperty LINK_DROPRATE = new NonfunctionalPropertyWithPrefix("PerLinkDropRate");

   public static final NonfunctionalProperty LATENCY = new NonfunctionalPropertyWithPrefix("EndToEndLatency");

   public static Optional<NonfunctionalProperty> getByName(final String name)
   {
      return NFPs.stream().filter(nfp -> nfp.getName().equals(name)).findFirst();
   }

   private static class NonfunctionalPropertyWithPrefix extends NonfunctionalProperty
   {

      public NonfunctionalPropertyWithPrefix(String name)
      {
         super(NFP_PREFIX + name);
         NFPs.add(this);
      }

   }
}
