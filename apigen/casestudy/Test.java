public class Test
{
  //{{{ public static final void main(String[] args)

  public static final void main(String[] args)
  {
    Leaf leaf1 = new Leaf(1);
    Leaf leaf2 = new Leaf(2);

    Fork fork1 = new Fork(leaf1, leaf2);
    Fork fork2 = new Fork(leaf2, leaf1);
    Fork fork3 = new Fork(fork1, fork2);

    NodeTree nt = new NodeTree(fork3);
    Apple green = new Apple("green");
    Apple red = new Apple("red");
    Branch branch = new Branch(green, nt, red);

    Counter counter = new Counter();
    LeafAccu accu = new LeafAccu();
    Sequence seq = new Sequence(counter, accu);
    TopDown td = new TopDown(seq);
    td.visit(branch);

    System.out.println("accu = " + accu.accu);
    System.out.println("total nodes = " + counter.counter);
  }

  //}}}
}

//{{{ class LeafAccu

class LeafAccu
  extends NodeVisitorAdapter
{
  int accu = 0;

  public void visitLeaf(Leaf leaf)
  {
    accu += leaf.getValue();
  }
}

//}}}
//{{{ class Counter

class Counter
  extends VisitorSupport
{
  int counter = 0;

  public void visit(Visitable visitable)
  {
    counter++;
  }
}

//}}}
