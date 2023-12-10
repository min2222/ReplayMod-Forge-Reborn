package com.replaymod.simplepathing.gui;

import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.replaymod.lib.de.johni0702.minecraft.gui.container.AbstractGuiContainer;
import com.replaymod.lib.de.johni0702.minecraft.gui.container.GuiPanel;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.GuiButton;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.GuiElement;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.GuiLabel;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.GuiNumberField;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.GuiTooltip;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.IGuiClickable;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.IGuiLabel;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.advanced.GuiDropdownMenu;
import com.replaymod.lib.de.johni0702.minecraft.gui.function.Typeable;
import com.replaymod.lib.de.johni0702.minecraft.gui.layout.GridLayout;
import com.replaymod.lib.de.johni0702.minecraft.gui.layout.HorizontalLayout;
import com.replaymod.lib.de.johni0702.minecraft.gui.layout.VerticalLayout;
import com.replaymod.lib.de.johni0702.minecraft.gui.popup.AbstractGuiPopup;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.Colors;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.Consumer;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.Utils;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.ReadablePoint;
import com.replaymod.pathing.properties.CameraProperties;
import com.replaymod.pathing.properties.TimestampProperty;
import com.replaymod.replay.ReplayModReplay;
import com.replaymod.replaystudio.pathing.change.Change;
import com.replaymod.replaystudio.pathing.change.CombinedChange;
import com.replaymod.replaystudio.pathing.interpolation.CatmullRomSplineInterpolator;
import com.replaymod.replaystudio.pathing.interpolation.CubicSplineInterpolator;
import com.replaymod.replaystudio.pathing.interpolation.Interpolator;
import com.replaymod.replaystudio.pathing.interpolation.LinearInterpolator;
import com.replaymod.replaystudio.pathing.path.Keyframe;
import com.replaymod.replaystudio.pathing.path.Path;
import com.replaymod.replaystudio.pathing.path.PathSegment;
import com.replaymod.simplepathing.InterpolatorType;
import com.replaymod.simplepathing.SPTimeline;
import com.replaymod.simplepathing.Setting;
import com.replaymod.simplepathing.properties.ExplicitInterpolationProperty;

import net.minecraft.client.resources.language.I18n;

public abstract class GuiEditKeyframe<T extends GuiEditKeyframe<T>> extends AbstractGuiPopup<T> implements Typeable {
	protected static final Logger logger = LogManager.getLogger();
	protected final GuiPathing guiPathing;
	protected final long time;
	protected final Keyframe keyframe;
	protected final Path path;
	public final GuiLabel title = new GuiLabel();
	public final GuiPanel inputs = new GuiPanel();
	public final GuiNumberField timeMinField = (GuiNumberField) ((GuiNumberField) newGuiNumberField().setSize(30, 20))
			.setMinValue(0);
	public final GuiNumberField timeSecField = (GuiNumberField) ((GuiNumberField) ((GuiNumberField) newGuiNumberField()
			.setSize(20, 20)).setMinValue(0)).setMaxValue(59);
	public final GuiNumberField timeMSecField = (GuiNumberField) ((GuiNumberField) ((GuiNumberField) newGuiNumberField()
			.setSize(30, 20)).setMinValue(0)).setMaxValue(999);
	public final GuiPanel timePanel;
	public final GuiButton saveButton;
	public final GuiButton cancelButton;
	public final GuiPanel buttons;

	private static GuiNumberField newGuiNumberField() {
		return (GuiNumberField) ((GuiNumberField) (new GuiNumberField()).setPrecision(0))
				.setValidateOnFocusChange(true);
	}

