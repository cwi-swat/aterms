package aterm.tool;

import java.io.*;
import java.util.*;
import java.net.*;
import aterm.*;


//{ abstract public class Tool implements Runnable

/** 
 * You can create a ToolBus tool written in Java by subclassing
 * this Tool class. Data can be exchanged with the ToolBus using
 * ATerms (see the toolbus.aterm package mentioned below).
 * The Tool package implements Runnable, in order to facilitate
 * running a ToolBus tool as a separate thread.
 *
 * A more elegant way of creating a ToolBus tool is by generating
 * a subclass of this Tool class using javatif (see the JavaTif
 * class mentioned below), and subclassing your own tool class from 
 * this generated class.
 *
 * @author Pieter Olivier
 * @version 0.1
 * @see toolbus.aterm.ATerm
 * @see toolbus.tool.JavaTif
 *
 */

abstract public class Tool implements Runnable
{
  static final int LENSPEC = 8;
  static final int MAX_HANDSHAKE = 512;
  static final int MIN_MSG_SIZE = 128;

  private String name = "unknown";
  private String host = null;
  private InetAddress address = null;
  private int port = 8999;
  private int toolid = -1;
  private Socket socket = null;
  private InputStream istream;
  private OutputStream ostream;
  private ToolBusChannel channel;
  private boolean verbose = false;
  private boolean running;
  private Hashtable queues;

  protected World world;
  private ATerm termSndVoid;

  //{ public Tool(String name)

  /**
     @param name The name of the tool.
    */

  public Tool(String name)
       throws UnknownHostException
  {
    this(name, InetAddress.getLocalHost(), 8998);
  }

  //}
  //{ public Tool(String name, InetAddress address, int port)

  public Tool(String name, InetAddress address, int port)
       throws UnknownHostException
  {
    this.name = name;
    this.address = address;
    this.port = port;
    init();
  }

  //}
  //{ public Tool(String[] args)

  public Tool(String[] args)
       throws UnknownHostException
  {
    for(int i=0; i<args.length; i++) {
      if(args[i].equals("-TB_PORT"))
        port = Integer.parseInt(args[++i]);
      if(args[i].equals("-TB_HOST"))
        address = InetAddress.getByName(args[++i]);
      if(args[i].equals("-TB_TOOL_NAME"))
        name = args[++i];
      if(args[i].equals("-TB_TOOL_ID"))
        toolid = Integer.parseInt(args[++i]);
      if(args[i].equals("-TB_VERBOSE"))
				verbose = true;
    }
    if(address == null)
      address = InetAddress.getLocalHost();
    init();
  }

  //}

  //{ private void init()

  private void init()
  {
    world = ATerm.the_world;
    try {
      termSndVoid = world.makeAppl("snd-void", world.empty);
    } catch (ParseError e) {
      System.err.println("internal error in Tool.init()");
    }
    queues = new Hashtable();
  }

  //}

  //{ public void connect()

  public void connect()
       throws IOException, ToolException
  {
    if(verbose)
      System.err.println("connecting to " + address.toString() + ", port=" + port);

    socket = new Socket(address, port);
    istream = socket.getInputStream();
    ostream = socket.getOutputStream();

    channel = new ToolBusChannel(istream);

    //{ Determine the host name, even for the local host

    host = address.getHostName();
    if(host == null) {
      String pair = address.toString();
      host = pair.substring(0, pair.indexOf('/'));
      if(verbose)
        System.err.println("local host = " + host);
    }

    //}
    //{ Start the handshake
    writeHandshake(name + " " + host + " " + toolid);
    int tid = readInt();
    if(tid < 0)
      throw new ToolException(this, "no port assigned by ToolBus");
    if(toolid < 0) {
      toolid = tid;
      if(verbose)
        System.err.println("got tool-id: " + toolid);
    }
    else if(toolid != tid)
      throw new ToolException(this, "tool-id out of phase");

    //}
    //{ Complete the handshake by checking the input signature

    try {
      ATerm term = readTerm();
			Vector matches = term.match("rec-do(signature([<list>],[<list>]))");
      if(matches != null) {
        if(verbose)
          System.err.println("checking input signature...");
        checkInputSignature((ATermList)matches.elementAt(0));
				send(termSndVoid);
      } else
        throw new ToolException(this, "signature information garbled", term);
    } catch (ParseError e) {
      throw new ToolException(this, "signature information garbled");
    }

  //}

    running = true;
  }

