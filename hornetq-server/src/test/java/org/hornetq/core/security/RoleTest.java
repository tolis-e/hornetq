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
package org.hornetq.core.security;

import org.junit.Test;

import static org.hornetq.core.security.CheckType.CONSUME;
import static org.hornetq.core.security.CheckType.CREATE_DURABLE_QUEUE;
import static org.hornetq.core.security.CheckType.CREATE_NON_DURABLE_QUEUE;
import static org.hornetq.core.security.CheckType.DELETE_DURABLE_QUEUE;
import static org.hornetq.core.security.CheckType.DELETE_NON_DURABLE_QUEUE;
import static org.hornetq.core.security.CheckType.SEND;
import org.junit.Assert;


import org.hornetq.core.security.Role;

/**
 * @author <a href="mailto:jmesnil@redhat.com">Jeff Mesnil</a>
 */
public class RoleTest extends Assert
{
   // Constants -----------------------------------------------------

   // Attributes ----------------------------------------------------

   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------

   // Public --------------------------------------------------------

   @Test
   public void testReadRole() throws Exception
   {
      Role role = new Role("testReadRole", true, false, false, false, false, false, false);
      Assert.assertTrue(SEND.hasRole(role));
      Assert.assertFalse(CONSUME.hasRole(role));
      Assert.assertFalse(CREATE_DURABLE_QUEUE.hasRole(role));
      Assert.assertFalse(CREATE_NON_DURABLE_QUEUE.hasRole(role));
      Assert.assertFalse(DELETE_DURABLE_QUEUE.hasRole(role));
      Assert.assertFalse(DELETE_NON_DURABLE_QUEUE.hasRole(role));
   }

   @Test
   public void testWriteRole() throws Exception
   {
      Role role = new Role("testWriteRole", false, true, false, false, false, false, false);
      Assert.assertFalse(SEND.hasRole(role));
      Assert.assertTrue(CONSUME.hasRole(role));
      Assert.assertFalse(CREATE_DURABLE_QUEUE.hasRole(role));
      Assert.assertFalse(CREATE_NON_DURABLE_QUEUE.hasRole(role));
      Assert.assertFalse(DELETE_DURABLE_QUEUE.hasRole(role));
      Assert.assertFalse(DELETE_NON_DURABLE_QUEUE.hasRole(role));
   }

   @Test
   public void testCreateRole() throws Exception
   {
      Role role = new Role("testWriteRole", false, false, true, false, false, false, false);
      Assert.assertFalse(SEND.hasRole(role));
      Assert.assertFalse(CONSUME.hasRole(role));
      Assert.assertTrue(CREATE_DURABLE_QUEUE.hasRole(role));
      Assert.assertFalse(CREATE_NON_DURABLE_QUEUE.hasRole(role));
      Assert.assertFalse(DELETE_DURABLE_QUEUE.hasRole(role));
      Assert.assertFalse(DELETE_NON_DURABLE_QUEUE.hasRole(role));
   }

   @Test
   public void testEqualsAndHashcode() throws Exception
   {
      Role role = new Role("testEquals", true, true, true, false, false, false, false);
      Role sameRole = new Role("testEquals", true, true, true, false, false, false, false);
      Role roleWithDifferentName = new Role("notEquals", true, true, true, false, false, false, false);
      Role roleWithDifferentRead = new Role("testEquals", false, true, true, false, false, false, false);
      Role roleWithDifferentWrite = new Role("testEquals", true, false, true, false, false, false, false);
      Role roleWithDifferentCreate = new Role("testEquals", true, true, false, false, false, false, false);

      Assert.assertTrue(role.equals(role));

      Assert.assertTrue(role.equals(sameRole));
      Assert.assertTrue(role.hashCode() == sameRole.hashCode());

      Assert.assertFalse(role.equals(roleWithDifferentName));
      Assert.assertFalse(role.hashCode() == roleWithDifferentName.hashCode());

      Assert.assertFalse(role.equals(roleWithDifferentRead));
      Assert.assertFalse(role.hashCode() == roleWithDifferentRead.hashCode());

      Assert.assertFalse(role.equals(roleWithDifferentWrite));
      Assert.assertFalse(role.hashCode() == roleWithDifferentWrite.hashCode());

      Assert.assertFalse(role.equals(roleWithDifferentCreate));
      Assert.assertFalse(role.hashCode() == roleWithDifferentCreate.hashCode());

      Assert.assertFalse(role.equals(null));
   }

   // Package protected ---------------------------------------------

   // Protected -----------------------------------------------------

   // Private -------------------------------------------------------

   // Inner classes -------------------------------------------------
}
