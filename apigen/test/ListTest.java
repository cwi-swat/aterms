package test;

import aterm.*;
import aterm.pure.*;
import java.util.*;

public class ListTest {

  private ListFactory factory;

  public ListTest(ListFactory factory) {
    this.factory = factory;
  }

  public final static void main(String[] args) {
    ListTest test = new ListTest(new ListFactory());
    return;
  }

}

