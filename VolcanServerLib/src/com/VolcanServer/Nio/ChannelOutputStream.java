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
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.WritableByteChannel;

/**
 * A non-blocking channel output stream which may wait until ready.
 * 
 * <p>
 * If a selection key is given, the stream sets OP_WRITE to the interest set of
 * the key and waits for the buffer, when it needs it flushed, until a timeout
 * has elapsed or until notified. After the timeout or the notification, it
 * synchronizes the buffer with the channel. The notifier thread may also flush
 * the buffer before notifying the stream or if the stream is not in the wait
 * state.
 * </p>
 * 
 * <p>
 * Note that all I/O operations are synchronized on the buffer.
 * </p>
 * 
 * @author Ilkka Priha
 * @version $Id: ChannelOutputStream.java,v 1.13 2009/09/28 15:08:50 cvsimp Exp $
 */
public class ChannelOutputStream extends OutputStream
{
    /**
     * The ready mask.
     */
    private static final int READY = 0x00000001;

    /**
     * The selected mask.
     */
    private static final int SELECTED = 0x00000002;

    /**
     * The waiting mask.
     */
    private static final int WAITING = 0x00000004;

    /**
     * The notified mask.
     */
    private static final int NOTIFIED = 0x00000008;

    /**
     * The writable channel.
     */
    private final WritableByteChannel channel;

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
     * Constructs a new stream.
     * 
     * @param key the selection key.
     * @param c the writable channel.
     * @param b the buffer in the put state.
     * @param t timeout in msecs to wait until ready.
     */
    public ChannelOutputStream(SelectionKey key, WritableByteChannel c,
        ByteBuffer b, long t)
    {
        super();
        if ((key == null) && (c == null) || (b == null))
        {
            throw new NullPointerException("SelectionKey key...");
        }

        selectionKey = key;
        channel = c;
        buffer = b;
        timeout = t < 0 ? -1 : t;
        secure = c instanceof SecureChannel ? (SecureChannel) c : null;
    }

    public void write(int b) throws IOException
    {
        synchronized (buffer)
        {
            if (buffer.remaining() <= 0)
            {
                drain();
            }
            buffer.put((byte) (b & 0x000000FF));
        }
    }

    public void write(byte[] bb, int off, int len) throws IOException
    {
        synchronized (buffer)
        {
            while (len > 0)
            {
                int r = buffer.remaining();
                if (r <= 0)
                {
                    drain();
                    r = buffer.remaining();
                }
                if (r > len)
                {
                    r = len;
                }
                buffer.put(bb, off, r);
                off += r;
                len -= r;
            }
        }
    }

    public void flush() throws IOException
    {
        drain();
    }

