/****************************************************************************
 Copyright 2017, Colorado School of Mines and others.
 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 ****************************************************************************/
package edu.mines.jtk.util;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Random;

import static edu.mines.jtk.util.ArrayMath.*;
import static org.testng.Assert.*;
import static edu.mines.jtk.util.ArrayMathTest.*;

/**
 * Tests byte array operations in {@link edu.mines.jtk.util.ArrayMath}.
 * @author Chris Engelsma
 * @version 2017.05.31
 */
public class ArrayMathByteTest {

  private static final int n3 = 8;
  private static final int n2 = 6;
  private static final int n1 = 4;

  @BeforeMethod
  public void setUp() {
    a1 = rampbyte((byte)0,(byte)1,n1);
    a2 = rampbyte((byte)0,(byte)1,(byte)10,n1,n2);
    a3 = rampbyte((byte)0,(byte)1,(byte)10,(byte)100,n1,n2,n3);
    b1 = zerobyte(n1);
    b2 = zerobyte(n1,n2);
    b3 = zerobyte(n1,n2,n3);
  }

  @Test
  public void testEqualityComparators() {
    zero(a3);
    assertTrue(equal(a3,b3));

    fill((byte)1,a3);
    assertFalse(equal(a3,b3));
  }

  @Test
  public void testRampBytes() {
    assertArraySize(a1,n1);
    assertArraySize(a2,n1,n2);
    assertArraySize(a3,n1,n2,n3);

    for (int i1=0; i1<n1; ++i1) {
      byte expected = (byte)i1;
      assertEquals(expected,a3[0][0][i1]);
      assertEquals(expected,a2[0][i1]);
      assertEquals(expected,a1[i1]);
    }
  }

  @Test
  public void testFillBytes() {
    byte val = (byte)1;

    assertEqual(zerobyte(n1,n2,n3),fillbyte((byte)0,n1,n2,n3));

    a1 = fillbyte(val,n1);
    a2 = fillbyte(val,n1,n2);
    a3 = fillbyte(val,n1,n2,n3);

    assertArraySize(a1,n1);
    assertArraySize(a2,n1,n2);
    assertArraySize(a3,n1,n2,n3);

    assertOnlyContains(val,a3);
    assertOnlyContains(val,a2);
    assertOnlyContains(val,a1);
  }

  @Test
  public void testZero() {
    zero(a1);
    zero(a2);
    zero(a3);

    assertOnlyContains((byte)0,a1);
    assertOnlyContains((byte)0,a2);
    assertOnlyContains((byte)0,a3);
  }

