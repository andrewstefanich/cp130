package com.github.astefanich.exchange;

/**
 * {@link String}/Integer constants for {@code StockExchange} commands being sent over the network,
 * which composes the exchange protcol. The protocol supports events and commands. Events are
 * one-way messages sent from the exchange to the broker(s). <br>
 * <br>
 * Constants are String literals unless it is specified as an int. <br>
 * The protocol supports the following events: <br>
 * Event: [OPEN_EVENT] <br>
 * - <br>
 * Event: [CLOSED_EVENT] <br>
 * - <br>
 * Event: [PRICE_CHANGE_EVENT][ELEMENT_DELIMITER]<i>symbol</i>[ELEMENT_DELIMITER]<i>price</i> <br>
 * <br>
 * <br>
 * Commands conform to a request/response model where requests are sent from a broker and the result
 * is a response sent to the requesting broker from the exchange. <br>
 * <br>
 * The protocol supports the following commands: <br>
 * Request: [GET_STATE_CMD] <br>
 * Response: [OPEN_STATE]|[CLOSED_STATE] <br>
 * - <br>
 * Request: [GET_TICKERS_CMD] <br>
 * Response: <i>symbol</i>[ELEMENT_DELIMITER]<i>symbol</i>... <br>
 * - <br>
 * Request: [GET_QUOTE_CMD][ELEMENT_DELIMITER]<i>symbol</i> <br>
 * Response: <i>price</i> <br>
 * - <br>
 * Request: [EXECUTE_TRADE_CMD][ELEMENT_DELIMITER][BUY_ORDER]|[SELL_ORDER] <br>
 * [ELEMENT_DELIMITER]<i>account_ID</i>[ELEMENT_DELIMITER]<i>symbol</i>[ELEMENT_DELIMITER]<i>shares</i>
 * <br>
 * Response: <i>execution_price</i>
 * 
 * @author AndrewStefanich
 */
public enum ProtocolConstants {
	/** [BUY_ORDER] */
	BUY_ORDER("BUY_ORDER"),

	/** [CLOSED_EVENT] */
	CLOSED_EVENT("CLOSED_EVENT"),

	/** [CLOSED_STATE] */
	CLOSED_STATE("CLOSED_STATE"),

	/** [0] (int) */
	CMD_ELEMENT(0),

	/** [:] */
	ELEMENT_DELIMITER(":"),

	/** [UTF-8] */
	ENCODING("UTF-8"),

	/** [EXECUTE_TRADE_CMD] */
	EXECUTE_TRADE_CMD("EXECUTE_TRADE_CMD"),

	/** [2] (int) */
	EXECUTE_TRADE_CMD_ACCOUNT_ELEMENT(2),

	/** [4] (int) */
	EXECUTE_TRADE_CMD_SHARES_ELEMENT(4),

	/** [3] (parse as Integer) */
	EXECUTE_TRADE_CMD_TICKER_ELEMENT(3),

	/** [1] (int) */
	EXECUTE_TRADE_CMD_TYPE_ELEMENT(1),

	/** [0] (int) */
	EVENT_ELEMENT(0),

	/** [GET_QUOTE_CMD] */
	GET_QUOTE_CMD("GET_QUOTE_CMD"),

	/** [GET_STATE_CMD] */
	GET_STATE_CMD("GET_STATE_CMD"),

	/** [GET_TICKERS_CMD] */
	GET_TICKERS_CMD("GET_TICKERS_CMD"),

	/** [-1] (int) */
	INVALID_STOCK(-1),

	/** [OPEN_EVENT] */
	OPEN_EVENT("OPEN_EVENT"),

	/** [OPEN_STATE] */
	OPEN_STATE("OPEN_STATE"),

	/** [PRICE_CHANGE_EVENT] */
	PRICE_CHANGE_EVENT("PRICE_CHANGE_EVENT"),

	/** [2] (int) */
	PRICE_CHANGE_EVENT_PRICE_ELEMENT(2),

	/** [1] (int) */
	PRICE_CHANGE_EVENT_TICKER_ELEMENT(1),

	/** [1] (int) */
	QUOTE_CMD_TICKER_ELEMENT(1),

	/** [SELL_ORDER] */
	SELL_ORDER("SELL_ORDER");

	private Object value;

	private ProtocolConstants(Object value) {
		this.value = value;
	}

	/**
	 * {@link String} representation of this enum constant
	 * 
	 * @return the String
	 */
	@Override
	public String toString() {
		return value.toString();
	}

	/**
	 * Gets the {@link String}/{@link Integer} value of this enum constant.
	 * 
	 * @return the FullName
	 */
	public Object getValue() {
		return value;
	}
}
