/*
 * Copyright 2005-2014 Red Hat, Inc.
 * Red Hat licenses this file to you under the Apache License, version
 * 2.0 (the "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *    http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.  See the License for the specific language governing
 * permissions and limitations under the License.
 */

package org.hornetq.core.server.impl;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.hornetq.api.core.HornetQException;
import org.hornetq.api.core.TransportConfiguration;
import org.hornetq.api.core.client.TopologyMember;
import org.hornetq.core.server.LiveNodeLocator;
import org.hornetq.api.core.Pair;

/**
 * This implementation looks for any available live node, once tried with no success it is marked as
 * tried and the next available is used.
 * @author <a href="mailto:andy.taylor@jboss.org">Andy Taylor</a>
 */
public class AnyLiveNodeLocator extends LiveNodeLocator
{
   private final Lock lock = new ReentrantLock();
   private final Condition condition = lock.newCondition();
   Map<String, Pair<TransportConfiguration, TransportConfiguration>> untriedConnectors = new HashMap<String, Pair<TransportConfiguration, TransportConfiguration>>();
   Map<String, Pair<TransportConfiguration, TransportConfiguration>> triedConnectors = new HashMap<String, Pair<TransportConfiguration, TransportConfiguration>>();

   private String nodeID;

   public AnyLiveNodeLocator(QuorumManager quorumManager)
   {
      super(quorumManager);
   }

   @Override
   public void locateNode() throws HornetQException
   {
      //first time
      try
      {
         lock.lock();
         if(untriedConnectors.isEmpty())
         {
            try
            {
               condition.await();
            }
            catch (InterruptedException e)
            {

            }
         }
      }
      finally
      {
         lock.unlock();
      }
   }

   @Override
   public void nodeUP(TopologyMember topologyMember, boolean last)
   {
      try
      {
         lock.lock();
         Pair<TransportConfiguration, TransportConfiguration> connector =
                  new Pair<TransportConfiguration, TransportConfiguration>(topologyMember.getLive(), topologyMember.getBackup());
         untriedConnectors.put(topologyMember.getNodeId(), connector);
         condition.signal();
      }
      finally
      {
         lock.unlock();
      }
   }

   /**
    * if a node goes down we try all the connectors again as one may now be available for
    * replication
    * <p>
    * TODO: there will be a better way to do this by finding which nodes backup has gone down.
    */
   @Override
   public void nodeDown(long eventUID, String nodeID)
   {
      try
      {
         lock.lock();
         untriedConnectors.putAll(triedConnectors);
         triedConnectors.clear();
         if(untriedConnectors.size() > 0)
         {
            condition.signal();
         }
      }
      finally
      {
         lock.unlock();
      }
   }

   @Override
   public String getNodeID()
   {
      return nodeID;
   }

   @Override
   public Pair<TransportConfiguration, TransportConfiguration> getLiveConfiguration()
   {
      try
      {
         lock.lock();
         Iterator<String> iterator = untriedConnectors.keySet().iterator();
         //sanity check but this should never happen
         if(iterator.hasNext())
         {
            nodeID = iterator.next();
         }
         return untriedConnectors.get(nodeID);
      }
      finally
      {
         lock.unlock();
      }
   }

   @Override
   public void notifyRegistrationFailed(boolean alreadyReplicating)
   {
      try
      {
         lock.lock();
         Pair<TransportConfiguration, TransportConfiguration> tc = untriedConnectors.remove(nodeID);
         //it may have been removed
         if (tc != null)
         {
            triedConnectors.put(nodeID, tc);
         }
      }
      finally
      {
         lock.unlock();
      }
      super.notifyRegistrationFailed(alreadyReplicating);
   }
}

