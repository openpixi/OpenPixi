package org.openpixi.pixi.ui.util.yaml;

import org.openpixi.pixi.physics.Settings;
import org.openpixi.pixi.ui.util.yaml.currentgenerators.*;

import java.util.ArrayList;

public class YamlCurrents {

    /**
     * List of current generators.
     */
    public ArrayList<YamlSU2WireCurrent> SU2WireCurrent = new ArrayList<YamlSU2WireCurrent>();

    public ArrayList<YamlSU2DeltaPulseCurrent> SU2DeltaPulseCurrent = new ArrayList<YamlSU2DeltaPulseCurrent>();

    public ArrayList<YamlSU2LightConeDeltaPulseCurrent> SU2LightConeDeltaPulseCurrent = new ArrayList<YamlSU2LightConeDeltaPulseCurrent>();

    /**
     * Creates CurrentGenerator instances and applies them to the Settings instance.
     * @param s
     */
    public void applyTo(Settings s) {
        for (YamlSU2WireCurrent wire : SU2WireCurrent) {
            if (wire.checkConsistency(s)) {
                s.addCurrentGenerator(wire.getCurrentGenerator());
            }
        }

        for (YamlSU2DeltaPulseCurrent pulse : SU2DeltaPulseCurrent) {
            if (pulse.checkConsistency(s)) {
                s.addCurrentGenerator(pulse.getCurrentGenerator());
            }
        }

        for (YamlSU2LightConeDeltaPulseCurrent lightcone : SU2LightConeDeltaPulseCurrent) {
            if (lightcone.checkConsistency(s)) {
                s.addCurrentGenerator(lightcone.getCurrentGenerator());
            }
        }

    }

}
