package apigen;

public class Step
{
  public static final int ARG  = 0;
  public static final int ELEM = 1;
  public static final int TAIL = 2;

  int type;
  int index;

  //{{{ public Step(int type, int index)

  public Step(int type, int index)
  {
    this.type  = type;
    this.index = index;
  }

  //}}}
  //{{{ public void makeTail()

  public void makeTail()
  {
    if (type != ELEM) {
      throw new RuntimeException("only ELEM can be turned " +
				 "into TAIL");
    }
    type = TAIL;
  }

  //}}}

  //{{{ public int getType()

  public int getType()
  {
    return type;
  }

  //}}}
  //{{{ public int getIndex()

  public int getIndex()
  {
    return index;
  }

  //}}}

  //{{{ public String toString()

  public String toString()
  {
    if (type == ARG) {
      return "arg(" + index + ")";
    } else if (type == ELEM) {
      return "elem(" + index + ")";
    } else if (type == TAIL) {
      return "tail(" + index + ")";
    } else {
      throw new RuntimeException("illegal Step type: " + type);
    }
  }

  //}}}
}
