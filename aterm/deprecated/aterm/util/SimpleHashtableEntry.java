package aterm.util;

import java.util.*;
import java.io.*;

public interface SimpleHashtableEntry {
  public SimpleHashtableEntry getNextHashEntry();
  public void setNextHashEntry(SimpleHashtableEntry next);
}
