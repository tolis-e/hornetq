/*
   * JBoss, Home of Professional Open Source
   * Copyright 2005, JBoss Inc., and individual contributors as indicated
   * by the @authors tag. See the copyright.txt in the distribution for a
   * full listing of individual contributors.
   *
   * This is free software; you can redistribute it and/or modify it
   * under the terms of the GNU Lesser General Public License as
   * published by the Free Software Foundation; either version 2.1 of
   * the License, or (at your option) any later version.
   *
   * This software is distributed in the hope that it will be useful,
   * but WITHOUT ANY WARRANTY; without even the implied warranty of
   * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
   * Lesser General Public License for more details.
   *
   * You should have received a copy of the GNU Lesser General Public
   * License along with this software; if not, write to the Free
   * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
   * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
   */

package org.jboss.jms.client.container;

import org.jboss.aop.advice.Interceptor;
import org.jboss.aop.joinpoint.Invocation;
import org.jboss.jms.client.delegate.DelegateSupport;
import org.jboss.jms.client.delegate.ClientConnectionDelegate;
import org.jboss.jms.client.state.ConnectionState;
import org.jboss.jms.client.remoting.JMSRemotingConnection;
import org.jboss.logging.Logger;
import org.jboss.remoting.CannotConnectException;
import org.jboss.remoting.ConnectionListener;
import org.jboss.remoting.Client;
import java.io.IOException;
import EDU.oswego.cs.dl.util.concurrent.ReadWriteLock;
import EDU.oswego.cs.dl.util.concurrent.ReentrantWriterPreferenceReadWriteLock;

/**
 * This aspect will intercept failures from any HA object.
 * <p/>
 * The reason why I've made an extension of HAAspect instead of implementing new methods there is
 * ValveAspect needs to cache ClientConnectionDelegate while HAAspect needs to cache CF related objects.
 * I have made this an extension of HAAspect as it's needed one instance of this aspect per
 * ConnectionCreated.
 * <p/>
 * We will cache the ClientConnectionDelegate on this aspect so we won't need to do any operation on delegates
 * to retrieve the current ConnectionDelegate.
 *
 * @author <a href="mailto:clebert.suconic@jboss.org">Clebert Suconic</a>
 * @version <tt>$Revision:$</tt>
 *          <p/>
 *          $Id:$
 */
public class ValveAspect extends HAAspect implements Interceptor
{
   private static final Logger log = Logger.getLogger(ValveAspect.class);

   private ClientConnectionDelegate delegate;

   // alternative implementation
   //private Valve valve = new Valve();
   private ReadWriteLock lockValve;

   ValveAspect(ClientConnectionDelegate delegate, HAAspect copy)
   {
      super(copy);
      this.delegate = delegate;
      lockValve = new ReentrantWriterPreferenceReadWriteLock();

      ConnectionListener listener = new ConnectionFailureListener(delegate);

      ((ConnectionState) ((DelegateSupport) delegate).getState()).
         getRemotingConnectionListener().addDelegateListener(listener);


   }

   public String getName()
   {
      return this.getClass().getName();
   }


