package com.saucelabs.rdc.model;

import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Objects;

public abstract class Id<T> {

	private final T value;

	protected Id(T value) {
		if (value == null) {
			throw new NullPointerException();
		}
		this.value = value;
	}

	@JsonValue
	public T value() {
		return value;
	}

	@Override public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		Id<?> id = (Id<?>) o;
		return Objects.equals(value, id.value);
	}

	@Override public int hashCode() {
		return Objects.hash(value);
	}

	@Override
	public String toString() {
		return String.valueOf(value);
	}

}
