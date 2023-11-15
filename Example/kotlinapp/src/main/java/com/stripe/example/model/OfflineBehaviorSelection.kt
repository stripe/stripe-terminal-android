package com.stripe.example.model

import androidx.annotation.StringRes
import com.stripe.example.R
import com.stripe.stripeterminal.external.models.OfflineBehavior

enum class OfflineBehaviorSelection(
    /**
     *  The actual OfflineBehavior value this selection maps to.
     */
    val offlineBehavior: OfflineBehavior?,

    /**
     * Label used in the dropdown selection for the user
     */
    @StringRes val labelResource: Int,
) {
    DEFAULT(null, R.string.offline_behavior_default),

    PREFER_ONLINE(OfflineBehavior.PREFER_ONLINE, R.string.offline_behavior_prefer_online),
    REQUIRE_ONLINE(OfflineBehavior.REQUIRE_ONLINE, R.string.offline_behavior_require_online),
    FORCE_OFFLINE(OfflineBehavior.FORCE_OFFLINE, R.string.offline_behavior_force_offline),
}
