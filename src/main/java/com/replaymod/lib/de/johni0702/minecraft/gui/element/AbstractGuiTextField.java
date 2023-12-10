package com.replaymod.lib.de.johni0702.minecraft.gui.element;

import java.util.Objects;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.replaymod.lib.de.johni0702.minecraft.gui.GuiRenderer;
import com.replaymod.lib.de.johni0702.minecraft.gui.RenderInfo;
import com.replaymod.lib.de.johni0702.minecraft.gui.container.GuiContainer;
import com.replaymod.lib.de.johni0702.minecraft.gui.function.Clickable;
import com.replaymod.lib.de.johni0702.minecraft.gui.function.Focusable;
import com.replaymod.lib.de.johni0702.minecraft.gui.function.Tickable;
import com.replaymod.lib.de.johni0702.minecraft.gui.function.Typeable;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.Consumer;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.Utils;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.Color;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.Dimension;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.Point;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.ReadableColor;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.ReadableDimension;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.ReadablePoint;
import com.replaymod.lib.de.johni0702.minecraft.gui.versions.MCVer;
import com.replaymod.lib.de.johni0702.minecraft.gui.versions.MCVer.Keyboard;

import net.minecraft.SharedConstants;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;

public abstract class AbstractGuiTextField<T extends AbstractGuiTextField<T>> extends AbstractGuiElement<T>
		implements Clickable, Tickable, Typeable, IGuiTextField<T> {
	private static final ReadableColor BORDER_COLOR = new Color(160, 160, 160);
	private static final ReadableColor CURSOR_COLOR = new Color(240, 240, 240);
	private static final int BORDER = 4;
	private boolean focused;
	private Focusable next;
	private Focusable previous;
	private int maxLength = 32;
	private String text = "";
	private int cursorPos;
	private int selectionPos;
	private String hint;
	private int currentOffset;
	private int blinkCursorTick;
	private ReadableColor textColorEnabled = new Color(224, 224, 224);
	private ReadableColor textColorDisabled = new Color(112, 112, 112);
	private ReadableDimension size = new Dimension(0, 0);
	private Consumer<String> textChanged;
	private Consumer<Boolean> focusChanged;
	private Runnable onEnter;

	public AbstractGuiTextField() {
	}

	public AbstractGuiTextField(GuiContainer container) {
		super(container);
	}

	public T setText(String text) {
		if (text.length() > this.maxLength) {
			text = text.substring(0, this.maxLength);
		}

		this.text = text;
		this.selectionPos = this.cursorPos = text.length();
		return this.getThis();
	}

	public T setI18nText(String text, Object... args) {
		return this.setText(I18n.get(text, args));
	}

	public T setMaxLength(int maxLength) {
		Preconditions.checkArgument(maxLength >= 0, "maxLength must not be negative");
		this.maxLength = maxLength;
		if (this.text.length() > maxLength) {
			this.setText(this.text);
		}

		return this.getThis();
	}

	public String deleteText(int from, int to) {
		Preconditions.checkArgument(from <= to, "from must not be greater than to");
		Preconditions.checkArgument(from >= 0, "from must be greater than zero");
		Preconditions.checkArgument(to < this.text.length(), "to must be less than test.length()");
		String deleted = this.text.substring(from, to + 1);
		String var10001 = this.text.substring(0, from);
		this.text = var10001 + this.text.substring(to + 1);
		return deleted;
	}

	public int getSelectionFrom() {
		return this.cursorPos > this.selectionPos ? this.selectionPos : this.cursorPos;
	}

	public int getSelectionTo() {
		return this.cursorPos > this.selectionPos ? this.cursorPos : this.selectionPos;
	}

	public String getSelectedText() {
		return this.text.substring(this.getSelectionFrom(), this.getSelectionTo());
	}

	public String deleteSelectedText() {
		if (this.cursorPos == this.selectionPos) {
			return "";
		} else {
			int from = this.getSelectionFrom();
			String deleted = this.deleteText(from, this.getSelectionTo() - 1);
			this.cursorPos = this.selectionPos = from;
			return deleted;
		}
	}

	private void updateCurrentOffset() {
		this.currentOffset = Math.min(this.currentOffset, this.cursorPos);
		String line = this.text.substring(this.currentOffset, this.cursorPos);
		Font fontRenderer = MCVer.getFontRenderer();
		int currentWidth = fontRenderer.width(line);
		if (currentWidth > this.size.getWidth() - 8) {
			this.currentOffset = this.cursorPos
					- fontRenderer.plainSubstrByWidth(line, this.size.getWidth() - 8, true).length();
		}

	}

	public T writeText(String append) {
		char[] var2 = append.toCharArray();
		int var3 = var2.length;

		for (int var4 = 0; var4 < var3; ++var4) {
			char c = var2[var4];
			this.writeChar(c);
		}

		return this.getThis();
	}

	public T writeChar(char c) {
		if (!SharedConstants.isAllowedChatCharacter(c)) {
			return this.getThis();
		} else {
			this.deleteSelectedText();
			if (this.text.length() >= this.maxLength) {
				return this.getThis();
			} else {
				this.text = this.text.substring(0, this.cursorPos) + c + this.text.substring(this.cursorPos);
				this.selectionPos = ++this.cursorPos;
				return this.getThis();
			}
		}
	}

	public T deleteNextChar() {
		if (this.cursorPos < this.text.length()) {
			String var10001 = this.text.substring(0, this.cursorPos);
			this.text = var10001 + this.text.substring(this.cursorPos + 1);
		}

		this.selectionPos = this.cursorPos;
		return this.getThis();
	}

	protected int getNextWordLength() {
		int length = 0;
		boolean inWord = true;

		for (int i = this.cursorPos; i < this.text.length(); ++i) {
			if (inWord) {
				if (this.text.charAt(i) == ' ') {
					inWord = false;
				}
			} else if (this.text.charAt(i) != ' ') {
				return length;
			}

			++length;
		}

		return length;
	}

	public String deleteNextWord() {
		int worldLength = this.getNextWordLength();
		return worldLength > 0 ? this.deleteText(this.cursorPos, this.cursorPos + worldLength - 1) : "";
	}

	public T deletePreviousChar() {
		if (this.cursorPos > 0) {
			String var10001 = this.text.substring(0, this.cursorPos - 1);
			this.text = var10001 + this.text.substring(this.cursorPos);
			this.selectionPos = --this.cursorPos;
		}

		return this.getThis();
	}

	protected int getPreviousWordLength() {
		int length = 0;
		boolean inWord = false;

		for (int i = this.cursorPos - 1; i >= 0; --i) {
			if (inWord) {
				if (this.text.charAt(i) == ' ') {
					return length;
				}
			} else if (this.text.charAt(i) != ' ') {
				inWord = true;
			}

			++length;
		}

		return length;
	}

	public String deletePreviousWord() {
		int worldLength = this.getPreviousWordLength();
		String deleted = "";
		if (worldLength > 0) {
			deleted = this.deleteText(this.cursorPos - worldLength, this.cursorPos - 1);
			this.selectionPos = this.cursorPos -= worldLength;
		}

		return deleted;
	}

	public T setCursorPosition(int pos) {
		Preconditions.checkArgument(pos >= 0 && pos <= this.text.length());
		this.selectionPos = this.cursorPos = pos;
		return this.getThis();
	}

	protected ReadableDimension calcMinSize() {
		return new Dimension(0, 0);
	}

	public boolean mouseClick(ReadablePoint position, int button) {
		if (this.getContainer() != null) {
			this.getContainer().convertFor(this, (Point) (position = new Point((ReadablePoint) position)));
		}

		boolean hovering = this.isMouseHovering((ReadablePoint) position);
		if (hovering && this.isFocused() && button == 0) {
			this.updateCurrentOffset();
			int mouseX = ((ReadablePoint) position).getX() - 4;
			Font fontRenderer = MCVer.getFontRenderer();
			String text = this.text.substring(this.currentOffset);
			int textX = fontRenderer.plainSubstrByWidth(text, mouseX).length() + this.currentOffset;
			this.setCursorPosition(textX);
			return true;
		} else {
			this.setFocused(hovering);
			return false;
		}
	}

	protected boolean isMouseHovering(ReadablePoint pos) {
		return pos.getX() > 0 && pos.getY() > 0 && pos.getX() < this.size.getWidth()
				&& pos.getY() < this.size.getHeight();
	}

	public T setFocused(boolean isFocused) {
		if (isFocused && !this.focused) {
			this.blinkCursorTick = 0;
		}

		if (this.focused != isFocused) {
			this.focused = isFocused;
			this.onFocusChanged(this.focused);
		}

		return this.getThis();
	}

	public T setNext(Focusable next) {
		this.next = next;
		return this.getThis();
	}

	public T setPrevious(Focusable previous) {
		this.previous = previous;
		return this.getThis();
	}

	public void draw(GuiRenderer renderer, ReadableDimension size, RenderInfo renderInfo) {
		this.size = size;
		this.updateCurrentOffset();
		super.draw(renderer, size, renderInfo);
		int width = size.getWidth();
		int height = size.getHeight();
		Font fontRenderer = MCVer.getFontRenderer();
		int var10000 = height / 2;
		Objects.requireNonNull(fontRenderer);
		int posY = var10000 - 9 / 2;
		renderer.drawRect(0, 0, width, height, this.isFocused() ? ReadableColor.WHITE : BORDER_COLOR);
		renderer.drawRect(1, 1, width - 2, height - 2, ReadableColor.BLACK);
		String renderText;
		if (this.text.isEmpty() && !this.isFocused() && !Strings.isNullOrEmpty(this.hint)) {
			renderText = fontRenderer.plainSubstrByWidth(this.hint, width - 8);
			renderer.drawString(4, posY, this.textColorDisabled, renderText);
		} else {
			renderText = this.text.substring(this.currentOffset);
			renderText = fontRenderer.plainSubstrByWidth(renderText, width - 8);
			ReadableColor color = this.isEnabled() ? this.textColorEnabled : this.textColorDisabled;
			int var10002 = height / 2;
			Objects.requireNonNull(fontRenderer);
			int lineEnd = renderer.drawString(4, var10002 - 9 / 2, color, renderText);
			int from = this.getSelectionFrom();
			int to = this.getSelectionTo();
			String leftStr = renderText.substring(0, Utils.clamp(from - this.currentOffset, 0, renderText.length()));
			String rightStr = renderText.substring(Utils.clamp(to - this.currentOffset, 0, renderText.length()));
			int left = 4 + fontRenderer.width(leftStr);
			int right = lineEnd - fontRenderer.width(rightStr) - 1;
			renderer.invertColors(right, height - 2, left, 2);
			if (this.blinkCursorTick / 6 % 2 == 0 && this.focused) {
				String beforeCursor = renderText.substring(0, this.cursorPos - this.currentOffset);
				int posX = 4 + fontRenderer.width(beforeCursor);
				if (this.cursorPos == this.text.length()) {
					renderer.drawString(posX, posY, CURSOR_COLOR, "_", true);
				} else {
					var10002 = posY - 1;
					Objects.requireNonNull(fontRenderer);
					renderer.drawRect(posX, var10002, 1, 1 + 9, CURSOR_COLOR);
				}
			}
		}

	}

	@Override
	public boolean typeKey(ReadablePoint mousePosition, int keyCode, char keyChar, boolean ctrlDown,
			boolean shiftDown) {
		if (!this.focused) {
			return false;
		}

		if (keyCode == Keyboard.KEY_TAB) {
			Focusable other = shiftDown ? previous : next;
			if (other != null) {
				setFocused(false);
				other.setFocused(true);
				// If the other field is a text field, by default select all its text (saves a
				// Ctrl+A)
				if (other instanceof AbstractGuiTextField) {
					AbstractGuiTextField<?> field = (AbstractGuiTextField<?>) other;
					field.cursorPos = 0;
					field.selectionPos = field.text.length();
				}
			}
			return true;
		}

		if (keyCode == Keyboard.KEY_RETURN) {
			onEnter();
			return true;
		}

		String textBefore = text;
		try {
			if (Screen.hasControlDown()) {
				switch (keyCode) {
				case Keyboard.KEY_A: // Select all
					cursorPos = 0;
					selectionPos = text.length();
					return true;
				case Keyboard.KEY_C: // Copy
					MCVer.setClipboardString(getSelectedText());
					return true;
				case Keyboard.KEY_V: // Paste
					if (isEnabled()) {
						writeText(MCVer.getClipboardString());
					}
					return true;
				case Keyboard.KEY_X: // Cut
					if (isEnabled()) {
						MCVer.setClipboardString(deleteSelectedText());
					}
					return true;
				}
			}

			boolean words = Screen.hasControlDown();
			boolean select = Screen.hasShiftDown();
			switch (keyCode) {
			case Keyboard.KEY_HOME:
				cursorPos = 0;
				break;
			case Keyboard.KEY_END:
				cursorPos = text.length();
				break;
			case Keyboard.KEY_LEFT:
				if (cursorPos != 0) {
					if (words) {
						cursorPos -= getPreviousWordLength();
					} else {
						cursorPos--;
					}
				}
				break;
			case Keyboard.KEY_RIGHT:
				if (cursorPos != text.length()) {
					if (words) {
						cursorPos += getNextWordLength();
					} else {
						cursorPos++;
					}
				}
				break;
			case Keyboard.KEY_BACK:
				if (isEnabled()) {
					if (getSelectedText().length() > 0) {
						deleteSelectedText();
					} else if (words) {
						deletePreviousWord();
					} else {
						deletePreviousChar();
					}
				}
				return true;
			case Keyboard.KEY_DELETE:
				if (isEnabled()) {
					if (getSelectedText().length() > 0) {
						deleteSelectedText();
					} else if (words) {
						deleteNextWord();
					} else {
						deleteNextChar();
					}
				}
				return true;
			default:
				if (isEnabled()) {
					if (keyChar == '\r') {
						keyChar = '\n';
					}
					writeChar(keyChar);
				}
				return true;
			}

			if (!select) {
				selectionPos = cursorPos;
			}
			return true;
		} finally {
			if (!textBefore.equals(text)) {
				onTextChanged(textBefore);
			}
		}
	}

	public void tick() {
		++this.blinkCursorTick;
	}

	protected void onEnter() {
		if (this.onEnter != null) {
			this.onEnter.run();
		}

	}

	protected void onTextChanged(String from) {
		if (this.textChanged != null) {
			this.textChanged.consume(from);
		}

	}

	protected void onFocusChanged(boolean focused) {
		if (this.focusChanged != null) {
			this.focusChanged.consume(focused);
		}

	}

	public T onEnter(Runnable onEnter) {
		this.onEnter = onEnter;
		return this.getThis();
	}

	public T onTextChanged(Consumer<String> textChanged) {
		this.textChanged = textChanged;
		return this.getThis();
	}

	public T onFocusChange(Consumer<Boolean> focusChanged) {
		this.focusChanged = focusChanged;
		return this.getThis();
	}

	public T setHint(String hint) {
		this.hint = hint;
		return this.getThis();
	}

	public T setI18nHint(String hint, Object... args) {
		return this.setHint(I18n.get(hint, new Object[0]));
	}

	public ReadableColor getTextColor() {
		return this.textColorEnabled;
	}

	public T setTextColor(ReadableColor textColor) {
		this.textColorEnabled = textColor;
		return this.getThis();
	}

	public ReadableColor getTextColorDisabled() {
		return this.textColorDisabled;
	}

	public T setTextColorDisabled(ReadableColor textColorDisabled) {
		this.textColorDisabled = textColorDisabled;
		return this.getThis();
	}

	public boolean isFocused() {
		return this.focused;
	}

	public Focusable getNext() {
		return this.next;
	}

	public Focusable getPrevious() {
		return this.previous;
	}

	public int getMaxLength() {
		return this.maxLength;
	}

	public String getText() {
		return this.text;
	}

	public String getHint() {
		return this.hint;
	}
}