	public GuiEditKeyframe(GuiPathing gui, SPTimeline.SPPath path, long time, String type) {
		super(ReplayModReplay.instance.getReplayHandler().getOverlay());
		this.timePanel = (GuiPanel) ((GuiPanel) (new GuiPanel())
				.setLayout((new HorizontalLayout(HorizontalLayout.Alignment.RIGHT)).setSpacing(3)))
				.addElements(new HorizontalLayout.Data(0.5D),
						new GuiElement[] {
								(new GuiLabel()).setI18nText("replaymod.gui.editkeyframe.timelineposition",
										new Object[0]),
								this.timeMinField, (new GuiLabel()).setI18nText("replaymod.gui.minutes", new Object[0]),
								this.timeSecField, (new GuiLabel()).setI18nText("replaymod.gui.seconds", new Object[0]),
								this.timeMSecField,
								(new GuiLabel()).setI18nText("replaymod.gui.milliseconds", new Object[0]) });
		this.saveButton = (GuiButton) ((GuiButton) (new GuiButton()).setSize(150, 20))
				.setI18nLabel("replaymod.gui.save", new Object[0]);
		this.cancelButton = (GuiButton) ((GuiButton) ((GuiButton) (new GuiButton()).onClick(() -> {
			this.close();
		})).setSize(150, 20)).setI18nLabel("replaymod.gui.cancel", new Object[0]);
		this.buttons = (GuiPanel) ((GuiPanel) (new GuiPanel())
				.setLayout((new HorizontalLayout(HorizontalLayout.Alignment.CENTER)).setSpacing(7)))
				.addElements(new HorizontalLayout.Data(0.5D), new GuiElement[] { this.saveButton, this.cancelButton });
		this.setBackgroundColor(Colors.DARK_TRANSPARENT);
		((GuiPanel) this.popup.setLayout((new VerticalLayout()).setSpacing(10))).addElements(
				new VerticalLayout.Data(0.5D, false),
				new GuiElement[] { this.title, this.inputs, this.timePanel, this.buttons });
		this.guiPathing = gui;
		this.time = time;
		this.path = gui.getMod().getCurrentTimeline().getPath(path);
		this.keyframe = this.path.getKeyframe(time);
		Consumer<String> updateSaveButtonState = (s) -> {
			this.saveButton.setEnabled(this.canSave());
		};
		((GuiNumberField) this.timeMinField.setValue((double) (time / 1000L / 60L)))
				.onTextChanged(updateSaveButtonState);
		((GuiNumberField) this.timeSecField.setValue((double) (time / 1000L % 60L)))
				.onTextChanged(updateSaveButtonState);
		((GuiNumberField) this.timeMSecField.setValue((double) (time % 1000L))).onTextChanged(updateSaveButtonState);
		this.title.setI18nText("replaymod.gui.editkeyframe.title." + type, new Object[0]);
		this.saveButton.onClick(() -> {
			Change change = this.save();
			long newTime = (long) ((this.timeMinField.getInteger() * 60 + this.timeSecField.getInteger()) * 1000
					+ this.timeMSecField.getInteger());
			if (newTime != time) {
				change = CombinedChange.createFromApplied((Change) change,
						gui.getMod().getCurrentTimeline().moveKeyframe(path, time, newTime));
				if (gui.getMod().getSelectedPath() == path && gui.getMod().getSelectedTime() == time) {
					gui.getMod().setSelected(path, newTime);
				}
			}

			gui.getMod().getCurrentTimeline().getTimeline().pushChange((Change) change);
			this.close();
		});
	}

