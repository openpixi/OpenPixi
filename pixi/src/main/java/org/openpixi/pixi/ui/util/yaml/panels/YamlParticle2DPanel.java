package org.openpixi.pixi.ui.util.yaml.panels;

import java.awt.Component;

import org.openpixi.pixi.ui.PanelManager;
import org.openpixi.pixi.ui.panel.Particle2DPanel;

public class YamlParticle2DPanel {

	public Integer colorIndex;
	public Integer directionIndex;

	public Component inflate(PanelManager panelManager) {

		Particle2DPanel panel = new Particle2DPanel(panelManager.getSimulationAnimation());

		if (colorIndex != null) {
			panel.getColorProperties().setColorIndex(colorIndex);
		}

		if (directionIndex != null) {
			panel.getColorProperties().setDirectionIndex(directionIndex);
		}

		return panel;
	}
}
