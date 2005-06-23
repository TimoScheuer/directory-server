/*
 *   Copyright 2004 The Apache Software Foundation
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */
package org.apache.ldap.server.partition;

import java.util.ArrayList;
import java.util.List;

import javax.naming.Name;
import javax.naming.NameNotFoundException;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.OperationNotSupportedException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.ModificationItem;

import org.apache.ldap.common.name.LdapName;
import org.apache.ldap.server.configuration.ContextPartitionConfiguration;
import org.apache.ldap.server.jndi.ContextFactoryConfiguration;

/**
 * A {@link ContextPartition} that helps users to implement their own partition.
 * Most methods are implemented by default.  Please look at the description of
 * each methods for the detail of implementations.
 *
 * @author The Apache Directory Project
 * @version $Rev$, $Date$
 */
public abstract class AbstractContextPartition implements ContextPartition
{
    /** {@link ContextFactoryConfiguration} specified at {@link #init(ContextFactoryConfiguration, ContextPartitionConfiguration)}. */
    private ContextFactoryConfiguration factoryCfg;
    /** {@link ContextPartitionConfiguration} specified at {@link #init(ContextFactoryConfiguration, ContextPartitionConfiguration)}. */
    private ContextPartitionConfiguration cfg;
    /** <tt>true</tt> if and only if this partition is initialized. */
    private boolean initialized;
    
    protected AbstractContextPartition()
    {
    }

    /**
     * Sets up default properties(<tt>factoryConfiguration</tt> and <tt>configuration</tt>) and
     * calls {@link #doInit()} where you have to put your initialization code in.
     * {@link #isInitialized()} will return <tt>true</tt> if {@link #doInit()} returns
     * without any errors.  {@link #destroy()} is called automatically as a clean-up process
     * if {@link #doInit()} throws an exception.
     */
    public final void init( ContextFactoryConfiguration factoryCfg, ContextPartitionConfiguration cfg ) throws NamingException
    {
        if( initialized )
        {
            // Already initialized.
            return;
        }

        this.factoryCfg = factoryCfg;
        this.cfg = cfg;
        try{
            doInit();
            initialized = true;
        }
        finally
        {
            if( !initialized )
            {
                destroy();
            }
        }
    }

    /**
     * Override this method to put your initialization code.
     */
    protected void doInit() throws NamingException
    {
    }
    
    /**
     * Calls {@link #doDestroy()} where you have to put your destroy code in,
     * and clears default properties.  Once this method is invoked, {@link #isInitialized()}
     * will return <tt>false</tt> even if {@link #doDestroy()} failed.
     */
    public final void destroy() throws NamingException
    {
        if( cfg == null )
        {
            // Already destroyed.
            return;
        }

        try
        {
            doDestroy();
        }
        finally
        {
            initialized = false;
            factoryCfg = null;
            cfg = null;
        }
    }

    /**
     * Override this method to put your initialization code.
     */
    protected void doDestroy() throws NamingException
    {
    }

    /**
     * Returns <tt>true</tt> if this context partition is initialized successfully.
     */
    public final boolean isInitialized()
    {
        return initialized;
    }

    /**
     * Returns {@link ContextFactoryConfiguration} that is provided from
     * {@link #init(ContextFactoryConfiguration, ContextPartitionConfiguration)}.
     */
    public final ContextFactoryConfiguration getFactoryConfiguration()
    {
        return factoryCfg;
    }
    
    /**
     * Returns {@link ContextPartitionConfiguration} that is provided from
     * {@link #init(ContextFactoryConfiguration, ContextPartitionConfiguration)}.
     */
    public final ContextPartitionConfiguration getConfiguration()
    {
        return cfg;
    }

    public final Name getSuffix( boolean normalized ) throws NamingException
    {
        if( normalized )
        {
            return getConfiguration().getNormalizedSuffix(
                    getFactoryConfiguration().getGlobalRegistries().getMatchingRuleRegistry() );
        }
        else
        {
            return new LdapName( getConfiguration().getSuffix() );
        }
    }

    public final boolean isSuffix( Name name ) throws NamingException
    {
        return getSuffix( true ).equals( name ) || getSuffix( false ).equals( name );
    }

    /**
     * This method does nothing by default.
     */
    public void sync() throws NamingException
    {
    }
    
    /**
     * This method calls {@link #lookup(Name)} and return <tt>true</tt>
     * if it returns an entry by default.  Please override this method if
     * there is more effective way for your implementation.
     */
    public boolean hasEntry( Name name ) throws NamingException
    {
        try
        {
            return lookup( name ) != null;
        }
        catch( NameNotFoundException e )
        {
            return false;
        }
    }

    /**
     * This method calls {@link ContextPartition#lookup(Name, String[])}
     * with null <tt>attributeIds</tt> by default.  Please override
     * this method if there is more effective way for your implementation.
     */
    public Attributes lookup( Name name ) throws NamingException
    {
        return lookup( name, null );
    }

    /**
     * This method forwards the request to
     * {@link ContextPartition#modify(Name, ModificationItem[])} after
     * translating parameters to {@link ModificationItem}<tt>[]</tt> by default.
     * Please override this method if there is more effactive way for your
     * implementation.
     */
    public void modify( Name name, int modOp, Attributes mods ) throws NamingException
    {
        List items = new ArrayList( mods.size() );
        NamingEnumeration e = mods.getAll();
        while( e.hasMore() )
        {
            items.add( new ModificationItem( modOp, ( Attribute ) e.next() ) );
        }
        
        ModificationItem[] itemsArray = new ModificationItem[ items.size() ];
        itemsArray = ( ModificationItem[] ) items.toArray( itemsArray );
        modify( name, itemsArray );
    }

    /**
     * This method calls {@link ContextPartition#move(Name, Name)} and
     * {@link ContextPartition#modifyRn(Name, String, boolean)} subsequently
     * by default.  Please override this method if there is more effactive
     * way for your implementation.
     */
    public void move( Name oldName, Name newParentName, String newRn, boolean deleteOldRn ) throws NamingException
    {
        Name newName = ( Name ) newParentName.clone();
        newName.add( newRn );
        move( oldName, newParentName );
        modifyRn( newName, newRn, deleteOldRn );
    }

    /**
     * This method throws {@link OperationNotSupportedException} by default.
     * Please override this method to implement move operation.
     */
    public void move( Name oldName, Name newParentName ) throws NamingException
    {
        throw new OperationNotSupportedException();
    }
}
