/**
 * Copyright (c) 2016-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package com.zsmartsystems.zigbee.dongle.ember.ezsp.structure;

import java.util.HashMap;
import java.util.Map;

/**
 * Class to implement the Ember Enumeration <b>EzspStatus</b>.
 * <p>
 * Note that this code is autogenerated. Manual changes may be overwritten.
 *
 * @author Chris Jackson - Initial contribution of Java code generator
 */
public enum EzspStatus {
    /**
     * Default unknown value
     */
    UNKNOWN(-1),

    /**
     * Success
     */
    EZSP_SUCCESS(0x0000),

    /**
     * Fatal error
     */
    EZSP_SPI_ERR_FATAL(0x0010),

    /**
     * The Response frame of the current transaction indicates the NCP has reset.
     */
    EZSP_SPI_ERR_NCP_RESET(0x0011),

    /**
     * The NCP is reporting that the Command frame of the current transaction is oversized (the
     * length byte is too large).
     */
    EZSP_SPI_ERR_OVERSIZED_EZSP_FRAME(0x0012),

    /**
     * The Response frame of the current transaction indicates the previous transaction was
     * aborted (nSSEL deasserted too soon).
     */
    EZSP_SPI_ERR_ABORTED_TRANSACTION(0x0013),

    /**
     * The Response frame of the current transaction indicates the frame terminator is missing
     * from the Command frame.
     */
    EZSP_SPI_ERR_MISSING_FRAME_TERMINATOR(0x0014),

    /**
     * The NCP has not provided a Response within the time limit defined by WAIT_SECTION_TIMEOUT.
     */
    EZSP_SPI_ERR_WAIT_SECTION_TIMEOUT(0x0015),

    /**
     * The Response frame from the NCP is missing the frame terminator.
     */
    EZSP_SPI_ERR_NO_FRAME_TERMINATOR(0x0016),

    /**
     * The Host attempted to send an oversized Command (the length byte is too large) and the AVR's
     * spi-protocol.c blocked the transmission.
     */
    EZSP_SPI_ERR_EZSP_COMMAND_OVERSIZED(0x0017),

    /**
     * The NCP attempted to send an oversized Response (the length byte is too large) and the AVR's
     * spi-protocol.c blocked the reception.
     */
    EZSP_SPI_ERR_EZSP_RESPONSE_OVERSIZED(0x0018),

    /**
     * The Host has sent the Command and is still waiting for the NCP to send a Response.
     */
    EZSP_SPI_WAITING_FOR_RESPONSE(0x0019),

    /**
     * The NCP has not asserted nHOST_INT within the time limit defined by
     * WAKE_HANDSHAKE_TIMEOUT.
     */
    EZSP_SPI_ERR_HANDSHAKE_TIMEOUT(0x001A),

    /**
     * The NCP has not asserted nHOST_INT after an NCP reset within the time limit defined by
     * STARTUP_TIMEOUT.
     */
    EZSP_SPI_ERR_STARTUP_TIMEOUT(0x001B),

    /**
     * The Host attempted to verify the SPI Protocol activity and version number, and the
     * verification failed.
     */
    EZSP_SPI_ERR_STARTUP_FAIL(0x001C),

    /**
     * The Host has sent a command with a SPI Byte that is unsupported by the current mode the NCP is
     * operating in.
     */
    EZSP_SPI_ERR_UNSUPPORTED_SPI_COMMAND(0x001D),

    /**
     * Operation not yet complete.
     */
    EZSP_ASH_IN_PROGRESS(0x0020),

    /**
     * Fatal error detected by host.
     */
    EZSP_HOST_FATAL_ERROR(0x0021),

    /**
     * Fatal error detected by NCP.
     */
    EZSP_ASH_NCP_FATAL_ERROR(0x0022),

    /**
     * Tried to send DATA frame too long.
     */
    EZSP_DATA_FRAME_TOO_LONG(0x0023),

    /**
     * Tried to send DATA frame too short.
     */
    EZSP_DATA_FRAME_TOO_SHORT(0x0024),

    /**
     * No space for tx'ed DATA frame.
     */
    EZSP_NO_TX_SPACE(0x0025),

    /**
     * No space for rec'd DATA frame.
     */
    EZSP_NO_RX_SPACE(0x0026),

    /**
     * No receive data available.
     */
    EZSP_NO_RX_DATA(0x0027),

    /**
     * Not in Connected state
     */
    EZSP_NOT_CONNECTED(0x0028),

    /**
     * The NCP received a command before the EZSP version had been set.
     */
    EZSP_ERROR_VERSION_NOT_SET(0x0030),

    /**
     * The NCP received a command containing an unsupported frame ID
     */
    EZSP_ERROR_INVALID_FRAME_ID(0x0031),

    /**
     * The direction flag in the frame control field was incorrect.
     */
    EZSP_ERROR_WRONG_DIRECTION(0x0032),

    /**
     * The truncated flag in the frame control field was set, indicating there was not enough memory
     * available to complete the response or that the response would have exceeded the maximum EZSP
     * frame length.
     */
    EZSP_ERROR_TRUNCATED(0x0033),

    /**
     * The overflow flag in the frame control field was set, indicating one or more callbacks
     * occurred since the previous response and there was not enough memory available to report
     * them to the Host.
     */
    EZSP_ERROR_OVERFLOW(0x0340),

    /**
     * Insufficient memory was available.
     */
    EZSP_ERROR_OUT_OF_MEMORY(0x0035),

