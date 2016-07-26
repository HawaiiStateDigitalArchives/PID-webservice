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
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * A collection of unique values.
 *
 * @author lruffin
 * @param <T>
 */
public class Cache<T> {

    private long size_;
    private Set<T> set_;

    public Cache() {
        set_ = new LinkedHashSet<>();
        size_ = 0;
    }

    public Cache(Set<T> set) {
        set_ = set;
        size_ = set.size();
    }

    /**
     * Adds an element to the cache.
     *
     * @param value Element to be added
     * @return True if the element was successfully added, false otherwise
     */
    public boolean add(T value) {
        if (!set_.contains(value)) {
            size_++;
            return set_.add(value);
        }
        else {
            return false;
        }
    }

    /**
     * Gets the size of the cache.
     *
     * @return Size of the cache
     */
    public long getSize() {
        return this.size_;
    }

    /**
     * True if the cache is empty, false otherwise
     *
     * @return True if the cache empty, false otherwise
     */
    public boolean isEmpty() {
        return size_ == 0;
    }

    /**
     * Returns a set of elements contained within the cache. If the requested
     * amount exceeds the size of the cache, the entire cache is retrieved.
     *
     * Elements are retrieved on a first-in-first-out basis.
     *
     * @param amount The requested amount
     * @return A set of Pids
     */
    public Set<T> peek(long amount) {
        Set<T> set = new LinkedHashSet<>();

        int i = 0;
        Iterator<T> iter = set_.iterator();
        while (iter.hasNext() && i < amount) {
            set.add(iter.next());
            i++;
        }

        return set;
    }

    /**
     * Returns a set of elements within the cache and then proceeds to remove
     * those elements from the cache. If the requested amount exceeds the size
     * of the cache, the entire cache is retrieved and removed from the cache.
     *
     * Elements are retrieved on a first-in-first-out basis.
     *
     * @param amount The requested amount
     * @return A set of Pids
     */
    public Set<T> collect(long amount) {
        Set<T> set = new LinkedHashSet<>();

        int i = 0;
        Iterator<T> iter = set_.iterator();
        while (iter.hasNext() && i < amount) {
            set.add(iter.next());
            i++;
        }

        set_.removeAll(set);
        size_ = (amount > size_) ? 0 : size_ - amount;
        return set;
    }

    /**
     * Removes all elements from the cache.
     */
    public void removeAll() {
        set_.clear();
        size_ = 0;
    }

}