   /**
    * This method executes the valve, listening for erros on the underlaying IO layer,
    * and it will call the failure for HA
    */
   public Object invoke(Invocation invocation) throws Throwable
   {

      Object returnObject = null;

      boolean failure = false;

      // Eventually retries in case of listed exceptions
      for (int i = 0; i < MAX_IO_RETRY_COUNT; i++)
      {

         if (i > 0)
         {
            log.info("Retrying a call " + i);
         }


         // An alternate way of playing with Valves is to set an attribute to true
         // and wait for notifications until it's being set. Valve encpasulates this logic
         //  if (i > 0)
         //  {
         //     valve.open(true);
         //  }
         //  else
         //  {
         //     valve.isOpened(true);
         //  }

         // We shouldn't have any calls being made while the failover is being executed
         if (i > 0)
         {
            // On retries we will use writeLocks, as the failover was already being processed
            lockValve.writeLock().acquire();
         }
         else
         {
            lockValve.readLock().acquire();
         }

         failure = false;

         try
         {
            returnObject = invocation.invokeNext();
         }
         catch (CannotConnectException e)
         {
            log.error("Got an exception on HAAspect, retryCount=" + i, e);
            failure = true;
         }
         catch (IOException e)
         {
            log.error("Got an exception on HAAspect, retryCount=" + i, e);
            failure = true;
         }
         catch (Throwable e)
         {
            log.error("ValveAspect didn't catch the exception " + e + ", and it will be forwarded", e);
            throw e;
         }
         finally
         {
            // An alternate way of playing with Valves is to set an attribute to true
            // and wait for notifications until it's being set. Valve encpasulates this logic
            //if (i>0)
            //{
            //   valve.reset();
            //}
            if (i > 0)
            {
               lockValve.writeLock().release();
            }
            else
            {
               lockValve.readLock().release();
            }
         }

         if (!failure)
         {
            break;
         }
      }


      if (failure)
      {
         handleConnectionFailure(delegate, delegate.getRemotingConnection());
         // if on the end we still have an exception there is nothing we can do besides throw an exception
         // so, no retires on the failedOver Invocation
         returnObject = invocation.invokeNext();
      }

      // if the object returned is another DelegateSupport, we will install the aspect on the returned object

      return returnObject;

   }


   /**
    * Since we are listening for exceptions on the invocation layer, several objects might
    * get the exception at the same time.
    * Suppose you have 30 (or any X number>=2) Consumers, using the same JBossConnection failing at the same time.
    * We will get simultaneous calls on handleFailures while we just need to process one single failure.
    * <p/>
    * On this case this method will open a valve and it will perform only the first handleFailure captured, and
    * it will just return all the others as soon as the valve is closed. This way all the simultaneous failures will
    * act as they were processed while we called failover only once.
    */
   protected void handleConnectionFailure(ClientConnectionDelegate failedConnDelegate,
                                          JMSRemotingConnection jmsConnection) throws Exception
   {
      log.info("Processing handleConnectionFailure ");

      // only one execution should be performed if multiple exceptions happened at the same time
      lockValve.writeLock().acquire();

      // An alternate way of playing with Valves is to set an attribute to true
      // and wait for notifications until it's being set. Valve encpasulates this logic
//      if (!valve.open())
//      {
//         return;
//      }

      try
      {
         if (jmsConnection.isFailed())
         {
            log.info("Failover on " + failedConnDelegate + " was already performed, so just ignoring call to handleConnectionFailure");
            return;
         }

         log.info("Processing valve on exception failure");
         jmsConnection.setFailed(true);
         super.handleConnectionFailure(failedConnDelegate);
         log.info("Failover on client finished");
      }
      catch (Exception e)
      {
         log.error("An exception happened during client failover processing!", e);
         throw e;
      }
      finally
      {
         // An alternate way of playing with Valves is to set an attribute to true
         // and wait for notifications until it's being set. Valve encpasulates this logic
//         valve.close();
//         valve = new Valve();
         lockValve.writeLock().release();
      }

   }

   // Inner classes -------------------------------------------------


   /** I have moved this ConnectionListener to ValveAspect (from HAAspect) because
    *  it needs to use the same valve as exception listeners.
    *  While we are processing failover, we should block any calls on the client side.
    *  (No call should be made while the client failover is being executed). It doesn't matter if
    *  the failover was captured by Lease (ConnectionFactory) or Exception handling on invoke at this class */
   private class ConnectionFailureListener implements ConnectionListener
   {
      private ClientConnectionDelegate cd;

      ConnectionFailureListener(ClientConnectionDelegate cd)
      {
         this.cd = cd;
      }

      // ConnectionListener implementation ---------------------------

      public void handleConnectionException(Throwable throwable, Client client)
      {
         try
         {
            log.debug(this + " is being notified of connection failure: " + throwable);
            handleConnectionFailure(cd, cd.getRemotingConnection());
         }
         catch (Throwable e)
         {
            log.error("Caught exception in handling failure", e);
         }
      }

      public String toString()
      {
         return "ConnectionFailureListener[" + cd + "]";
      }
   }


}
