/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package org.aitools.programd.graph;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

/**
 * This is an optimization of {@link NodemapperFactory} that avoids creating
 * the internal {@link java.util.LinkedHashMap LinkedMap} until the
 * number of mappings exceeds one (1).
 * 
 * @author <a href="mailto:noel@aitools.org">Noel Bush</a>
 * @version 4.6
 */
public class OneOptimalNodemaster extends AbstractNodemaster
{
    protected int size = 0;

    protected String key;

    protected Object value;

    /**
     * Puts the given object into the Nodemaster, associated with the given key.
     * 
     * @param keyToUse the key to use
     * @param valueToPut the value to put
     * @return the same object that was put into the Nodemaster
     */
    public Object put(String keyToUse, Object valueToPut)
    {
        if (this.size == 0)
        {
            this.key = keyToUse.toUpperCase().intern();
            if (valueToPut instanceof String)
            {
                this.value = ((String) valueToPut).intern();
            }
            else
            {
                this.value = valueToPut;
            }
            this.size = 1;
            return this.value;
        }
        else if (this.size == 1)
        {
            this.hidden = new LinkedHashMap<String, Object>();
            this.hidden.put(this.key, this.value);
            this.key = null;
            this.value = null;
            this.size = 2;
            if (valueToPut instanceof String)
            {
                return this.hidden.put(keyToUse.toUpperCase().intern(), ((String) valueToPut).intern());
            }
            // otherwise...
            return this.hidden.put(keyToUse.toUpperCase().intern(), valueToPut);
        }
        else
        {
            this.size++;
            if (valueToPut instanceof String)
            {
                return this.hidden.put(keyToUse.toUpperCase().intern(), ((String) valueToPut).intern());
            }
            // otherwise...
            return this.hidden.put(keyToUse.toUpperCase().intern(), valueToPut);
        }
    }

    /**
     * Removes the given object from the Nodemaster.
     * 
     * @param valueToRemove the object to remove
     */
    public void remove(Object valueToRemove)
    {
        if (this.size == 1)
        {
            if (valueToRemove.equals(this.value))
            {
                this.value = null;
                this.key = null;
            }
            else
            {
                // We didn't find a key.
                Logger.getLogger("programd.graphmaster").error(String.format("Key was not found for value when trying to remove \"%s\".", valueToRemove));
                return;
            }
            this.size = 0;
        }
        else if (this.size > 1)
        {
            // Find the key for this value.
            Object keyToRemove = null;
            for (Map.Entry<String, Object> item : this.hidden.entrySet())
            {
                if (item.getValue().equals(valueToRemove))
                {
                    // Found it.
                    keyToRemove = item.getKey();
                    break;
                }
            }
            if (keyToRemove == null)
            {
                // We didn't find a key.
                Logger.getLogger("programd.graphmaster").error(String.format("Key was not found for value when trying to remove \"%s\".", valueToRemove));
                return;
            }
            if (this.size > 2)
            {
                // Remove the value from the HashMap (ignore the primary
                // value/key pair).
                this.hidden.remove(keyToRemove);
                this.size--;
            }
            // otherwise it is exactly 2...
            else
            {
                // Remove this item from the HashMap.
                this.hidden.remove(keyToRemove);
                // Set the last item in the HashMap to be the primary value/key
                // pair for this Nodemapper.
                this.key = this.hidden.keySet().iterator().next();
                this.value = this.hidden.remove(this.key);
                // Remove the empty HashMap to save space.
                this.hidden = null;
                this.size = 1;
            }
        }
        else // if (this.size == 0)
        {
            // We didn't find a key.
            Logger.getLogger("programd.graphmaster").error(String.format("No keys in Nodemapper when trying to remove \"%s\".", valueToRemove));
        }
    }

    /**
     * Gets the object associated with the specified key.
     * 
     * @param keyToGet the key to use
     * @return the object associated with the given key
     */
    public Object get(String keyToGet)
    {
        if (this.size == 0)
        {
            return null;
        }
        else if (this.size == 1)
        {
            if (keyToGet.equalsIgnoreCase(this.key))
            {
                return this.value;
            }
            // (otherwise...)
            return null;
        }
        else
        {
            return this.hidden.get(keyToGet.toUpperCase());
        }
    }

    /**
     * @return the keyset of the Nodemaster
     */
    public Set<String> keySet()
    {
        if (this.size == 1)
        {
            Set<String> result = new HashSet<String>();
            if (this.key != null)
            {
                result.add(this.key);
            }
            return result;
        }
        // (otherwise...)
        return this.hidden.keySet();
    }

    /**
     * @param keyToCheck the key to check
     * @return whether or not the Nodemaster contains the given key
     */
    public boolean containsKey(String keyToCheck)
    {
        if (this.size == 0)
        {
            return false;
        }
        else if (this.size == 1)
        {
            return (keyToCheck.equalsIgnoreCase(this.key));
        }
        return this.hidden.containsKey(keyToCheck.toUpperCase());
    }

    /**
     * @return the size of the Nodemaster
     */
    public int size()
    {
        return this.size;
    }

    public double getAverageSize()
    {
        double total = 0d;
        if (this.size == 1)
        {
            if (this.value != null && this.value instanceof Nodemapper)
            {
                total = ((Nodemapper)this.value).getAverageSize();
            }
        }
        else
        {
            for (Object object : this.hidden.values())
            {
                if (object instanceof Nodemapper)
                {
                    total += ((Nodemapper)object).getAverageSize();
                }
            }
        }
        if (this.parent != null)
        {
            return (this.size + (total / this.size)) / 2d;
        }
        // otherwise...
        return total / this.size;
    }
}