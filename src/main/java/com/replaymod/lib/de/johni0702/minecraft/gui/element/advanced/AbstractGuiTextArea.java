package com.replaymod.lib.de.johni0702.minecraft.gui.element.advanced;

import static com.replaymod.lib.de.johni0702.minecraft.gui.utils.Utils.clamp;

import java.util.Arrays;
import java.util.Objects;

import com.replaymod.lib.de.johni0702.minecraft.gui.GuiRenderer;
import com.replaymod.lib.de.johni0702.minecraft.gui.RenderInfo;
import com.replaymod.lib.de.johni0702.minecraft.gui.container.GuiContainer;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.AbstractGuiElement;
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

import net.minecraft.SharedConstants;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;

public abstract class AbstractGuiTextArea<T extends AbstractGuiTextArea<T>> extends AbstractGuiElement<T>
		implements Clickable, Typeable, Tickable, IGuiTextArea<T> {
	private static final ReadableColor BACKGROUND_COLOR = new Color(160, 160, 160);
	private static final ReadableColor CURSOR_COLOR = new Color(240, 240, 240);
	private static final int BORDER = 4;
	private static final int LINE_SPACING = 2;
	private boolean focused;
	private Focusable next;
	private Focusable previous;
	private Consumer<Boolean> focusChanged;
	private int maxTextWidth = -1;
	private int maxTextHeight = -1;
	private int maxCharCount = -1;
	private String[] text = new String[] { "" };
	private String[] hint;
	private int cursorX;
	private int cursorY;
	private int selectionX;
	private int selectionY;
	private int currentXOffset;
	private int currentYOffset;
	private int blinkCursorTick;
	public ReadableColor textColorEnabled = new Color(224, 224, 224);
	public ReadableColor textColorDisabled = new Color(112, 112, 112);
	private ReadableDimension size = new Dimension(0, 0);

	public AbstractGuiTextArea() {
	}

	public AbstractGuiTextArea(GuiContainer container) {
		super(container);
	}

	public T setText(String[] lines) {
		if (lines.length > this.maxTextHeight) {
			lines = (String[]) Arrays.copyOf(lines, this.maxTextHeight);
		}

		this.text = lines;

		for (int i = 0; i < lines.length; ++i) {
			if (lines[i].length() > this.maxTextWidth) {
				lines[i] = lines[i].substring(0, this.maxTextWidth);
			}
		}

		return this.getThis();
	}

	public String[] getText() {
		return this.text;
	}

	public String getText(int fromX, int fromY, int toX, int toY) {
		StringBuilder sb = new StringBuilder();
		if (fromY == toY) {
			sb.append(this.text[fromY].substring(fromX, toX));
		} else {
			sb.append(this.text[fromY].substring(fromX)).append('\n');

			for (int y = fromY + 1; y < toY; ++y) {
				sb.append(this.text[y]).append('\n');
			}

			sb.append(this.text[toY].substring(0, toX));
		}

		return sb.toString();
	}

	private void deleteText(int fromX, int fromY, int toX, int toY) {
		String[] newText = new String[this.text.length - (toY - fromY)];
		if (fromY > 0) {
			System.arraycopy(this.text, 0, newText, 0, fromY);
		}

		newText[fromY] = this.text[fromY].substring(0, fromX) + this.text[toY].substring(toX);
		if (toY + 1 < this.text.length) {
			System.arraycopy(this.text, toY + 1, newText, fromY + 1, this.text.length - toY - 1);
		}

		this.text = newText;
	}

	public int getSelectionFromX() {
		if (this.cursorY == this.selectionY) {
			return this.cursorX > this.selectionX ? this.selectionX : this.cursorX;
		} else {
			return this.cursorY > this.selectionY ? this.selectionX : this.cursorX;
		}
	}

	public int getSelectionToX() {
		if (this.cursorY == this.selectionY) {
			return this.cursorX > this.selectionX ? this.cursorX : this.selectionX;
		} else {
			return this.cursorY > this.selectionY ? this.cursorX : this.selectionX;
		}
	}

	public int getSelectionFromY() {
		return this.cursorY > this.selectionY ? this.selectionY : this.cursorY;
	}

	public int getSelectionToY() {
		return this.cursorY > this.selectionY ? this.cursorY : this.selectionY;
	}

	public String getSelectedText() {
		if (this.cursorX == this.selectionX && this.cursorY == this.selectionY) {
			return "";
		} else {
			int fromX = this.getSelectionFromX();
			int fromY = this.getSelectionFromY();
			int toX = this.getSelectionToX();
			int toY = this.getSelectionToY();
			return this.getText(fromX, fromY, toX, toY);
		}
	}

	public void deleteSelectedText() {
		if (this.cursorX != this.selectionX || this.cursorY != this.selectionY) {
			int fromX = this.getSelectionFromX();
			int fromY = this.getSelectionFromY();
			int toX = this.getSelectionToX();
			int toY = this.getSelectionToY();
			this.deleteText(fromX, fromY, toX, toY);
			this.cursorX = this.selectionX = fromX;
			this.cursorY = this.selectionY = fromY;
		}
	}

	private void updateCurrentOffset() {
		this.currentXOffset = Math.min(this.currentXOffset, this.cursorX);
		String line = this.text[this.cursorY].substring(this.currentXOffset, this.cursorX);
		Font fontRenderer = MCVer.getFontRenderer();
		int currentWidth = fontRenderer.width(line);
		if (currentWidth > this.size.getWidth() - 8) {
			this.currentXOffset = this.cursorX
					- fontRenderer.plainSubstrByWidth(line, this.size.getWidth() - 8, true).length();
		}

		this.currentYOffset = Math.min(this.currentYOffset, this.cursorY);
		Objects.requireNonNull(MCVer.getFontRenderer());
		int lineHeight = 9 + 2;
		int contentHeight = this.size.getHeight() - 8;
		int maxLines = contentHeight / lineHeight;
		if (this.cursorY - this.currentYOffset >= maxLines) {
			this.currentYOffset = this.cursorY - maxLines + 1;
		}

	}

	public String cutSelectedText() {
		String selection = this.getSelectedText();
		this.deleteSelectedText();
		return selection;
	}

	public void writeText(String append) {
		char[] var2 = append.toCharArray();
		int var3 = var2.length;

		for (int var4 = 0; var4 < var3; ++var4) {
			char c = var2[var4];
			this.writeChar(c);
		}

	}

	public void writeChar(char c) {
		if (SharedConstants.isAllowedChatCharacter(c)) {
			int totalCharCount = 0;
			String[] newText = this.text;
			int var4 = newText.length;

			for (int var5 = 0; var5 < var4; ++var5) {
				String line = newText[var5];
				totalCharCount += line.length();
			}

			if (this.maxCharCount <= 0 || totalCharCount - this.getSelectedText().length() < this.maxCharCount) {
				this.deleteSelectedText();
				if (c == '\n') {
					if (this.text.length >= this.maxTextHeight) {
						return;
					}

					newText = new String[this.text.length + 1];
					if (this.cursorY > 0) {
						System.arraycopy(this.text, 0, newText, 0, this.cursorY);
					}

					newText[this.cursorY] = this.text[this.cursorY].substring(0, this.cursorX);
					newText[this.cursorY + 1] = this.text[this.cursorY].substring(this.cursorX);
					if (this.cursorY + 1 < this.text.length) {
						System.arraycopy(this.text, this.cursorY + 1, newText, this.cursorY + 2,
								this.text.length - this.cursorY - 1);
					}

					this.text = newText;
					this.selectionX = this.cursorX = 0;
					this.selectionY = ++this.cursorY;
				} else {
					String line = this.text[this.cursorY];
					if (line.length() >= this.maxTextWidth) {
						return;
					}

					line = line.substring(0, this.cursorX) + c + line.substring(this.cursorX);
					this.text[this.cursorY] = line;
					this.selectionX = ++this.cursorX;
				}

			}
		}
	}

	private void deleteNextChar() {
		String line = this.text[this.cursorY];
		if (this.cursorX < line.length()) {
			String var10000 = line.substring(0, this.cursorX);
			line = var10000 + line.substring(this.cursorX + 1);
			this.text[this.cursorY] = line;
		} else if (this.cursorY + 1 < this.text.length) {
			this.deleteText(this.cursorX, this.cursorY, 0, this.cursorY + 1);
		}

	}

	private int getNextWordLength() {
		int length = 0;
		String line = this.text[this.cursorY];
		boolean inWord = true;

		for (int i = this.cursorX; i < line.length(); ++i) {
			if (inWord) {
				if (line.charAt(i) == ' ') {
					inWord = false;
				}
			} else if (line.charAt(i) != ' ') {
				return length;
			}

			++length;
		}

		return length;
	}

	private void deleteNextWord() {
		int worldLength = this.getNextWordLength();
		if (worldLength == 0) {
			this.deleteNextChar();
		} else {
			this.deleteText(this.cursorX, this.cursorY, this.cursorX + worldLength, this.cursorY);
		}

	}

	private void deletePreviousChar() {
		if (this.cursorX > 0) {
			String line = this.text[this.cursorY];
			String var10000 = line.substring(0, this.cursorX - 1);
			line = var10000 + line.substring(this.cursorX);
			this.selectionX = --this.cursorX;
			this.text[this.cursorY] = line;
		} else if (this.cursorY > 0) {
			int fromX = this.text[this.cursorY - 1].length();
			this.deleteText(fromX, this.cursorY - 1, this.cursorX, this.cursorY);
			this.selectionX = this.cursorX = fromX;
			this.selectionY = --this.cursorY;
		}

	}

	private int getPreviousWordLength() {
		int length = 0;
		String line = this.text[this.cursorY];
		boolean inWord = false;

		for (int i = this.cursorX - 1; i >= 0; --i) {
			if (inWord) {
				if (line.charAt(i) == ' ') {
					return length;
				}
			} else if (line.charAt(i) != ' ') {
				inWord = true;
			}

			++length;
		}

		return length;
	}

	private void deletePreviousWord() {
		int worldLength = this.getPreviousWordLength();
		if (worldLength == 0) {
			this.deletePreviousChar();
		} else {
			this.deleteText(this.cursorX, this.cursorY, this.cursorX - worldLength, this.cursorY);
			this.selectionX = this.cursorX -= worldLength;
		}

	}

	public T setCursorPosition(int x, int y) {
		this.selectionY = this.cursorY = Utils.clamp(y, 0, this.text.length - 1);
		this.selectionX = this.cursorX = Utils.clamp(x, 0, this.text[this.cursorY].length());
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
			int mouseY = ((ReadablePoint) position).getY() - 4;
			Font fontRenderer = MCVer.getFontRenderer();
			Objects.requireNonNull(fontRenderer);
			int textY = Utils.clamp(mouseY / (9 + 2) + this.currentYOffset, 0, this.text.length - 1);
			if (this.cursorY != textY) {
				this.currentXOffset = 0;
			}

			String line = this.text[textY].substring(this.currentXOffset);
			int textX = fontRenderer.plainSubstrByWidth(line, mouseX).length() + this.currentXOffset;
			this.setCursorPosition(textX, textY);
		}

		this.setFocused(hovering);
		return hovering;
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

	@Override
	public void draw(GuiRenderer renderer, ReadableDimension size, RenderInfo renderInfo) {
		this.size = size;
		updateCurrentOffset();
		super.draw(renderer, size, renderInfo);

		Font fontRenderer = MCVer.getFontRenderer();
		int width = size.getWidth();
		int height = size.getHeight();

		// Draw black rect once pixel smaller than gray rect
		renderer.drawRect(0, 0, width, height, BACKGROUND_COLOR);
		renderer.drawRect(1, 1, width - 2, height - 2, ReadableColor.BLACK);

		ReadableColor textColor = isEnabled() ? textColorEnabled : textColorDisabled;

		int lineHeight = fontRenderer.lineHeight + LINE_SPACING;
		int contentHeight = height - BORDER * 2;
		int maxLines = contentHeight / lineHeight;
		int contentWidth = width - BORDER * 2;

		// Draw hint if applicable
		if (hint != null && !isFocused() && Arrays.stream(text).allMatch(String::isEmpty)) {
			for (int i = 0; i < maxLines && i < hint.length; i++) {
				String line = fontRenderer.plainSubstrByWidth(hint[i], contentWidth);

				int posY = BORDER + i * lineHeight;
				renderer.drawString(BORDER, posY, textColorDisabled, line, true);
			}
			return;
		}

		// Draw lines
		for (int i = 0; i < maxLines && i + currentYOffset < text.length; i++) {
			int lineY = i + currentYOffset;
			String line = text[lineY];
			int leftTrimmed = 0;
			if (lineY == cursorY) {
				line = line.substring(currentXOffset);
				leftTrimmed = currentXOffset;
			}
			line = fontRenderer.plainSubstrByWidth(line, contentWidth);

			// Draw line
			int posY = BORDER + i * lineHeight;
			int lineEnd = renderer.drawString(BORDER, posY, textColor, line, true);

			// Draw selection
			int fromX = getSelectionFromX();
			int fromY = getSelectionFromY();
			int toX = getSelectionToX();
			int toY = getSelectionToY();
			if (lineY > fromY && lineY < toY) { // Whole line selected
				renderer.invertColors(lineEnd, posY - 1 + lineHeight, BORDER, posY - 1);
			} else if (lineY == fromY && lineY == toY) { // Part of line selected
				String leftStr = line.substring(0, clamp(fromX - leftTrimmed, 0, line.length()));
				String rightStr = line.substring(clamp(toX - leftTrimmed, 0, line.length()));
				int left = BORDER + fontRenderer.width(leftStr);
				int right = lineEnd - fontRenderer.width(rightStr) - 1;
				renderer.invertColors(right, posY - 1 + lineHeight, left, posY - 1);
			} else if (lineY == fromY) { // End of line selected
				String rightStr = line.substring(clamp(fromX - leftTrimmed, 0, line.length()));
				renderer.invertColors(lineEnd, posY - 1 + lineHeight, lineEnd - fontRenderer.width(rightStr), posY - 1);
			} else if (lineY == toY) { // Beginning of line selected
				String leftStr = line.substring(0, clamp(toX - leftTrimmed, 0, line.length()));
				int right = BORDER + fontRenderer.width(leftStr);
				renderer.invertColors(right, posY - 1 + lineHeight, BORDER, posY - 1);
			}

			// Draw cursor
			if (lineY == cursorY && blinkCursorTick / 6 % 2 == 0 && focused) {
				String beforeCursor = line.substring(0, cursorX - leftTrimmed);
				int posX = BORDER + fontRenderer.width(beforeCursor);
				if (cursorX == text[lineY].length()) {
					renderer.drawString(posX, posY, CURSOR_COLOR, "_", true);
				} else {
					renderer.drawRect(posX, posY - 1, 1, 1 + fontRenderer.lineHeight, CURSOR_COLOR);
				}
			}
		}
	}

	public boolean typeKey(ReadablePoint mousePosition, int keyCode, char keyChar, boolean ctrlDown,
			boolean shiftDown) {
		if (keyCode == 258) {
			Focusable other = shiftDown ? this.previous : this.next;
			if (other != null) {
				this.setFocused(false);
				other.setFocused(true);
			}

			return true;
		} else if (!this.focused) {
			return false;
		} else {
			if (Screen.hasControlDown()) {
				switch (keyCode) {
				case 65:
					this.cursorX = this.cursorY = 0;
					this.selectionY = this.text.length - 1;
					this.selectionX = this.text[this.selectionY].length();
					return true;
				case 67:
					MCVer.setClipboardString(this.getSelectedText());
					return true;
				case 86:
					if (this.isEnabled()) {
						this.writeText(MCVer.getClipboardString());
					}

					return true;
				case 88:
					if (this.isEnabled()) {
						MCVer.setClipboardString(this.cutSelectedText());
					}

					return true;
				}
			}

			boolean words = Screen.hasControlDown();
			boolean select = Screen.hasShiftDown();
			switch (keyCode) {
			case 259:
				if (this.isEnabled()) {
					if (this.getSelectedText().length() > 0) {
						this.deleteSelectedText();
					} else if (words) {
						this.deletePreviousWord();
					} else {
						this.deletePreviousChar();
					}
				}

				return true;
			case 260:
			case 266:
			case 267:
			default:
				if (this.isEnabled()) {
					if (keyChar == '\r') {
						keyChar = '\n';
					}

					this.writeChar(keyChar);
				}

				return true;
			case 261:
				if (this.isEnabled()) {
					if (this.getSelectedText().length() > 0) {
						this.deleteSelectedText();
					} else if (words) {
						this.deleteNextWord();
					} else {
						this.deleteNextChar();
					}
				}

				return true;
			case 262:
				if (this.cursorX == this.text[this.cursorY].length()) {
					if (this.cursorY < this.text.length - 1) {
						++this.cursorY;
						this.cursorX = 0;
					}
				} else if (words) {
					this.cursorX += this.getNextWordLength();
				} else {
					++this.cursorX;
				}
				break;
			case 263:
				if (this.cursorX == 0) {
					if (this.cursorY > 0) {
						--this.cursorY;
						this.cursorX = this.text[this.cursorY].length();
					}
				} else if (words) {
					this.cursorX -= this.getPreviousWordLength();
				} else {
					--this.cursorX;
				}
				break;
			case 264:
				if (this.cursorY + 1 < this.text.length) {
					++this.cursorY;
					this.cursorX = Math.min(this.cursorX, this.text[this.cursorY].length());
				}
				break;
			case 265:
				if (this.cursorY > 0) {
					--this.cursorY;
					this.cursorX = Math.min(this.cursorX, this.text[this.cursorY].length());
				}
				break;
			case 268:
				this.cursorX = 0;
				break;
			case 269:
				this.cursorX = this.text[this.cursorY].length();
			}

			if (!select) {
				this.selectionX = this.cursorX;
				this.selectionY = this.cursorY;
			}

			return true;
		}
	}

	public void tick() {
		++this.blinkCursorTick;
	}

	public T setMaxTextWidth(int maxTextWidth) {
		this.maxTextWidth = maxTextWidth;
		return this.getThis();
	}

	public T setMaxTextHeight(int maxTextHeight) {
		this.maxTextHeight = maxTextHeight;
		return this.getThis();
	}

	public T setMaxCharCount(int maxCharCount) {
		this.maxCharCount = maxCharCount;
		return this.getThis();
	}

	public T setTextColor(ReadableColor textColor) {
		this.textColorEnabled = textColor;
		return this.getThis();
	}

	public T setTextColorDisabled(ReadableColor textColorDisabled) {
		this.textColorDisabled = textColorDisabled;
		return this.getThis();
	}

	public T onFocusChange(Consumer<Boolean> focusChanged) {
		this.focusChanged = focusChanged;
		return this.getThis();
	}

	protected void onFocusChanged(boolean focused) {
		if (this.focusChanged != null) {
			this.focusChanged.consume(focused);
		}

	}

	public String[] getHint() {
		return this.hint;
	}

	public T setHint(String... hint) {
		this.hint = hint;
		return this.getThis();
	}

	public T setI18nHint(String hint, Object... args) {
		this.setHint(I18n.get(hint, args).split("/n"));
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

	public int getMaxTextWidth() {
		return this.maxTextWidth;
	}

	public int getMaxTextHeight() {
		return this.maxTextHeight;
	}

	public int getMaxCharCount() {
		return this.maxCharCount;
	}
}
