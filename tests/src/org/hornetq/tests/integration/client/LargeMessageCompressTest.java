/*
 * Copyright 2009 Red Hat, Inc.
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

package org.hornetq.tests.integration.client;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.atomic.AtomicLong;

import junit.framework.Assert;

import org.hornetq.api.core.Message;
import org.hornetq.api.core.client.ClientConsumer;
import org.hornetq.api.core.client.ClientMessage;
import org.hornetq.api.core.client.ClientProducer;
import org.hornetq.api.core.client.ClientSession;
import org.hornetq.api.core.client.ClientSessionFactory;
import org.hornetq.api.core.client.HornetQClient;
import org.hornetq.api.core.client.ServerLocator;
import org.hornetq.core.server.HornetQServer;
import org.hornetq.tests.util.RandomUtil;

/**
 * A LargeMessageCompressTest
 *
 * Just extend the LargeMessageTest
 *
 * @author <a href="mailto:hgao@redhat.com">Howard Gao</a>
 *
 *
 */
public class LargeMessageCompressTest extends LargeMessageTest
{
   // Constructors --------------------------------------------------
   public LargeMessageCompressTest()
   {
      isCompressedTest = true;
   }

   protected boolean isNetty()
   {
      return false;
   }

   protected ServerLocator createFactory(final boolean isNetty) throws Exception
   {
      ServerLocator locator = super.createFactory(isNetty);
      locator.setCompressLargeMessage(true);
      return locator;
   }

   public void testLargeMessageCompression() throws Exception
   {
      final int messageSize = (int)(3.5 * HornetQClient.DEFAULT_MIN_LARGE_MESSAGE_SIZE);

      ClientSession session = null;

      try
      {
         server = createServer(true, isNetty());

         server.start();

         ClientSessionFactory sf = locator.createSessionFactory();

         session = sf.createSession(false, false, false);

         session.createTemporaryQueue(LargeMessageTest.ADDRESS, LargeMessageTest.ADDRESS);

         ClientProducer producer = session.createProducer(LargeMessageTest.ADDRESS);

         Message clientFile = createLargeClientMessage(session, messageSize, true);

         producer.send(clientFile);

         session.commit();

         session.start();

         ClientConsumer consumer = session.createConsumer(LargeMessageTest.ADDRESS);
         ClientMessage msg1 = consumer.receive(1000);
         Assert.assertNotNull(msg1);

         for (int i = 0 ; i < messageSize; i++)
         {
            byte b = msg1.getBodyBuffer().readByte();
            assertEquals("position = "  + i, getSamplebyte(i), b);
         }

         msg1.acknowledge();
         session.commit();

         consumer.close();

         session.close();

         validateNoFilesOnLargeDir();
      }
      finally
      {
         try
         {
            server.stop();
         }
         catch (Throwable ignored)
         {
         }

         try
         {
            session.close();
         }
         catch (Throwable ignored)
         {
         }
      }
   }

   public void testLargeMessageCompression2() throws Exception
   {
      final int messageSize = (int)(3.5 * HornetQClient.DEFAULT_MIN_LARGE_MESSAGE_SIZE);

      ClientSession session = null;

      try
      {
         server = createServer(true, isNetty());

         server.start();

         ClientSessionFactory sf = locator.createSessionFactory();

         session = sf.createSession(false, false, false);

         session.createTemporaryQueue(LargeMessageTest.ADDRESS, LargeMessageTest.ADDRESS);

         ClientProducer producer = session.createProducer(LargeMessageTest.ADDRESS);

         Message clientFile = createLargeClientMessage(session, messageSize, true);

         producer.send(clientFile);

         session.commit();

         session.start();

         ClientConsumer consumer = session.createConsumer(LargeMessageTest.ADDRESS);
         ClientMessage msg1 = consumer.receive(1000);
         Assert.assertNotNull(msg1);

         String testDir = this.getTestDir();
         File testFile = new File(testDir, "async_large_message");
         FileOutputStream output = new FileOutputStream(testFile);

         msg1.setOutputStream(output);

         msg1.waitOutputStreamCompletion(0);

         output.close();

         msg1.acknowledge();

         session.commit();

         consumer.close();

         session.close();

         //verify
         FileInputStream input = new FileInputStream(testFile);
         for (int i = 0 ; i < messageSize; i++)
         {
            byte b = (byte)input.read();
            assertEquals("position = "  + i, getSamplebyte(i), b);
         }
         input.close();
         testFile.delete();
         validateNoFilesOnLargeDir();
      }
      finally
      {
         try
         {
            server.stop();
         }
         catch (Throwable ignored)
         {
         }

         try
         {
            session.close();
         }
         catch (Throwable ignored)
         {
         }
      }
   }

