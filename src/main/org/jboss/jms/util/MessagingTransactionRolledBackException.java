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
package org.jboss.jms.util;

import javax.jms.TransactionRolledBackException;

import org.jboss.logging.Logger;

/**
 * 
 * A JBossTransactionRolledBackException.
 * 
 * @author <a href="tim.fox@jboss.com">Tim Fox</a>
 * @version 1.1
 *
 * MessagingTransactionRolledBackException.java,v 1.1 2006/03/28 14:26:17 timfox Exp
 */
public class MessagingTransactionRolledBackException extends TransactionRolledBackException
{
   // Constants -----------------------------------------------------
   
   private static final long serialVersionUID = -1395246656299977995L;

   private static final Logger log = Logger.getLogger(MessagingTransactionRolledBackException.class);
   
   
   // Static --------------------------------------------------------
      
   // Attributes ----------------------------------------------------
   
   // Constructors --------------------------------------------------
   
   public MessagingTransactionRolledBackException(String reason)
   {
      this(reason, null, null);
   }
   
   public MessagingTransactionRolledBackException(Exception cause)
   {
      this(null, null, cause);
   }
   
   public MessagingTransactionRolledBackException(String reason, String errorCode)
   {
      this(reason, errorCode, null);
   }
   
   public MessagingTransactionRolledBackException(String reason, Throwable cause)
   {
      this(reason, null, cause);
   }
   
   public MessagingTransactionRolledBackException(String reason, String errorCode, Throwable cause)
   {
      super(reason, errorCode);
      if (cause != null)
      {
         if (cause instanceof Exception)
         {
            setLinkedException((Exception)cause);
         }
         else
         {
            setLinkedException(new Exception(cause));
         }
      }
   }
   
   // Public --------------------------------------------------------
   
   // Package protected ---------------------------------------------
   
   // Protected -----------------------------------------------------
   
   // Private -------------------------------------------------------
   
   // Inner classes -------------------------------------------------  
}