	private boolean canSave() {
		long newTime = (long) ((this.timeMinField.getInteger() * 60 + this.timeSecField.getInteger()) * 1000
				+ this.timeMSecField.getInteger());
		if (newTime >= 0L && newTime <= (long) this.guiPathing.timeline.getLength()) {
			return newTime == this.keyframe.getTime() || this.path.getKeyframe(newTime) == null;
		} else {
			return false;
		}
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

	public void open() {
		super.open();
	}

	protected abstract Change save();

	public static class Position extends GuiEditKeyframe<GuiEditKeyframe.Position> {
		public final GuiNumberField xField = (GuiNumberField) ((GuiNumberField) GuiEditKeyframe.newGuiNumberField()
				.setSize(60, 20)).setPrecision(5);
		public final GuiNumberField yField = (GuiNumberField) ((GuiNumberField) GuiEditKeyframe.newGuiNumberField()
				.setSize(60, 20)).setPrecision(5);
		public final GuiNumberField zField = (GuiNumberField) ((GuiNumberField) GuiEditKeyframe.newGuiNumberField()
				.setSize(60, 20)).setPrecision(5);
		public final GuiNumberField yawField = (GuiNumberField) ((GuiNumberField) GuiEditKeyframe.newGuiNumberField()
				.setSize(60, 20)).setPrecision(5);
		public final GuiNumberField pitchField = (GuiNumberField) ((GuiNumberField) GuiEditKeyframe.newGuiNumberField()
				.setSize(60, 20)).setPrecision(5);
		public final GuiNumberField rollField = (GuiNumberField) ((GuiNumberField) GuiEditKeyframe.newGuiNumberField()
				.setSize(60, 20)).setPrecision(5);
		public final GuiEditKeyframe.Position.InterpolationPanel interpolationPanel = new GuiEditKeyframe.Position.InterpolationPanel();

		public Position(GuiPathing gui, SPTimeline.SPPath path, long keyframe) {
			super(gui, path, keyframe, "pos");
			GuiPanel positionInputs = (GuiPanel) ((GuiPanel) (new GuiPanel())
					.setLayout((new GridLayout()).setCellsEqualSize(false).setColumns(4).setSpacingX(3).setSpacingY(5)))
					.addElements(new GridLayout.Data(1.0D, 0.5D), new GuiElement[] {
							(new GuiLabel()).setI18nText("replaymod.gui.editkeyframe.xpos", new Object[0]), this.xField,
							(new GuiLabel()).setI18nText("replaymod.gui.editkeyframe.camyaw", new Object[0]),
							this.yawField,
							(new GuiLabel()).setI18nText("replaymod.gui.editkeyframe.ypos", new Object[0]), this.yField,
							(new GuiLabel()).setI18nText("replaymod.gui.editkeyframe.campitch", new Object[0]),
							this.pitchField,
							(new GuiLabel()).setI18nText("replaymod.gui.editkeyframe.zpos", new Object[0]), this.zField,
							(new GuiLabel()).setI18nText("replaymod.gui.editkeyframe.camroll", new Object[0]),
							this.rollField });
			((GuiPanel) this.inputs.setLayout((new VerticalLayout()).setSpacing(10))).addElements(
					new VerticalLayout.Data(0.5D, false), new GuiElement[] { positionInputs, this.interpolationPanel });
			this.keyframe.getValue(CameraProperties.POSITION).ifPresent((pos) -> {
				this.xField.setValue((Double) pos.getLeft());
				this.yField.setValue((Double) pos.getMiddle());
				this.zField.setValue((Double) pos.getRight());
			});
			this.keyframe.getValue(CameraProperties.ROTATION).ifPresent((rot) -> {
				this.yawField.setValue((double) (Float) rot.getLeft());
				this.pitchField.setValue((double) (Float) rot.getMiddle());
				this.rollField.setValue((double) (Float) rot.getRight());
			});
			Utils.link(this.xField, this.yField, this.zField, this.yawField, this.pitchField, this.rollField,
					this.timeMinField, this.timeSecField, this.timeMSecField);
			this.popup.invokeAll(IGuiLabel.class, (e) -> {
				e.setColor(Colors.BLACK);
			});
		}

		protected Change save() {
			SPTimeline timeline = this.guiPathing.getMod().getCurrentTimeline();
			Change positionChange = timeline.updatePositionKeyframe(this.time, this.xField.getDouble(),
					this.yField.getDouble(), this.zField.getDouble(), this.yawField.getFloat(),
					this.pitchField.getFloat(), this.rollField.getFloat());
			if (this.interpolationPanel.getSettingsPanel() == null) {
				return positionChange;
			} else {
				Interpolator interpolator = this.interpolationPanel.getSettingsPanel().createInterpolator();
				return this.interpolationPanel.getInterpolatorType() == InterpolatorType.DEFAULT
						? CombinedChange.createFromApplied(positionChange, timeline.setInterpolatorToDefault(this.time),
								timeline.setDefaultInterpolator(interpolator))
						: CombinedChange.createFromApplied(positionChange,
								timeline.setInterpolator(this.time, interpolator));
			}
		}

		protected GuiEditKeyframe.Position getThis() {
			return this;
		}

		public class InterpolationPanel extends AbstractGuiContainer<GuiEditKeyframe.Position.InterpolationPanel> {
			private GuiEditKeyframe.Position.InterpolationPanel.SettingsPanel settingsPanel;
			private GuiDropdownMenu<InterpolatorType> dropdown;

			public InterpolationPanel() {
				this.setLayout(new VerticalLayout());
				this.dropdown = (GuiDropdownMenu) ((GuiDropdownMenu) ((GuiDropdownMenu) ((GuiDropdownMenu) (new GuiDropdownMenu())
						.setToString((s) -> {
							return I18n.get(s.toString(), new Object[0]);
						})).setValues(InterpolatorType.values())).setHeight(20)).onSelection((i) -> {
							this.setSettingsPanel((InterpolatorType) this.dropdown.getSelectedValue());
						});
				Iterator var2 = this.dropdown.getDropdownEntries().entrySet().iterator();

				while (var2.hasNext()) {
					Entry<InterpolatorType, IGuiClickable> e = (Entry) var2.next();
					((IGuiClickable) e.getValue()).setTooltip((new GuiTooltip())
							.setI18nText(((InterpolatorType) e.getKey()).getI18nDescription(), new Object[0]));
				}

				GuiPanel dropdownPanel = (GuiPanel) ((GuiPanel) (new GuiPanel()).setLayout(
						(new GridLayout()).setCellsEqualSize(false).setColumns(2).setSpacingX(3).setSpacingY(5)))
						.addElements(new GridLayout.Data(1.0D, 0.5D), new GuiElement[] {
								(new GuiLabel()).setI18nText("replaymod.gui.editkeyframe.interpolator", new Object[0]),
								this.dropdown });
				this.addElements(new VerticalLayout.Data(0.5D, false), new GuiElement[] { dropdownPanel });
				Optional<PathSegment> segment = Position.this.path.getSegments().stream().filter((s) -> {
					return s.getStartKeyframe() == Position.this.keyframe;
				}).findFirst();
				if (segment.isPresent()) {
					Interpolator interpolator = ((PathSegment) segment.get()).getInterpolator();
					InterpolatorType type = InterpolatorType.fromClass(interpolator.getClass());
					if (Position.this.keyframe.getValue(ExplicitInterpolationProperty.PROPERTY).isPresent()) {
						this.dropdown.setSelected(type);
					} else {
						this.setSettingsPanel(InterpolatorType.DEFAULT);
						type = InterpolatorType.DEFAULT;
					}

					if (this.getInterpolatorTypeNoDefault(type).getInterpolatorClass().isInstance(interpolator)) {
						this.settingsPanel.loadSettings(interpolator);
					}
				} else {
					this.dropdown.setDisabled();
				}

			}

			public GuiEditKeyframe.Position.InterpolationPanel.SettingsPanel getSettingsPanel() {
				return this.settingsPanel;
			}

			public void setSettingsPanel(InterpolatorType type) {
				this.removeElement(this.settingsPanel);
				switch (this.getInterpolatorTypeNoDefault(type)) {
				case CATMULL_ROM:
					this.settingsPanel = new GuiEditKeyframe.Position.InterpolationPanel.CatmullRomSettingsPanel();
					break;
				case CUBIC:
					this.settingsPanel = new GuiEditKeyframe.Position.InterpolationPanel.CubicSettingsPanel();
					break;
				case LINEAR:
					this.settingsPanel = new GuiEditKeyframe.Position.InterpolationPanel.LinearSettingsPanel();
				}

				this.addElements(new GridLayout.Data(0.5D, 0.5D), new GuiElement[] { this.settingsPanel });
			}

			protected InterpolatorType getInterpolatorTypeNoDefault(InterpolatorType interpolatorType) {
				if (interpolatorType != InterpolatorType.DEFAULT && interpolatorType != null) {
					return interpolatorType;
				} else {
					InterpolatorType defaultType = InterpolatorType.fromString((String) Position.this.guiPathing
							.getMod().getCore().getSettingsRegistry().get(Setting.DEFAULT_INTERPOLATION));
					return defaultType;
				}
			}

			public InterpolatorType getInterpolatorType() {
				return (InterpolatorType) this.dropdown.getSelectedValue();
			}

			protected GuiEditKeyframe.Position.InterpolationPanel getThis() {
				return this;
			}

			public abstract class SettingsPanel<I extends Interpolator, T extends GuiEditKeyframe.Position.InterpolationPanel.SettingsPanel<I, T>>
					extends AbstractGuiContainer<T> {
				public abstract void loadSettings(I interpolator);

				public abstract I createInterpolator();
			}

			public class CatmullRomSettingsPanel extends
					GuiEditKeyframe.Position.InterpolationPanel.SettingsPanel<CatmullRomSplineInterpolator, GuiEditKeyframe.Position.InterpolationPanel.CatmullRomSettingsPanel> {
				public final GuiLabel alphaLabel;
				public final GuiNumberField alphaField;

				public CatmullRomSettingsPanel() {
					super();
					this.alphaLabel = (GuiLabel) ((GuiLabel) (new GuiLabel()).setColor(Colors.BLACK))
							.setI18nText("replaymod.gui.editkeyframe.interpolator.catmullrom.alpha", new Object[0]);
					this.alphaField = (GuiNumberField) ((GuiNumberField) ((GuiNumberField) ((GuiNumberField) (new GuiNumberField())
							.setSize(100, 20)).setPrecision(5)).setMinValue(0)).setValidateOnFocusChange(true);
					this.setLayout(new HorizontalLayout(HorizontalLayout.Alignment.CENTER));
					this.addElements(new HorizontalLayout.Data(0.5D),
							new GuiElement[] { this.alphaLabel, this.alphaField });
				}

				public void loadSettings(CatmullRomSplineInterpolator interpolator) {
					this.alphaField.setValue(interpolator.getAlpha());
				}

				public CatmullRomSplineInterpolator createInterpolator() {
					return new CatmullRomSplineInterpolator(this.alphaField.getDouble());
				}

				protected GuiEditKeyframe.Position.InterpolationPanel.CatmullRomSettingsPanel getThis() {
					return this;
				}
			}

			public class CubicSettingsPanel extends
					GuiEditKeyframe.Position.InterpolationPanel.SettingsPanel<CubicSplineInterpolator, GuiEditKeyframe.Position.InterpolationPanel.CubicSettingsPanel> {
				public CubicSettingsPanel() {
					super();
				}

				public void loadSettings(CubicSplineInterpolator interpolator) {
				}

				public CubicSplineInterpolator createInterpolator() {
					return new CubicSplineInterpolator();
				}

				protected GuiEditKeyframe.Position.InterpolationPanel.CubicSettingsPanel getThis() {
					return this;
				}
			}

			public class LinearSettingsPanel extends
					GuiEditKeyframe.Position.InterpolationPanel.SettingsPanel<LinearInterpolator, GuiEditKeyframe.Position.InterpolationPanel.LinearSettingsPanel> {
				public LinearSettingsPanel() {
					super();
				}

				public void loadSettings(LinearInterpolator interpolator) {
				}

				public LinearInterpolator createInterpolator() {
					return new LinearInterpolator();
				}

				protected GuiEditKeyframe.Position.InterpolationPanel.LinearSettingsPanel getThis() {
					return this;
				}
			}
		}
	}

	public static class Time extends GuiEditKeyframe<GuiEditKeyframe.Time> {
		public final GuiNumberField timestampMinField = (GuiNumberField) ((GuiNumberField) GuiEditKeyframe
				.newGuiNumberField().setSize(30, 20)).setMinValue(0);
		public final GuiNumberField timestampSecField = (GuiNumberField) ((GuiNumberField) ((GuiNumberField) GuiEditKeyframe
				.newGuiNumberField().setSize(20, 20)).setMinValue(0)).setMaxValue(59);
		public final GuiNumberField timestampMSecField = (GuiNumberField) ((GuiNumberField) ((GuiNumberField) GuiEditKeyframe
				.newGuiNumberField().setSize(30, 20)).setMinValue(0)).setMaxValue(999);

		public Time(GuiPathing gui, SPTimeline.SPPath path, long keyframe) {
			super(gui, path, keyframe, "time");
			((GuiPanel) this.inputs.setLayout((new HorizontalLayout(HorizontalLayout.Alignment.RIGHT)).setSpacing(3)))
					.addElements(new HorizontalLayout.Data(0.5D),
							new GuiElement[] {
									(new GuiLabel()).setI18nText("replaymod.gui.editkeyframe.timestamp", new Object[0]),
									this.timestampMinField,
									(new GuiLabel()).setI18nText("replaymod.gui.minutes", new Object[0]),
									this.timestampSecField,
									(new GuiLabel()).setI18nText("replaymod.gui.seconds", new Object[0]),
									this.timestampMSecField,
									(new GuiLabel()).setI18nText("replaymod.gui.milliseconds", new Object[0]) });
			this.keyframe.getValue(TimestampProperty.PROPERTY).ifPresent((time) -> {
				this.timestampMinField.setValue(time / 1000 / 60);
				this.timestampSecField.setValue(time / 1000 % 60);
				this.timestampMSecField.setValue(time % 1000);
			});
			Utils.link(this.timestampMinField, this.timestampSecField, this.timestampMSecField, this.timeMinField,
					this.timeSecField, this.timeMSecField);
			this.popup.invokeAll(IGuiLabel.class, (e) -> {
				e.setColor(Colors.BLACK);
			});
		}

		protected Change save() {
			int time = (this.timestampMinField.getInteger() * 60 + this.timestampSecField.getInteger()) * 1000
					+ this.timestampMSecField.getInteger();
			return this.guiPathing.getMod().getCurrentTimeline().updateTimeKeyframe(this.keyframe.getTime(), time);
		}

		protected GuiEditKeyframe.Time getThis() {
			return this;
		}
	}

	public static class Spectator extends GuiEditKeyframe<GuiEditKeyframe.Spectator> {
		public Spectator(GuiPathing gui, SPTimeline.SPPath path, long keyframe) {
			super(gui, path, keyframe, "spec");
			Utils.link(this.timeMinField, this.timeSecField, this.timeMSecField);
			this.popup.invokeAll(IGuiLabel.class, (e) -> {
				e.setColor(Colors.BLACK);
			});
		}

		protected Change save() {
			return CombinedChange.createFromApplied();
		}

		protected GuiEditKeyframe.Spectator getThis() {
			return this;
		}
	}
}
