/**
 * Copyright (c) 2006 The Norther Organization (http://www.norther.org).
 *
 * Tammi is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * Tammi is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Tammi; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02111-1307 USA
 */

package com.VolcanServer.Nio;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SelectionKey;

/**
 * A non-blocking channel input stream which may wait before ready.
 * 
 * <p>
 * If the timeout is positive, the stream adds OP_READ to the interest set of
 * the key and waits for the buffer when it needs a filled one until a timeout
 * has elapsed or until notified. If the timeout is negative, the interest set
 * of the key is not modified and the stream waits for the buffer without a
 * timeout until notified. The latter alternative allows better control over the
 * selection process from outside of the stream.
 * </p>
 * 
 * <p>
 * Note that I/O operations are synchronized on the buffer.
 * </p>
 * 
 * @author Ilkka Priha
 * @version $Id: ChannelInputStream.java,v 1.13 2009/09/28 15:08:50 cvsimp Exp $
 */
public class ChannelInputStream extends InputStream
{
    /**
     * The selected mask.
     */
    private static final int SELECTED = 0x00000001;

    /**
     * The waiting mask.
     */
    private static final int WAITING = 0x00000002;

    /**
     * The notified mask.
     */
    private static final int NOTIFIED = 0x00000004;

    /**
     * The channel.
     */
    private final ReadableByteChannel channel;

    /**
     * The optional secure channel.
     */
    private final SecureChannel secure;

    /**
     * The selection key.
     */
    private final SelectionKey selectionKey;

    /**
     * A byte buffer.
     */
    private final ByteBuffer buffer;

    /**
     * The block timeout.
     */
    private volatile long timeout;

    /**
     * The wait state.
     */
    private int waiting;

    /**
     * The marked position.
     */
    private int marked = -1;

    /**
     * Constructs a new stream.
     * 
     * @param key the selection key.
     * @param c the readable channel.
     * @param b the buffer in the get state.
     * @param t timeout in msecs to wait for the buffer.
     */
    public ChannelInputStream(SelectionKey key, ReadableByteChannel c,
        ByteBuffer b, long t)
    {
        super();
        if ((key == null) || (c == null) || (b == null))
        {
            throw new NullPointerException("SelectionKey key...");
        }

        selectionKey = key;
        channel = c;
        buffer = b;
        timeout = t < 0 ? -1 : t;
        secure = c instanceof SecureChannel ? (SecureChannel) c : null;
    }

    @Override
    public int available() throws IOException
    {
        synchronized (buffer)
        {
            int n = timeout != 0 ? buffer.remaining() : fill();
            return n < 0 ? 0 : n;
        }
    }

    @Override
    public int read() throws IOException
    {
        synchronized (buffer)
        {
            return fill() > 0 ? buffer.get() : -1;
        }
    }

    @Override
    public int read(byte bb[], int off, int len) throws IOException
    {
        synchronized (buffer)
        {
            int n = 0;
            while (len > 0)
            {
                int x = fill();
                if (x > 0)
                {
                    if (x > len)
                    {
                        x = len;
                    }
                    buffer.get(bb, off, x);
                    len -= x;
                    off += x;
                    n += x;
                }
                else
                {
                    if ((x < 0) && (n == 0))
                    {
                        n = -1;
                    }
                    break;
                }
            }
            return n;
        }
    }

    @Override
    public void mark(int readlimit)
    {
        synchronized (buffer)
        {
            try
            {
                if (readlimit > available())
                {
                    throw new IllegalArgumentException("Read limit too big");
                }
            }
            catch (IOException x)
            {
                throw new IllegalStateException("Mark not available");
            }

            buffer.mark();
            marked = buffer.position();
        }
    }

    @Override
    public void reset() throws IOException
    {
        synchronized (buffer)
        {
            marked = -1;
            buffer.reset();
        }
    }

    @Override
    public boolean markSupported()
    {
        return true;
    }

    @Override
    public void close() throws IOException
    {
        synchronized (buffer)
        {
            marked = -1;
            channel.close();
            buffer.clear();
            buffer.limit(0);
            buffer.notifyAll();
        }
    }

    /**
     * Gets the waiting timeout of this stream.
     * 
     * @return the timeout in msecs.
     */
    public long getTimeout()
    {
        return timeout;
    }

    /**
     * Sets the waiting timeout of this stream.
     * 
     * @param t the timeout in msecs.
     */
    public void setTimeout(long t)
    {
        timeout = t < 0 ? -1 : t;
    }

    /**
     * Syncs the stream with the channel by notifying the thread waiting on the
     * buffer. If none is available, read in the calling thread.
     * 
     * @return true if synced, false otherwise.
     * @throws IOException on I/O errors.
     */
    public boolean sync() throws IOException
    {
        boolean synced;
        if ((waiting & SELECTED) != 0)
        {
            // A thread is selected.
            if ((waiting & WAITING) != 0)
            {
                // A thread is waiting.
                if ((waiting & NOTIFIED) == 0)
                {
                    // A thread is not yet notified, don't synchronize
                    // before necessary as the thread may be blocked
                    // in a read operation and we don't want this
                    // thread to block.
                    synchronized (buffer)
                    {
                        if (selectionKey.isValid()
                            && (selectionKey.interestOps() != 0))
                        {
                            // No read ops needed until the next round starts.
                            selectionKey.interestOps(0);
                        }

                        waiting |= NOTIFIED;
                        buffer.notifyAll();
                    }
                }
            }
            synced = true;
        }
        else
        {
            // No selected threads, read in the calling one.
            boolean finished = (secure == null) || secure.finished();
            synchronized (buffer)
            {
                synced = read(buffer, finished) >= 0;
            }
        }
        return synced;
    }

