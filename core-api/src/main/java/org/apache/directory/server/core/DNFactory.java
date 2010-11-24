/*
 *   Licensed to the Apache Software Foundation (ASF) under one
 *   or more contributor license agreements.  See the NOTICE file
 *   distributed with this work for additional information
 *   regarding copyright ownership.  The ASF licenses this file
 *   to you under the Apache License, Version 2.0 (the
 *   "License"); you may not use this file except in compliance
 *   with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied.  See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 *
 */
package org.apache.directory.server.core;


import org.apache.directory.shared.ldap.exception.LdapInvalidDnException;
import org.apache.directory.shared.ldap.name.DN;


/**
 * A factory for DNs, with a cache.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public interface DNFactory
{

    /**
     * Creates a DN from user provided RDNs.
     *
     * @param upRdns the user provided RDNs
     * @return the created DN
     * @throws LdapInvalidDnException if one of the strings isn't a valid RDN
     */
    DN create( String... upRdns ) throws LdapInvalidDnException;


    /**
     * Creates a DN form a user provided DN.
     *
     * @param upDn the user provided DN
     * @return the created DN
     * @throws LdapInvalidDnException if the string isn't a valid DN
     */
    DN create( String upDn ) throws LdapInvalidDnException;

}