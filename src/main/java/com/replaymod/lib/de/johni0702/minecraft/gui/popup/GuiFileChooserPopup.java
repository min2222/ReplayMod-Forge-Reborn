package com.replaymod.lib.de.johni0702.minecraft.gui.popup;

import java.io.File;
import java.io.IOException;
import java.nio.file.InvalidPathException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import com.replaymod.lib.de.johni0702.minecraft.gui.GuiRenderer;
import com.replaymod.lib.de.johni0702.minecraft.gui.RenderInfo;
import com.replaymod.lib.de.johni0702.minecraft.gui.container.GuiContainer;
import com.replaymod.lib.de.johni0702.minecraft.gui.container.GuiPanel;
import com.replaymod.lib.de.johni0702.minecraft.gui.container.GuiScrollable;
import com.replaymod.lib.de.johni0702.minecraft.gui.container.GuiVerticalList;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.GuiButton;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.GuiElement;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.GuiTextField;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.advanced.GuiDropdownMenu;
import com.replaymod.lib.de.johni0702.minecraft.gui.function.Typeable;
import com.replaymod.lib.de.johni0702.minecraft.gui.layout.CustomLayout;
import com.replaymod.lib.de.johni0702.minecraft.gui.layout.HorizontalLayout;
import com.replaymod.lib.de.johni0702.minecraft.gui.layout.VerticalLayout;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.Colors;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.Consumer;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.Dimension;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.ReadableDimension;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.ReadablePoint;
import com.replaymod.lib.de.johni0702.minecraft.gui.versions.MCVer;

public class GuiFileChooserPopup extends AbstractGuiPopup<GuiFileChooserPopup> implements Typeable {
	private Consumer<File> onAccept = (file) -> {
	};
	private Runnable onCancel = () -> {
	};
	private final GuiScrollable pathScrollable;
	private final GuiPanel pathPanel;
	private final GuiVerticalList fileList;
	private final GuiTextField nameField;
	private final GuiButton acceptButton;
	private final GuiButton cancelButton;
	private final String[] fileExtensions;
	private final boolean load;
	private File folder;

	public static GuiFileChooserPopup openSaveGui(GuiContainer container, String buttonLabel,
			String... fileExtensions) {
		GuiFileChooserPopup popup = (GuiFileChooserPopup) (new GuiFileChooserPopup(container, fileExtensions, false))
				.setBackgroundColor(Colors.DARK_TRANSPARENT);
		popup.acceptButton.setI18nLabel(buttonLabel, new Object[0]);
		popup.open();
		return popup;
	}

	public static GuiFileChooserPopup openLoadGui(GuiContainer container, String buttonLabel,
			String... fileExtensions) {
		GuiFileChooserPopup popup = (GuiFileChooserPopup) (new GuiFileChooserPopup(container, fileExtensions, true))
				.setBackgroundColor(Colors.DARK_TRANSPARENT);
		((GuiButton) popup.acceptButton.setI18nLabel(buttonLabel, new Object[0])).setDisabled();
		popup.open();
		return popup;
	}