  @Test
  public void testCopySimple() {
    b1 = copy(a1);
    b2 = copy(a2);
    b3 = copy(a3);
    assertEqual(b1,a1);
    assertEqual(b2,a2);
    assertEqual(b3,a3);

    copy(a1,b1);
    copy(a2,b2);
    copy(a3,b3);
    assertEqual(b1,a1);
    assertEqual(b2,a2);
    assertEqual(b3,a3);

    b1 = copy(n1-1,a1);
    b2 = copy(n1-1,n2-1,a2);
    b3 = copy(n1-1,n2-1,n3-1,a3);
    assertEqual(b1,rampbyte((byte)0,(byte)1,n1-1));
    assertEqual(b2,rampbyte((byte)0,(byte)1,(byte)10,n1-1,n2-1));
    assertEqual(b3,rampbyte((byte)0,(byte)1,(byte)10,(byte)100,n1-1,n2-1,n3-1));

    copy(n1-1,a1,b1);
    copy(n1-1,n2-1,a2,b2);
    copy(n1-1,n2-1,n3-1,a3,b3);
    assertEqual(b1,rampbyte((byte)0,(byte)1,n1-1));
    assertEqual(b2,rampbyte((byte)0,(byte)1,(byte)10,n1-1,n2-1));
    assertEqual(b3,rampbyte((byte)0,(byte)1,(byte)10,(byte)100,n1-1,n2-1,n3-1));

    b1 = copy(n1-1,1,a1);
    b2 = copy(n1-2,n2-1,2,1,a2);
    b3 = copy(n1-3,n2-2,n3-1,3,2,1,a3);
    assertEqual(b1,rampbyte((byte)1,(byte)1,n1-1));
    assertEqual(b2,rampbyte((byte)12,(byte)1,(byte)10,n1-1,n2-1));
    assertEqual(b3,rampbyte((byte)123,(byte)1,(byte)10,(byte)100,n1-1,n2-1,n3-1));

    copy(n1-1,1,a1,0,b1);
    copy(n1-2,n2-1,2,1,a2,0,0,b2);
    copy(n1-3,n2-2,n3-1,3,2,1,a3,0,0,0,b3);
    assertEqual(b1,rampbyte((byte)1,(byte)1,n1-1));
    assertEqual(b2,rampbyte((byte)12,(byte)1,(byte)10,n1-1,n2-1));
    assertEqual(b3,rampbyte((byte)123,(byte)1,(byte)10,(byte)100,n1-1,n2-1,n3-1));

    b1 = copy(n1/2,0,2,a1);
    b2 = copy(n1/2,n2/2,0,0,2,2,a2);
    b3 = copy(n1/2,n2/2,n3/2,0,0,0,2,2,2,a3);
    assertEqual(b1,rampbyte((byte)0,(byte)2,n1/2));
    assertEqual(b2,rampbyte((byte)0,(byte)2,(byte)20,n1/2,n2/2));
    assertEqual(b3,rampbyte((byte)0,(byte)2,(byte)20,(byte)200,n1/2,n2/2,n3/2));

    b1 = copy(a1);
    b2 = copy(a2);
    b3 = copy(a3);
    copy(n1-1,1,a1,1,b1);
    copy(n1-2,n2-1,2,1,a2,2,1,b2);
    copy(n1-3,n2-2,n3-1,3,2,1,a3,3,2,1,b3);
    assertEqual(b1,rampbyte((byte)0,(byte)1,n1));
    assertEqual(b2,rampbyte((byte)0,(byte)1,(byte)10,n1,n2));
    assertEqual(b3,rampbyte((byte)0,(byte)1,(byte)10,(byte)100,n1,n2,n3));
  }

  @Test
  public void testReverse() {
    b1 = reverse(reverse(a1));
    assertEqual(b1,a1);
  }

  @Test
  public void testReshape() {
    b2 = reshape(n1,n2,flatten(a2));
    b3 = reshape(n1,n2,n3,flatten(a3));
    assertEqual(a2,b2);
    assertEqual(a3,b3);
  }

  @Test
  public void testTranspose() {
    b2 = transpose(transpose(a2));
    assertEqual(a2,b2);
  }

  @Test
  public void testDistinct() {
    a3 = b3;
    a2 = b2;
    a1 = b1;
    assertFalse(distinct(a3,b3));
    assertFalse(distinct(a2,b2));
    assertFalse(distinct(a1,b1));

    b3 = new byte[n3][n2][n1];
    b2 = new byte[n2][n1];
    b1 = new byte[n1];
    assertTrue(distinct(a3,b3));
    assertTrue(distinct(a2,b2));
    assertTrue(distinct(a1,b1));

    b3[1] = a3[1];
    b2[1] = a2[1];
    assertFalse(distinct(a3,b3));
    assertFalse(distinct(a2,b2));
  }