  //}

  //{ private void writeHandshake(String string)

  private void writeHandshake(String string)
     throws IOException
  {
    // jdk 1.1
    // byte[] hs = string.getBytes();

    // jdk 1.02
    byte[] hs = new byte[string.length()];
    string.getBytes(0, string.length(), hs, 0);

    byte[] handshake = new byte[MAX_HANDSHAKE];
    for(int i=0; i<hs.length; i++)
      handshake[i] = hs[i];
    write(handshake);
  }

  //}
  //{ private void write(byte[] data)

  private void write(byte[] data)
       throws IOException
  {
    for(int i=0; i<data.length; i++)
      ostream.write(data[i]);
  }

  //}
  //{ private int readInt()

  private int readInt()
       throws IOException, ToolException
  {
    byte[] buffer = new byte[MAX_HANDSHAKE];

    istream.read(buffer);

    // jdk 1.1
    // String string = new String(buffer);

    // jdk 1.02
    String string = new String(buffer, 0);

    int space = string.indexOf(' ');
    int end = string.indexOf(0);
    String toolname = string.substring(0, space);
    if(!toolname.equals(name))
      throw new ToolException(this, "wrong name in readInt: "+toolname);
    try {
      return Integer.parseInt(string.substring(space+1, end));
    } catch (NumberFormatException e) {
      throw new ToolException(this, "wrong int format: " + string);
    }
  }

  //}
  //{ protected synchronized ATerm readTerm()

  protected synchronized ATerm readTerm()
       throws ParseError, IOException
  {
    channel.reset();
    channel.read();
    ATerm t = world.parseATerm(channel);
    if(verbose) {
      System.err.print("tool " + name + " has read term: ");
      t.print(System.err);
      System.err.println("");
    }
    return t;
  }

  //}
  //{ static public ATerm readTerm(World world, InputStream s)

  static public ATerm readTerm(World world, InputStream s)
    throws ParseError, IOException
  {
    ToolBusChannel channel = new ToolBusChannel(s);
    channel.reset();
    channel.read();
    return world.parseATerm(channel);
  }

  //}
  //{ public synchronized void send(ATerm term)

  public synchronized void send(ATerm term)
    throws ToolException
  {
    try {
      int size = term.toString().length();
      String lenspec = "00000000" + (size+LENSPEC) + ":";
      int len = lenspec.length();
      byte[] ls = new byte[LENSPEC];

      for(int i=0; i<LENSPEC; i++)
				ls[i] = (byte)lenspec.charAt(len+i-LENSPEC);

      if(verbose) {
        System.out.print("tool " + name + " writes term:\n");
        // jdk 1.1
				// System.out.print(new String(ls));

				// jdk 1.02
				System.out.print(new String(ls, 0));

        term.print(System.out);
        System.out.println("");
      }
      ostream.write(ls);
      term.write(ostream);
      if(verbose && LENSPEC+size < MIN_MSG_SIZE)
        System.out.println("filling with " + (MIN_MSG_SIZE-(LENSPEC+size)) + " zero bytes.");
      for(int i=LENSPEC+size; i<MIN_MSG_SIZE; i++)
        ostream.write(0);
    } catch (IOException e) {
      System.err.println(e.toString());
      throw new ToolException(this, "error while writing to ToolBus", term);
    }
  }

  //}

  //{ public void run()

  public void run()
  {
    try {
      while(running && istream != null) {
				channel.reset();
				channel.read();
				ATerm t = world.parseATerm(channel);
				if(verbose) {
					System.err.print("tool " + name + " has read term: ");
					t.print(System.err);
					System.err.println("");
				}
				handleOne(t);
      }
    } catch (IOException e) {
      error("IOException while reading incoming ToolBus term", null);
    } catch (ParseError e) {
      error("parse error while reading incoming ToolBus term", e);
    }
  }

  //}
  //{ private synchronized void handleOne()