	public GuiFileChooserPopup(GuiContainer container, String[] fileExtensions, boolean load) {
		super(container);
		this.pathScrollable = new GuiScrollable(this.popup) {
			public void draw(GuiRenderer renderer, ReadableDimension size, RenderInfo renderInfo) {
				this.scrollX(0);
				super.draw(renderer, size, renderInfo);
			}
		};
		this.pathPanel = (GuiPanel) (new GuiPanel(this.pathScrollable)).setLayout(new HorizontalLayout());
		this.fileList = new GuiVerticalList(this.popup);
		this.nameField = (GuiTextField) ((GuiTextField) ((GuiTextField) (new GuiTextField(this.popup))
				.onEnter(new Runnable() {
					public void run() {
						if (GuiFileChooserPopup.this.acceptButton.isEnabled()) {
							GuiFileChooserPopup.this.acceptButton.onClick();
						}

					}
				})).onTextChanged(new Consumer<String>() {
					public void consume(String oldName) {
						GuiFileChooserPopup.this.updateButton();
					}
				})).setMaxLength(Integer.MAX_VALUE);
		this.acceptButton = (GuiButton) ((GuiButton) (new GuiButton(this.popup)).onClick(new Runnable() {
			public void run() {
				String fileName = GuiFileChooserPopup.this.nameField.getText();
				if (!GuiFileChooserPopup.this.load && GuiFileChooserPopup.this.fileExtensions.length > 0
						&& !GuiFileChooserPopup.this.hasValidExtension(fileName)) {
					fileName = fileName + "." + GuiFileChooserPopup.this.fileExtensions[0];
				}

				GuiFileChooserPopup.this.onAccept.consume(new File(GuiFileChooserPopup.this.folder, fileName));
				GuiFileChooserPopup.this.close();
			}
		})).setSize(50, 20);
		this.cancelButton = (GuiButton) ((GuiButton) ((GuiButton) (new GuiButton(this.popup)).onClick(new Runnable() {
			public void run() {
				GuiFileChooserPopup.this.onCancel.run();
				GuiFileChooserPopup.this.close();
			}
		})).setI18nLabel("gui.cancel", new Object[0])).setSize(50, 20);
		this.fileList.setLayout((new VerticalLayout()).setSpacing(1));
		this.popup.setLayout(new CustomLayout<GuiPanel>() {
			protected void layout(GuiPanel container, int width, int height) {
				this.pos(GuiFileChooserPopup.this.pathScrollable, 0, 0);
				this.size(GuiFileChooserPopup.this.pathScrollable, width, 20);
				this.pos(GuiFileChooserPopup.this.cancelButton,
						width - this.width(GuiFileChooserPopup.this.cancelButton),
						height - this.height(GuiFileChooserPopup.this.cancelButton));
				this.pos(GuiFileChooserPopup.this.acceptButton,
						this.x(GuiFileChooserPopup.this.cancelButton) - 5
								- this.width(GuiFileChooserPopup.this.acceptButton),
						this.y(GuiFileChooserPopup.this.cancelButton));
				this.size(GuiFileChooserPopup.this.nameField, this.x(GuiFileChooserPopup.this.acceptButton) - 5, 20);
				this.pos(GuiFileChooserPopup.this.nameField, 0,
						height - this.height(GuiFileChooserPopup.this.nameField));
				this.pos(GuiFileChooserPopup.this.fileList, 0, this.y(GuiFileChooserPopup.this.pathScrollable)
						+ this.height(GuiFileChooserPopup.this.pathScrollable) + 5);
				this.size(GuiFileChooserPopup.this.fileList, width,
						this.y(GuiFileChooserPopup.this.nameField) - this.y(GuiFileChooserPopup.this.fileList) - 5);
			}

			public ReadableDimension calcMinSize(GuiContainer container) {
				return new Dimension(300, 200);
			}
		});
		this.fileExtensions = fileExtensions;
		this.load = load;
		this.setFolder(new File("."));
	}

	protected void updateButton() {
		String name = this.nameField.getText();
		File file = new File(this.folder, name);
		boolean valid = !name.contains(File.separator);

		try {
			file.toPath();
		} catch (InvalidPathException var5) {
			valid = false;
		}

		if (this.load) {
			valid &= file.exists();
		}

		this.acceptButton.setEnabled(valid);
	}

