/*
 * Copyright 2007-2017 David Koelle - http://www.DaveKoelle.com
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE
 * USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.github.jikoo.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;

/**
 * The Alphanum Algorithm is an improved sorting algorithm for strings
 * containing numbers.  Instead of sorting numbers in ASCII order like
 * a standard sort, this algorithm sorts numbers in numeric order.
 *
 * <p>This is an updated version with enhancements made by Daniel Migowski,
 * Andre Bogus, and David Koelle. Updated by David Koelle in 2017.
 *
 * <p>Note that David Koelle's site is down; the most recent Wayback Machine
 * capture of his Alphanum page can be found
 * <a href=https://web.archive.org/web/20210803201519/http://www.davekoelle.com/alphanum.html>here</a>.
 */
public class AlphanumComparator implements Comparator<String> {

  private boolean isDigit(char ch) {
    return ((ch >= 48) && (ch <= 57));
  }

  /** Length of string is passed in for improved efficiency (only need to calculate it once) **/
  private @NotNull String getChunk(@NotNull String s, int slength, int marker) {
    StringBuilder chunk = new StringBuilder();
    char c = s.charAt(marker);
    chunk.append(c);
    marker++;
    if (isDigit(c)) {
      while (marker < slength) {
        c = s.charAt(marker);
        if (!isDigit(c))
          break;
        chunk.append(c);
        marker++;
      }
    } else {
      while (marker < slength) {
        c = s.charAt(marker);
        if (isDigit(c))
          break;
        chunk.append(c);
        marker++;
      }
    }
    return chunk.toString();
  }

  /**
   * Compare two strings containing numbers. Returns a negative integer, zero, or a positive
   * integer as the first argument is less than, equal to, or greater than the second.
   *
   * <p>This comparator uses null-first ordering. If null values are to be sorted last, wrap with
   * {@link Comparator#nullsLast(Comparator)}.</p>
   *
   * @param s1 the first object to be compared.
   * @param s2 the second object to be compared.
   * @return a negative integer, zero, or a positive integer as the first argument is less than,
   *         equal to, or greater than the second.
   */
  public int compare(@Nullable String s1, @Nullable String s2) {
    if (s1 == null) {
      return s2 == null ? 0 : -1;
    } else if (s2 == null) {
      return 1;
    }

    int thisMarker = 0;
    int thatMarker = 0;
    int s1Length = s1.length();
    int s2Length = s2.length();

    while (thisMarker < s1Length && thatMarker < s2Length) {
      String thisChunk = getChunk(s1, s1Length, thisMarker);
      thisMarker += thisChunk.length();

      String thatChunk = getChunk(s2, s2Length, thatMarker);
      thatMarker += thatChunk.length();

      // If both chunks contain numeric characters, sort them numerically
      int result;
      if (isDigit(thisChunk.charAt(0)) && isDigit(thatChunk.charAt(0))) {
        // Simple chunk comparison by length.
        int thisChunkLength = thisChunk.length();
        result = thisChunkLength - thatChunk.length();
        // If equal, the first different number counts
        if (result == 0) {
          for (int i = 0; i < thisChunkLength; i++) {
            result = thisChunk.charAt(i) - thatChunk.charAt(i);
            if (result != 0) {
              return result;
            }
          }
        }
      } else {
        result = thisChunk.compareTo(thatChunk);
      }

      if (result != 0) {
        return result;
      }
    }

    return s1Length - s2Length;
  }

}