  private synchronized void handleOne()
  {
    ATerm term, result;
		Vector terms;
    try {
      term = readTerm();
      result = handler(term);
      if(term.match("rec-terminate(<term>)") != null)
        running = false;
      if(term.match("rec-do(<term>)") != null)
				send(termSndVoid);
			terms = term.match("rec-ack-event(<term>)");
			if(terms != null)
				ackEvent((ATermAppl)terms.elementAt(0));
      if(result != null)
        send(result);
    } catch (IOException e) {
      error("IOException while reading incoming ToolBus term", null);
    } catch (ParseError e) {
      error("parse error while reading incoming ToolBus term", null);
    } catch (ToolException e) {
      error("ToolException generated while handling term", e);
    }
  }

  //}
  //{ private synchronized void handleOne(ATerm term)

  private synchronized void handleOne(ATerm term)
  {
    ATerm result;
		Vector terms;
    try {
      result = handler(term);
      if(term.match("rec-terminate(<term>)") != null)
        running = false;
      if(term.match("rec-do(<term>)") != null)
				send(termSndVoid);
			terms = term.match("rec-ack-event(<term>)");
      if(terms != null)
				ackEvent((ATermAppl)terms.elementAt(0));
      if(result != null)
        send(result);
    } catch (ToolException e) {
      error("ToolException generated while handling term", e);
    }
  }

  //}

  //{ abstract protected void checkInputSignature(ATermList sig)

  abstract protected void checkInputSignature(ATermList sig) 
       throws ToolException;

  //}
  //{ abstract protected ATerm handler(ATerm term)

  abstract protected ATerm handler(ATerm term) throws ToolException;

  //}
  //{ protected void idle()

  protected void idle()
  {
    Thread.yield();
  } 

  //}
  //{ protected void error(String msg, Exception e)

  protected void error(String msg, Exception e)
  {
    System.err.println("warning from tool " + name + ": " + msg);
		if(e != null)
			System.err.println("exception: " + e.toString());
  }

  //}

  //{ public void event(ATermAppl appl)

  /**
   * Send an event, without looking at the event queues.
   * @exception ToolException when a ToolBus connection error occurs.
   */

  public void event(ATermAppl appl)
     throws ToolException
  {
    send(world.make("snd-event(<term>)", appl));
  }

  //}
  //{ public void post(ATermAppl appl)

  /**
   * Post an event. If no corresponding ack is waiting,
   * the event is send immediately. Otherwise, the event
   * is stored in its event queue.
   * @exception ToolException when a ToolBus connection error occurs.
   */

  public void post(ATermAppl appl)
     throws ToolException
  {
    EventQueue Queue = (EventQueue)queues.get(appl.getName());
    if(Queue == null) {
      Queue = new EventQueue();
      queues.put(appl.getName(), Queue);
    }
    if(Queue.ackWaiting())
      Queue.addEvent(appl);
    else {
      send(world.make("snd-event(<term>)", appl));
      Queue.setAckWaiting();
    }
  }

  //}
  //{ protected void ackEvent(ATerm t)

  /**
   * Acknowledge an event. When this event has an event
   * queue, and more events are waiting in it, the next
   * event is sent.
   */

  private void ackEvent(ATerm t)
       throws ToolException
  {
    ATermAppl appl = (ATermAppl)t;
    EventQueue queue = (EventQueue)queues.get(appl.getName());
    if(queue != null && queue.ackWaiting()) {
      appl = queue.nextEvent();
      if(appl != null) {
				send(world.make("snd-event(<term>)", appl));
      }
    }
  }

  //}

  public boolean getVerbose()                  { return verbose; }
  public void setVerbose(boolean on)           { verbose = on; }
  public String  getName()                     { return name; }
  public int getTid()                          { return toolid; }
  public InetAddress getAddress()              { return address; }
  public int getPort()                         { return port; }
}

//}

//{ class EventQueue extends Vector

/**
 * The class EventQueue stores a queue of events, and their
 * acknowledgement status.
 *
 */

class EventQueue
{
  private boolean ack = false;
  private Vector events = new Vector();

  public boolean ackWaiting() { return ack; }
  public void setAckWaiting() { ack = true; }
  public ATermAppl nextEvent()
  {
    if(events.size() == 0) {
      ack = false;
      return null;
    }

    ATermAppl event = (ATermAppl)events.firstElement();
    events.removeElementAt(0);
    return event;
  }
  
  public void addEvent(ATermAppl event)
  {
    events.addElement(event);
  }
}

//}


