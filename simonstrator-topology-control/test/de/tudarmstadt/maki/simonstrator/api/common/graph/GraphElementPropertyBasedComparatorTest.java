/*
 * Copyright (c) 2005-2010 KOM - Multimedia Communications Lab
 *
 * This file is part of Simonstrator.KOM.
 * 
 * Simonstrator.KOM is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 * 
 * PeerfactSim.KOM is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with PeerfactSim.KOM.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package de.tudarmstadt.maki.simonstrator.api.common.graph;

import org.junit.Assert;
import org.junit.Test;

import de.tudarmstadt.maki.simonstrator.api.Graphs;

/**
 * Unit tests for {@link GraphElementPropertyBasedComparator}
 * @author rkluge
 *
 */
public class GraphElementPropertyBasedComparatorTest
{

   private static final GraphElementProperty<Integer> TEST_PROPERTY1 = new GraphElementProperty<>("Test Property 1", Integer.class);

   @Test(expected = MissingPropertyException.class)
   public void test_MissingProperty1() throws Exception
   {
      final Node node1 = Graphs.createNode("1");
      node1.setProperty(TEST_PROPERTY1, 1);
      final Node node2 = Graphs.createNode("2");
      new GraphElementPropertyBasedComparator(TEST_PROPERTY1).compare(node1, node2);
   }

   @Test(expected = MissingPropertyException.class)
   public void test_MissingProperty2() throws Exception
   {
      final Node node1 = Graphs.createNode("1");
      final Node node2 = Graphs.createNode("2");
      node2.setProperty(TEST_PROPERTY1, 1);
      new GraphElementPropertyBasedComparator(TEST_PROPERTY1).compare(node1, node2);
   }

   @Test
   public void test_Equality() throws Exception
   {
      final Node node1 = Graphs.createNode("1");
      node1.setProperty(TEST_PROPERTY1, 1);
      final Node node2 = Graphs.createNode("2");
      node2.setProperty(TEST_PROPERTY1, 1);
      Assert.assertTrue(0 == new GraphElementPropertyBasedComparator(TEST_PROPERTY1).compare(node1, node2));
   }

   @Test
   public void test_FirstElementLarger() throws Exception
   {
      final Node node = Graphs.createNode("1");
      node.setProperty(TEST_PROPERTY1, 1);
      final IEdge edge = Graphs.createDirectedEdge(INodeID.get("x"), INodeID.get("y"));
      edge.setProperty(TEST_PROPERTY1, 3);
      Assert.assertTrue(0 > new GraphElementPropertyBasedComparator(TEST_PROPERTY1).compare(node, edge));
   }

   @Test
   public void test_FirstElementSmaller() throws Exception
   {
      final Node node = Graphs.createNode("1");
      node.setProperty(TEST_PROPERTY1, 1);
      final IEdge edge = Graphs.createDirectedEdge(INodeID.get("x"), INodeID.get("y"));
      edge.setProperty(TEST_PROPERTY1, 3);
      Assert.assertTrue(0 < new GraphElementPropertyBasedComparator(TEST_PROPERTY1).compare(edge, node));
   }

}