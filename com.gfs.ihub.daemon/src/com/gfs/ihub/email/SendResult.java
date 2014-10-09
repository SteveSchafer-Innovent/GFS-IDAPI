package com.gfs.ihub.email;

class SendResult {
	boolean success;
	String message;

	public SendResult(final boolean success, final String message) {
		this.success = success;
		this.message = message;
	}

	public String getJSON(final String address) {
		final StringBuilder sb = new StringBuilder();
		sb.append("{");
		sb.append("status: \"");
		sb.append(success ? "succeeded" : "failed");
		sb.append("\"");
		if (address != null) {
			sb.append(", address: \"");
			sb.append(address.replaceAll("\"", ""));
			sb.append("\"");
		}
		if (message != null) {
			sb.append(", message: \"");
			sb.append(message.replaceAll("\"", "").replaceAll("\\n", "").replaceAll("\\r", ""));
			sb.append("\"");
		}
		sb.append("}");
		return sb.toString();
	}
}