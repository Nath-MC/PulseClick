package com.skytendo.pulseclick;

import eu.midnightdust.lib.config.MidnightConfig;

public class PulseClickConfig extends MidnightConfig {

    @Comment public static Comment timingModeComment;
    @Entry(name = "Timing mode") public static TimingMode timingMode = TimingMode.TICKS;

    @Comment public static Comment spacer1;

    @Comment public static Comment tickBetweenClicksComment;
    @Entry(name = "Ticks between clicks", min = 1, max = 5000) public static int ticksBetweenClicks = 100;
}
