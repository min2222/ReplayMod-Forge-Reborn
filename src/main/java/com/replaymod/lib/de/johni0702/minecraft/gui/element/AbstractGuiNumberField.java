package com.replaymod.lib.de.johni0702.minecraft.gui.element;

import java.util.Locale;
import java.util.regex.Pattern;

import com.google.common.base.Preconditions;
import com.replaymod.lib.de.johni0702.minecraft.gui.container.GuiContainer;

public abstract class AbstractGuiNumberField<T extends AbstractGuiNumberField<T>> extends AbstractGuiTextField<T>
		implements IGuiNumberField<T> {
	private int precision;
	private volatile Pattern precisionPattern;
	private Double minValue;
	private Double maxValue;
	private boolean validateOnFocusChange = false;

	public AbstractGuiNumberField() {
		this.setValue(0);
	}

	public AbstractGuiNumberField(GuiContainer container) {
		super(container);
		this.setValue(0);
	}

	public T setText(String text) {
		if (!this.isTextValid(text, !this.validateOnFocusChange)) {
			throw new IllegalArgumentException(text + " is not a valid number!");
		} else {
			return super.setText(text);
		}
	}

	public T setValidateOnFocusChange(boolean validateOnFocusChange) {
		this.validateOnFocusChange = validateOnFocusChange;
		return this.getThis();
	}

	private boolean isSemiZero(String text) {
		return text.isEmpty() || "-".equals(text);
	}

	private boolean isTextValid(String text, boolean validateRange) {
		if (this.validateOnFocusChange && this.isSemiZero(text)) {
			return !validateRange || this.valueInRange(0.0D);
		} else {
			try {
				if (this.precision == 0) {
					int val = Integer.parseInt(text);
					return !validateRange || this.valueInRange((double) val);
				} else {
					double val = Double.parseDouble(text);
					return !validateRange || this.valueInRange(val) && this.precisionPattern.matcher(text).matches();
				}
			} catch (NumberFormatException var5) {
				return false;
			}
		}
	}

	private boolean valueInRange(double value) {
		return (this.minValue == null || value >= this.minValue) && (this.maxValue == null || value <= this.maxValue);
	}

	protected void onTextChanged(String from) {
		if (this.isTextValid(this.getText(), !this.validateOnFocusChange)) {
			super.onTextChanged(from);
		} else {
			this.setText(from);
		}

	}

	public byte getByte() {
		return this.validateOnFocusChange && this.isSemiZero(this.getText()) ? 0 : Byte.parseByte(this.getText());
	}

	public short getShort() {
		return this.validateOnFocusChange && this.isSemiZero(this.getText()) ? 0 : Short.parseShort(this.getText());
	}

	public int getInteger() {
		return this.validateOnFocusChange && this.isSemiZero(this.getText()) ? 0 : Integer.parseInt(this.getText());
	}

	public long getLong() {
		return this.validateOnFocusChange && this.isSemiZero(this.getText()) ? 0L : Long.parseLong(this.getText());
	}

	public float getFloat() {
		return this.validateOnFocusChange && this.isSemiZero(this.getText()) ? 0.0F : Float.parseFloat(this.getText());
	}

	public double getDouble() {
		return this.validateOnFocusChange && this.isSemiZero(this.getText()) ? 0.0D
				: Double.parseDouble(this.getText());
	}

	public T setValue(int value) {
		this.setText(Integer.toString(value));
		return this.getThis();
	}

	public T setValue(double value) {
		this.setText(String.format(Locale.ROOT, "%." + this.precision + "f", value));
		return this.getThis();
	}

	public T setPrecision(int precision) {
		Preconditions.checkArgument(precision >= 0, "precision must not be negative");
		this.precisionPattern = Pattern.compile(String.format("-?[0-9]*+((\\.[0-9]{0,%d})?)||(\\.)?", precision));
		this.precision = precision;
		return this.getThis();
	}

	public T setMinValue(Double minValue) {
		this.minValue = minValue;
		return this.getThis();
	}

	public T setMaxValue(Double maxValue) {
		this.maxValue = maxValue;
		return this.getThis();
	}

	public T setMinValue(int minValue) {
		return this.setMinValue((double) minValue);
	}

	public T setMaxValue(int maxValue) {
		return this.setMaxValue((double) maxValue);
	}

	private double clampToBounds() {
		double d = this.getDouble();
		if (this.getMinValue() != null && d < this.getMinValue()) {
			return this.getMinValue();
		} else {
			return this.getMaxValue() != null && d > this.getMaxValue() ? this.getMaxValue() : d;
		}
	}

	protected void onFocusChanged(boolean focused) {
		this.setValue(this.clampToBounds());
		super.onFocusChanged(focused);
	}

	public Double getMinValue() {
		return this.minValue;
	}

	public Double getMaxValue() {
		return this.maxValue;
	}
}
