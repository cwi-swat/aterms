package toolbus;

import java.io.*;
import java.awt.*;
import java.awt.event.*;

import aterm.*;

public class Example
  extends Frame
  implements ExampleTif, ActionListener
{
  private ATermFactory factory;
  private ExampleBridge bridge;
  private Button button;
  private int count;

  public static final void main(String[] args)
    throws IOException
  {
    Example example = new Example(args);
  }

  public Example(String[] args)
    throws IOException
  {
    factory = new aterm.pure.PureFactory();

    // Build the user interface: just a single button
    button = new Button("Button");
    button.addActionListener(this);
    add(button);
    pack();
    show();

    // Create the bridge that will forward incoming messages
    // to method calls in this Example object
    bridge = new ExampleBridge(factory, this);

    // Initalize vital parameters, like the ToolBus TCP/IP port, tool name,
    // etc. that are passed to us using the command line arguments.
    bridge.init(args);
    
    // Actually establish the connection with the ToolBus
    bridge.connect();

    // Start the tool event loop
    bridge.run();
  }

  public void actionPerformed(ActionEvent event)
  {
    if (event.getSource() == button) {
      // When the user presses the button, we send an event to the ToolBus
      bridge.postEvent(factory.make("button(<str>)", button.getLabel()));
    }
  }

  public ATerm msg(String message)
  {
    // Print the incoming message
    System.out.println("Example tool received msg: " + message);

    // Increase the counter and return the current value to the ToolBus
    return factory.make("snd-value(<int>)", new Integer(count++));
  }

  public void recAckEvent(ATerm event)
  {
    // This simple tool ignores event acknowledgements
  }

  public void recTerminate(ATerm arg)
  {
    // Just exit when the ToolBus terminates
    System.exit(0);
  }
}
