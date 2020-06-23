package com.orion.mobile.gateway.domain;

import lombok.Data;

import java.io.Serializable;

@Data
public class GatewayResponse implements Serializable {
	private String id;
	private String version;
	private boolean success=true;
	private String code;
	private String message;
	private Object data;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public boolean getSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public Object getData() {
		return data;
	}

	public void setData(Object data) {
		this.data = data;
	}

	@Override
	public String toString() {
		final StringBuffer sb = new StringBuffer("GatewayResponse{");
		sb.append("id=").append(id);
		sb.append(", version='").append(version).append('\'');
		sb.append(", success='").append(success).append('\'');
		sb.append(", code='").append(code).append('\'');
		sb.append(", message='").append(message).append('\'');
		sb.append(", data=").append(data);
		sb.append('}');
		return sb.toString();
	}
}
