// Java tool interface class Testing
// This file is generated automatically, please do not edit!
// generation time: 24-Jun-98 5:20:10 PM

package aterm.tool;
import aterm.*;
import aterm.tool.*;
import java.net.UnknownHostException;
import java.net.InetAddress;
import java.util.Vector;
import java.util.Hashtable;


abstract public class Testing extends Tool
{
  // This table will hold the complete input signature
  private Hashtable sigTable = new Hashtable();

  // Declare the patterns that are used to match against incoming terms
  private ATerm Ptestit0;
  private ATerm Ptestit1;
  private ATerm Ptestit2;
  private ATerm Pquestion0;
  private ATerm PrecTerminate0;
  private ATerm PrecAckEvent0;

  // Mimic the three constructors from the Tool class
  protected Testing(String name) throws UnknownHostException { super(name); init(); }
  protected Testing(String name, InetAddress address, int port) throws UnknownHostException  { super(name, address, port); init(); }
  protected Testing(String[] args) throws UnknownHostException { super(args); init(); }

  // Initializations common to all constructors
  private void init() { initSigTable(); initPatterns(); }

  // This method initializes the table with input signatures
  private void initSigTable()
  {
    try {
      sigTable.put(world.parse("rec-terminate(<testing>,<term>)"), new Boolean(true));
      sigTable.put(world.parse("rec-ack-event(<testing>,<term>)"), new Boolean(true));
      sigTable.put(world.parse("rec-eval(<testing>,question(square))"), new Boolean(true));
      sigTable.put(world.parse("rec-do(<testing>,testit(<str>,f(<int>)))"), new Boolean(true));
      sigTable.put(world.parse("rec-do(<testing>,testit(<str>,<int>))"), new Boolean(true));
      sigTable.put(world.parse("rec-do(<testing>,testit(<str>))"), new Boolean(true));
    } catch (ParseError e) { }
  }

  // Initialize the patterns that are used to match against incoming terms
  private void initPatterns()
  {
    try {
      Ptestit0 = world.parse("rec-do(testit(<str>))");
      Ptestit1 = world.parse("rec-do(testit(<str>,<int>))");
      Ptestit2 = world.parse("rec-do(testit(<str>,<appl>))");
      Pquestion0 = world.parse("rec-eval(question(<appl>))");
      PrecTerminate0 = world.parse("rec-terminate(<term>)");
      PrecAckEvent0 = world.parse("rec-ack-event(<term>)");
    } catch (ParseError e) {}
  }


  // Override these abstract methods to handle incoming ToolBus terms
  abstract void testit(String s0) throws ToolException;
  abstract void testit(String s0, int i1) throws ToolException;
  abstract void testit(String s0, ATermAppl a1) throws ToolException;
  abstract ATerm question(ATermAppl a0) throws ToolException;
  abstract void recTerminate(ATerm t0) throws ToolException;
  abstract void recAckEvent(ATerm t0) throws ToolException;

  // The generic handler calls the specific handlers
  protected ATerm handler(ATerm term)
	throws ToolException
  {
    Vector result;
    result = term.match(Ptestit0);
if(result != null) {
      testit((String)result.elementAt(0));
    } else result = term.match(Ptestit1);
if(result != null) {
      testit((String)result.elementAt(0), ((Integer)result.elementAt(1)).intValue());
    } else result = term.match(Ptestit2);
if(result != null) {
      testit((String)result.elementAt(0), (ATermAppl)result.elementAt(1));
    } else result = term.match(Pquestion0);
if(result != null) {
      return question((ATermAppl)result.elementAt(0));
    } else result = term.match(PrecTerminate0);
if(result != null) {
      recTerminate((ATerm)result.elementAt(0));
    } else result = term.match(PrecAckEvent0);
if(result != null) {
      recAckEvent((ATerm)result.elementAt(0));
    } else 
      notInInputSignature(term);
    return null;
  }

  // Check the input signature
  protected void checkInputSignature(ATermList sigs)
         throws ToolException
  {
    while(!sigs.isEmpty()) {
      ATermAppl sig = (ATermAppl)sigs.getFirst();
      sigs = sigs.getNext();
      if(!sigTable.containsKey(sig)) {
        // Sorry, but the term is not in the input signature!
        notInInputSignature(sig);
      }
    }
  }

  // This function is called when an input term
  // was not in the input signature.
  void notInInputSignature(ATerm t)
        throws ToolException
  {
    throw new ToolException(this, "term not in input signature", t);
  }
}

