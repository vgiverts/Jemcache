package org.jemcache.codec;

import org.jemcache.IJemcacheConstants;
import org.jemcache.api.*;
import org.jemcache.server.ISocketSession;
import org.jemcache.util.IOutput;
import org.jemcache.util.IByteBuffer;
import org.jemcache.util.CumulativeByteBuffer;

import java.nio.ByteBuffer;

/**
 * Created by IntelliJ IDEA.
 * User: Vladimir Giverts
 * Date: Dec 3, 2007
 * Time: 5:26:18 PM
 */
public class MemcachedProtocolDecoder implements IMemcachedMessageDecoder {

    private static final String DECODER_STATE = MemcachedProtocolDecoder.class + ".DECODER_STATE";

    public boolean decode(IByteBuffer in, IOutput<MemcachedMessage> out, ISocketSession session) {
        DecoderState decoderState = getState(session);

        while (true) {
            switch (decoderState.step) {

                case Command:
                    String line = readLine(in);
                    if (line == null) {
                        // Entire line doesn't exist yet.
                        return false;
                    } else {
                        int i = line.indexOf(' ');
                        decoderState.message = getMessage(line, i);
                        if (i != -1)
                            decoderState.message.parse(line.substring(i + 1));

                        // If this is a data message, then goto Data, otherwise we're Done.
                        if (decoderState.message instanceof DataMessage) {
                            decoderState.step = Step.Data;
                            break;
                        } else {
                            out.write(decoderState.message);
                            session.removeAttribute(DECODER_STATE);
                            return true;
                        }
                    }

                case Data:
                    DataMessage dataMessage = (DataMessage) decoderState.message;

                    int totalBytesRead = readBytes(in, dataMessage.getData(), dataMessage.getBytesRead());
                    dataMessage.setBytesRead(totalBytesRead);
                    if (totalBytesRead == dataMessage.getBytes()) {
                        out.write(decoderState.message);
                        decoderState.step = Step.EOL;
                        break;
                    } else {
                        return false;
                    }

                case EOL:
                    line = readLine(in);
                    if (line != null) {
                        session.removeAttribute(DECODER_STATE);
                        return true;
                    } else {
                        return false;
                    }

            }
        }
    }

    private int readBytes(IByteBuffer data, byte[] dest, int destOffset) {
        int remaining = data.remaining();
        int toRead = Math.min(remaining, dest.length - destOffset);
        data.get(dest, destOffset, toRead);
        return toRead + destOffset;
    }

    private String readLine(IByteBuffer in) {
        in.mark();
        int len = in.remaining();
        byte[] bytes = new byte[len];
        in.get(bytes);
        boolean eol = false;
        int lineLen = -1;
        loop:
        for (int i = 0; i < len; i++) {
            switch (bytes[i]) {
                case 10:
                    if (eol)
                        lineLen = i - 1;
                    break loop;
                case 13:
                    eol = true;
                    break;
                default:
                    if (eol)
                        eol = false;
            }
        }
        if (lineLen != -1) {
            String str = new String(bytes, 0, lineLen);
            if (lineLen != len - 1) {
                if (in instanceof CumulativeByteBuffer)
                    ((CumulativeByteBuffer) in).replace(ByteBuffer.wrap(bytes, lineLen + 2, len - (lineLen + 2)));
                else {
                    in.reset();
                    in.get(bytes, 0, lineLen + 2);
                }
            }
            return str;
        } else {
            if (in instanceof CumulativeByteBuffer)
                ((CumulativeByteBuffer) in).replace(ByteBuffer.wrap(bytes));
            else
                in.flip();
            return null;
        }
    }

    private MemcachedMessage getMessage(String line, int spaceIdx) {

        // If there is no space, then it must be a SimpleResponse or a SimpleValueResponse.
        if (spaceIdx == -1) {
            MemcachedMessage msg = getMessage(line);
            if (msg != null) {
                return msg;
            }
            // If the line does not correspond to a response command, then it might be a value.
            else {
                try {
                    return new SimpleValueResponse(Long.parseLong(line));
                } catch (NumberFormatException e) {
                    // It's not a command or a value, thus, it must be an invalid command sent by the client.
                    return new SimpleResponse(IJemcacheConstants.Cmd.ERROR);
                }
            }
        }
        // Otherwise, create a message object from the command string.
        else {
            return getMessage(line.substring(0, spaceIdx));
        }
    }

    // Obtain the command enum and create the corresponding message instance.
    public MemcachedMessage getMessage(String cmdStr) {
        IJemcacheConstants.Cmd cmd;
        try {
            cmd = IJemcacheConstants.Cmd.valueOf(cmdStr);
        } catch (IllegalArgumentException e) {
            // If we get an IllegalArgumentException, then the cmdStr does not correspond to a command.
            return null;
        }

        MemcachedMessage message;
        try {
            message = cmd.getType().newInstance();
        } catch (Exception e) {
            // Some kind of unexpected error.
            e.printStackTrace();
            return new StringResponse(IJemcacheConstants.Cmd.SERVER_ERROR, e.getMessage());
        }

        message.setCmd(cmd);
        return message;
    }

    private DecoderState getState(ISocketSession session) {
        Object decoderState = session.getAttribute(DECODER_STATE);
        if (decoderState == null) {
            decoderState = new DecoderState();
            session.setAttribute(DECODER_STATE, decoderState);
        }
        return (DecoderState) decoderState;
    }


    enum Step {
        Command,
        EOL,
        Data,
    }

    static class DecoderState {
        Step step = Step.Command;
        MemcachedMessage message;
    }
}
