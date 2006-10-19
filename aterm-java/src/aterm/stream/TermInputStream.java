package aterm.stream;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * InputStream that is designed for reading term data from a NIO socket. Simply
 * provied it with a buffer and call the waitForEmptyBuffer() method. When
 * reading from the buffer is done the method will return and all further read
 * actions will at that point be blocked. As soon as you supply a new buffer
 * reading will proceed. As soon as the finalized flag is set, no further
 * reading is possible. <br />
 * <br />
 * IMPORTANT NOTE: This class may only be used by two threads at a time, one for
 * reading and one for handling the buffers.
 * 
 * @author Arnold Lankamp
 */
public class TermInputStream extends InputStream{
	private final static int BYTEMASK = 0xFF;

	private final Object lock = new Object();
	private ByteBuffer buffer = null;

	private final Object replaceLock = new Object();
	private boolean replaceable = false;
	private boolean finalized = false;

	/**
	 * Constructor.
	 * 
	 * @param initialBuffer
	 *            The buffer to use for input.
	 */
	public TermInputStream(ByteBuffer initialBuffer){
		super();

		this.buffer = initialBuffer;
	}

	/**
	 * Reads one byte from the present buffer. This call blocks until the buffer
	 * is replaced in case it's empty.
	 * 
	 * @see java.io.InputStream#read()
	 */
	public int read() throws IOException{
		int data = 0;
		synchronized(lock){
			if(finalized) return -1;

			if(buffer.hasRemaining()){
				data = (buffer.get() & BYTEMASK);

				if(!buffer.hasRemaining()){
					synchronized(replaceLock){
						replaceable = true;
						replaceLock.notify();
					}
				}
			}else{
				// This block is just for insurance, in case we received an
				// empty buffer for example.
				synchronized(replaceLock){
					replaceable = true;
					replaceLock.notify();
				}

				try{
					lock.wait();
				}catch(InterruptedException irex){
					throw new RuntimeException("Interrupted while waiting to write a byte.");
				}

				data = read();
			}
		}

		return data;
	}

	/**
	 * Bulk read method. This call blocks until the buffer is replaced in case
	 * it's empty.
	 * 
	 * @see InputStream#read(byte[], int, int)
	 */
	public int read(byte[] data, int offset, int length){
		int dataWritten = 0;
		synchronized(lock){
			if(finalized) return -1;

			if(buffer.hasRemaining()){
				if(length < 0 || (offset + length) > data.length) throw new ArrayIndexOutOfBoundsException("length was < 0 || (offset + length) > data.length");

				dataWritten = length;
				if(length > buffer.remaining()){
					dataWritten = buffer.remaining();
				}

				buffer.get(data, offset, dataWritten);

				if(!buffer.hasRemaining()){
					synchronized(replaceLock){
						replaceable = true;
						replaceLock.notify();
					}
				}
			}else{
				// This block is just for insurance, in case we received an
				// empty buffer for example.
				synchronized(replaceLock){
					replaceable = true;
					replaceLock.notify();
				}

				try{
					lock.wait();
				}catch(InterruptedException irex){
					throw new RuntimeException("Interrupted while waiting to write a byte.");
				}

				// Try again after the buffer has been replaced.
				dataWritten = read(data, offset, length);
			}
		}
		return dataWritten;
	}

	/**
	 * This will continue the stream, with the given buffer as input.
	 * 
	 * @param nextBuffer
	 *            The buffer to use as input.
	 */
	public void proceed(ByteBuffer nextBuffer){
		synchronized(lock){
			synchronized(replaceLock){
				if(!replaceable) throw new RuntimeException("You're not allowed to replace the buffer if it isn't empty");

				this.buffer = nextBuffer;

				replaceable = false;

				lock.notify();
			}
		}
	}

	/**
	 * Returns the present buffer. You can only call this method if the buffer
	 * is ready to be replaced.
	 * 
	 * @return The present buffer.
	 */
	public ByteBuffer getBuffer(){
		if(!replaceable) throw new RuntimeException("Can't retrieve buffer when the stream is still 'live'.");

		return buffer;
	}

	/**
	 * This method will block untill the present buffer is empty.
	 * 
	 * @throws IOException
	 *             When the stream has already been closed.
	 */
	public void waitForEmptyBuffer() throws IOException{
		synchronized(replaceLock){
			if(finalized) throw new IOException("This stream has already been closed");
			if(replaceable) return;

			try{
				replaceLock.wait();
			}catch(InterruptedException irex){
				throw new RuntimeException(irex);
			}
		}
	}

	/**
	 * Terminates this stream. You can no longer supply this stream with buffers
	 * nor read from it; attempting to do so will result in an IOException.
	 */
	public void endStream(){
		synchronized(lock){
			synchronized(replaceLock){
				finalized = true;

				// Notify any blocked threads
				lock.notify();
			}
		}
	}

	/**
	 * Returns true if the buffer needs to be replaced.
	 * 
	 * @return True if the buffer needs to be replaced.
	 */
	public boolean isReplaceable(){
		return replaceable;
	}

	/**
	 * Returns true if the stream is finalized.
	 * 
	 * @return True if the stream is finalized.
	 */
	public boolean isFinalized(){
		return finalized;
	}
}
