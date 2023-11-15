package com.stripe.example.javaapp.model;

import androidx.annotation.StringRes;

import com.stripe.example.javaapp.R;
import com.stripe.stripeterminal.external.models.OfflineBehavior;

public enum OfflineBehaviorSelection {
    DEFAULT(OfflineBehavior.PREFER_ONLINE, R.string.offline_behavior_default),
    PREFER_ONLINE(OfflineBehavior.PREFER_ONLINE, R.string.offline_behavior_prefer_online),
    REQUIRE_ONLINE(OfflineBehavior.REQUIRE_ONLINE, R.string.offline_behavior_require_online),
    FORCE_OFFLINE(OfflineBehavior.FORCE_OFFLINE, R.string.offline_behavior_force_offline);

    public final OfflineBehavior offlineBehavior;
    public final @StringRes int labelResource;

    OfflineBehaviorSelection(OfflineBehavior offlineBehavior, @StringRes int labelResource) {
        this.offlineBehavior = offlineBehavior;
        this.labelResource = labelResource;
    }
}
