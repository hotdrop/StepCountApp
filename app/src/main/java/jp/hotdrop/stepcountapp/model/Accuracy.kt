package jp.hotdrop.stepcountapp.model

import jp.hotdrop.stepcountapp.R

sealed class Accuracy {
    object High: Accuracy() {
        override val labelResId: Int = R.string.accuracy_high_message
        override val colorResId: Int = R.color.accuracyHigh
    }
    object Medium: Accuracy() {
        override val labelResId: Int = R.string.accuracy_medium_message
        override val colorResId: Int = R.color.accuracyMedium
    }
    object Low: Accuracy() {
        override val labelResId: Int = R.string.accuracy_low_message
        override val colorResId: Int = R.color.accuracyLow
    }
    object Unreliable: Accuracy() {
        override val labelResId: Int = R.string.accuracy_unreliable_message
        override val colorResId: Int = R.color.accuracyUnreliable
    }
    object NoContact: Accuracy() {
        override val labelResId: Int = R.string.accuracy_no_contact_message
        override val colorResId: Int = R.color.accuracyNoContact
    }

    abstract val labelResId: Int
    abstract val colorResId: Int
}