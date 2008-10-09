/*
 * Copyright (c) 2002-2008, CWI and INRIA
 *
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the University of California, Berkeley nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package aterm.test;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import aterm.ATerm;
import aterm.ATermFactory;
import aterm.pure.PureFactory;

/**
 * Exercise the various readers. Supply the test/ directory as the
 * first command line argument. It will read the various test.* files
 * there and apply the appropriate reader.
 * 
 * @author Karl Trygve Kalleberg
 *
 */
public class TestReaders {
  ATermFactory factory;
  String srcdir;
  ATerm baseline;

  public final static void main(String[] args) throws IOException {
	  TestReaders pureSuite = new TestReaders(new PureFactory(), args[0]);
	  pureSuite.testAll();
  }

  public TestReaders(ATermFactory factory, String srcdir) {
    this.factory = factory;
    this.srcdir = srcdir;
  }

  void test_assert(boolean condition) {
    if (!condition) {
      throw new RuntimeException("assertion failed.");
    }
  }

  void testReadText() throws FileNotFoundException, IOException {
	  ATerm t = factory.readFromTextFile(new FileInputStream(srcdir + "/" + "test.trm"));
	  baseline = t;
  }
  
  void testReadBAF() throws FileNotFoundException, IOException {
	  ATerm t = factory.readFromBinaryFile(new FileInputStream(srcdir + "/" + "test.baf"));
	  test_assert(t.equals(baseline));
  }
  
  void testReadSAF() {
	  // test SAF reader
  }
  
  void testReadTAF() throws FileNotFoundException, IOException {
	  ATerm t = factory.readFromSharedTextFile(new FileInputStream(srcdir + "/" + "test.taf"));
	  test_assert(t.equals(baseline));
  }
  
  
  public void testAll() throws IOException {
	  testReadText();
	  testReadBAF();
	  testReadSAF();
	  testReadTAF();
  }

}