    /**
     * Fills the buffer if it is empty.
     * 
     * @return the number of bytes or -1 if eof.
     * @throws IOException on I/O errors.
     */
    private int fill() throws IOException
    {
        synchronized (buffer)
        {
            // Check whether we have bytes already and read new ones only
            // if not. Don't allow more than one thread to wait for fill.
            int n = buffer.remaining();
            if ((n <= 0) && ((waiting & SELECTED) == 0))
            {
                // We don't have remaining bytes, wait until notified,
                // if the timeout is specified, as reading from
                // a socket channel without a proper ready operation
                // in the selection key proved to cause invalid responses
                // causing browsers to block occasionally.
                try
                {
                    waiting |= SELECTED;

                    boolean interrupted;
                    int continuation = 0;
                    boolean expired = false;
                    boolean finished = false;
                    do
                    {
                        if (!finished)
                        {
                            finished = (secure == null) || secure.finished();
                        }
                        long t = timeout;
                        interrupted = Thread.currentThread().isInterrupted();
                        if ((t != 0) && !interrupted && selectionKey.isValid()
                            && channel.isOpen())
                        {
                            // We have a non-interrupted and valid condition
                            // to wait for the specified timeout to be notified
                            // about a read operation, but if the buffer size
                            // was smaller than the number of bytes to be
                            // read, we might try to read without waiting
                            // unless we are handshaking or the selection key
                            // doesn't indicate that the channel is ready for
                            // reading.
                            if (!finished
                                || (continuation > 0)
                                || ((selectionKey.readyOps() & SelectionKey.OP_READ) == 0))
                            {
                                try
                                {
                                    // Mark as waiting before wakeup
                                    // to not to miss the notification.
                                    waiting |= WAITING;
                                    waiting &= ~NOTIFIED;

                                    // DON'T change the interest set if we
                                    // are in the middle of the handshake,
                                    // otherwise set it to OP_READ if missing.
                                    if (finished
                                        && (selectionKey.interestOps() & SelectionKey.OP_READ) == 0)
                                    {
                                        selectionKey
                                            .interestOps(SelectionKey.OP_READ);
                                    }

                                    // Wakeup seems to be required
                                    // to be sure to be notified.
                                    selectionKey.selector().wakeup();

                                    if (t > 0)
                                    {
                                        buffer.wait(t);
                                    }
                                    else
                                    {
                                        buffer.wait();
                                    }
                                    expired = (waiting & NOTIFIED) == 0;
                                }
                                catch (InterruptedException x)
                                {
                                    Thread.currentThread().interrupt();
                                    interrupted = true;
                                }
                                finally
                                {
                                    waiting &= ~WAITING;
                                }
                            }
                        }
                        n = read(buffer, finished);
                        continuation++;

                        // We should have received something but especially
                        // MSIE has problems to keep in sync, so we retry
                        // as long as we are notified, and not expired or
                        // interrupted, until we get some bytes.
                    } while ((n == 0) && !interrupted && !expired);

                    n = buffer.remaining();
                    if (n == 0)
                    {
                        n = -1;
                    }
                }
                finally
                {
                    waiting = 0;
                }
            }
            return n;
        }
    }

    /**
     * Reads the available bytes from the channel to the buffer.
     * 
     * Note that the buffer must be synchronized by the caller.
     * 
     * @param bb the byte buffer.
     * @param finished false if still handshaking, true otherwise.
     * @return the number of bytes read or -1 if eof or not read.
     * @throws IOException on I/O errors.
     */
    private int read(ByteBuffer bb, boolean finished) throws IOException
    {
        int n = -1;
        if (!finished)
        {
            // Still handshaking.
            if (selectionKey.isValid())
            {
                int ops = selectionKey.readyOps();
                int hsk = secure.handshake(ops);
                if (hsk == 0)
                {
                    // Handshake finished, restore OP_READ | OP_WRITE
                    // as we are not sure what to do after handskake.
                    selectionKey.interestOps(SelectionKey.OP_READ
                        | SelectionKey.OP_WRITE);
                    finished = true;
                }
                else if ((selectionKey.interestOps() & hsk) == 0)
                {
                    // Continue handshaking.
                    selectionKey.interestOps(hsk);
                }
            }
        }
        if (finished)
        {
            int r = bb.remaining();
            if (r > 0)
            {
                // Compact unread or marked ones.
                if (marked > 0)
                {
                    bb.reset();
                    bb.compact();
                    bb.flip();
                    bb.mark();
                    bb.position(bb.limit() - r);
                    marked = 0;
                }
                else
                {
                    bb.compact();
                    bb.flip();
                }
            }
            else
            {
                // Clear read ones.
                bb.clear();
                bb.limit(0);
                marked = -1;
            }

            int capacity = bb.capacity() - bb.limit();
            if (capacity > 0)
            {
                // Append after unread ones.
                int p = bb.position();
                bb.position(bb.limit());
                bb.limit(bb.capacity());
                try
                {
                    n = channel.read(bb);
                }
                finally
                {
                    bb.limit(bb.position());
                    bb.position(p);

                    if ((n != 0) && selectionKey.isValid()
                        && (selectionKey.interestOps() != 0))
                    {
                        // Something was read or is not readable,
                        // clear the interest set for the next round.
                        selectionKey.interestOps(0);
                    }
                }
            }
            else
            {
                // Incidentally we have no room to read but we don't
                // clear the interest set as that could prevent us to
                // dissolve this condition e.g. during handshake.
            }
        }
        else if (selectionKey.isValid())
        {
            n = 0;
        }
        return n;
    }
}
