package aterm.stream;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

/**
 * OutputStream that is designed for streaming ATerms over a NIO socket. Simply
 * provied it with a buffer and call the waitForFilledBuffer() method. When
 * writing to the buffer is done the method will return and all further write
 * actions will be blocked. As soon as you supply a new buffer writing will
 * proceed. As soon as the finalized flag is set, the buffer can be retrieved
 * and no further writing is possible.<br />
 * <br />
 * IMPORTANT NOTE: This class may only be used by two threads at a time, one for
 * writing and one for handling the buffers.
 * 
 * @author Arnold Lankamp
 */
public class TermOutputStream extends OutputStream{
	private final Object lock = new Object();
	private ByteBuffer buffer = null;

	private final Object replaceLock = new Object();
	private boolean replaceable = false;
	private boolean finalized = false;

	/**
	 * Constructor
	 * 
	 * @param initialBuffer
	 *            The bytebuffer this stream should be initiated with.
	 */
	public TermOutputStream(ByteBuffer initialBuffer){
		super();

		this.buffer = initialBuffer;
		this.buffer.clear();
	}

	/**
	 * Writes a single byte to the underlaying buffer. This method should only
	 * be called by a single thread at a time. When the underlaying buffer is
	 * full this method call will block untill it is replaced.
	 * 
	 * @see java.io.OutputStream#write(int)
	 */
	public void write(int byteToWrite) throws IOException{
		synchronized(lock){
			if(finalized) throw new IOException("Unable to write because the stream has been finalized.");

			if(buffer.hasRemaining()){
				buffer.put((byte) byteToWrite);

				if(!buffer.hasRemaining()){
					synchronized(replaceLock){
						replaceable = true;
						replaceLock.notify();
					}
				}
			}else{
				try{
					lock.wait();
				}catch(InterruptedException irex){
					throw new RuntimeException("Interrupted while waiting to write a byte. The stream was probably finalized prematuraly.");
				}

				write(byteToWrite);
			}
		}
	}

	/**
	 * Bulk write method. This method should only be called by a single thread
	 * at a time. When the underlaying buffer is full this method call will
	 * block untill it is replaced.
	 * 
	 * @see OutputStream#write(byte[], int, int)
	 */
	public void write(byte[] bytes, int offset, int length) throws IOException{
		synchronized(lock){
			if(finalized) throw new IOException("Unable to write because the stream has been finalized.");

			int remaining = buffer.remaining();
			if(remaining > 0){
				if(remaining > length){
					buffer.put(bytes, offset, length);
				}else if(remaining < length){
					buffer.put(bytes, offset, remaining);

					synchronized(replaceLock){
						replaceable = true;
						replaceLock.notify();
					}

					write(bytes, offset + remaining, length - remaining);
				}else if(remaining == length){
					buffer.put(bytes, offset, length);

					synchronized(replaceLock){
						replaceable = true;
						replaceLock.notify();
					}
				}
			}else{
				try{
					lock.wait();
				}catch(InterruptedException irex){
					throw new RuntimeException("Interrupted while waiting to write a byte. The stream was probably finalized prematuraly.");
				}

				write(bytes, offset, length);
			}
		}
	}

	/**
	 * This will continue the stream. It will cause the present buffer to be
	 * cleared.
	 */
	public void proceed(){
		synchronized(lock){
			synchronized(replaceLock){
				if(!replaceable) throw new RuntimeException("You're not allowed to continue if the buffer isn't full");

				replaceable = false;

				if(!finalized){
					buffer.clear();

					lock.notify();
				}
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

		buffer.flip();

		return buffer;
	}

	/**
	 * This method will block untill the present buffer is filled.
	 * 
	 * @throws IOException
	 *             When the stream has already been closed.
	 */
	public void waitForFilledBuffer() throws IOException{
		synchronized(replaceLock){
			if(replaceable) return;
			if(finalized) throw new IOException("This stream has already been closed");

			try{
				replaceLock.wait();
			}catch(InterruptedException irex){
				throw new RuntimeException(irex);
			}
		}
	}

	/**
	 * Terminates this stream. If any threads where waiting to retreive the
	 * buffer they can. Futher writing will no longer be possible.
	 */
	public void endStream(){
		synchronized(lock){
			synchronized(replaceLock){
				replaceable = true;
				finalized = true;

				replaceLock.notify();

				// Notify any blocked thread, just in case
				lock.notify();
			}
		}
	}

	/**
	 * Returns true if the buffer can be replaced.
	 * 
	 * @return True if the buffer can be replaced.
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