	public void setFolder(File folder) {
		if (!folder.isDirectory()) {
			throw new IllegalArgumentException("Folder has to be a directory.");
		}
		try {
			this.folder = folder = folder.getCanonicalFile();
		} catch (IOException e) {
			close();
			throw new RuntimeException(e);
		}

		updateButton();

		for (GuiElement element : new ArrayList<>(pathPanel.getElements().keySet())) {
			pathPanel.removeElement(element);
		}
		for (GuiElement element : new ArrayList<>(fileList.getListPanel().getElements().keySet())) {
			fileList.getListPanel().removeElement(element);
		}

		File[] files = folder.listFiles();
		if (files != null) {
			Arrays.sort(files, new Comparator<File>() {
				@Override
				public int compare(File f1, File f2) {
					if (f1.isDirectory() && !f2.isDirectory()) {
						return -1;
					} else if (!f1.isDirectory() && f2.isDirectory()) {
						return 1;
					}
					return f1.getName().compareToIgnoreCase(f2.getName());
				}
			});
			for (final File file : files) {
				if (file.isDirectory()) {
					fileList.getListPanel().addElements(new VerticalLayout.Data(0),
							new GuiButton().onClick(new Runnable() {
								@Override
								public void run() {
									setFolder(file);
								}
							}).setLabel(file.getName() + File.separator));
				} else {
					if (hasValidExtension(file.getName())) {
						fileList.getListPanel().addElements(new VerticalLayout.Data(0),
								new GuiButton().onClick(new Runnable() {
									@Override
									public void run() {
										setFileName(file.getName());
									}
								}).setLabel(file.getName()));
					}
				}
			}
		}
		fileList.setOffsetY(0);

		File[] roots = File.listRoots();
		if (roots != null && roots.length > 1) {
			// Windows can have multiple file system roots
			// So we place a dropdown menu (skinned like a button) at the front of the path
			final GuiDropdownMenu<File> dropdown = new GuiDropdownMenu<File>(pathPanel) {
				private final GuiButton skin = new GuiButton();

				@Override
				protected ReadableDimension calcMinSize() {
					ReadableDimension dim = super.calcMinSize();
					return new Dimension(dim.getWidth() - 5 - MCVer.getFontRenderer().lineHeight, dim.getHeight());
				}

				@Override
				public void layout(ReadableDimension size, RenderInfo renderInfo) {
					super.layout(size, renderInfo);
					if (renderInfo.layer == 0) {
						skin.layout(size, renderInfo);
					}
				}

				@Override
				public void draw(GuiRenderer renderer, ReadableDimension size, RenderInfo renderInfo) {
					super.draw(renderer, size, renderInfo);
					if (renderInfo.layer == 0) {
						skin.setLabel(getSelectedValue().toString());
						skin.draw(renderer, size, renderInfo);
					}
				}
			};
			List<File> actualRoots = new ArrayList<>();
			File selected = null;
			for (File root : roots) {
				// Windows apparently also has file system roots that aren't directories, so
				// we'll have to filter those
				if (root.isDirectory()) {
					actualRoots.add(root);
					if (folder.getAbsolutePath().startsWith(root.getAbsolutePath())) {
						selected = root;
					}
				}
			}
			assert selected != null;
			// First set values and current selection
			dropdown.setValues(actualRoots.toArray(new File[actualRoots.size()])).setSelected(selected);
			// then add selection handler afterwards
			dropdown.onSelection(new Consumer<Integer>() {
				@Override
				public void consume(Integer old) {
					setFolder(dropdown.getSelectedValue());
				}
			});
		}
		LinkedList<File> parents = new LinkedList<>();
		while (folder != null) {
			parents.addFirst(folder);
			folder = folder.getParentFile();
		}
		for (final File parent : parents) {
			pathPanel.addElements(null, new GuiButton().onClick(new Runnable() {
				@Override
				public void run() {
					setFolder(parent);
				}
			}).setLabel(parent.getName() + File.separator));
		}
		pathScrollable.setOffsetX(Integer.MAX_VALUE);
	}

	public void setFileName(String fileName) {
		this.nameField.setText(fileName);
		this.nameField.setCursorPosition(fileName.length());
		this.updateButton();
	}

	private boolean hasValidExtension(String name) {
		String[] var2 = this.fileExtensions;
		int var3 = var2.length;

		for (int var4 = 0; var4 < var3; ++var4) {
			String fileExtension = var2[var4];
			if (name.endsWith("." + fileExtension)) {
				return true;
			}
		}

		return false;
	}

	protected GuiFileChooserPopup getThis() {
		return this;
	}

	public GuiFileChooserPopup onAccept(Consumer<File> onAccept) {
		this.onAccept = onAccept;
		return this;
	}

	public GuiFileChooserPopup onCancel(Runnable onCancel) {
		this.onCancel = onCancel;
		return this;
	}

	public boolean typeKey(ReadablePoint mousePosition, int keyCode, char keyChar, boolean ctrlDown,
			boolean shiftDown) {
		if (keyCode == 256) {
			this.cancelButton.onClick();
			return true;
		} else {
			return false;
		}
	}

	public GuiButton getAcceptButton() {
		return this.acceptButton;
	}

	public GuiButton getCancelButton() {
		return this.cancelButton;
	}
}
