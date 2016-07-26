/*
 * Copyright 2016 Lawrence Ruffin, Leland Lopez, Brittany Cruz, Stephen Anspach
 *
 * Developed in collaboration with the Hawaii State Digital Archives.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.hida.model;

import java.util.Iterator;
import java.util.Set;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Tests the functionality of the cache data structure
 * @author lruffin
 */
public class CacheTest {
    
    /**
     * Tests the functionality of isEmpty
     */
    @Test(dependsOnMethods = {"testAdd"})
    public void testIsEmpty(){
        Cache<Integer> c = new Cache<>();         
        
        Assert.assertEquals(c.isEmpty(), true);
        c.add(0);
        Assert.assertEquals(c.isEmpty(), false);        
    }
    
    /**
     * Tests the functionality of getSize
     */
    @Test(dependsOnMethods = {"testAdd"})
    public void testGetSize(){
        Cache<Integer> c = new Cache<>();
        int size = 10;
        for (int i = 0; i < size; i++) {
            c.add(i);
        }
        
        Assert.assertEquals(c.getSize(), size);
    }
    
    /**
     * Tests the functionality of add
     */
    @Test
    public void testAdd(){
        Cache<Integer> c = new Cache<>(); 
        
        Assert.assertEquals(c.add(0), true);
        Assert.assertEquals(c.add(0), false);
    }
    
    /**
     * Tests the functionality of peek
     */
    @Test(dependsOnMethods = {"testAdd"})
    public void testPeek(){
        Cache<Integer> c = new Cache<>();
        int size = 10;
        int limit = 5;
        
        // populate the cahe
        for (int i = 0; i < size; i++) {
            c.add(i);
        }
        
        // check the value of the cache
        Set<Integer> set = c.peek(limit);
        Iterator<Integer> iter1 = set.iterator();                
        for (int i = 0; i < limit; i++) {
            Assert.assertEquals(iter1.next().intValue(), i);
        }
        Assert.assertEquals(c.getSize(), size);            
    }
    
    /**
     * Tests the functionality of collect
     */
    @Test(dependsOnMethods = {"testAdd"})
    public void testCollect(){
        Cache<Integer> c = new Cache<>();
        int size = 10;
        int limit = 5;
        
        // populate the cahe
        for (int i = 0; i < size; i++) {
            c.add(i);
        }
        
        // check the value of the cache
        Set<Integer> set = c.collect(limit);
        Iterator<Integer> iter1 = set.iterator();                
        for (int i = 0; i < limit; i++) {
            Assert.assertEquals(iter1.next().intValue(), i);
        }
        Assert.assertEquals(c.getSize(), size - limit);
    }
    
    /**
     * Tests the functionality of removeAll
     */
    @Test(dependsOnMethods = {"testAdd", "testIsEmpty"})
    public void testRemoveAll(){
        Cache<Integer> c = new Cache<>();
        int size = 10;        
        
        // populate the cahe
        for (int i = 0; i < size; i++) {
            c.add(i);
        }
        
        c.removeAll();
        
        Assert.assertEquals(c.isEmpty(), true);
    }        
}