  @Test
  public void testMinMax() {
    a1 = rampbyte((byte)0,(byte)1,n1);
    a2 = rampbyte((byte)0,(byte)1,(byte)1,n1,n2);
    a3 = rampbyte((byte)0,(byte)1,(byte)1,(byte)1,n1,n2,n3);

    byte min = (byte)(-128);
    byte max = (byte)( 127);
    Random r = new Random();
    int j3 = r.nextInt(n3-1)+1;
    int j2 = r.nextInt(n2-1)+1;
    int j1 = r.nextInt(n1-1)+1;
    int k1 = r.nextInt(n1-1)+1;
    while (j1==k1) k1 = r.nextInt(n1-1)+1;

    int[] imin1 = new int[1]; int[] imax1 = new int[1];
    int[] imin2 = new int[2]; int[] imax2 = new int[2];
    int[] imin3 = new int[3]; int[] imax3 = new int[3];

    a3[j3][j2][j1] = min; a3[j3][j2][k1] = max;
    a2[j2][j1]     = min; a2[j2][k1]     = max;
    a1[j1]         = min; a1[k1]         = max;

    dump(a3);
    dump(a2);
    dump(a1);

    byte min3 = min(a3,imin3); byte max3 = max(a3,imax3);
    byte min2 = min(a2,imin2); byte max2 = max(a2,imax2);
    byte min1 = min(a1,imin1); byte max1 = max(a1,imax1);

    assertEq(min,min3);    assertEq(min,min2);    assertEq(min,min1);
    assertEq(j1,imin3[0]); assertEq(j1,imin2[0]); assertEq(j1,imin1[0]);
    assertEq(j2,imin3[1]); assertEq(j2,imin2[1]);
    assertEq(j3,imin3[2]);

    assertEq(max,max3);    assertEq(max,max2);    assertEq(max,max1);
    assertEq(k1,imax3[0]); assertEq(k1,imax2[0]); assertEq(k1,imax1[0]);
    assertEq(j2,imax3[1]); assertEq(j2,imax2[1]);
    assertEq(j3,imax3[2]);

    assertEq(max,max(a3)); assertEq(max,max(a2)); assertEq(max,max(a1));
    assertEq(min,min(a3)); assertEq(min,min(a2)); assertEq(min,min(a1));
  }

  @Test
  public void testRegularity() {
    assertTrue(isRegular(a2));
    assertTrue(isRegular(a3));

    a2[0] = new byte[3];
    a3[0] = a2;

    assertFalse(isRegular(a2));
    assertFalse(isRegular(a3));
  }

  @Test
  public void testIncreasingDecreasingMonotonic() {
    assertTrue(isIncreasing(a1));
    assertFalse(isDecreasing(a1));
    assertTrue(isMonotonic(a1));

    a1 = reverse(a1);

    assertFalse(isIncreasing(a1));
    assertTrue(isDecreasing(a1));
    assertTrue(isMonotonic(a1));

    zero(a1);

    assertFalse(isIncreasing(a1));
    assertFalse(isDecreasing(a1));
    assertFalse(isMonotonic(a1));
  }

  private static void assertOnlyContains(byte val,byte[][][] a) {
    for (int i3=0; i3<a.length; ++i3)
      assertOnlyContains(val,a[i3]);
  }

  private static void assertOnlyContains(byte val,byte[][] a) {
    for (int i2=0; i2<a.length; ++i2)
      assertOnlyContains(val,a[i2]);
  }

  private static void assertOnlyContains(byte val,byte[] a) {
    for (int i1=0; i1<a.length; ++i1)
      assertEquals(val,a[i1]);
  }

  private static void assertArraySize(byte[][][] a,int n1,int n2,int n3) {
    assertEquals(n3,a.length);
    assertEquals(n2,a[0].length);
    assertEquals(n1,a[0][0].length);
  }

  private static void assertArraySize(byte[][] a,int n1,int n2) {
    assertEquals(n2,a.length);
    assertEquals(n1,a[0].length);
  }

  private static void assertArraySize(byte[] a,int n) {
    assertEquals(n,a.length);
  }


  private byte[]     a1;
  private byte[][]   a2;
  private byte[][][] a3;

  private byte[]     b1;
  private byte[][]   b2;
  private byte[][][] b3;

}