    public void close() throws IOException
    {
        synchronized (buffer)
        {
            flush();
            channel.close();
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
     * buffer. If none is available, write in the calling thread.
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
                    // in a write operation and we don't want this
                    // thread to block...
                    synchronized (buffer)
                    {
                        // No write ops needed until the next round starts.
                        if (selectionKey.isValid()
                            && (selectionKey.interestOps() != 0))
                        {
                            selectionKey.interestOps(0);
                        }

                        waiting |= READY | NOTIFIED;
                        buffer.notifyAll();
                    }
                }
            }
            synced = true;
        }
        else
        {
            // No selected threads, write in the calling one.
            boolean finished = (secure == null) || secure.finished();
            int n = write(finished);
            synced = n >= 0;
        }
        return synced;
    }

    /**
     * Drains the buffer if it is not empty.
     * 
     * @return the number of bytes left or -1 if not written.
     * @throws IOException on I/O errors.
     */
    private int drain() throws IOException
    {
        synchronized (buffer)
        {
            // Check whether we have bytes to drain and write only if we
            // have. Don't allow more than one thread to wait for drain.
            int n = buffer.position();
            if (secure != null)
            {
                n += secure.encrypted();
            }
            if ((n > 0) && ((waiting & SELECTED) == 0))
            {
                // We have bytes to drain, wait until notified,
                // if the timeout is specified, as writing to
                // a socket channel without a proper ready operation
                // in the selection key proved to cause invalid
                // responses causing browsers to block occasionally.
                try
                {
                    waiting |= SELECTED;

                    int p;
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
                        interrupted = Thread.currentThread().isInterrupted();
                        long t = timeout;
                        if ((t != 0) && !interrupted && selectionKey.isValid()
                            && channel.isOpen())
                        {
                            // We have a non-interrupted and valid condition
                            // to wait for the specified timeout to be notified
                            // about a write operation, but if the buffer size
                            // was smaller than the number of bytes to be
                            // written, we might try to write without waiting
                            // unless we are handshaking or the selection key
                            // doesn't indicate that the channel is ready for
                            // writing.
                            if (!finished
                                || (continuation > 0)
                                || ((waiting & READY) == 0)
                                || ((selectionKey.readyOps() & SelectionKey.OP_WRITE) == 0))
                            {
                                try
                                {
                                    // Mark as waiting before wakeup
                                    // to not to miss the notification.
                                    waiting |= WAITING;
                                    waiting &= ~NOTIFIED;

                                    // DON'T change the interest set if we
                                    // are in the middle of the handshake.
                                    if (finished)
                                    {
                                        // Set the interest set
                                        // to OP_WRITE if missing.
                                        int ops = selectionKey.interestOps();
                                        if ((ops & SelectionKey.OP_WRITE) == 0)
                                        {
                                            selectionKey
                                                .interestOps(SelectionKey.OP_WRITE);
                                        }
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
                        p = n;
                        n = write(finished);
                        continuation++;

                        // Continue as long as we are notified
                        // and have bytes left to be written.
                    } while ((((n > 0) && (n < p)) || ((n >= 0) && !finished))
                        && !interrupted && !expired);
                }
                finally
                {
                    // The ready state can't be maintained across
                    // write operations, opposite to read operations,
                    // even when the buffer is fully drained to make
                    // responses finnish properly.
                    waiting = 0;
                }
            }
            return n;
        }
    }

    /**
     * Writes available bytes from the buffer to the channel.
     * 
     * @param finished false if still handshaking, true otherwise.
     * @return the number of bytes left or -1 if not written.
     * @throws IOException on I/O errors.
     */
    private int write(boolean finished) throws IOException
    {
        synchronized (buffer)
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
                int r = secure != null ? secure.encrypted() : 0;
                int p = buffer.position();
                if ((p > 0) || (r > 0))
                {
                    if (r == 0)
                    {
                        buffer.flip();
                    }

                    try
                    {
                        if (secure != null)
                        {
                            if (r > 0)
                            {
                                secure.flush();
                                n = 0;
                            }
                            else
                            {
                                n = p - channel.write(buffer);
                            }
                            n += secure.encrypted();
                        }
                        else
                        {
                            n = p - channel.write(buffer);
                        }
                    }
                    finally
                    {
                        if (r == 0)
                        {
                            buffer.compact();
                        }

                        if (selectionKey.isValid())
                        {
                            int ops = selectionKey.interestOps();
                            if ((n > 0) && ((ops & SelectionKey.OP_WRITE) == 0))
                            {
                                // We need to write more.
                                selectionKey.interestOps(SelectionKey.OP_WRITE);
                            }
                            else if ((n <= 0) && (ops != 0))
                            {
                                // Everything was written or not writable.
                                selectionKey.interestOps(0);
                            }
                        }
                    }
                }
                else
                {
                    // Incidentally we have nothing to write but we don't
                    // clear the interest set as that could prevent us to
                    // dissolve this condition e.g. during handshake.
                }
            }
            else if (selectionKey.isValid())
            {
                n = buffer.position();
            }
            return n;
        }
    }
}