    /**
     * The value was out of bounds.
     */
    EZSP_ERROR_INVALID_VALUE(0x0036),

    /**
     * The configuration id was not recognized.
     */
    EZSP_ERROR_INVALID_ID(0x0037),

    /**
     * Configuration values can no longer be modified.
     */
    EZSP_ERROR_INVALID_CALL(0x0038),

    /**
     * The NCP failed to respond to a command
     */
    EZSP_ERROR_NO_RESPONSE(0x0039),

    /**
     * The length of the command exceeded the maximum EZSP frame length.
     */
    EZSP_ERROR_COMMAND_TOO_LONG(0x0040),

    /**
     * The UART receive queue was full causing a callback response to be dropped.
     */
    EZSP_ERROR_QUEUE_FULL(0x0041),

    /**
     * The command has been filtered out by NCP.
     */
    EZSP_ERROR_COMMAND_FILTERED(0x0042),

    /**
     * Incompatible ASH version
     */
    EZSP_ASH_ERROR_VERSION(0x0050),

    /**
     * Exceeded max ACK timeouts
     */
    EZSP_ASH_ERROR_TIMEOUTS(0x0051),

    /**
     * Timed out waiting for RSTACK
     */
    EZSP_ASH_ERROR_RESET_FAIL(0x0052),

    /**
     * Unexpected NCP reset
     */
    EZSP_ASH_ERROR_NCP_RESET(0x0053),

    /**
     * Serial port initialization failed
     */
    EZSP_ERROR_SERIAL_INIT(0x0054),

    /**
     * Invalid NCP processor type
     */
    EZSP_ASH_ERROR_NCP_TYPE(0x0055),

    /**
     * Invalid ncp reset method
     */
    EZSP_ASH_ERROR_RESET_METHOD(0x0056),

    /**
     * XON/XOFF not supported by host driver
     */
    EZSP_ASH_ERROR_XON_XOF(0x0057),

    /**
     * 
     */
    EZSP_ASH_STARTED(0x0070),

    /**
     * ASH protocol connected
     */
    EZSP_ASH_CONNECTED(0x0071),

    /**
     * ASH protocol disconnected
     */
    EZSP_ASH_DISCONNECTED(0x0072),

    /**
     * Timer expired waiting for ack
     */
    EZSP_ASH_ACK_TIMEOUT(0x0073),

    /**
     * Frame in progress cancelled
     */
    EZSP_ASH_CANCELLED(0x0074),

    /**
     * Received frame out of sequence
     */
    EZSP_ASH_OUT_OF_SEQUENCE(0x0075),

    /**
     * Received frame with CRC error
     */
    EZSP_ASH_BAD_CRC(0x0076),

    /**
     * Received frame with comm error
     */
    EZSP_ASH_COMM_ERROR(0x0077),

    /**
     * Received frame with bad ackNum
     */
    EZSP_ASH_BAD_ACKNUM(0x0078),

    /**
     * Received frame shorter than minimum
     */
    EZSP_ASH_TOO_SHORT(0x0079),

    /**
     * Received frame longer than maximum
     */
    EZSP_ASH_TOO_LONG(0x007A),

    /**
     * Received frame with illegal control byte
     */
    EZSP_ASH_BAD_CONTROL(0x007B),

    /**
     * Received frame with illegal length for its type
     */
    EZSP_ASH_BAD_LENGTH(0x007C),

    /**
     *  Received ASH Ack
     */
    EZSP_ASH_ACK_RECEIVED(0x007D),

    /**
     * Sent ASH Ack
     */
    EZSP_ASH_ACK_SENT(0x007E),

    /**
     * Received ASH Nak
     */
    EZSP_ASH_NAK_RECEIVED(0x007F),

    /**
     * Sent ASH Nak
     */
    EZSP_ASH_NAK_SENT(0x0080),

    /**
     * Received ASH RST
     */
    EZSP_ASH_RST_RECEIVED(0x0081),

    /**
     * Sent ASH RST
     */
    EZSP_ASH_RST_SENT(0x0082),

    /**
     * ASH Status
     */
    EZSP_ASH_STATUS(0x0083),

    /**
     * ASH TX
     */
    EZSP_ASH_TX(0x0084),

    /**
     * ASH RX
     */
    EZSP_ASH_RX(0x0085),

    /**
     * No reset or error
     */
    EZSP_NO_ERROR(0x00FF);

    /**
     * A mapping between the integer code and its corresponding type to
     * facilitate lookup by code.
     */
    private static Map<Integer, EzspStatus> codeMapping;

    private int key;

    private EzspStatus(int key) {
        this.key = key;
    }

    private static void initMapping() {
        codeMapping = new HashMap<Integer, EzspStatus>();
        for (EzspStatus s : values()) {
            codeMapping.put(s.key, s);
        }
    }

    /**
     * Lookup function based on the EmberStatus type code. Returns null if the
     * code does not exist.
     *
     * @param code
     *            the code to lookup
     * @return enumeration value of the alarm type.
     */
    public static EzspStatus getEzspStatus(int code) {
        if (codeMapping == null) {
            initMapping();
        }

        if (codeMapping.get(code) == null) {
            return UNKNOWN;
        }

        return codeMapping.get(code);
    }

    /**
     * Returns the EZSP protocol defined value for this enum
     *
     * @return the EZSP protocol key
     */
    public int getKey() {
        return key;
    }
}