   public void testLargeMessageCompression3() throws Exception
   {
      final int messageSize = (int)(3.5 * HornetQClient.DEFAULT_MIN_LARGE_MESSAGE_SIZE);

      ClientSession session = null;

      try
      {
         server = createServer(true, isNetty());

         server.start();

         ClientSessionFactory sf = locator.createSessionFactory();

         session = sf.createSession(false, false, false);

         session.createTemporaryQueue(LargeMessageTest.ADDRESS, LargeMessageTest.ADDRESS);

         ClientProducer producer = session.createProducer(LargeMessageTest.ADDRESS);

         Message clientFile = createLargeClientMessage(session, messageSize, true);

         producer.send(clientFile);

         session.commit();

         session.start();

         ClientConsumer consumer = session.createConsumer(LargeMessageTest.ADDRESS);
         ClientMessage msg1 = consumer.receive(1000);
         Assert.assertNotNull(msg1);

         String testDir = this.getTestDir();
         File testFile = new File(testDir, "async_large_message");
         FileOutputStream output = new FileOutputStream(testFile);

         msg1.saveToOutputStream(output);

         msg1.acknowledge();

         output.close();

         session.commit();

         consumer.close();

         session.close();

         //verify
         FileInputStream input = new FileInputStream(testFile);
         for (int i = 0 ; i < messageSize; i++)
         {
            byte b = (byte)input.read();
            assertEquals("position = "  + i, getSamplebyte(i), b);
         }
         input.close();

         testFile.delete();
         validateNoFilesOnLargeDir();
      }
      finally
      {
         try
         {
            server.stop();
         }
         catch (Throwable ignored)
         {
         }

         try
         {
            session.close();
         }
         catch (Throwable ignored)
         {
         }
      }
   }

   // This test will send 1 Gig of spaces. There shouldn't be enough memory to uncompress the file in memory
   // but this will make sure we can work through compressed channels on saving it to stream
   public void testHugeStreamingSpacesCompressed() throws Exception
   {
      final long messageSize = 1024l * 1024l * 1024l;

      System.out.println("Message size = " + messageSize);

      HornetQServer server = createServer(true, isNetty());

      server.start();

      // big enough to hold the whole message compressed on a single message (about 1M on our tests)
      locator.setMinLargeMessageSize( 100 * 1024 * 1024);

      ClientSessionFactory sf = locator.createSessionFactory();

      ClientSession session = sf.createSession(false, false, false);

      session.createQueue(LargeMessageTest.ADDRESS, LargeMessageTest.ADDRESS);

      ClientProducer producer = session.createProducer(LargeMessageTest.ADDRESS);

      ClientMessage clientMessage = session.createMessage(true);

      clientMessage.setBodyInputStream(new InputStream()
      {
         private long count;

         private boolean closed = false;

         @Override
         public void close() throws IOException
         {
            super.close();
            closed = true;
         }

         @Override
         public int read() throws IOException
         {
            if (closed)
            {
               throw new IOException("Stream was closed");
            }

            if (count++ < messageSize)
            {
               return ' ';
            }
            else
            {
               return -1;
            }
         }
      });

      producer.send(clientMessage);

      session.commit();

      // this is to make sure the message was sent as a regular message (not taking a file on server)
      validateNoFilesOnLargeDir();

      session.start();

      ClientConsumer consumer = session.createConsumer(LargeMessageTest.ADDRESS);
      ClientMessage msg1 = consumer.receive(1000);
      Assert.assertNotNull(msg1);

      final AtomicLong numberOfSpaces = new AtomicLong();

      msg1.saveToOutputStream(new OutputStream()
      {
         public void write(int content)
         {
            if (content == ' ')
            {
               numberOfSpaces.incrementAndGet();
            }
         }
      });


      assertEquals(messageSize, numberOfSpaces.get());

      msg1.acknowledge();

      session.commit();

      session.close();
      
      server.stop();
   }

   public void testLargeMessageCompressionRestartAndCheckSize() throws Exception
   {
      final int messageSize = 1024 * 1024;

      ClientSession session = null;

      try
      {
         server = createServer(true, isNetty());

         server.start();

         ClientSessionFactory sf = locator.createSessionFactory();

         session = sf.createSession(false, false, false);

         session.createQueue(LargeMessageTest.ADDRESS, LargeMessageTest.ADDRESS, true);

         ClientProducer producer = session.createProducer(LargeMessageTest.ADDRESS);

         byte [] msgs = new byte[1024 * 1024];
         for (int i = 0 ; i < msgs.length; i++)
         {
            msgs[i] = RandomUtil.randomByte();
         }

         Message clientFile = createLargeClientMessage(session, msgs, true);

         producer.send(clientFile);

         session.commit();

         session.close();

         sf.close();

         locator.close();

         server.stop();

         server = createServer(true, isNetty());

         server.start();

         locator = createFactory(isNetty());

         sf = locator.createSessionFactory();

         session = sf.createSession();

         session.start();

         ClientConsumer consumer = session.createConsumer(LargeMessageTest.ADDRESS);
         ClientMessage msg1 = consumer.receive(1000);
         Assert.assertNotNull(msg1);

         assertEquals(messageSize, msg1.getBodySize());

         String testDir = this.getTestDir();
         File testFile = new File(testDir, "async_large_message");
         FileOutputStream output = new FileOutputStream(testFile);

         msg1.saveToOutputStream(output);

         msg1.acknowledge();

         session.commit();

         consumer.close();

         session.close();

         //verify
         FileInputStream input = new FileInputStream(testFile);
         for (int i = 0 ; i < messageSize; i++)
         {
            byte b = (byte)input.read();
            assertEquals("position = "  + i, msgs[i], b);
         }
         input.close();

         testFile.delete();
         validateNoFilesOnLargeDir();
      }
      finally
      {
         try
         {
            server.stop();
         }
         catch (Throwable ignored)
         {
         }

         try
         {
            session.close();
         }
         catch (Throwable ignored)
         {
         }
      }
   }


   public void testSendServerMessage() throws Exception
   {
      // doesn't make sense as compressed
   }
}
